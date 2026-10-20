#!/usr/bin/env python3
"""Exercise release signing with a disposable key and no Central upload."""
import os
from pathlib import Path
import subprocess
import tarfile
import tempfile

import central_release
import check_package
import check_repository


def main():
    root = check_repository.ROOT
    with tempfile.TemporaryDirectory(prefix='qca-central-dry-run-') as temporary:
        work = Path(temporary)
        source, keys, candidate, bundle = (work / name for name in ('source', 'keys', 'candidate', 'bundle'))
        source.mkdir()
        keys.mkdir(mode=0o700)
        archive = work / 'source.tar'
        subprocess.run(['git', 'archive', '--output', str(archive), 'HEAD'], cwd=root, check=True)
        with tarfile.open(archive) as tar:
            tar.extractall(source, filter='data')
        version = check_repository.check_versions(root)
        for pom in (source / 'pom.xml', source / 'examples/pom.xml'):
            pom.write_text(pom.read_text().replace(version, '0.0.0'))
        env = dict(os.environ, GNUPGHOME=str(keys), MAVEN_GPG_PASSPHRASE='disposable-test-key')
        for name in ('CENTRAL_USERNAME', 'CENTRAL_PASSWORD'):
            env.pop(name, None)
        gpg = ['gpg', '--batch', '--pinentry-mode', 'loopback', '--passphrase', env['MAVEN_GPG_PASSPHRASE']]
        try:
            subprocess.run(gpg + ['--quick-generate-key', 'SDK Release Test <test@example.invalid>',
                                 'rsa2048', 'sign', '1d'], env=env, check=True)
            env['MAVEN_GPG_KEY'] = subprocess.check_output(
                gpg + ['--armor', '--export-secret-keys'], env=env, text=True)
            subprocess.run(['./mvnw', '-B', '-ntp', 'clean', 'package'], cwd=source, env=env, check=True)
            check_package.check(source)
            candidate.mkdir()
            (candidate / 'package-check.json').write_bytes((source / 'target/package-check.json').read_bytes())
            (candidate / 'release-notes.md').write_text('Disposable signing check; never published.\n')
            subprocess.run(['./mvnw', '-B', '-ntp', '-Pcentral', '-Dgpg.signer=bc',
                            '-Dcentral.skipUpload=true', '-DskipTests', 'clean', 'deploy'],
                           cwd=source, env=env, check=True)
            os.environ.update(RELEASE_VERSION='0.0.0', RELEASE_SHA='a' * 40, RELEASE_BATCH='offline-signing-check',
                              GITHUB_REPOSITORY=central_release.REPOSITORY, GITHUB_REF='refs/heads/main',
                              GITHUB_EVENT_NAME='workflow_dispatch', GITHUB_RUN_ID='1', GITHUB_SHA='a' * 40)
            central_release.save(candidate / 'intent.json', central_release.identity())
            central_release.seal(source, candidate, bundle)
            contents = central_release.read_bundle(bundle / 'central-bundle.zip', '0.0.0')
            for name, data in contents.items():
                if not name.endswith('.asc'):
                    continue
                signature, payload = work / 'signature.asc', work / 'payload'
                signature.write_bytes(data)
                payload.write_bytes(contents[name[:-4]])
                subprocess.run(['gpg', '--batch', '--verify', str(signature), str(payload)], env=env, check=True)
            print('Central dry run passed: reproducible SDK/POM/sources/Javadoc, four valid signatures, no upload')
        finally:
            subprocess.run(['gpgconf', '--kill', 'gpg-agent'], env=env, check=False)


if __name__ == '__main__':
    main()
