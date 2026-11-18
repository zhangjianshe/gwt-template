"""End-to-end CLI tests with a disposable HTTP endpoint and actual process restarts."""
import hashlib
import http.server
import json
import os
from pathlib import Path
import socket
import subprocess
import tempfile
import threading
import unittest
import urllib.parse
import uuid

SCRIPT = Path(__file__).with_name('drive-sync.py')
ROOT = '1' * 32
CREDENTIAL = 'test-credential-not-for-state'


class Fixture:
    def __init__(self):
        self.entries = {'a' * 32: dict(id='a' * 32, parentId=ROOT, name='already.txt', directory=False,
                                     size=3, sha256=hashlib.sha256(b'old').hexdigest())}
        self.contents = {'a' * 32: b'old'}
        self.sessions = {}
        self.offsets = []
        self.fail_offset = None
        self.fail_after_ack = False
        self.failed = False
        self.writes = 0
        self.redirect_to = None
        self.on_chunk = None
        self.lock = threading.Lock()

    def handler(self):
        fixture = self
        class Handler(http.server.BaseHTTPRequestHandler):
            def log_message(self, *args): pass
            def reply(self, data=None, status=200):
                raw = json.dumps(dict(code=status, message='fixture error' if status != 200 else 'OK', data=data)).encode()
                self.send_response(status)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', str(len(raw)))
                self.end_headers()
                self.wfile.write(raw)

            def route(self):
                if fixture.redirect_to:
                    self.send_response(302); self.send_header('Location', fixture.redirect_to); self.end_headers(); return
                if self.headers.get('X-Drive-Token') != CREDENTIAL:
                    self.reply(status=401)
                    return
                parsed = urllib.parse.urlsplit(self.path)
                path = parsed.path.removeprefix('/api/v1/drive')
                query = urllib.parse.parse_qs(parsed.query)
                raw = self.rfile.read(int(self.headers.get('Content-Length', '0')))
                data = json.loads(raw) if raw and self.headers.get('Content-Type', '').startswith('application/json') else {}
                with fixture.lock:
                    if self.command != 'GET': fixture.writes += 1
                    if path == '/entries':
                        values = [v for v in fixture.entries.values() if v['parentId'] == query['parentId'][0]]
                        self.reply(dict(entries=values, total=len(values)))
                    elif path == '/directories':
                        value = dict(id=uuid.uuid4().hex, parentId=data['parentId'], name=data['name'], directory=True)
                        fixture.entries[value['id']] = value
                        self.reply(value)
                    elif path == '/uploads':
                        upload_id = data['uploadId']
                        if upload_id not in fixture.sessions:
                            fixture.sessions[upload_id] = dict(data, receivedSize=0, chunkSize=4096, completed=False)
                            fixture.sessions[upload_id]['payload'] = b''
                        self.reply(self.clean(fixture.sessions[upload_id]))
                    elif path.startswith('/uploads/'):
                        parts = path.strip('/').split('/')
                        value = fixture.sessions.get(parts[1])
                        if value is None:
                            self.reply(status=404)
                            return
                        if len(parts) == 2 and self.command == 'GET':
                            self.reply(self.clean(value))
                        elif len(parts) == 2 and self.command == 'DELETE':
                            fixture.sessions.pop(parts[1]); self.reply(dict(cancelled=True))
                        elif parts[-1] == 'chunk':
                            offset, size = int(query['offset'][0]), int(query['size'][0])
                            fixture.offsets.append(offset)
                            fail = fixture.fail_offset == offset and not fixture.failed
                            if fail and not fixture.fail_after_ack:
                                fixture.failed = True; self.connection.shutdown(socket.SHUT_RDWR); self.connection.close(); return
                            if hashlib.sha256(raw).hexdigest() != self.headers.get('X-Chunk-SHA256') or len(raw) != size:
                                self.reply(status=422); return
                            if offset == value['receivedSize']:
                                value['payload'] += raw; value['receivedSize'] += size
                            elif value['payload'][offset:offset+size] != raw:
                                self.reply(status=409); return
                            if fail:
                                fixture.failed = True; self.connection.shutdown(socket.SHUT_RDWR); self.connection.close(); return
                            if fixture.on_chunk: fixture.on_chunk(value)
                            self.reply(self.clean(value))
                        elif parts[-1] == 'complete':
                            if hashlib.sha256(value['payload']).hexdigest() != value['sha256']:
                                self.reply(status=422); return
                            entry = dict(id=uuid.uuid4().hex, parentId=value['parentId'], name=value['name'], directory=False,
                                         size=len(value['payload']), sha256=value['sha256'])
                            fixture.entries[entry['id']] = entry
                            fixture.contents[entry['id']] = value['payload']
                            value['completed'] = True; value['entry'] = entry
                            self.reply(self.clean(value))
                        else: self.reply(status=404)
                    else: self.reply(status=404)

            @staticmethod
            def clean(value): return {k: v for k, v in value.items() if k != 'payload'}
            do_GET = route; do_POST = route; do_PUT = route; do_DELETE = route
        return Handler


class SyncTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.source = self.base / 'source'
        self.source.mkdir()
        (self.source / 'docs').mkdir()
        self.payload = bytes(range(251)) * 70
        (self.source / 'docs/big.bin').write_bytes(self.payload)
        (self.source / 'already.txt').write_bytes(b'new-and-different')
        (self.source / 'zero.txt').touch()
        (self.source / 'dist').mkdir(); (self.source / 'dist/package.bin').write_bytes(b'package')
        (self.source / '.git').mkdir(); (self.source / '.git/private').write_text('excluded')
        (self.source / 'build').mkdir(); (self.source / 'build/cache').write_text('excluded')
        (self.base / 'outside').write_text('not part of source')
        (self.source / 'external-link').symlink_to(self.base / 'outside')
        self.state = self.base / 'state/state.sqlite3'
        self.fixture = Fixture()
        self.server = http.server.ThreadingHTTPServer(('127.0.0.1', 0), self.fixture.handler())
        thread = threading.Thread(target=self.server.serve_forever, daemon=True); thread.start()
        self.addCleanup(self.server.server_close); self.addCleanup(self.server.shutdown)
        self.url = f'http://127.0.0.1:{self.server.server_port}'

    def run_cli(self, *args):
        environment = dict(os.environ, DRIVE_TOKEN=CREDENTIAL)
        return subprocess.run(['python3', str(SCRIPT), '--server', self.url, '--source', str(self.source),
                               '--directory', ROOT, '--state-file', str(self.state), '--timeout', '5', *args],
                              env=environment, text=True, capture_output=True, timeout=30)

    def test_process_restart_resumes_and_second_complete_run_skips_everything(self):
        self.fixture.fail_offset = 4096
        first = self.run_cli('--retries', '0')
        self.assertEqual(first.returncode, 1, first.stdout + first.stderr)
        self.assertTrue(self.state.exists())
        self.assertNotIn(CREDENTIAL.encode(), self.state.read_bytes())
        second = self.run_cli('--retries', '0')
        self.assertEqual(second.returncode, 0, second.stdout + second.stderr)
        self.assertIn('RESUME docs/big.bin at 4096/', second.stdout)
        self.assertEqual(self.fixture.offsets.count(0), 2)  # big.bin and package.bin; zero-byte file sends no chunks
        big = next(e for e in self.fixture.entries.values() if e['name'] == 'big.bin')
        self.assertEqual(self.fixture.contents[big['id']], self.payload)
        self.assertEqual(self.fixture.contents['a' * 32], b'old')
        names = {e['name'] for e in self.fixture.entries.values()}
        self.assertTrue({'big.bin', 'package.bin', 'zero.txt'} <= names)
        self.assertFalse({'private', 'cache', 'external-link'} & names)
        before = self.fixture.writes
        third = self.run_cli('--ignore-existing')
        self.assertEqual(third.returncode, 0, third.stderr)
        self.assertEqual(self.fixture.writes, before)
        self.assertIn('"uploaded": 0', third.stdout)

    def test_lost_ack_retries_the_same_chunk_without_duplicate_bytes(self):
        self.fixture.fail_offset = 0; self.fixture.fail_after_ack = True
        result = self.run_cli('--retries', '1')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        big = next(e for e in self.fixture.entries.values() if e['name'] == 'big.bin')
        self.assertEqual(self.fixture.contents[big['id']], self.payload)
        self.assertGreaterEqual(self.fixture.offsets.count(0), 3)

    def test_dry_run_has_no_writes_or_state_and_keeps_dist(self):
        result = self.run_cli('--dry-run')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.fixture.writes, 0)
        self.assertFalse(self.state.exists())
        self.assertIn('PLAN dist/package.bin', result.stdout)
        self.assertNotIn('PLAN .git/', result.stdout)
        self.assertNotIn('PLAN build/', result.stdout)

    def test_verify_existing_detects_changes_without_overwrite(self):
        result = self.run_cli('--verify-existing', '--dry-run')
        self.assertEqual(result.returncode, 1)
        self.assertIn('Existing file differs', result.stderr)
        self.assertEqual(self.fixture.writes, 0)
        self.assertEqual(self.fixture.contents['a' * 32], b'old')

    def test_credentials_are_not_forwarded_to_redirects(self):
        received = []
        class Other(http.server.BaseHTTPRequestHandler):
            def log_message(self, *args): pass
            def do_GET(self):
                received.append(self.headers.get('X-Drive-Token'))
                self.send_response(200); self.end_headers()
        other = http.server.ThreadingHTTPServer(('127.0.0.1', 0), Other)
        threading.Thread(target=other.serve_forever, daemon=True).start()
        self.addCleanup(other.server_close); self.addCleanup(other.shutdown)
        self.fixture.redirect_to = f'http://127.0.0.1:{other.server_port}/collect'
        result = self.run_cli('--dry-run')
        self.assertEqual(result.returncode, 1)
        self.assertIn('redirected', result.stderr)
        self.assertEqual(received, [])
        self.assertNotIn(CREDENTIAL, result.stdout + result.stderr)

    def test_source_change_aborts_then_new_run_replaces_only_the_pending_upload(self):
        changed = []
        def mutate(value):
            if value['name'] == 'big.bin' and not changed:
                (self.source / 'docs/big.bin').write_bytes(b'new-source')
                changed.append(True)
        self.fixture.on_chunk = mutate
        first = self.run_cli('--retries', '0')
        self.assertEqual(first.returncode, 1)
        self.assertIn('Source changed', first.stderr)
        self.assertFalse(any(e['name'] == 'big.bin' for e in self.fixture.entries.values()))
        second = self.run_cli('--retries', '0')
        self.assertEqual(second.returncode, 0, second.stdout + second.stderr)
        big = next(e for e in self.fixture.entries.values() if e['name'] == 'big.bin')
        self.assertEqual(self.fixture.contents[big['id']], b'new-source')


if __name__ == '__main__':
    unittest.main()
