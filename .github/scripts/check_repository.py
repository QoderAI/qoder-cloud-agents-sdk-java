#!/usr/bin/env python3
"""Verify Maven versions, changelog and local documentation links; never publish."""
import argparse
from pathlib import Path
import re
import sys
import urllib.parse
import xml.etree.ElementTree as ET

import release_notes

ROOT = Path(__file__).resolve().parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def check_versions(root):
    pom = ET.parse(root / "pom.xml").getroot()
    version = pom.findtext("m:version", namespaces=NS)
    if not version or not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:-[A-Za-z0-9.-]+)?", version):
        raise ValueError("Use an explicit SDK version in pom.xml")
    if pom.findtext("m:groupId", namespaces=NS) != "com.qoder" or pom.findtext("m:artifactId", namespaces=NS) != "qca-sdk":
        raise ValueError("Unexpected Maven coordinates")
    consumer = ET.parse(root / "examples/pom.xml").getroot()
    deps = [d for d in consumer.findall("m:dependencies/m:dependency", NS)
            if d.findtext("m:groupId", namespaces=NS) == "com.qoder"
            and d.findtext("m:artifactId", namespaces=NS) == "qca-sdk"]
    if len(deps) != 1 or deps[0].findtext("m:version", namespaces=NS) != version:
        raise ValueError("examples SDK dependency must match the root POM")
    if consumer.findtext("m:version", namespaces=NS) != version:
        raise ValueError("examples project version must match the root POM")
    props = (root / "src/main/resources/com/qoder/cloudagents/version.properties").read_text()
    if "version=${project.version}" not in props.splitlines():
        raise ValueError("Embedded version must be filtered from project.version")
    entries = release_notes.parse_changelog((root / "CHANGELOG.md").read_text())
    if not version.endswith("-SNAPSHOT") and version not in entries:
        raise ValueError("Non-SNAPSHOT version requires its own changelog entry")
    return version


def check_links(root):
    paths = [root / name for name in ("README.md", "CONTRIBUTING.md", "CHANGELOG.md",
                                     "examples/README.md", ".github/pull_request_template.md")]
    paths += list((root / "docs").rglob("*.md"))
    count = 0
    for path in paths:
        visible = "\n".join(release_notes.outside_fences(path.read_text()))
        for match in re.finditer(r"\[[^\]]*\]\(([^\s)]+)(?:\s+[^)]*)?\)", visible):
            target = match[1].strip("<>")
            url = urllib.parse.urlsplit(target)
            if url.scheme or url.netloc or not url.path:
                continue
            resolved = (path.parent / urllib.parse.unquote(url.path)).resolve()
            if not resolved.is_relative_to(root.resolve()) or not resolved.exists():
                raise ValueError(f"{path.relative_to(root)}: invalid local link {target}")
            count += 1
    return count


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    args = parser.parse_args()
    version = check_versions(args.root)
    links = check_links(args.root)
    print(f"Repository checks passed: version {version}, {links} local links")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, ET.ParseError) as error:
        sys.exit(str(error))
