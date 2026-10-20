# Changelog

## [Unreleased]

### Validation

- Accept documented assistant-message projections in Realtime Work History while still checking Work IDs, terminal events and pagination.
- Complete Schedule/Batch execution, Usage pagination/filtering, Identity SAT permissions/revocation and Realtime Work/History acceptance in CN and Global.
- JDK 8/11/17/21/25 compatibility CI, package-content and Java 8 bytecode checks, and retained test reports and candidate checksums.
- Opt-in packaged-JAR live checks for CN/Global PAT/SA, isolated resources, Session/Thread SSE and Realtime text/PCM/History, with cleanup and redacted diagnostics.
- Correct file download examples to use `session_resource`, verify Skill ZIP contents after server-side wrapping, and validate the documented Realtime close outcomes plus persisted History.

### Added

- Realtime conversation creation with required idempotency keys and separate History cursor pagination.
- Shared PAT/SA WebSocket authentication, validated voice.ready gating, typed text/PCM/transcript/Work events, bounded queues, cancellable asynchronous operations, stage timeouts and acknowledged graceful close.
- Text/raw-PCM and History examples consuming the installed JAR, without audio device dependencies or automatic input replay.

- Session and Thread SSE subscriptions with Java 8 iteration, asynchronous opening, ordered callbacks, cancellation, response metadata and explicit Last-Event-ID resumption.
- Bounded incremental SSE parsing, shared PAT/SA authentication on reconnect, subscription-wide deadlines and an independent packaged streaming example.

- Java 8 compatible Maven foundation for Forward, with synchronous and asynchronous clients sharing authentication and HTTP behavior.
- PAT, static SAT, custom credentials and explicit `ServiceAccountCredential` with lazy exchange, shared refresh and expiry handling.
- Model listing, response metadata, unknown JSON fields, timeout/retry overrides and cancellation.
- Template create, update, retrieve, list, archive and clone with synchronous/asynchronous methods and structured model, tool, MCP, Skill, managed-tool, multi-agent and resource configuration.
- Immutable request builders preserving omitted/null/empty values, and lazy Template cursor pagination with cancellation, retained request options and repeated-cursor detection.
- A Template lifecycle example with archive cleanup and independent packaged-consumer verification.
- Identity CRUD, administrator ensure, statistics, associated Template listing, clear, enable and disable, with synchronous/asynchronous methods and response metadata.
- Identity Config listing, retrieval, upsert and effective configuration retrieval; typed sparse overrides, partial model settings and null-based inheritance restoration.
- Shared cursor pagination for Templates, Identities and Identity Configs, preserving nested resource paths and filters.
- An isolated Identity configuration example with clear-before-delete cleanup and independent JAR consumer tests.
- Identity Memory Store mount/list/detach, preserving server order and separating detach from Store deletion.
- Session create/list/retrieve/update/archive/cancel, event send/history, file attachment and Thread retrieval/archive/history, with synchronous/asynchronous response access.
- Typed Session configuration, content blocks, outcome rubrics, usage and Thread statistics; unknown events and exact JSON numbers remain available.
- A Session lifecycle example with optional shared Memory Store mounting and independent JAR consumer tests for cleanup and failure recovery.
- Memory Store CRUD/archive, Memory CRUD and Version list/retrieve/redact, with synchronous/asynchronous methods, response metadata and cursor pagination.
- Typed Store creation mounts, immutable metadata replacement, optimistic Memory updates with content hashes, and distinct missing/empty/redacted content states.
- An isolated Memory Store lifecycle example that cleans only its own Store and test data, with packaged-consumer tests covering success and failure recovery.
- Environment CRUD/archive with typed cloud/self-hosted configuration, package lists and setup scripts.
- File metadata/list/delete, multipart byte/path/stream uploads, signed download grants and owned streaming downloads with isolated storage requests.
- Page-token pagination, optional Identity scope queries, upload replay/ownership rules and download timeout/cancellation through body reads.
- An isolated Environment/File example with content comparison and packaged-consumer cleanup tests.
- Offline contract/authentication/transport tests, independent JAR consumer example and Java compatibility CI matrix.
- Skill and immutable Skill Version APIs with repeated multipart files, legacy compatibility and owned direct ZIP downloads.
- Vault and Credential APIs with typed secret creation/rotation parameters, redacted response models and metadata merge patches; credential rotation always disables automatic retries.
- Skill/Vault page-token pagination and owner-scope queries, plus an isolated packaged-consumer example that verifies downloaded ZIP members and exact SKILL.md contents and cleans up its own resources.

- Schedule and Schedule Run APIs with manual/automatic trigger configuration, merge patches, explicit bounded archive, source metadata and cursor pagination.
- Batch and Batch Task APIs with idle-window control, queue reasons, exact decimal usage, structured task errors and separate output/error URL grants.
- Opt-in Schedule execution and Batch submission examples with installed-JAR consumer checks and recovery-safe input retention.

- Baseline Channel CRUD, cursor listing, message-scope Pairing/revocation and QR authorization with synchronous/asynchronous clients and typed configuration.
- Current Channel credential fields, code-or-ID pairing selectors, scope details and QR error states; separate request-only credentials from response models.
- Disabled Channel lifecycle and explicit QR/Pairing UI helpers, with packaged-consumer checks that keep authorization secrets out of default output.

- Hourly Identity/Template Usage aggregation with precise decimals, multiple-ID filters and cursor pagination.
- Forward admin/Identity SAT issuance and revocation with isolated per-call SA Key authentication, disabled retries and safe default object output.
- Drive listing, upload/download URL grants, entry deletion and clearing, preserving server order, opaque page tokens, path encoding and legacy response compatibility.
- Usage, Token management and Drive round-trip examples with installed-JAR consumer checks, scoped cleanup and storage credential isolation.
- HTTP error request ID fallback from top-level and nested error bodies when response headers omit it.

The current SNAPSHOT can be built from source; Maven Central publication is not enabled. See the [maintenance guide](docs/maintaining.md) for current checks and delivery boundaries.
