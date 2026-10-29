# Forward and Managed examples

This independent Maven project consumes `com.qoder.sdk:qoder-cloud-agents-sdk` as a JAR dependency. `ForwardReadOnlyExample` lists Models and the first Template page, then optionally retrieves `QODER_TEMPLATE_ID`, without mutating resources. `TemplateLifecycleExample` creates, updates, clones and retrieves isolated Templates, then archives every created ID. `IdentityConfigExample` creates an isolated Identity, configures it for an existing Template, retrieves effective configuration, then clears and deletes that Identity. All share PAT/SA setup through `ExampleClient`.

To use a published version, check out its matching release tag and run from the repository root. Maven resolves the SDK dependency without building SDK sources:

```sh
./mvnw -B -ntp -f examples/pom.xml clean verify
```

For SDK development or an unpublished version, install the local SDK first:

```sh
./mvnw clean install
./mvnw -f examples/pom.xml verify
```

Verification uses a local fake service and asserts the SDK was loaded from a JAR. It does not use production credentials.

`ClientExtensionsExample` demonstrates a read-only generic JSON request, a per-attempt tracing interceptor and an independently closeable client copy. It also accepts an application-owned `HttpTransport`. These extension APIs are included in 0.2.0; run with `-Dexec.mainClass=com.qoder.cloudagents.examples.ClientExtensionsExample`. The packaged-consumer test verifies an adapter built with Java 8 `HttpURLConnection`, without importing SDK internals.

To run against your environment, configure credentials in your shell and choose one mode:

| Mode | Environment |
| --- | --- |
| PAT | `QODER_AUTH_MODE=pat`, `QODER_PAT`; unset `QODER_ACCESS_TOKEN` to use the PAT |
| SA automatic exchange | `QODER_AUTH_MODE=sa`, `QODER_SA_KEY`, `QODER_REGION=cn` or `global` |
| Existing SAT | `QODER_AUTH_MODE=sa`, `QODER_ACCESS_TOKEN`; unset `QODER_SA_KEY` |

Set `QODER_FORWARD_BASE_URL` to `https://api.qoder.com.cn/api/v1/forward` for CN, or `https://api.qoder.com/api/v1/forward` for Global. SA region and BaseURL must match. Optionally set `QODER_TEMPLATE_ID` to an accessible template.

```sh
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ForwardReadOnlyExample
```

`QODER_AUTH_MODE` and `QODER_REGION` are read by the **example helper**. The SDK selects SA through explicit `ServiceAccountCredential` or `.authMode(AuthMode.SA)`. Merely exporting an SA Key does not change SDK authentication.

## Template lifecycle

Use the same PAT/SA setup above, plus an existing test Environment accessible to that credential:

```sh
export QODER_ALLOW_WRITE=true
export QODER_ENVIRONMENT_ID=env_example
export QODER_MODEL=auto
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.TemplateLifecycleExample
```

The example uses unique names and a separate idempotency key per write. It archives created Templates even when a later operation fails, and reports failed cleanup IDs. Archive retains the record; the public Template API has no delete endpoint. An ambiguous network failure after creation can still require a manual check by the unique `sdk-java-<UUID>` name.

## Identity configuration

Use the same PAT/SA setup, plus an accessible test Template:

```sh
export QODER_ALLOW_WRITE=true
export QODER_TEMPLATE_ID=tmpl_example
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.IdentityConfigExample
```

The example uses a new `sdk-java-<UUID>` external ID and separate idempotency keys for each write. It updates only that Identity's configuration and does not change the Template. Cleanup first calls `clear`, checks its `completed` status, then deletes the Identity. A failed/incomplete clear leaves the Identity available for recovery and prints `cleanup_failed_identity`; cleanup errors do not hide an earlier business failure. A create timeout can require searching by the unique external ID. Do not substitute an existing business Identity into this cleanup sequence: `clear` affects its associated resources and sessions.

`verify` runs all examples only against loopback mock services, including cleanup-failure paths; running the commands above is a separate live action. See [live testing](../docs/live-testing.md) for opt-in environment checks.

## Session lifecycle and optional Memory Store mount

`SessionLifecycleExample` uses the same PAT/SA setup. It creates an isolated Identity and Session from an existing Template, retrieves/updates the Session, reads one page of event and Thread history, then cleans up. It does not send model input. Creating a Session can still provision runtime resources.

```sh
export QODER_ALLOW_WRITE=true
export QODER_TEMPLATE_ID=tmpl_example
# Optional: an existing, active Store accessible to the credential.
export QODER_MEMORY_STORE_ID=memstore_example
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.SessionLifecycleExample
```

Omit `QODER_MEMORY_STORE_ID` to skip the optional mount operation. When supplied, the Store is mounted on the newly created Identity before creating its Session. Cleanup archives the Session, detaches the mount, then clears/deletes the test Identity; the supplied Store and Template are not deleted. A failed archive or detach leaves the Identity for recovery. Subsequent cleanup errors are retained alongside the original error and relevant IDs are printed.

The create external ID is unique. If creation succeeds server-side but the response is lost, manually inspect that external ID before retrying the whole example. These workflows have local mock coverage using the installed SDK JAR; use [live testing](../docs/live-testing.md) to exercise scenarios against a selected environment.

## Memory Store lifecycle

`MemoryStoreLifecycleExample` uses the same PAT/SA setup and needs no existing Template, Identity or Store:

```sh
export QODER_ALLOW_WRITE=true
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.MemoryStoreLifecycleExample
```

It creates an isolated, unmounted Store with a creation idempotency key; retrieves/updates the Store; creates and updates a Memory using the retrieved content hash; reads Memory and Version lists and a version's details; then deletes the Memory and Store. Store deletion also removes this test data's history. It does not archive or redact and never uses an existing Store as its cleanup target.

The unique Store name and creation key are printed before the first request. If creation succeeds but its response is lost, use those values to locate or recover the same creation before starting a new run. Cleanup failures report the Store ID and remain attached to any original business exception. Content and credentials are not printed. See [Memory Store semantics](../docs/memory-stores.md).

This example has local mock coverage. It is also part of the opt-in resource group described in [live testing](../docs/live-testing.md).

## Environment and File lifecycle

Use the same PAT/SA setup; no pre-existing Environment, Template or File is required:

```sh
export QODER_ALLOW_WRITE=true
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.EnvironmentFileExample
```

The example creates an isolated cloud Environment, retrieves/updates its configuration, uploads a small UTF-8 text file, reads metadata and lists, and compares the streamed download to the original bytes. It then deletes only its own File and Environment. It does not start a Session or execute a setup script. Both cleanup operations are attempted, and their errors are retained alongside any original failure.

The batch and creation idempotency keys are printed before creating resources; use them to recover an ambiguous creation response. Signed URLs, credentials and file content are not printed. Default verification uses local mock services and the installed JAR. See [Environment/File behavior](../docs/environments-files.md).

Never commit actual keys, tokens or live responses. Session streaming and Realtime examples are documented below.

## Skill versions and Vault secrets

Use the shared PAT/SA setup and explicitly enable writes:

```sh
export QODER_ALLOW_WRITE=true
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.SkillVaultExample
```

This example creates its own Skill ZIP and immutable version, checks the downloaded ZIP member name and exact SKILL.md bytes (the service wraps the canonical ZIP, so its download hash differs from stored metadata), then creates a Vault and an environment-variable Credential with a random test secret. It rotates the secret and checks the update acknowledgement; a redacted response cannot independently verify the stored value. It deletes the extra version and attempts cleanup of its Credential, Vault and Skill, retaining failures. It never starts a Session, contacts an MCP server or reads an existing secret.

Creation keys and resource IDs are printed for recovery after an ambiguous failure; secrets and package contents are not printed. Credential update never retries. This workflow also passed the first CN/Global PAT/SA live matrix. See [Skill/Vault behavior](../docs/skills-vaults.md).

## Schedule and Run

```bash
export QODER_ALLOW_WRITE=true
export QODER_IDENTITY_ID=idn_your_test_identity
export QODER_TEMPLATE_ID=tmpl_your_test_template
export QODER_ENVIRONMENT_ID=env_your_test_environment
../mvnw -B -ntp compile exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ScheduleLifecycleExample
```

This creates one manual Schedule, reads/updates it, pauses/unpauses it, lists its Runs and archives the Schedule in cleanup. It creates no Channel sink, never modifies the supplied Identity/Template/Environment, and does not execute by default. Setting `QODER_ALLOW_EXECUTION=true` additionally creates one Run, which may consume credits. The example records its Run ID and current Session ID but does not wait for completion. Archiving a Schedule does not cancel its existing Run; use the printed IDs to inspect/clean up execution where permitted. Primary errors retain cleanup errors as suppressed exceptions.

## Batch submission and inspection

Prepare `batch.jsonl` with existing accessible test resources (one JSON object per line):

```jsonl
{"custom_id":"task-1","template_id":"tmpl_your_test_template","identity_id":"idn_your_test_identity","body":{"input":"Reply with exactly SDK_JAVA_BATCH_OK"}}
```

```bash
export QODER_ALLOW_WRITE=true
export QODER_ALLOW_EXECUTION=true
export QODER_BATCH_INPUT=/absolute/path/batch.jsonl
# Optional: default false. True bypasses idle hours, not queue/capacity rules.
export QODER_IGNORE_IDLE_WINDOW=true
../mvnw -B -ntp compile exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.BatchExample
```

Submission uploads JSONL with `purpose=session_resource`, creates a Batch with a unique idempotency key, and prints Batch/input IDs. It does not wait for execution or delete input that may still be needed. Reconcile with the printed key/ID on ambiguous failure; retain input until terminal, then delete your own file when no longer referenced. Completion is not guaranteed immediately even with `ignore_idle_window=true`.

For read-only inspection, set `QODER_BATCH_ID` to the returned ID and run the same class. This mode needs no write/execution opt-in. It prints state and up to 20 task summaries; terminal result grants are fetched only when file IDs exist. It prints grant expiration times, never signed URLs. Batch output/error IDs cannot be downloaded using the generic Files API. See [behavior and usage](../docs/schedules-batches.md).

These examples have been exercised against local HTTP fixtures through the installed SDK JAR, not CN/Global production.

## Channel lifecycle

```bash
export QODER_ALLOW_WRITE=true
export QODER_CHANNEL_TYPE=feishu
../mvnw -B -ntp compile exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ChannelLifecycleExample
```

Creates a disabled pairing-mode Channel without IM credentials, reads it, updates display/response options while keeping it disabled, lists disabled Channels, and deletes only its own Channel. It never starts QR authorization, pairs a message scope, enables a Channel or sends messages. Creation keys/IDs are printed for reconciliation. Cleanup failures are retained alongside the original error. Run against an explicitly selected test environment when online acceptance is arranged; this stage has only local mock verification.

`ChannelAuthorizationExample` has application-callable helpers rather than a CLI that dumps QR secrets:

- `beginQr` returns the QR payload to your authorized UI.
- `inspectQr` performs one poll and refreshes Channel details on `confirmed`; it does not initiate Pairing or re-authorize an expired QR.
- `pair` accepts either a pairing code or pending Pairing ID, plus Identity and Template IDs. It returns the binding for the caller to keep.
- `unpair` explicitly revokes the chosen binding.

Choose an existing enabled test Channel before QR creation. Protect QR session keys, QR content/images, pairing codes and IM credentials; helpers do not print them. Full behavior and direct credential field mapping are in [Channels](../docs/channels.md). All example helper paths are compiled and tested through the installed SDK JAR with local services only.

## Usage, Forward SAT management and Drive

`UsageExample` is read-only and uses the shared PAT/SA setup. Set `QODER_USAGE_START_AT` and `QODER_USAGE_END_AT` to an hourly Asia/Shanghai window, for example `2026-09-14T09:00:00` and `2026-09-14T12:00:00`. It prints the first page of both aggregations and whether more pages exist. Use PAT or an administrator SAT.

```sh
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.UsageExample
```

`ForwardTokenManagementExample` needs `QODER_ALLOW_WRITE=true`, `QODER_FORWARD_BASE_URL`, `QODER_SA_KEY` and an accessible `QODER_IDENTITY_ID`. It explicitly uses the Key for Forward Token management, issues admin and Identity SATs, makes read-only Models/Identity Config calls with those SATs, then revokes only the issued Tokens. It does not perform the platform `ServiceAccountCredential` exchange. Failed revocations retain IDs for recovery; a lost issuance response can require manual inspection. No Key or SAT is printed.

```sh
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ForwardTokenManagementExample
```

`DriveExample` uses the shared PAT/SA setup plus `QODER_ALLOW_WRITE=true` and `QODER_IDENTITY_ID`. It writes one small UTF-8 file at a unique path, lists that directory, downloads and compares the content, then deletes only its own file. It never calls `clear`. A separate storage connection uses only the grant headers, permits HTTPS (HTTP loopback for local tests), disables redirects and closes streams/connections. Signed URLs and file content are not printed.

```sh
./mvnw -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.DriveExample
```

These examples have local API/storage mock coverage. The opt-in [live runner](../docs/live-testing.md) exercises Drive, Usage and SAT permission boundaries against a selected environment. See [semantics and method examples](../docs/usage-sat-drive.md). Session SSE and Realtime examples are documented below.


## Session / Thread SSE

`SessionStreamingExample` subscribes to an existing authorized Session or Thread using the shared PAT/SA setup. Set `QODER_SESSION_ID`; optionally set `QODER_THREAD_ID`, `QODER_LAST_EVENT_ID` and `QODER_STREAM_ASYNC=true`. The default mode uses synchronous iteration. It explicitly enables reconnects, ends after 20 events or a two-minute total deadline, and closes its own connection without cancelling/archiving the Session or sending a message.

```sh
./mvnw -B -ntp -f examples/pom.xml exec:java   -Dexec.mainClass=com.qoder.cloudagents.examples.SessionStreamingExample
```

Use the last ID from the corresponding history endpoint when you need continuity. With no cursor the service starts at the current tail. Only Session streams request `agent.message` deltas; Thread streams have no selectors. The example prints event types/IDs and finally the resume checkpoint/request ID, not message content or credentials. Async callbacks run sequentially with pull backpressure. A timeout or nonretryable error remains visible. See [SSE lifecycle and recovery](../docs/session-streaming.md).


## Realtime text, PCM and History

`RealtimeExample` shares `ExampleClient` authentication. For SA live access use a Forward-issued, bound SAT (`QODER_AUTH_MODE=sa` and `QODER_ACCESS_TOKEN`, with no `QODER_SA_KEY`); platform SA exchange does not automatically create that binding. It connects to an existing `QODER_CONVERSATION_ID`, or creates one using `QODER_IDENTITY_ID`, a Realtime-enabled `QODER_TEMPLATE_ID` and a stable `QODER_IDEMPOTENCY_KEY`. Creation retains a conversation on the service; closing does not delete it. Text and audio consume model quota.

```sh
# Configure QODER_FORWARD_BASE_URL and the existing PAT/SA variables first.
export QODER_CONVERSATION_ID='your-authorized-conversation'
export QODER_REALTIME_TEXT='Hello'
../mvnw -B -ntp exec:java -Dexec.mainClass=com.qoder.cloudagents.examples.RealtimeExample
```

For speech, unset `QODER_REALTIME_TEXT` and set `QODER_PCM_FILE` to a raw PCM16 little-endian, 16 kHz mono file (at most 30 seconds). The example sends 20 ms chunks at real-time pace while receiving concurrently. WAV headers and incomplete samples are rejected. With neither text nor PCM, it only waits for ready, pings and gracefully closes.

The example checks nonempty final transcripts and audio completion, reports event IDs/types and PCM byte counts, and sends `playback.cancelled` because it has no speaker. It never claims that received audio has been played. It waits for the matching close acknowledgement, then reads up to 10 History pages. It prints no transcripts or credentials. `RealtimeExample.history(...)` can be called separately for read-only history.

Default verification uses mock services. The opt-in [live runner](../docs/live-testing.md) exercises text, PCM, ASR, TTS and History against a selected environment. See [Realtime semantics](../docs/realtime.md).

## Repeatable live acceptance

See [the opt-in live runner](../docs/live-testing.md) for the `read`, `resources`, `session`, `realtime-text`, `realtime-pcm`, `schedule`, `batch`, `usage`, `sa-tokens` and `realtime-work` groups. The dedicated Schedule/Batch checks wait for completion and validate output and Session history; the smaller lifecycle examples above intentionally return earlier. This runner consumes the installed JAR and is never launched by the default Maven test lifecycle.

Forward Webhook listing and receiver verification: see [Webhook guide](../docs/webhooks.md). The example does not create subscriptions or emit test deliveries.

## Forward additions in 0.2.0

[ForwardCapabilitiesExample](src/main/java/com/qoder/cloudagents/examples/ForwardCapabilitiesExample.java) demonstrates resource Search, Decision creation, Dream creation and Pairing listing. See [parameters, opt-in flags and side effects](../docs/forward-parity.md#verification-and-executable-example). These APIs require SDK 0.2.0 or later.

## Managed API

`ManagedExampleClient` configures PAT / SA Key / SAT authentication with `QODER_BASE_URL` for `/api/v1/cloud`. `ManagedLiveCheck` provides explicit `read`, `resources`, `session`, `deployment` and `work` integration groups against the resolved JAR. See [Managed usage](../docs/managed.md) and [opt-in live testing](../docs/managed-live-testing.md) for permissions, test resources and cleanup. These examples require SDK 0.2.0 or later.
