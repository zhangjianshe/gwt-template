#!/usr/bin/env python3
"""Copy a directory tree to the HTTP drive; resume partial files and skip existing files.

Credentials are read from environment variables, never command-line values or the state database.
"""
import argparse
import fnmatch
import hashlib
import http.client
import json
import os
from pathlib import Path
import sqlite3
import stat
import sys
import time
import urllib.parse
import uuid

DEFAULT_EXCLUDES = {
    '.git', '.hg', '.svn', 'node_modules', '__pycache__', '.pytest_cache',
    '.mypy_cache', '.ruff_cache', '.gradle', '.dart_tool', '.cache', 'target', 'build',
}


class ApiError(Exception):
    def __init__(self, status, message):
        super().__init__(message)
        self.status = status


class Api:
    def __init__(self, server, credential, admin=False, retries=4, timeout=120):
        url = urllib.parse.urlsplit(server)
        if (url.scheme != 'https' and not (url.scheme == 'http' and url.hostname in ('localhost', '127.0.0.1', '::1'))) or not url.hostname or url.username or url.password or url.query or url.fragment:
            raise ValueError('Use an HTTPS server URL; HTTP is allowed only for loopback development')
        self.server = server.rstrip('/')
        self.base = self.server + '/api/v1/drive'
        self.headers = {'API-TOKEN' if admin else 'X-Drive-Token': credential}
        self.retries, self.timeout = retries, timeout
        self.host, self.port = url.hostname, url.port
        self.scheme = url.scheme
        self.prefix = url.path.rstrip('/') + '/api/v1/drive'
        self.connection = None

    def close(self):
        if self.connection:
            self.connection.close()
            self.connection = None

    def call(self, method, path, body=None, headers=None):
        outgoing = dict(self.headers)
        outgoing.update(headers or {})
        if isinstance(body, dict):
            body = json.dumps(body).encode()
            outgoing['Content-Type'] = 'application/json'
        for attempt in range(self.retries + 1):
            try:
                if self.connection is None:
                    connection_type = http.client.HTTPSConnection if self.scheme == 'https' else http.client.HTTPConnection
                    self.connection = connection_type(self.host, self.port, timeout=self.timeout)
                self.connection.request(method, self.prefix + path, body=body, headers=outgoing)
                response = self.connection.getresponse()
                status = response.status
                raw = response.read(16 * 1024 * 1024 + 1)
                if len(raw) > 16 * 1024 * 1024:
                    self.close()
                    raise ApiError(502, 'Server metadata response exceeded 16 MiB')
                if 300 <= status < 400:
                    raise ApiError(status, 'Server redirected the request; verify --server (credentials were not forwarded)')
                try:
                    value = json.loads(raw)
                except (ValueError, UnicodeError):
                    raise ApiError(status if status != 200 else 502, f'Invalid server response (HTTP {status})') from None
                if status != 200 or value.get('code') != 200:
                    raise ApiError(status, value.get('message') or f'HTTP {status}')
                return value['data']
            except (OSError, http.client.HTTPException) as error:
                self.close()
                problem = ApiError(0, f'Connection failed: {type(error).__name__}')
            except ApiError as error:
                problem = error
                if error.status not in (0, 408, 429, 500, 502, 503, 504):
                    raise
            if attempt == self.retries:
                raise problem
            time.sleep(min(2 ** attempt, 15))
        raise AssertionError('unreachable')

    def entries(self, parent):
        result = {}
        page = 1
        while True:
            data = self.call('GET', '/entries?' + urllib.parse.urlencode({'parentId': parent, 'pageSize': 1000, 'page': page}))
            for entry in data['entries']:
                result[entry['name']] = entry
            if page * 1000 >= data['total']:
                return result
            page += 1
            if page > 100000:
                raise ApiError(400, 'Remote listing exceeded safety limit')


class State:
    def __init__(self, path):
        self.path = Path(path).expanduser()
        self.path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
        self.db = sqlite3.connect(self.path)
        os.chmod(self.path, 0o600)
        self.db.row_factory = sqlite3.Row
        self.db.execute('''CREATE TABLE IF NOT EXISTS uploads (
            server TEXT, destination TEXT, source TEXT, path TEXT,
            upload_id TEXT, sha256 TEXT, size INTEGER, mtime_ns INTEGER, device INTEGER, inode INTEGER,
            PRIMARY KEY(server,destination,source,path))''')
        self.db.commit()

    def get(self, key):
        row = self.db.execute('SELECT * FROM uploads WHERE server=? AND destination=? AND source=? AND path=?', key).fetchone()
        return dict(row) if row else None

    def put(self, key, upload_id, digest, fingerprint):
        self.db.execute('INSERT OR REPLACE INTO uploads VALUES (?,?,?,?,?,?,?,?,?,?)',
                        (*key, upload_id, digest, *fingerprint))
        self.db.commit()

    def remove(self, key):
        self.db.execute('DELETE FROM uploads WHERE server=? AND destination=? AND source=? AND path=?', key)
        self.db.commit()

    def close(self):
        self.db.close()


def fingerprint(path):
    value = path.lstat()
    if not stat.S_ISREG(value.st_mode):
        raise ValueError(f'Not a regular file: {path}')
    return value.st_size, value.st_mtime_ns, value.st_dev, value.st_ino


def check_source(path, expected, file=None):
    if fingerprint(path) != expected:
        raise ValueError(f'Source changed during transfer: {path}')
    if file is not None:
        value = os.fstat(file.fileno())
        if (value.st_size, value.st_mtime_ns, value.st_dev, value.st_ino) != expected:
            raise ValueError(f'Opened source changed during transfer: {path}')


def open_source(path):
    return os.fdopen(os.open(path, os.O_RDONLY | getattr(os, 'O_NOFOLLOW', 0)), 'rb')


def file_hash(path, expected):
    result = hashlib.sha256()
    with open_source(path) as file:
        check_source(path, expected, file)
        while block := file.read(1024 * 1024):
            result.update(block)
        check_source(path, expected, file)
    return result.hexdigest()


class Sync:
    def __init__(self, api, source, destination, state=None, dry_run=False, excludes=(),
                 default_excludes=True, verify_existing=False, quiet=False, max_files=None):
        self.api, self.source, self.destination = api, Path(source).expanduser().resolve(), destination
        if not self.source.is_dir():
            raise ValueError('Source must be an existing directory')
        self.state, self.dry_run, self.excludes = state, dry_run, list(excludes)
        self.default_excludes, self.verify_existing, self.quiet = default_excludes, verify_existing, quiet
        self.max_files = max_files
        self.summary = dict(uploaded=0, skipped=0, directories=0, excluded=0, resumed=0, bytes_sent=0, planned=0, planned_bytes=0)

    def log(self, text):
        if not self.quiet:
            print(text, flush=True)

    def excluded(self, path):
        relative = path.relative_to(self.source).as_posix()
        if self.state and path.resolve() == self.state.path.resolve():
            return True
        return (self.default_excludes and path.name in DEFAULT_EXCLUDES) or any(
            fnmatch.fnmatchcase(relative, pattern) or fnmatch.fnmatchcase(path.name, pattern)
            for pattern in self.excludes)

    def run(self):
        self.walk(self.source, self.destination)
        return self.summary

    def walk(self, local, remote):
        children = self.api.entries(remote) if remote else {}
        for path in sorted(local.iterdir(), key=lambda item: (not item.is_dir(), item.name)):
            if path.is_symlink() or self.excluded(path):
                self.summary['excluded'] += 1
                continue
            relative = path.relative_to(self.source).as_posix()
            if path.is_dir():
                existing = children.get(path.name)
                if existing and not existing['directory']:
                    raise ValueError(f'Remote file conflicts with directory: {relative}')
                if existing:
                    target = existing['id']
                else:
                    self.log(f'MKDIR {relative}')
                    self.summary['directories'] += 1
                    target = None
                    if not self.dry_run:
                        try:
                            target = self.api.call('POST', '/directories', {'parentId': remote, 'name': path.name})['id']
                        except ApiError as error:
                            if error.status != 409:
                                raise
                            actual = self.api.entries(remote).get(path.name)
                            if not actual or not actual['directory']:
                                raise
                            target = actual['id']
                self.walk(path, target)
            elif path.is_file():
                if self.max_files is not None and self.summary['uploaded'] + self.summary['planned'] >= self.max_files:
                    continue
                expected = fingerprint(path)
                existing = children.get(path.name)
                if existing:
                    if existing['directory']:
                        raise ValueError(f'Remote directory conflicts with file: {relative}')
                    if self.verify_existing and (existing['size'] != expected[0] or existing.get('sha256') != file_hash(path, expected)):
                        raise ValueError(f'Existing file differs; it was not overwritten: {relative}')
                    self.summary['skipped'] += 1
                    self.log(f'SKIP {relative}')
                    if self.state:
                        self.state.remove((self.api.server, self.destination, str(self.source), relative))
                    continue
                if self.dry_run:
                    self.summary['planned'] += 1
                    self.summary['planned_bytes'] += expected[0]
                    self.log(f'PLAN {relative} ({expected[0]} bytes)')
                else:
                    self.upload(path, relative, remote, expected)
            else:
                self.summary['excluded'] += 1

    def upload(self, path, relative, parent, expected):
        key = self.api.server, self.destination, str(self.source), relative
        prior = self.state.get(key)
        matching = prior and tuple(prior[k] for k in ('size', 'mtime_ns', 'device', 'inode')) == expected
        digest = prior['sha256'] if matching else file_hash(path, expected)
        upload_id = prior['upload_id'] if matching else uuid.uuid4().hex
        if prior and not matching:
            try:
                self.api.call('DELETE', f'/uploads/{prior["upload_id"]}')
            except ApiError as error:
                if error.status not in (401, 403, 404, 410):
                    raise
        state = None
        if matching:
            try:
                state = self.api.call('GET', f'/uploads/{upload_id}')
            except ApiError as error:
                if error.status not in (404, 410):
                    raise
                upload_id = uuid.uuid4().hex
        self.state.put(key, upload_id, digest, expected)
        if state is None:
            state = self.api.call('POST', '/uploads', dict(uploadId=upload_id, parentId=parent,
                                 name=path.name, totalSize=expected[0], sha256=digest))
        if (state['sha256'] != digest or state['totalSize'] != expected[0] or
                state['parentId'] != parent or state['name'] != path.name):
            raise ValueError(f'Upload session does not match source: {relative}')
        offset, chunk_size = int(state['receivedSize']), int(state['chunkSize'])
        if offset < 0 or offset > expected[0] or not 0 < chunk_size <= 64 * 1024 * 1024:
            raise ValueError('Server returned an invalid upload offset or chunk size')
        if offset:
            self.summary['resumed'] += 1
            self.log(f'RESUME {relative} at {offset}/{expected[0]} bytes')
        else:
            self.log(f'UPLOAD {relative} ({expected[0]} bytes)')
        conflicts = 0
        last_progress = time.monotonic()
        with open_source(path) as file:
            while offset < expected[0] and not state['completed']:
                check_source(path, expected, file)
                file.seek(offset)
                block = file.read(min(chunk_size, expected[0] - offset))
                if not block:
                    raise ValueError(f'Source ended unexpectedly: {relative}')
                query = urllib.parse.urlencode({'offset': offset, 'size': len(block)})
                try:
                    state = self.api.call('PUT', f'/uploads/{upload_id}/chunk?{query}', block,
                                          {'Content-Type': 'application/octet-stream', 'X-Chunk-SHA256': hashlib.sha256(block).hexdigest()})
                except ApiError as error:
                    if error.status != 409 or conflicts >= 3:
                        raise
                    state = self.api.call('GET', f'/uploads/{upload_id}')
                    conflicts += 1
                    authoritative = int(state['receivedSize'])
                    if not 0 <= authoritative <= expected[0]:
                        raise ValueError('Server returned an invalid retry offset')
                    offset = authoritative
                    continue
                acknowledged = int(state['receivedSize'])
                if not offset + len(block) <= acknowledged <= expected[0]:
                    raise ValueError('Server did not acknowledge the uploaded chunk')
                self.summary['bytes_sent'] += len(block)
                offset = acknowledged
                conflicts = 0
                now = time.monotonic()
                if expected[0] > chunk_size and (now - last_progress >= 3 or offset == expected[0]):
                    self.log(f'PROGRESS {relative}: {offset * 100 / expected[0]:.1f}% ({offset}/{expected[0]} bytes)')
                    last_progress = now
            check_source(path, expected, file)
        if not state['completed']:
            state = self.api.call('POST', f'/uploads/{upload_id}/complete')
        entry = state.get('entry') or {}
        if not state['completed'] or entry.get('sha256') != digest or entry.get('size') != expected[0]:
            raise ValueError(f'Completed file verification failed: {relative}')
        self.state.remove(key)
        self.summary['uploaded'] += 1
        self.log(f'DONE {relative}')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server', default='https://soft.cangling.cn:22002')
    parser.add_argument('--source', required=True)
    parser.add_argument('--directory', required=True, help='Destination drive directory ID; source contents go directly inside it')
    credentials = parser.add_mutually_exclusive_group()
    credentials.add_argument('--token-env', default='DRIVE_TOKEN', help='Environment variable holding a read/write directory token')
    credentials.add_argument('--api-token-env', help='Optional administrator API-token environment variable')
    parser.add_argument('--state-file', default=str(Path(os.environ.get('XDG_STATE_HOME', str(Path.home() / '.local/state'))) / 'cangling-drive-sync/state.sqlite3'))
    parser.add_argument('--exclude', action='append', default=[], help='Exclude a name or source-relative glob; repeatable')
    parser.add_argument('--no-default-excludes', action='store_true')
    policy = parser.add_mutually_exclusive_group()
    policy.add_argument('--verify-existing', action='store_true', help='Fail on existing files whose size/SHA256 differs; never overwrite')
    policy.add_argument('--ignore-existing', action='store_false', dest='verify_existing', help='Skip existing filenames (default, like rsync --ignore-existing)')
    parser.set_defaults(verify_existing=False)
    parser.add_argument('--dry-run', action='store_true', help='Read-only plan: no remote changes or local state file')
    parser.add_argument('--quiet', action='store_true')
    parser.add_argument('--retries', type=int, default=4)
    parser.add_argument('--timeout', type=int, default=120)
    parser.add_argument('--max-files', type=int, help='Limit new uploads, useful for a small first run')
    args = parser.parse_args()
    environment = args.api_token_env or args.token_env
    credential = os.environ.get(environment, '').strip()
    if not credential:
        parser.error(f'Set {environment}; credentials are never accepted as command-line values')
    if args.retries < 0 or args.timeout < 1 or (args.max_files is not None and args.max_files < 1):
        parser.error('Invalid retry, timeout or max-files value')
    if args.directory == 'root' or not args.directory or not all(c in '0123456789abcdef' for c in args.directory) or len(args.directory) != 32:
        parser.error('--directory must be a real directory ID, not the virtual root')
    os.umask(0o077)
    state = None
    api = None
    try:
        api = Api(args.server, credential, bool(args.api_token_env), args.retries, args.timeout)
        if not args.dry_run:
            state = State(args.state_file)
        sync = Sync(api, args.source, args.directory, state, args.dry_run, args.exclude,
                    not args.no_default_excludes, args.verify_existing, args.quiet, args.max_files)
        result = sync.run()
        print(('DRY RUN ' if args.dry_run else '') + 'SUMMARY ' + json.dumps(result, ensure_ascii=False), flush=True)
        return 0
    except KeyboardInterrupt:
        print('\nInterrupted. Run the same command again to resume.', file=sys.stderr)
        return 130
    except (ApiError, OSError, ValueError, sqlite3.Error) as error:
        print(f'ERROR: {error}. Run the same command again to resume.', file=sys.stderr)
        return 1
    finally:
        if state:
            state.close()
        if api:
            api.close()


if __name__ == '__main__':
    sys.exit(main())
