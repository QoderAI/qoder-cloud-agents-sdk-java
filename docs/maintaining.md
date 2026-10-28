# Java SDK maintenance

The development SDK covers Forward and Managed and targets Java 8. Use JDK 17 or later for maintenance tools; the runtime compatibility matrix remains Java 8, 11, 17, 21 and 25.

## Local checks and pull requests

```sh
make quality
make test
```

`quality` checks Java formatting with pinned google-java-format 1.25.2, repository Markdown links, version consistency, changelog structure and the maintenance scripts' tests. The formatter download is SHA-256 verified. Run `make format` to apply formatting. These checks do not replace security or dependency scanning.

`test` builds and installs the SDK locally, verifies the independent examples consumer, and checks the main, sources and Javadoc JARs, embedded version, license and Java 8 bytecode. Default tests use mock servers. Production acceptance is a separate opt-in step described in [live testing](live-testing.md).

GitHub CI has six required-check names: `quality`, `check (Java 8)`, `check (Java 11)`, `check (Java 17)`, `check (Java 21)` and `check (Java 25)`. The [main ruleset payload](../.github/config/main-ruleset.json) must keep those names aligned with the workflow. Adding the JSON file does not activate GitHub protection.

The GitHub `quality` job also exercises the Central bundle profile with a temporary source copy and disposable signing key. It verifies reproducible artifact bytes and signatures without uploading a package or requiring publishing credentials.

CODEOWNERS assigns the same maintainer as the other SDK repositories. The prepared protection policy follows the Go baseline: a PR, resolved discussions, required checks, linear history, and no force pushes or branch deletion. It does not add a mandatory review count or mandatory CODEOWNER approval. Use the PR template to record cross-SDK impact, validation, skipped checks and candidate source evidence.

## Changelog and versions

Record user-visible changes under `CHANGELOG.md`'s `Unreleased` section. Describe behavior and migration steps for incompatible changes. Maintenance-only changes do not require an artificial product change entry.

For a release, move the applicable entries beneath `## [VERSION] - YYYY-MM-DD` and retain an empty `Unreleased`. Update all three version locations together:

- Root `pom.xml` project version.
- `examples/pom.xml` project version.
- The examples' `com.qoder.sdk:qoder-cloud-agents-sdk` dependency version.

The SDK's embedded version is filtered from `${project.version}`; do not replace that template with a hard-coded value. `check_repository.py` rejects mismatched versions and requires a matching changelog entry for a non-SNAPSHOT version.

```sh
python3 .github/scripts/release_notes.py check
# After adding the actual release entry:
python3 .github/scripts/release_notes.py extract --version 0.1.0
```

The release version in this checkout is `0.1.0`. Also update the README dependency and version-specific maintenance examples. Acceptance records must include the resolved version, source SHA and JAR digest. Release versions must not be overwritten; a successful test of an older build is not evidence for a newer one.

### RC qualification for 0.1.0

The `0.1.0-rc.1` package from source `7742942d6d9dc3f681cb974b45ecfffe5083ad4c` was downloaded from Maven Central and tested on Java 8, 11, 17, 21 and 25 on 2026-10-21. Its JAR SHA-256 was `4eb33d871d234fd80b0302b2bf03d90d9c121d87ad0d50eed098a28505afaa86`.

- All 608 offline checks per JDK passed: 545 SDK regression tests, 62 example-consumer tests and one independent published-JAR/hash/Java 8 bytecode check, for 3,040 executions.
- The CN/Global PAT/SA live matrix ran 38 groups per JDK. The first run passed 184 of 190 groups. Six CN failures covered three Schedule reply checks, one Batch output check, one Realtime `work.failed` event and one Realtime PCM connection exception.
- Each failed case passed one targeted retry with the same package, configuration, assertions and JDK. Original failures were retained; no assertion was weakened. This is passing coverage after retries, not a clean first run or proof that the production failures were fixed. No cleanup failures or retained execution resources were reported.
- Channel/QR/Pairing external-platform flows, audio-device quality, long-duration stability and production fault injection were not part of that live matrix; see [live testing](live-testing.md).

The `0.1.0` preparation changes version metadata and documentation, keeping the RC's implementation and test sources unchanged. The release workflow must still build, sign and independently verify the actual `0.1.0` package; RC acceptance does not replace those checks.

## Build candidates and releases

GitHub CI produces candidate JARs, sources, Javadoc, checksums and test reports. A successful CI run is not a Maven Central publication or a live-environment acceptance result. The default workflow uses mock services; run the opt-in checks in [live testing](live-testing.md) with separately supplied credentials and isolated resources when needed.

See [release setup](releasing.md) for optional repository protection, Central registration and repository Actions secrets, the manual publication workflow and deployment recovery. A manual dispatch from `main` proceeds after preflight without a separate environment approval. Central credentials and signing setup are still required; adding workflow files alone does not enable public publication.
