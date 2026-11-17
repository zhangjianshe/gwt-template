"""Publish a universal APK. Secrets come from environment, never source files."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import urllib.request

root = Path(__file__).resolve().parents[1]
server = os.environ.get('MOBILE_RELEASE_SERVER', 'https://soft.cangling.cn:22002').rstrip('/')
if not server.startswith('https://'):
    raise SystemExit('HTTPS release server required')
token = os.environ.get('SOFTWARE_TOKEN')
if not token:
    raise SystemExit('Set SOFTWARE_TOKEN to the repository software token')
package_version = re.search(r'^version:\s*(\S+)', (root / 'pubspec.yaml').read_text(), re.M)[1]
# Existing server directory helper decodes '+' to a space. Use plain semver paths.
version, build = package_version.split('+')
apk = root / 'build/app/outputs/flutter-apk/app-release.apk'
name = f'cangling-mobile-{version}-build{build}.apk'
if not apk.is_file():
    raise SystemExit('Run ./tool/build.sh apk --release first')
summary = os.environ.get('RELEASE_NOTES', '优化应用图标；SQLite 本地配置；新增检查更新与 APK 下载入口。')
# Multipart fields are sent before the file; never place tokens in process arguments.
with tempfile.TemporaryDirectory(prefix='mobile-publish-') as directory:
    directory = Path(directory)
    (directory / 'token').write_text(token)
    config = directory / 'curl.conf'
    lines = [f'url = "{server}/api/v1/software/upload"', 'silent', 'show-error', 'connect-timeout = 30', 'max-time = 900',
             'retry = 2', 'retry-delay = 5', 'retry-connrefused',
             f'form = "token=<{directory}/token"']
    api_token = os.environ.get('API_TOKEN')
    if api_token:
        lines.append(f'header = "API-TOKEN: {api_token}"')
    config.write_text('\n'.join(lines))
    config.chmod(0o600)
    result = subprocess.run(['curl', '--config', str(config), '-F', f'version={version}',
        '-F', f'name={name}', '-F', f'summary={summary}', '-F', 'os=android', '-F', 'arch=all',
        '-F', f'file=@{apk};filename={name}'], capture_output=True, text=True)
    if result.returncode:
        raise SystemExit(f'Upload transport failed (curl {result.returncode}): {result.stderr.strip()}')
    response = json.loads(result.stdout)
    if response.get('code') != 200:
        raise SystemExit(f"Upload rejected: {response.get('code')} {response.get('message')}")
    digest = hashlib.file_digest(apk.open('rb'), 'sha256').hexdigest()
    url = server + response['data']['url']
    downloaded = hashlib.sha256()
    size = 0
    with urllib.request.urlopen(url, timeout=90) as download:
        while chunk := download.read(1024 * 1024):
            downloaded.update(chunk)
            size += len(chunk)
    if size != apk.stat().st_size or downloaded.hexdigest() != digest:
        raise SystemExit('Published APK download failed size/SHA256 verification')
    print(json.dumps({'version': version, 'url': url,
        'sha256': digest, 'size': size, 'verified': True}, ensure_ascii=False))
