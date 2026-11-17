"""Bump only mobile/pubspec.yaml and push an independent mobile-vX.Y.Z tag."""
import argparse
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[2]

def git(*args):
    return subprocess.check_output(['git', *args], cwd=root, text=True).strip()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--version', help='Explicit next stable version, e.g. 0.3.0')
    args = parser.parse_args()
    path = root / 'mobile/pubspec.yaml'
    text = path.read_text()
    match = re.search(r'^version: (\d+)\.(\d+)\.(\d+)\+(\d+)$', text, re.M)
    if not match:
        raise SystemExit('Expected mobile version: X.Y.Z+BUILD')
    major, minor, patch, build = map(int, match.groups())
    version = args.version or f'{major}.{minor}.{patch + 1}'
    if not re.fullmatch(r'(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)', version):
        raise SystemExit('Version must be X.Y.Z')
    if tuple(map(int, version.split('.'))) <= (major, minor, patch):
        raise SystemExit('Mobile version must increase')
    tag = f'mobile-v{version}'
    branch = git('branch', '--show-current')
    if not branch:
        raise SystemExit('A branch checkout is required')
    if git('tag', '--list', tag):
        raise SystemExit(f'Tag already exists: {tag}')
    print(f'Mobile: {match[0][9:]} -> {version}+{build + 1}\nTag: {tag}\nBranch: {branch}\nWorkflow: Mobile release')
    if args.dry_run:
        print('Dry run: no files, commits, tags or remote state changed.')
        return
    if git('status', '--porcelain'):
        raise SystemExit('Commit or stash existing changes before releasing.')
    if git('ls-remote', '--tags', 'origin', f'refs/tags/{tag}'):
        raise SystemExit(f'Remote tag already exists: {tag}')
    text = text[:match.start()] + f'version: {version}+{build + 1}' + text[match.end():]
    path.write_text(text)
    subprocess.run(['git', 'add', 'mobile/pubspec.yaml'], cwd=root, check=True)
    subprocess.run(['git', 'commit', '-m', f'Release mobile {version} (build {build + 1})'], cwd=root, check=True)
    subprocess.run(['git', 'tag', '-a', tag, '-m', f'cangling mobile {version}'], cwd=root, check=True)
    # Publish the exact commit and tag together; never trigger the server v* workflow.
    subprocess.run(['git', 'push', '--atomic', 'origin', f'HEAD:refs/heads/{branch}', f'refs/tags/{tag}'], cwd=root, check=True)
    print(f'Published {tag}. Track the Mobile release workflow in GitHub Actions.')

if __name__ == '__main__':
    main()
