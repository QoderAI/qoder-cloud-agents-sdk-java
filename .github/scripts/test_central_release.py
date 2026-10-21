import hashlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import urllib.error
import zipfile

import central_release as release

ENV = dict(RELEASE_VERSION='1.2.3', RELEASE_SHA='a' * 40, RELEASE_BATCH='test-batch',
           GITHUB_REPOSITORY=release.REPOSITORY, GITHUB_REF='refs/heads/main',
           GITHUB_EVENT_NAME='workflow_dispatch', GITHUB_SHA='b' * 40, GITHUB_RUN_ID='123',
           RESUME_RUN_ID='', CENTRAL_DEPLOYMENT_ID='')
UUID = '12345678-1234-1234-1234-123456789abc'


def write_bundle(path, transform=lambda values: values):
    prefix = 'com/qoder/sdk/qoder-cloud-agents-sdk/1.2.3/qoder-cloud-agents-sdk-1.2.3'
    values = {}
    for suffix in ('.pom', '.jar', '-sources.jar', '-javadoc.jar'):
        name = prefix + suffix
        values[name] = ('payload' + suffix).encode()
        values[name + '.asc'] = b'synthetic signature'
        for algo in ('md5', 'sha1', 'sha256', 'sha512'):
            values[name + '.' + algo] = hashlib.new(algo, values[name]).hexdigest().encode()
    with zipfile.ZipFile(path, 'w') as archive:
        for name, data in transform(values).items():
            archive.writestr(name, data)
    return values


class CentralReleaseTests(unittest.TestCase):
    def setUp(self):
        self.env = patch.dict(os.environ, ENV)
        self.env.start()
        self.addCleanup(self.env.stop)
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.output = Path(self.temp.name)
        self.contents = write_bundle(self.output / 'central-bundle.zip')
        self.info = dict(release.identity(), run_id='123', workflow_sha='b' * 40,
                         bundle_sha256=hashlib.sha256((self.output / 'central-bundle.zip').read_bytes()).hexdigest(),
                         files={n: hashlib.sha256(v).hexdigest() for n, v in self.contents.items()})
        release.save(self.output / 'manifest.json', self.info)
        release.save(self.output / 'receipt.json', dict(deployment_id=UUID, bundle_sha256=self.info['bundle_sha256']))
        (self.output / 'release-notes.md').write_text('Reviewed notes\n')

    def status(self, state, **changes):
        return json.dumps(dict(deploymentId=UUID, deploymentName=release.deployment_name(self.info),
                               deploymentState=state, purls=['pkg:maven/com.qoder.sdk/qoder-cloud-agents-sdk@1.2.3'],
                               **changes)).encode()

    def test_identity_rejects_forks_events_and_snapshots(self):
        for key, value in [('GITHUB_REPOSITORY', 'someone/fork'), ('GITHUB_REF', 'refs/heads/feature'),
                           ('GITHUB_EVENT_NAME', 'push'), ('RELEASE_VERSION', '1.2.3-SNAPSHOT')]:
            with self.subTest(key=key), patch.dict(os.environ, {key: value}), self.assertRaises(ValueError):
                release.identity()

    def test_bundle_requires_signatures_checksums_and_exact_coordinates(self):
        self.assertEqual(self.info, release.load(self.output))
        prefix = 'com/qoder/sdk/qoder-cloud-agents-sdk/1.2.3/qoder-cloud-agents-sdk-1.2.3'
        for name in (prefix + '.pom.asc', prefix + '.jar.md5'):
            with self.subTest(name=name):
                write_bundle(self.output / 'central-bundle.zip', lambda values: {k: v for k, v in values.items() if k != name})
                with self.assertRaises(ValueError):
                    release.read_bundle(self.output / 'central-bundle.zip', '1.2.3')
        write_bundle(self.output / 'central-bundle.zip', lambda values: dict(values, **{'../credential.txt': b'private'}))
        with self.assertRaisesRegex(ValueError, 'only the signed SDK'):
            release.read_bundle(self.output / 'central-bundle.zip', '1.2.3')

    def test_bundle_rejects_wrong_checksums_and_manifest_tampering(self):
        key = 'com/qoder/sdk/qoder-cloud-agents-sdk/1.2.3/qoder-cloud-agents-sdk-1.2.3.jar.sha1'
        write_bundle(self.output / 'central-bundle.zip', lambda values: dict(values, **{key: b'bad'}))
        with self.assertRaisesRegex(ValueError, 'checksum mismatch'):
            release.read_bundle(self.output / 'central-bundle.zip', '1.2.3')
        with self.assertRaisesRegex(ValueError, 'Bundle digest differs'):
            release.load(self.output)

    def test_seal_detects_rebuild_differences(self):
        source, candidate = self.output / 'source', self.output / 'candidate'
        bundle_dir = source / 'target/central-publishing'
        bundle_dir.mkdir(parents=True)
        (bundle_dir / 'central-bundle.zip').write_bytes((self.output / 'central-bundle.zip').read_bytes())
        release.save(candidate / 'intent.json', release.identity())
        checks = {}
        for suffix in ('.pom', '.jar', '-sources.jar', '-javadoc.jar'):
            filename = 'qoder-cloud-agents-sdk-1.2.3' + suffix
            key = 'pom.xml' if suffix == '.pom' else 'target/' + filename
            checks[key] = self.info['files']['com/qoder/sdk/qoder-cloud-agents-sdk/1.2.3/' + filename]
        release.save(candidate / 'package-check.json', dict(sha256=checks))
        (candidate / 'release-notes.md').write_text('Reviewed notes\n')
        release.seal(source, candidate, self.output / 'sealed')
        checks['target/qoder-cloud-agents-sdk-1.2.3.jar'] = '0' * 64
        release.save(candidate / 'package-check.json', dict(sha256=checks))
        with self.assertRaisesRegex(ValueError, 'differs from reviewed build'):
            release.seal(source, candidate, self.output / 'sealed')

    def test_ci_requires_latest_success_and_all_five_jdks(self):
        runs = dict(workflow_runs=[dict(id=10, head_sha='a' * 40, conclusion='success')])
        jobs = dict(jobs=[dict(name=name, conclusion='success') for name in release.CHECKS])
        with patch.object(release, 'gh', side_effect=[runs, jobs]):
            release.check_ci(self.info)
        jobs['jobs'][0]['conclusion'] = 'skipped'
        with patch.object(release, 'gh', side_effect=[runs, jobs]), self.assertRaisesRegex(ValueError, 'all five'):
            release.check_ci(self.info)
        runs['workflow_runs'].append(dict(id=11, head_sha='a' * 40, conclusion='failure'))
        with patch.object(release, 'gh', return_value=runs), self.assertRaisesRegex(ValueError, 'Latest main CI'):
            release.check_ci(self.info)

    def test_tag_rejects_moved_sha_and_changed_bundle(self):
        ref = dict(object=dict(type='tag', sha='c' * 40))
        tag = dict(object=dict(type='commit', sha='a' * 40,
                               url=f'https://api.github.com/repos/{release.REPOSITORY}/git/commits/' + 'a' * 40),
                   message=release.tag_message(self.info) + '\n')
        with patch.object(release, 'gh', side_effect=[ref, tag]):
            release.check_tag(self.info)
        tag['message'] = 'different batch or digest'
        with patch.object(release, 'gh', side_effect=[ref, tag]), self.assertRaisesRegex(ValueError, 'batch or bundle'):
            release.check_tag(self.info)
        tag['object']['sha'] = 'e' * 40
        with patch.object(release, 'gh', side_effect=[ref, tag]), self.assertRaisesRegex(ValueError, 'source differs'):
            release.check_tag(self.info)

    def test_preflight_without_environment_still_requires_current_main_and_ci(self):
        source, candidate = self.output / 'source', self.output / 'candidate'
        coordinates = '<groupId>com.qoder.sdk</groupId><artifactId>qoder-cloud-agents-sdk</artifactId><version>1.2.3</version>'
        project = '<project xmlns="http://maven.apache.org/POM/4.0.0">{}</project>'
        files = {
            'pom.xml': project.format(coordinates),
            'examples/pom.xml': project.format(
                '<version>1.2.3</version><dependencies><dependency>' + coordinates + '</dependency></dependencies>'),
            'src/main/resources/com/qoder/cloudagents/version.properties': 'version=${project.version}\n',
            'CHANGELOG.md': '# Changelog\n\n## [Unreleased]\n\n## [1.2.3]\n\n### Added\n\n- Release example.\n',
        }
        for name, contents in files.items():
            path = source / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
        responses = {
            'commits/main': dict(sha='a' * 40),
            'git/ref/tags/v1.2.3': None,
            'actions/workflows/ci.yml/runs?event=push&branch=main&head_sha=' + 'a' * 40:
                dict(workflow_runs=[dict(id=10, head_sha='a' * 40, conclusion='success')]),
            'actions/runs/10/jobs?per_page=100':
                dict(jobs=[dict(name=name, conclusion='success') for name in release.CHECKS]),
        }
        # Any request for repository protections or an environment fails this test.
        with patch.object(release, 'git', return_value='a' * 40), \
                patch.object(release, 'gh', side_effect=responses.__getitem__):
            release.preflight(source, candidate)
            self.assertEqual(release.identity(), json.loads((candidate / 'intent.json').read_text()))
            self.assertIn('Release example.', (candidate / 'release-notes.md').read_text())
            responses['commits/main']['sha'] = 'c' * 40
            with self.assertRaisesRegex(ValueError, 'main changed'):
                release.preflight(source, candidate)
            responses['commits/main']['sha'] = 'a' * 40
            responses['actions/runs/10/jobs?per_page=100']['jobs'][0]['conclusion'] = 'failure'
            with self.assertRaisesRegex(ValueError, 'all five'):
                release.preflight(source, candidate)

    def test_recovery_cannot_upload(self):
        with patch.dict(os.environ, RESUME_RUN_ID='123'), patch.object(release, 'check_tag'), \
                patch.object(release, 'portal_request') as request, self.assertRaisesRegex(ValueError, 'cannot upload'):
            release.upload(self.output)
        request.assert_not_called()

    def test_upload_records_receipt_and_never_retries_uncertain_response(self):
        with patch.object(release, 'check_tag'), patch.object(release, 'portal_request', return_value=UUID.encode()) as request:
            release.upload(self.output)
        self.assertEqual(1, request.call_count)
        self.assertIn('publishingType=USER_MANAGED', request.call_args.args[0])
        self.assertIn(b'name="bundle"', request.call_args.args[1])
        self.assertEqual(UUID, json.loads((self.output / 'receipt.json').read_text())['deployment_id'])
        with patch.object(release, 'check_tag'), patch.object(release, 'portal_request', side_effect=TimeoutError) as request:
            with self.assertRaises(TimeoutError):
                release.upload(self.output)
        self.assertEqual(1, request.call_count)

    def test_publication_resumes_validation_and_publishes_once(self):
        responses = [self.status('VALIDATING'), self.status('VALIDATED'), b'',
                     self.status('VALIDATED'), self.status('PUBLISHING'), self.status('PUBLISHED')]
        with patch.object(release, 'check_tag'), patch.object(release, 'portal_request', side_effect=responses) as request, \
                patch.object(release.time, 'sleep'):
            release.publish(self.output)
        self.assertEqual(1, sum(c.args[0] == '/deployment/' + UUID for c in request.call_args_list))
        self.assertTrue((self.output / 'published.json').exists())

    def test_already_published_deployment_is_not_republished(self):
        with patch.object(release, 'check_tag'), patch.object(release, 'portal_request', return_value=self.status('PUBLISHED')) as request:
            release.publish(self.output)
        self.assertEqual(1, request.call_count)
        self.assertTrue(request.call_args.args[0].startswith('/status?'))

    def test_publication_rejects_wrong_deployment_and_failed_validation(self):
        statuses = [dict(deploymentId=UUID, deploymentName='another upload', deploymentState='VALIDATED'),
                    json.loads(self.status('FAILED')),
                    dict(deploymentId=UUID, deploymentName=release.deployment_name(self.info),
                         deploymentState='VALIDATED', purls=['pkg:maven/other/package@1.2.3'])]
        for status in statuses:
            with self.subTest(status=status), patch.object(release, 'check_tag'), \
                    patch.object(release, 'portal_request', return_value=json.dumps(status).encode()) as request:
                with self.assertRaises(ValueError):
                    release.publish(self.output)
                self.assertEqual(1, request.call_count)

    def test_verification_compares_public_bytes_and_does_not_republish(self):
        def open_file(url, timeout):
            self.assertTrue(url.startswith(release.CENTRAL))
            return io.BytesIO(self.contents[url.removeprefix(release.CENTRAL)])
        with patch.object(release, 'check_tag'), patch.object(release.urllib.request, 'build_opener') as opener:
            opener.return_value.open.side_effect = open_file
            release.verify(self.output)
            opener.return_value.open.side_effect = lambda *a, **kw: io.BytesIO(b'wrong public bytes')
            with self.assertRaisesRegex(ValueError, 'differs from approved bundle'):
                release.verify(self.output)

    def test_existing_release_is_reused_only_if_notes_match(self):
        existing = dict(tag_name='v1.2.3', name='v1.2.3', body='Reviewed notes\n', draft=False, prerelease=False)
        with patch.object(release, 'check_tag'), patch.object(release, 'gh', return_value=existing) as gh:
            release.release(self.output)
        self.assertEqual(1, gh.call_count)
        existing['body'] = 'different notes'
        with patch.object(release, 'check_tag'), patch.object(release, 'gh', return_value=existing), \
                self.assertRaisesRegex(ValueError, 'do not overwrite'):
            release.release(self.output)

    def test_portal_does_not_follow_redirects_or_print_response_secrets(self):
        with self.assertRaisesRegex(ValueError, 'credentials were not forwarded'):
            release.NoRedirect().redirect_request(None, None, 307, '', {}, 'https://other.invalid')
        error = urllib.error.HTTPError(release.PORTAL, 401, 'test', {}, io.BytesIO(b'private token'))
        with patch.dict(os.environ, CENTRAL_USERNAME='synthetic', CENTRAL_PASSWORD='synthetic'), \
                patch.object(release.urllib.request, 'build_opener') as opener:
            opener.return_value.open.side_effect = error
            with self.assertRaisesRegex(ValueError, '^Central returned HTTP 401;'):
                release.portal_request('/status?id=' + UUID)


if __name__ == '__main__':
    unittest.main()
