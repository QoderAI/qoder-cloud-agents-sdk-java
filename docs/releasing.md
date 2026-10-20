# Java SDK release setup

The development version is `com.qoder:qca-sdk:0.1.0-SNAPSHOT`. Maven Central publication is not enabled. The GitHub repository, Central namespace and publishing credentials are separate resources.

## Activate GitHub protection

After the first reviewed public source push, run CI and confirm all six checks described in [maintenance](maintaining.md) appear. A repository administrator can then apply the prepared payloads. Inspect existing rules first and update matching rules rather than creating duplicates:

```sh
gh api repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets
gh api --method POST repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets --input .github/config/main-ruleset.json
gh api --method POST repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets --input .github/config/tag-ruleset.json
gh api --method PUT repos/QoderAI/qoder-cloud-agents-sdk-java/environments/release --input .github/config/release-environment.json
gh api --method POST repos/QoderAI/qoder-cloud-agents-sdk-java/environments/release/deployment-branch-policies --input .github/config/release-branch.json
```

The main and `v*` tag protection follows the Go repository baseline. The prepared `release` environment uses maintainer `moonyue-w` (GitHub user ID 300878504), permits `main`, and does not prevent self-review, matching the existing SDK setup. Check that reviewer membership and repository policy are still correct before activation. Applying these settings requires repository administrator access. Configuration files do not activate remote settings by themselves.

## Read-only release preflight

The [Release preflight workflow](../.github/workflows/release-preflight.yml) runs only from `main`. It accepts:

- `version`: the version already present in both POMs and changelog, without `v` or `SNAPSHOT`.
- `commit_sha`: the full 40-character lowercase SHA of the current `main` commit.
- `batch_id`: 1–64 letters, digits, dots, underscores or hyphens, starting with a letter or digit.

It checks the source and changelog, runs quality checks, builds and tests on JDK 17, and retains the candidate JARs, POM, checksums, release intent and notes. It neither creates a tag nor uploads to Maven Central nor publishes a GitHub Release. Current `0.1.0-SNAPSHOT` cannot be passed as a release version. Five-JDK CI remains a separate required gate.

## Prepare a Maven Central bundle

The root POM supplies the public project URL, SCM, license and developer metadata. The opt-in `central` profile requires JDK 17+, a release project version and release dependencies; it signs artifacts with Maven GPG and uses Central Publishing Maven Plugin 0.12.0. Sources and Javadoc are produced by the normal build.

After preparing the release version and configuring a signing key locally, build a bundle with:

```sh
./mvnw -B -ntp -Pcentral clean deploy
```

The profile sets `central.skipUpload=true` and `autoPublish=false`, so this command prepares the signed bundle without uploading it. Do not override `central.skipUpload` until the publication action is authorized. Ordinary builds do not activate the profile, sign artifacts or upload anything.

Before enabling an actual publish workflow, confirm ownership of the `com.qoder` Central namespace, configure the Central Portal token under server ID `central` in external Maven settings, and configure the signing key and passphrase using CI secrets. Keep those credentials out of Git. The public repository existing does not establish namespace ownership.

The eventual publish workflow must bind approval to the reviewed SHA/version, upload the approved bundle, retain the Central deployment ID, verify the published coordinates from a clean consumer cache, and publish matching GitHub tag/release notes. Recovery must reuse the recorded deployment and immutable released version. That upload and recovery workflow is a later step after publisher identity is confirmed; this preflight must not be mistaken for it.

Reference: [Central publication requirements](https://central.sonatype.org/publish/requirements/) and [Central Publishing Maven Plugin](https://central.sonatype.org/publish/publish-portal-maven/).
