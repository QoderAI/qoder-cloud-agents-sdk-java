from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

import check_repository as checks
import release_preflight


class RepositoryTests(unittest.TestCase):
    def test_release_requires_current_main_version_and_notes(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            current = checks.check_versions(checks.ROOT)
            for name in ('pom.xml', 'examples/pom.xml',
                         'src/main/resources/com/qoder/cloudagents/version.properties'):
                target = root / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text((checks.ROOT / name).read_text().replace(current, '1.2.3'))
            (root / 'CHANGELOG.md').write_text('# Changelog\n\n## [Unreleased]\n\n## [1.2.3] - 2026-10-19\n\n### Added\n\n- Release fixture.\n')
            def git(*args):
                return subprocess.check_output(['git', '-c', 'user.name=Release Test',
                    '-c', 'user.email=release@example.invalid', *args], cwd=root,
                    stderr=subprocess.DEVNULL, text=True).strip()
            git('init', '-b', 'main')
            git('add', '.')
            git('commit', '-m', 'fixture')
            sha = git('rev-parse', 'HEAD')
            git('update-ref', 'refs/remotes/origin/main', sha)
            self.assertIn('Release fixture', release_preflight.validate_candidate(root, '1.2.3', sha))
            with self.assertRaisesRegex(ValueError, 'differs from POMs'):
                release_preflight.validate_candidate(root, '1.2.4', sha)
            git('commit', '--allow-empty', '-m', 'new main')
            git('update-ref', 'refs/remotes/origin/main', 'HEAD')
            git('checkout', '--detach', sha)
            with self.assertRaisesRegex(ValueError, 'current origin/main'):
                release_preflight.validate_candidate(root, '1.2.3', sha)

    def test_release_rejects_snapshots_abbreviated_sha_and_unsafe_batch(self):
        release_preflight.validate_inputs('0.1.0', 'a' * 40, 'java-20261019_01')
        for version, sha, batch in [('0.1.0-SNAPSHOT', 'a' * 40, 'batch'),
                                    ('v0.1.0', 'a' * 40, 'batch'),
                                    ('0.1.0', 'a' * 12, 'batch'),
                                    ('0.1.0', 'A' * 40, 'batch'),
                                    ('0.1.0', 'a' * 40, '../batch'),
                                    ('0.1.0', 'a' * 40, 'a' * 65)]:
            with self.assertRaises(ValueError):
                release_preflight.validate_inputs(version, sha, batch)

    def test_versions_and_embedded_version_have_one_source(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            for name in ('pom.xml', 'examples/pom.xml', 'CHANGELOG.md',
                         'src/main/resources/com/qoder/cloudagents/version.properties'):
                target = root / name
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(checks.ROOT / name, target)
            version = checks.check_versions(checks.ROOT)
            self.assertEqual(version, checks.check_versions(root))
            pom = root / 'examples/pom.xml'
            pom.write_text(pom.read_text().replace(version, '0.0.1'))
            with self.assertRaisesRegex(ValueError, 'dependency'):
                checks.check_versions(root)

    def test_links_ignore_code_but_reject_missing_or_external_local_files(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            for name in ('README.md', 'CONTRIBUTING.md', 'CHANGELOG.md',
                         'examples/README.md', '.github/pull_request_template.md'):
                path = root / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text('')
            readme = root / 'README.md'
            readme.write_text('[guide](CONTRIBUTING.md)\n```md\n[example](missing.md)\n```\n')
            self.assertEqual(1, checks.check_links(root))
            for link in ('missing.md', '../outside.md', '/etc/passwd'):
                readme.write_text('[bad](' + link + ')')
                with self.assertRaisesRegex(ValueError, 'invalid local link'):
                    checks.check_links(root)
