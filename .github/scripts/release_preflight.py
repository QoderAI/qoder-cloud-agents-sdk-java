#!/usr/bin/env python3
"""Validate release intent; never create a tag or publish a package."""
import argparse
import json
from pathlib import Path
import re
import subprocess
import sys

import check_repository
import release_notes


def validate_inputs(version, sha, batch):
    if not re.fullmatch(r'(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)(?:-(?:alpha|beta|rc)\.[1-9][0-9]*)?', version):
        raise ValueError('Use a release version without v or SNAPSHOT (for example 0.1.0 or 0.1.0-rc.1)')
    if not re.fullmatch(r'[0-9a-f]{40}', sha):
        raise ValueError('Use the full lowercase main commit SHA')
    if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,63}', batch):
        raise ValueError('batch_id must contain 1-64 letters, digits, dots, underscores or hyphens')


def validate_candidate(root, version, sha):
    def git(*parts):
        return subprocess.check_output(['git', *parts], cwd=root, text=True).strip()
    if git('rev-parse', 'HEAD') != sha or git('rev-parse', 'refs/remotes/origin/main') != sha:
        raise ValueError('Release candidate must be the checked out current origin/main commit')
    if check_repository.check_versions(root) != version:
        raise ValueError('Requested version differs from POMs')
    return release_notes.extract_notes((root / 'CHANGELOG.md').read_text(), version)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--version', required=True)
    parser.add_argument('--commit-sha', required=True)
    parser.add_argument('--batch-id', required=True)
    parser.add_argument('--output', type=Path, default=Path('build/release-preflight'))
    args = parser.parse_args()
    validate_inputs(args.version, args.commit_sha, args.batch_id)
    notes = validate_candidate(check_repository.ROOT, args.version, args.commit_sha)
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output / 'release-notes.md').write_text(notes)
    (args.output / 'release-intent.json').write_text(json.dumps(dict(
        version=args.version, commit_sha=args.commit_sha, batch_id=args.batch_id,
        publication='not_performed', build_jdk='17'), indent=2)+'\n')
    print('Release inputs and notes verified; publication has not been performed')


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        sys.exit(str(error))
