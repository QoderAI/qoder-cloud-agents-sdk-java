#!/usr/bin/env python3
"""Release one reviewed Maven bundle; recovery never uploads a replacement."""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zipfile

import check_repository
import release_notes
import release_preflight

REPOSITORY = 'QoderAI/qoder-cloud-agents-sdk-java'
PORTAL = 'https://central.sonatype.com/api/v1/publisher'
CENTRAL = 'https://repo.maven.apache.org/maven2/'
CHECKS = {'quality'} | {f'check (Java {v})' for v in (8, 11, 17, 21, 25)}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n')


def gh(endpoint, body=None, method=None):
    command = ['gh', 'api', f'repos/{REPOSITORY}/{endpoint}']
    if method:
        command += ['--method', method]
    if body is not None:
        command += ['--input', '-']
    result = subprocess.run(command, input=json.dumps(body) if body is not None else None,
                            text=True, capture_output=True)
    if result.returncode:
        # Only a verified 404 means absence; auth/network failures must not create duplicates.
        if '(HTTP 404)' in result.stderr:
            return None
        raise ValueError(f'GitHub API failed for {endpoint}; check access and connectivity')
    return json.loads(result.stdout) if result.stdout.strip() else {}


def git(source, *args):
    return subprocess.check_output(['git', *args], cwd=source, text=True).strip()


def identity():
    version, sha, batch = (os.environ[n] for n in ('RELEASE_VERSION', 'RELEASE_SHA', 'RELEASE_BATCH'))
    release_preflight.validate_inputs(version, sha, batch)
    require(os.environ.get('GITHUB_REPOSITORY') == REPOSITORY, 'Unexpected release repository')
    require(os.environ.get('GITHUB_REF') == 'refs/heads/main', 'Dispatch release from main')
    require(os.environ.get('GITHUB_EVENT_NAME') == 'workflow_dispatch', 'Manual release dispatch required')
    return dict(repository=REPOSITORY, version=version, source_sha=sha, batch_id=batch)


def run_id(value):
    require(re.fullmatch(r'[1-9][0-9]*', value or ''), 'Use a numeric GitHub Actions run ID')
    return value


def deployment_id(value):
    require(str(uuid.UUID(value)) == value, 'Use a canonical Central deployment UUID')
    return value


def tag_name(info):
    return 'v' + info['version']


def tag_message(info):
    return (f"Release {tag_name(info)} (batch_id: {info['batch_id']})\n\n"
            f"Source: {info['source_sha']}\nBundle-SHA256: {info['bundle_sha256']}")


def check_tag(info):
    ref = gh(f"git/ref/tags/{tag_name(info)}")
    require(ref is not None and ref['object']['type'] == 'tag', 'Matching annotated release tag required')
    tag = gh('git/tags/' + ref['object']['sha'])
    require(tag and tag['object']['type'] == 'commit' and tag['object']['sha'] == info['source_sha'],
            'Release tag source differs')
    require(tag['message'].rstrip('\n') == tag_message(info), 'Release tag batch or bundle digest differs')


def check_main(info):
    main = gh('commits/main')
    require(main and main['sha'] == info['source_sha'], 'main changed; prepare a new release dispatch')


def check_ci(info):
    runs = gh('actions/workflows/ci.yml/runs?event=push&branch=main&head_sha=' + info['source_sha'])
    require(runs and runs['workflow_runs'], 'No main CI run for the release SHA')
    current = max(runs['workflow_runs'], key=lambda r: r['id'])
    require(current['head_sha'] == info['source_sha'] and current['conclusion'] == 'success',
            'Latest main CI for this source must succeed')
    jobs = gh(f"actions/runs/{current['id']}/jobs?per_page=100")
    require(jobs and {j['name'] for j in jobs['jobs'] if j['conclusion'] == 'success'} >= CHECKS,
            'Quality and all five Java CI jobs must succeed')


def read_bundle(path, version):
    prefix = f'com/qoder/sdk/qoder-cloud-agents-sdk/{version}/qoder-cloud-agents-sdk-{version}'
    primary = {prefix + suffix for suffix in ('.pom', '.jar', '-sources.jar', '-javadoc.jar')}
    required = primary | {name + '.asc' for name in primary}
    allowed = required | {name + '.' + algo for name in required for algo in ('md5', 'sha1', 'sha256', 'sha512')}
    with zipfile.ZipFile(path) as archive:
        members = [entry for entry in archive.infolist() if not entry.is_dir()]
        names = {entry.filename for entry in members}
        require(len(names) == len(members), 'Duplicate bundle entry')
        require(required <= names <= allowed, 'Bundle must contain only the signed SDK coordinates')
        contents = {entry.filename: archive.read(entry) for entry in members}
    for name in required:
        require(contents[name], 'Empty bundle payload: ' + name)
    for name in primary:
        require(name + '.md5' in names and name + '.sha1' in names, 'Required Maven checksums missing')
    for name, data in contents.items():
        base, _, algo = name.rpartition('.')
        if algo in ('md5', 'sha1', 'sha256', 'sha512'):
            require(data.decode('ascii').strip() == hashlib.new(algo, contents[base]).hexdigest(),
                    'Bundle checksum mismatch: ' + name)
    return contents


def preflight(source, output):
    info = identity()
    require(git(source, 'rev-parse', 'HEAD') == info['source_sha'], 'Wrong source checkout')
    require(check_repository.check_versions(source) == info['version'], 'POM version differs from input')
    notes = release_notes.extract_notes((source / 'CHANGELOG.md').read_text(), info['version'])
    resume = os.environ.get('RESUME_RUN_ID', '')
    if resume:
        original = gh(f'actions/runs/{run_id(resume)}')
        require(original and original['event'] == 'workflow_dispatch' and original['head_branch'] == 'main'
                and original['path'] == '.github/workflows/release.yml', 'Not a main release workflow run')
    else:
        require(not os.environ.get('CENTRAL_DEPLOYMENT_ID'), 'deployment_id requires resume_run_id')
        check_main(info)
        require(gh(f'git/ref/tags/{tag_name(info)}') is None, 'Tag already exists; use resume_run_id')
    check_ci(info)
    output.mkdir(parents=True, exist_ok=True)
    save(output / 'intent.json', info)
    (output / 'release-notes.md').write_text(notes)
    print('Source, release notes and five-JDK CI verified')


def seal(source, candidate, output):
    info = identity()
    require(json.loads((candidate / 'intent.json').read_text()) == info, 'Candidate intent differs')
    bundle = source / 'target/central-publishing/central-bundle.zip'
    contents = read_bundle(bundle, info['version'])
    checks = json.loads((candidate / 'package-check.json').read_text())['sha256']
    for local, expected in checks.items():
        filename = f"qoder-cloud-agents-sdk-{info['version']}.pom" if local == 'pom.xml' else Path(local).name
        path = f"com/qoder/sdk/qoder-cloud-agents-sdk/{info['version']}/{filename}"
        require(hashlib.sha256(contents[path]).hexdigest() == expected,
                'Signed bundle differs from reviewed build: ' + filename)
    expected_files = {'pom.xml'} | {f"target/qoder-cloud-agents-sdk-{info['version']}{suffix}.jar"
                                   for suffix in ('', '-sources', '-javadoc')}
    require(set(checks) == expected_files, 'Incomplete candidate digests')
    info.update(run_id=run_id(os.environ['GITHUB_RUN_ID']), workflow_sha=os.environ['GITHUB_SHA'],
                bundle_sha256=hashlib.sha256(bundle.read_bytes()).hexdigest(),
                files={name: hashlib.sha256(data).hexdigest() for name, data in contents.items()})
    output.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(bundle, output / 'central-bundle.zip')
    shutil.copyfile(candidate / 'release-notes.md', output / 'release-notes.md')
    save(output / 'manifest.json', info)
    print('Signed bundle matches all reviewed candidate digests')


def load(output):
    info = json.loads((output / 'manifest.json').read_text())
    require(all(info.get(k) == v for k, v in identity().items()), 'Bundle release identity differs')
    path = output / 'central-bundle.zip'
    require(hashlib.sha256(path.read_bytes()).hexdigest() == info['bundle_sha256'], 'Bundle digest differs')
    contents = read_bundle(path, info['version'])
    require({n: hashlib.sha256(v).hexdigest() for n, v in contents.items()} == info['files'],
            'Bundle file manifest differs')
    return info


def recover(source, output):
    resume = run_id(os.environ.get('RESUME_RUN_ID'))
    original = gh(f'actions/runs/{resume}')
    require(original and original['event'] == 'workflow_dispatch' and original['head_branch'] == 'main'
            and original['path'] == '.github/workflows/release.yml', 'Not a main release workflow run')
    subprocess.run(['gh', 'run', 'download', resume, '--repo', REPOSITORY,
                    '--name', 'release-bundle', '--dir', str(output)], check=True)
    info = load(output)
    require(info['run_id'] == resume and info['workflow_sha'] == original['head_sha'],
            'Bundle provenance differs from the original workflow')
    notes = release_notes.extract_notes((source / 'CHANGELOG.md').read_text(), info['version'])
    require((output / 'release-notes.md').read_text() == notes, 'Saved release notes differ')
    check_tag(info)
    explicit_id = os.environ.get('CENTRAL_DEPLOYMENT_ID', '')
    if explicit_id:
        save(output / 'receipt.json', dict(deployment_id=deployment_id(explicit_id),
                                          bundle_sha256=info['bundle_sha256']))
    else:
        subprocess.run(['gh', 'run', 'download', resume, '--repo', REPOSITORY,
                        '--name', 'central-receipt', '--dir', str(output)], check=True)
    print('Recovered original bundle; no rebuild or upload will occur')


def create_tag(output):
    info = load(output)
    check_main(info)
    require(gh(f'git/ref/tags/{tag_name(info)}') is None, 'Tag already exists; recover the original run')
    tag = gh('git/tags', dict(tag=tag_name(info), message=tag_message(info),
                             object=info['source_sha'], type='commit'))
    require(tag is not None, 'Cannot create annotated tag')
    ref = gh('git/refs', dict(ref='refs/tags/' + tag_name(info), sha=tag['sha']))
    require(ref is not None, 'Cannot create tag reference')
    check_tag(info)


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise ValueError('Unexpected HTTP redirect; credentials were not forwarded')


def portal_request(path, data=b'', content_type=None):
    username, password = os.environ.get('CENTRAL_USERNAME'), os.environ.get('CENTRAL_PASSWORD')
    require(username and password, 'Central Portal token secrets are missing')
    token = base64.b64encode(f'{username}:{password}'.encode()).decode()
    headers = {'Authorization': 'Bearer ' + token}
    if content_type:
        headers['Content-Type'] = content_type
    request = urllib.request.Request(PORTAL + path, data=data, headers=headers, method='POST')
    try:
        with urllib.request.build_opener(NoRedirect).open(request, timeout=120) as response:
            return response.read()
    except urllib.error.HTTPError as error:
        raise ValueError(f'Central returned HTTP {error.code}; inspect Portal and retained deployment ID') from None
    except (urllib.error.URLError, TimeoutError, OSError):
        raise ValueError('Central connection failed. Do not repeat an uncertain upload; inspect Portal first') from None


def deployment_name(info):
    return f"qoder-cloud-agents-sdk-{info['version']}-{info['source_sha']}-{info['batch_id']}-{info['bundle_sha256']}"


def upload(output):
    info = load(output)
    check_tag(info)
    require(not os.environ.get('RESUME_RUN_ID'), 'Recovery cannot upload')
    boundary = 'qca-' + uuid.uuid4().hex
    body = (f'--{boundary}\r\nContent-Disposition: form-data; name="bundle"; '
            'filename="central-bundle.zip"\r\nContent-Type: application/octet-stream\r\n\r\n').encode()
    body += (output / 'central-bundle.zip').read_bytes() + f'\r\n--{boundary}--\r\n'.encode()
    query = urllib.parse.urlencode(dict(name=deployment_name(info), publishingType='USER_MANAGED'))
    result = portal_request('/upload?' + query, body, 'multipart/form-data; boundary=' + boundary)
    identifier = deployment_id(result.decode().strip())
    save(output / 'receipt.json', dict(deployment_id=identifier, bundle_sha256=info['bundle_sha256']))
    print('Central upload accepted; deployment ID:', identifier)


def publish(output):
    info = load(output)
    check_tag(info)
    receipt = json.loads((output / 'receipt.json').read_text())
    require(receipt['bundle_sha256'] == info['bundle_sha256'], 'Receipt bundle digest differs')
    identifier = deployment_id(receipt['deployment_id'])
    deadline = time.monotonic() + 1800
    requested = False
    while time.monotonic() < deadline:
        status = json.loads(portal_request('/status?' + urllib.parse.urlencode(dict(id=identifier))))
        require(status.get('deploymentId') == identifier
                and status.get('deploymentName') == deployment_name(info), 'Central deployment identity differs')
        expected = f"pkg:maven/com.qoder.sdk/qoder-cloud-agents-sdk@{info['version']}"
        require(not status.get('purls') or status['purls'] == [expected], 'Unexpected Central coordinates')
        state = status.get('deploymentState')
        require(state in ('PENDING', 'VALIDATING', 'VALIDATED', 'PUBLISHING', 'PUBLISHED', 'FAILED'),
                'Unrecognized Central deployment state')
        print('Central deployment state:', state, flush=True)
        if state == 'FAILED':
            raise ValueError('Central validation failed; inspect this deployment in Portal')
        if state == 'PUBLISHED':
            save(output / 'published.json', dict(deployment_id=identifier, state=state))
            return
        if state == 'VALIDATED' and not requested:
            portal_request('/deployment/' + identifier)
            requested = True
        time.sleep(15)
    raise ValueError('Central publication timed out; resume with the original run, never reupload')


def verify(output):
    info = load(output)
    check_tag(info)
    pending = dict(info['files'])
    deadline = time.monotonic() + 1200
    opener = urllib.request.build_opener(NoRedirect)
    while pending and time.monotonic() < deadline:
        for name, expected in list(pending.items()):
            try:
                with opener.open(CENTRAL + name, timeout=30) as response:
                    actual = hashlib.sha256(response.read()).hexdigest()
            except urllib.error.HTTPError as error:
                if error.code in (404, 429, 500, 502, 503, 504):
                    continue
                raise ValueError(f'Central download returned HTTP {error.code}') from None
            require(actual == expected, 'Published file differs from approved bundle: ' + name)
            del pending[name]
        if pending:
            print(f'Waiting for {len(pending)} public Maven files', flush=True)
            time.sleep(15)
    require(not pending, 'Maven Central files are not yet all visible; resume without reuploading')
    save(output / 'verification.json', dict(version=info['version'], source_sha=info['source_sha'],
                                           files_verified=len(info['files'])))
    print('All Maven Central payloads, signatures and checksums match the approved bundle')


def release(output):
    info = load(output)
    check_tag(info)
    body = (output / 'release-notes.md').read_text()
    payload = dict(tag_name=tag_name(info), name=tag_name(info), body=body,
                   draft=False, prerelease='-' in info['version'])
    existing = gh('releases/tags/' + tag_name(info))
    if existing:
        require(all(existing.get(key) == value for key, value in payload.items()),
                'Existing GitHub Release differs; do not overwrite it')
    else:
        require(gh('releases', payload) is not None, 'Cannot create GitHub Release')
    print('GitHub Release notes published')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=('preflight', 'seal', 'recover', 'tag', 'upload', 'publish', 'verify', 'release'))
    parser.add_argument('--source', type=Path, default=Path('source'))
    parser.add_argument('--candidate', type=Path, default=Path('candidate'))
    parser.add_argument('--output', type=Path, default=Path('bundle'))
    args = parser.parse_args()
    if args.command == 'preflight':
        preflight(args.source, args.output)
    elif args.command == 'seal':
        seal(args.source, args.candidate, args.output)
    elif args.command == 'recover':
        recover(args.source, args.output)
    else:
        {'tag': create_tag, 'upload': upload, 'publish': publish,
         'verify': verify, 'release': release}[args.command](args.output)


if __name__ == '__main__':
    try:
        main()
    except (ValueError, KeyError, OSError, subprocess.CalledProcessError, zipfile.BadZipFile) as error:
        sys.exit(str(error))
