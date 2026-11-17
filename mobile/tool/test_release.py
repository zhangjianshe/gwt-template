"""Exercise release isolation against disposable local Git repositories."""
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

SCRIPT = Path(__file__).with_name('release.py')

class ReleaseTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        base = Path(self.temp.name)
        self.repo = base / 'repo'
        self.repo.mkdir()
        (self.repo / 'mobile/tool').mkdir(parents=True)
        shutil.copy(SCRIPT, self.repo / 'mobile/tool/release.py')
        (self.repo / 'mobile/pubspec.yaml').write_text('name: example\nversion: 0.2.1+3\n')
        (self.repo / 'version.txt').write_text('1.0.218\n')
        self.git('init', '-b', 'main')
        self.git('config', 'user.name', 'Release Test')
        self.git('config', 'user.email', 'test@example.invalid')
        self.git('add', '.')
        self.git('commit', '-m', 'Initial')
        subprocess.run(['git', 'init', '--bare', str(base / 'remote.git')], check=True, capture_output=True)
        self.git('remote', 'add', 'origin', str(base / 'remote.git'))

    def git(self, *args):
        return subprocess.check_output(['git', *args], cwd=self.repo, text=True, stderr=subprocess.DEVNULL).strip()

    def release(self, *args):
        return subprocess.run(['python3', 'mobile/tool/release.py', *args], cwd=self.repo, capture_output=True, text=True)

    def test_dry_run_never_mutates(self):
        before = self.git('rev-parse', 'HEAD')
        self.assertEqual(self.release('--dry-run').returncode, 0)
        self.assertEqual(self.git('rev-parse', 'HEAD'), before)
        self.assertEqual(self.git('status', '--porcelain'), '')
        self.assertEqual(self.git('tag'), '')

    def test_release_updates_only_mobile_and_pushes_exact_tag(self):
        result = self.release()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('version: 0.2.2+4', (self.repo / 'mobile/pubspec.yaml').read_text())
        self.assertEqual((self.repo / 'version.txt').read_text(), '1.0.218\n')
        self.assertEqual(self.git('diff-tree', '--no-commit-id', '--name-only', '-r', 'HEAD'), 'mobile/pubspec.yaml')
        self.assertEqual(self.git('tag'), 'mobile-v0.2.2')
        self.assertIn('refs/tags/mobile-v0.2.2', self.git('ls-remote', '--tags', 'origin'))

    def test_rejects_dirty_and_nonincreasing_versions(self):
        self.assertNotEqual(self.release('--version', '0.2.0').returncode, 0)
        (self.repo / 'uncommitted').write_text('pending')
        self.assertNotEqual(self.release().returncode, 0)
        self.assertEqual(self.git('tag'), '')
        self.assertIn('version: 0.2.1+3', (self.repo / 'mobile/pubspec.yaml').read_text())

if __name__ == '__main__':
    unittest.main()
