# Java SDK maintenance

The SDK covers Forward and targets Java 8. Use JDK 17 or later for maintenance tools; the runtime compatibility matrix remains Java 8, 11, 17, 21 and 25.

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
- The examples' `com.qoder:qca-sdk` dependency version.

The SDK's embedded version is filtered from `${project.version}`; do not replace that template with a hard-coded value. `check_repository.py` rejects mismatched versions and requires a matching changelog entry for a non-SNAPSHOT version.

```sh
python3 .github/scripts/release_notes.py check
# After adding the actual release entry:
python3 .github/scripts/release_notes.py extract --version 0.1.0
```

The current version remains `0.1.0-SNAPSHOT`. A SNAPSHOT can resolve to different timestamped builds, so acceptance records must include the resolved version, source SHA and JAR digest. A successful test of an older SNAPSHOT is not evidence for a newer one.

## Build candidates and releases

GitHub CI produces candidate JARs, sources, Javadoc, checksums and test reports. A successful CI run is not a Maven Central publication or a live-environment acceptance result. The default workflow uses mock services; run the opt-in checks in [live testing](live-testing.md) with separately supplied credentials and isolated resources when needed.

See [release setup](releasing.md) for repository protection, Central registration and signing secrets, the manual publication workflow and deployment recovery. The workflow requires a configured, reviewed `release` environment; adding workflow files alone does not enable public publication.
