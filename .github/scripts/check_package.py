#!/usr/bin/env python3
"""Inspect the locally built SDK; no network access or publication."""

import argparse
import hashlib
import json
from pathlib import Path
import struct
import xml.etree.ElementTree as ET
import zipfile


def require(condition, message):
    if not condition:
        raise ValueError(message)


def check(root):
    pom = root / "pom.xml"
    xml = ET.parse(pom).getroot()
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    group = xml.findtext("m:groupId", namespaces=ns)
    artifact = xml.findtext("m:artifactId", namespaces=ns)
    version = xml.findtext("m:version", namespaces=ns)
    require(group == "com.qoder.sdk" and artifact == "qoder-cloud-agents-sdk" and version,
            "Unexpected SDK coordinates")
    target = root / "target"
    jars = [target / (artifact + "-" + version + suffix + ".jar")
            for suffix in ("", "-sources", "-javadoc")]
    for jar in jars:
        require(jar.is_file(), "Missing package: " + jar.name)
    with zipfile.ZipFile(jars[0]) as jar:
        names = jar.namelist()
        classes = [name for name in names if name.endswith(".class")]
        require(classes, "SDK JAR has no classes")
        require("com/qoder/cloudagents/ForwardClient.class" in names, "Missing Forward client")
        require(jar.read("META-INF/LICENSE") == (root / "LICENSE").read_bytes(), "License differs")
        properties = jar.read("com/qoder/cloudagents/version.properties").decode("utf-8")
        require("version=" + version in properties.splitlines(), "Embedded version differs from POM")
        for name in classes:
            require(name.startswith("com/qoder/cloudagents/") and "/examples/" not in name,
                    "Bundled dependency or example class: " + name)
            data = jar.read(name)
            require(data[:4] == b"\xca\xfe\xba\xbe" and struct.unpack(">H", data[6:8])[0] == 52,
                    "Class does not target Java 8: " + name)
        require(not any(name.startswith(("src/test/", "test-results/", "artifacts/"))
                        or Path(name).name.startswith(".env") for name in names),
                "Unexpected local or test data in SDK JAR")
    with zipfile.ZipFile(jars[1]) as jar:
        require("com/qoder/cloudagents/ForwardClient.java" in jar.namelist(), "Missing source attachment")
    with zipfile.ZipFile(jars[2]) as jar:
        require("com/qoder/cloudagents/ForwardClient.html" in jar.namelist(), "Missing Javadoc attachment")
        require(not any(name.startswith("com/qoder/cloudagents/core/internal/") for name in jar.namelist()),
                "Internal implementation leaked into public Javadoc")
    files = jars + [pom]
    digests = {str(path.relative_to(root)): hashlib.sha256(path.read_bytes()).hexdigest() for path in files}
    report = {"group_id": group, "artifact_id": artifact, "version": version, "class_count": len(classes),
              "class_major_version": 52, "sha256": digests}
    (target / "package-check.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    (target / "SHA256SUMS").write_text("".join(digest + "  " + name + "\n" for name, digest in digests.items()), encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    try:
        check(parser.parse_args().root.resolve())
    except (ValueError, KeyError, OSError, zipfile.BadZipFile, ET.ParseError) as error:
        raise SystemExit("Package check failed: " + str(error))
