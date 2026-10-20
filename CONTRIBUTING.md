# Contributing

This is an independent Java repository. Run Git and Maven commands from its root. Use a feature branch and Conventional Commits; remote creation, pushes and releases are separate actions.

## Organization

- `src/main/java/com/qoder/cloudagents`: public synchronous/asynchronous clients.
- `auth`: shared PAT / Service Account authentication. Resources must not independently read tokens or implement refreshes.
- `core`: request options, response metadata and JSON values. `core/internal` owns HTTP, retries and cancellation.
- `errors`: shared error hierarchy.
- `forward`: resource services and models; `forward/realtime` contains WebSocket and History support.
- `src/test/java`: offline request-contract, authentication, error, timeout and cancellation tests.
- `examples`: independent consumer of the packaged JAR; not another SDK artifact or reactor module.
- `docs`: API guides, coverage and maintenance instructions. `streaming` contains reusable SSE parsing, consumption and explicit resumption.

## Build and verify

Use JDK 17 for maintenance and also run on an actual JDK 8. Maven Wrapper pins Maven 3.9.12 and verifies its distribution checksum. Dependencies and plugins use fixed versions. Do not add private Maven repositories or credentials to the public POM.

```sh
make quality
./mvnw -B -ntp clean install
./mvnw -B -ntp -f examples/pom.xml clean verify
python3 .github/scripts/check_package.py
```

Run `make quality` on JDK 17+; the formatter is a maintenance tool and does not change the Java 8 SDK baseline. Run Maven with `JAVA_HOME` set to each required JDK. CI is configured for 8, 11, 17, 21 and 25; local results must state which runtimes were actually used. `install` only installs to the local Maven repository. The package check verifies Java 8 class files, version and license metadata, sources and public Javadoc; it writes `target/package-check.json` and `target/SHA256SUMS`. The opt-in Central profile defaults to preparing a bundle without uploading. See [maintenance](docs/maintaining.md) and [release setup](docs/releasing.md).

CI runs SDK tests/Javadoc, independent packaged-consumer tests and package checks on each JDK. It retains test reports even on failure and uploads candidate JARs/POM/checksums only after successful verification. Artifacts expire after 14 days and are build candidates, not published Maven versions. Downloaded candidates retain the `target/` layout; run `shasum -a 256 -c target/SHA256SUMS` from the extracted artifact root. See [maintenance](docs/maintaining.md) for the CI matrix and required checks.

Use Java 8 syntax and APIs. Do not introduce Java 9+ APIs, records, `Flow`, or `java.net.http`. The JDK 17 build uses `--release 8`; JDK 8 runs the actual baseline runtime and dependencies. Keep OkHttp and Jackson types out of the supported public API; `core.internal` is unsupported for application use.

Default tests use synthetic credentials and loopback servers. Request expectations are reviewed against pinned SDK commits in the coverage document, not generated from implementation under test. Opt-in packaged-JAR checks are described in [live-testing.md](docs/live-testing.md). A local mock response is not production verification.

When changing transport dependencies, recheck hidden retries, redirects, cancellation and Java 8 compatibility. OkHttp can independently follow 503/`Retry-After: 0` and coalesced HTTP 421 responses; the internal follow-up guard preserves SDK-owned retry policy. Verify observable request counts and statuses.

## Adding resources

1. Check request/response fields against the pinned public SDK baseline and current official contract.
2. Add typed immutable parameters and responses under `forward`; preserve unknown fields. Distinguish omitted fields from explicit null in updates.
3. Add the async service using the shared engine, then its synchronous wrapper. Use the internal future adapter to preserve cancellation.
4. Add independent tests; update coverage, examples and `CHANGELOG.md`.
5. Verify the packaged consumer. Live tests need a selected environment and isolated resources.

Template, Identity/Config, Identity Memory Store mounts, Session HTTP operations, Memory Store/Memory/Version, Environment/File, Skill/Version, Vault/Credential, Schedule/Run, Batch/Task and baseline Channel/Pairing/QR are implemented. The additional Pairing list/retrieve/update operations are outside the current shared SDK baseline; keep the scope recorded in the coverage document. Usage, Forward SAT management and Drive are now implemented. Session/Thread SSE and Realtime are implemented; use the opt-in live runner to verify behavior in a selected environment.

Nested configuration objects implement `JsonSerializable`; do not rely on Jackson bean conventions to serialize them. Request builders snapshot supplied collections and distinguish omitted fields from explicit null. Identity overrides also preserve null entries inside sparse maps. Reuse `CursorRequests` / `CursorPage` for cursor endpoints; do not apply this scheme to endpoints with different pagination contracts. Mount listing is a single response even though it contains `has_more`. Session and Thread histories have different allowed selectors. Keep header-only options (such as idempotency and beta headers) in `RequestOptions`. Fixtures include deliberately unknown values to verify forward compatibility; they are not a claim that the service accepts those values.

Memory Store creation requires an Idempotency-Key supplied in request options. Memory updates preserve the supplied content hash and whole-map metadata replacement; a 409 must not trigger an automatic overwrite. Memory content can be an empty string, while list responses omit it and redacted versions return null. Keep version redaction, Memory deletion, Store archive/deletion and mount detachment as distinct operations.

Environment/File use page/next_page pagination; retain explicit forward/backward cursor selectors when supplied. Multipart uploads must preserve source ownership and replay safety. Storage downloads must not copy API credentials, headers or query options; total deadlines and cancellation must remain active through body consumption. Never retry a download after exposing its stream.

Skill multipart uses repeated files parts and relative filenames. Skill ZIP downloads are direct authenticated streams, not File download grants. Credential updates must enforce zero retries regardless of request overrides; test both status and connection failures. Preserve write-only secret handling, metadata merge-patch nulls, and the distinction between create/update/response auth models.

Schedule batch archive sends only schedule_ids (1–50 unique nonempty IDs); do not copy the older TS scope field. Keep request trigger policy fields separate from computed response fields, preserve merge-patch omission/null distinctions and Batch decimal credits. Batch task cursors use custom IDs; output/error grants are not generic File IDs. Examples must preserve pending inputs and never print signed URLs.

Channel QR authorization and message-scope Pairing are different flows. Keep request-only IM credentials separate from redacted response configuration, and never print QR payloads/session keys or pairing codes. Channel tests must not send real IM messages; live QR/pairing acceptance needs a dedicated authorized connection. Preserve fixed/pairing required-field rules and the current code-or-Pairing-ID selector.

Usage accepts only hourly Asia/Shanghai windows with BigDecimal usage values. Preserve window/filters/direction across pages and reject legacy query parameters, including request overrides. Forward SAT management takes a separate per-call Key, bypasses the normal Credential and forces zero retries. Missing business credentials fail before sending ordinary requests, while Token management remains available.

Drive uses entries/next_page_token, server order and explicit identity_id in query. Preserve literal slashes only in Drive list path encoding after overrides; encode opaque tokens normally. Keep optional/legacy response fields readable. Signed URL transfer examples use a separate connection and copy only grant headers; they must not follow redirects or clear existing Drives in cleanup.

SSE reuses ClientEngine authentication and ownership. Do not replay user messages on reconnect or wrap protocol/authorization errors in infinite retries. Preserve repeated Session selectors and the separate Thread path. Advance the checkpoint only when yielding a complete decoded frame; the SSE ID may differ from JSON ID and deltas may share IDs. Streaming response-header timeout is distinct from subscription-wide totalTimeout. Iteration/callbacks apply pull backpressure without an event queue; keep frame-size bounds and test cancellation during credential lookup, header waits, reads and reconnect waits. Close the stream on callback failure and do not close caller-owned executors or shared credentials.
