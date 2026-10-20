#!/usr/bin/env python3
"""Check/apply the repository's pinned Java format using JDK 17+."""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import urllib.request

VERSION = "1.25.2"
SHA256 = "25157797a0a972c2290b5bc71530c4f7ad646458025e3484412a6e5a9b8c9aa6"
URL = ("https://repo.maven.apache.org/maven2/com/google/googlejavaformat/"
       f"google-java-format/{VERSION}/google-java-format-{VERSION}-all-deps.jar")
ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    jar = ROOT / "target/tools" / f"google-java-format-{VERSION}.jar"
    if not jar.exists():
        with urllib.request.urlopen(URL, timeout=60) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != SHA256:
            raise ValueError("Formatter download checksum mismatch")
        jar.parent.mkdir(parents=True, exist_ok=True)
        jar.write_bytes(data)
    if hashlib.sha256(jar.read_bytes()).hexdigest() != SHA256:
        raise ValueError("Cached formatter checksum mismatch")
    java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME") else "java"
    files = sorted(str(p.relative_to(ROOT)) for base in (ROOT / "src", ROOT / "examples/src")
                   for p in base.rglob("*.java"))
    command = [java, "-jar", str(jar)]
    command += ["--replace"] if args.apply else ["--dry-run", "--set-exit-if-changed"]
    subprocess.run(command + files, cwd=ROOT, check=True)
    print(f"Java format {'applied' if args.apply else 'verified'}: {len(files)} files")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError:
        raise SystemExit("Formatting differs; use make format on JDK 17+.")
