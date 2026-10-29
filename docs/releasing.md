# Java SDK release setup

The release version in this checkout is `com.qoder.sdk:qoder-cloud-agents-sdk:0.2.0`. The manual [Release workflow](../.github/workflows/release.yml) publishes using repository Actions secrets. Dispatching it from `main` authorizes publication after all automated checks pass; there is no separate environment approval. The Central account, namespace, signing key and repository settings below must be configured first. The GitHub repository and internal Maven access do not grant Maven Central publishing access.

`0.1.0-rc.1` was published to Maven Central on 2026-10-21; see its [Release](https://github.com/QoderAI/qoder-cloud-agents-sdk-java/releases/tag/v0.1.0-rc.1) and [successful publication workflow](https://github.com/QoderAI/qoder-cloud-agents-sdk-java/actions/runs/39572349619). A version committed to a POM is not yet a published package: check the workflow's public verification and matching GitHub Release for each version.

## Optional GitHub protection

Branch/tag rules and a `release` environment are not prerequisites for this workflow. Its own checks require a manual dispatch from `main`, the intended source SHA and all six successful CI jobs described in [maintenance](maintaining.md).

A repository administrator can optionally apply the prepared main and tag protection payloads later. Inspect existing rules first and update matching rules rather than creating duplicates:

```sh
gh api repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets
gh api --method POST repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets --input .github/config/main-ruleset.json
gh api --method POST repos/QoderAI/qoder-cloud-agents-sdk-java/rulesets --input .github/config/tag-ruleset.json
```

The main and `v*` tag protection follows the Go repository baseline. Configuration files do not activate remote settings by themselves. Without tag protection, the workflow still rejects conflicting tags and never overwrites them, but GitHub does not prevent another authorized actor from changing them.

The unused `release-environment.json` and `release-branch.json` files remain optional templates for adopting environment approval later. To use them, first review the named reviewer and policy, explicitly add `environment: release` to the publishing job, and configure its credentials in that environment. Creating an environment alone does not attach it to this workflow.

## Read-only release preflight

The [Release preflight workflow](../.github/workflows/release-preflight.yml) runs only from `main`. It accepts:

- `version`: the version already present in both POMs and changelog, without `v` or `SNAPSHOT`.
- `commit_sha`: the full 40-character lowercase SHA of the current `main` commit.
- `batch_id`: 1–64 letters, digits, dots, underscores or hyphens, starting with a letter or digit.

It checks the source and changelog, runs quality checks, builds and tests on JDK 17, and retains the candidate JARs, POM, checksums, release intent and notes. It neither creates a tag nor uploads to Maven Central nor publishes a GitHub Release. SNAPSHOT versions cannot be passed as release versions. Five-JDK CI remains a separate required gate.

## Prepare a Maven Central bundle

The root POM supplies the public project URL, SCM, license and developer metadata. The opt-in `central` profile requires JDK 17+, a release project version and release dependencies; it signs artifacts with Maven GPG and uses Central Publishing Maven Plugin 0.12.0. Sources and Javadoc are produced by the normal build.

After preparing the release version and configuring a signing key locally, build a bundle with:

```sh
./mvnw -B -ntp -Pcentral clean deploy
```

The profile sets `central.skipUpload=true` and `autoPublish=false`, so this command prepares the signed bundle without uploading it. Do not override `central.skipUpload` until the publication action is authorized. Ordinary builds do not activate the profile, sign artifacts or upload anything.

The release workflow uses this bundle-only mode, then uploads the retained bundle through the Central Publisher API. It does not pass the publishing token to Maven or re-run Maven to recover an uploaded deployment.

## One-time Central registration

1. Register or sign in to [Central Portal](https://central.sonatype.com/). Use the team's intended publisher account, not an unrelated personal namespace.
2. Open the account menu, **View Namespaces**, and confirm publisher access to `com.qoder.sdk`, matching the SDK's group. If the namespace is already owned by the team, obtain publisher access from its owner rather than registering another namespace or changing the SDK coordinates.
3. Complete any ownership verification requested by Portal before publishing. Existing authorized publishers do not need to repeat namespace registration. A GitHub repository under QoderAI does not itself verify a domain-based namespace.
4. In the publisher's [User Tokens page](https://central.sonatype.com/usertoken), generate a **User Token**. Save the generated username/password pair before closing the dialog; it cannot be retrieved later. These credentials are used for publication, not the normal account login password.
5. Prepare a team-owned, passphrase-protected GPG signing key. Retain its backup securely and make its public key discoverable as required by [Central's GPG guide](https://central.sonatype.org/publish/requirements/gpg/). The workflow expects an ASCII-armored exported OpenPGP secret key, compatible with Maven GPG Plugin's BC signer. Export only the intended signing key.

In repository **Settings → Secrets and variables → Actions**, configure repository-level values:

| Kind | Name | Value |
| --- | --- | --- |
| Variable | `CENTRAL_NAMESPACE` | `com.qoder.sdk`, after the namespace is verified and the publisher has access |
| Secret | `CENTRAL_USERNAME` | Central Portal User Token username |
| Secret | `CENTRAL_PASSWORD` | Central Portal User Token password |
| Secret | `MAVEN_GPG_KEY` | ASCII-armored exported private signing key |
| Secret | `MAVEN_GPG_PASSPHRASE` | Passphrase for that signing key |

Do not put credentials in POMs, repository files, command-line arguments or chat. For example, `gh secret set CENTRAL_USERNAME --repo QoderAI/qoder-cloud-agents-sdk-java` reads the value interactively; repeat for the password and passphrase. An exported key file can be redirected to `gh secret set MAVEN_GPG_KEY ...` without printing it. Do not use `--env release`: environment-only secrets are unavailable to this workflow. The public signing key must be registered separately; configuring a private-key secret does not publish its public key.

The namespace variable is a maintainer's setup confirmation, not an automatic ownership check. Portal enforces actual publishing permissions during upload. The workflow stops before signing/uploading when its required settings are absent. Configuring repository secrets is separate from setting branch/tag protections; a repository administrator may need to enter the credentials if the current maintainer lacks that permission.

## Prepare and publish a release

Prepare a PR that updates the root POM, the example project's version and SDK dependency, plus the changelog version section. Keep an empty `Unreleased` section. Review any version-specific install examples. Merge only after `quality` and all five Java checks succeed. The workflow does not bump versions or merge the PR.

For the combined Forward and Managed `0.2.0` release, run internal integration tests and accept a QA package built from the merged version commit. Record both API surfaces, the Java runtime, cleanup warnings and untested scenarios. Earlier development-package results do not replace this check. Use the merged `0.2.0` main SHA and a new batch ID; leave recovery inputs empty for the first publication.

Open **Actions → Release → Run workflow**, select **main**, and verify the following inputs before dispatch. Successful preflight automatically proceeds to signing and publication; it does not pause for an additional approval.

| Input | First publication |
| --- | --- |
| `version` | The reviewed version without `v` or `SNAPSHOT`, for example `0.2.0` |
| `commit_sha` | Full 40-character lowercase SHA of the current GitHub main |
| `batch_id` | 1–64 letters/digits/dots/underscores/hyphens, starting with a letter or digit |
| `resume_run_id` | Empty |
| `deployment_id` | Empty |

The stages are:

1. **preflight** checks input, POM/changelog, current main and the latest successful main CI run with all six required jobs. It re-runs quality, SDK tests and packaged consumer tests, then records the notes and unsigned package hashes in the run summary.
2. **publish** starts automatically after preflight succeeds. It rebuilds and signs with JDK 17, compares the JAR/POM/sources/Javadoc bytes to the verified candidate, and retains the exact signed bundle before any upload. It checks main again and creates an annotated `v<version>` tag binding the SHA, batch and bundle digest. It uploads once with `USER_MANAGED` publishing, immediately retains the deployment ID, waits for Central validation, requests publication, and waits for `PUBLISHED`. `USER_MANAGED` separates Central validation from the publish API call; the workflow makes that call automatically.
3. **verify** waits for the public Maven files and compares every payload, signature and checksum with the retained bundle. It then runs the independent example/consumer tests against Maven Central using empty Maven settings and a fresh local dependency cache; it never installs the SDK locally first.
4. **release-notes** creates the matching GitHub Release only after verification succeeds. Existing notes must match exactly; the workflow does not overwrite conflicting releases.

The `release-bundle` artifact contains the signed archive, file digests, source/batch/workflow identity and release notes. `central-receipt` records the deployment ID. Retention is 90 days; keep a durable copy for longer recovery needs. The workflow does not include signing secrets in artifacts. Public download verification or consumer failures do not undo a Central publication.

## Recover without replacing a published version

After a source tag or upload exists, start a new **Release** dispatch from main using the **same version, source SHA and batch**, and set `resume_run_id` to the **original** release run containing `release-bundle`. A newer workflow can execute recovery for the original source. It verifies the originating workflow, immutable annotated tag, bundle hashes and Central deployment identity. It skips rebuilding, signing and uploading.

Normally leave `deployment_id` empty: recovery downloads the original `central-receipt`. If the upload response or receipt was lost, first inspect the original run and Central Portal. Supply the exact Portal UUID only after locating the deployment whose name identifies the same source, batch and bundle. A mismatched deployment is rejected. Do not blindly retry an upload that timed out: the server may already have accepted it.

| Failure | Recovery |
| --- | --- |
| Before source tag creation, no upload | Fix setup/source and start a normal dispatch; source changes require the new SHA and successful CI |
| Bundle/tag retained but no receipt | Inspect Portal. If no upload was accepted, an authorized publisher can manually upload the retained `central-bundle.zip` with the exact deployment name described below, then provide its deployment UUID. Do not rebuild the archive |
| Central `FAILED` | Inspect validation errors in Portal. Fixing artifact contents requires a new version; do not replace the tagged bundle |
| Central still validating/publishing or wait timed out | Resume the original run and deployment; no second upload |
| Central already `PUBLISHED`, public propagation/consumer verification failed | Resume; compare the same public files and re-run the clean consumer |
| GitHub Release creation failed | Re-run only the failed release-notes job when its workflow is unchanged; otherwise resume the original publication |
| Tag, notes, source, batch, archive or deployment identity conflict | Stop and investigate the conflict; never move/delete the release tag or overwrite the version |
| Original artifacts expired or missing | Recover the preserved original archive/manifest with maintainer review; this workflow fails rather than rebuilding a replacement |

Central deployment names use `qoder-cloud-agents-sdk-<version>-<full-source-sha>-<batch_id>-<bundle-sha256>` from `manifest.json`. This name binds a manually recovered UUID to the retained bundle. Recovery always points to the original bundle run, not to a later recovery run. If a later recovery supplies the UUID manually, keep supplying it when the original run has no receipt.

## Offline release validation

`make quality` tests input validation, CI gates, bundle integrity, source/tag identity, one-time upload behavior, deployment recovery, public-file comparison and immutable release notes. GitHub's `quality` job also runs `check_central_bundle.py`: it uses a disposable GPG key and a temporary `0.0.0` source copy, runs real Maven packaging/signing with `skipUpload=true`, compares both builds and verifies all four signatures. It has no Central token and makes no publisher API calls. It does not prove actual namespace access, signing-key registration or production publication; the first authorized release must verify those conditions.

Reference: [Central publication requirements](https://central.sonatype.org/publish/requirements/) and [Central Publishing Maven Plugin](https://central.sonatype.org/publish/publish-portal-maven/).
