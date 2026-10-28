# Changelog

## [Unreleased]

### Added

- Add `ManagedClient` / `AsyncManagedClient` and 108 documented Managed operations: Agents and versions, Deployments and Runs, Sessions/Threads/events/resources, Environments/self-hosted Work, Files, Skills/Versions, Vaults/Credentials/OAuth, Memory Stores, Models and Dreams.
- Add typed Managed budget and Credits, Agent runtime patches, versioned multiagent references, OSS storage, repository resources and exact Session provenance markers. Preserve null/omitted values and unknown response fields; retain POST search filters and opaque cursors.
- Add Managed independent JAR examples, opt-in CN/Global PAT/SA integration groups, official-response contract fixtures and API coverage documentation. Forward and Managed additions target one combined 0.2.0 release.

- Add typed synchronous/asynchronous Forward resource Search for Environments, Files, Skills and Vaults, retaining body filters, Identity query scope and Beta selectors across cursor pages.
- Add Forward Dream create/list/retrieve/cancel/archive, Channel Pairing list/retrieve/update, and Decision create/retrieve/list with typed input, output and precise score values.
- Add Identity auto-memory configuration, independent mount access and pagination, Schedule file resources, optional Run input snapshots, and Credits accessors for Session events.
- Mark typed Session creation with `__qca_sdk_created`, `__qca_sdk_language` and the actual package version; preserve custom metadata and reject conflicting reserved values before HTTP.


- Add Forward Webhook endpoint creation, listing, retrieval, partial update, deletion, enable/disable and synthetic test delivery.
- Add raw-body webhook signature verification with HMAC-SHA256, multiple v1 signatures and a 300-second timestamp window; include receiver examples and offline contracts.
- Public Java 8 HTTP transport and per-attempt interceptor contracts for custom networking and tracing, without exposing OkHttp types. Cancellation reaches downstream transport futures; SSE bodies remain streaming. Storage downloads use the transport without API credentials or interceptors. WebSockets and SA Key exchange retain their existing transports.
- Synchronous/asynchronous `request(ApiRequest, RequestOptions)` for additional JSON endpoints, reusing authentication, retries, cancellation and response metadata with relative path segments.
- Synchronous/asynchronous `withOptions(...)` to derive independently closeable clients while retaining resolved authentication and unspecified settings. Caller-supplied credentials and transports remain caller-owned.

### Fixed

- Serialize text Outcome rubrics with the documented `content` field. Keep `text()` and `Builder.text(...)` as deprecated aliases for content.


- Add typed image `file_id`, Memory Store creation-mount `access`, Schedule/Run `initial_events_version` and Pairing `created_at` / `updated_at` fields. Missing versions remain unknown rather than becoming zero.

### Migration

- Prefer `OutcomeRubric.content()` / `Builder.content(...)`; raw JSON callers must replace the old rubric `text` key with `content`.
- Typed Session creation now adds three metadata entries. Do not use different values/types for these exact reserved keys, and allow for them within the service metadata limit. Other metadata, including future prefixed keys, remains unchanged.


## [0.1.0] - 2026-10-21

First stable release of the Java SDK for the Forward API, using `com.qoder.sdk:qoder-cloud-agents-sdk:0.1.0`.

### Features

- Java 8 compatible synchronous and asynchronous clients, checked on Java 8, 11, 17, 21 and 25.
- Shared PAT, static SAT and custom credentials, plus `ServiceAccountCredential` for automatic SA Key exchange, SAT caching and refresh. Forward administrator/Identity SAT issuance and revocation use explicit per-call SA Keys.
- Typed Forward resource APIs for Models, Templates, Identities and configuration, Sessions and Threads, Memory Stores, Environments, Files, Skills, Vaults and Credentials, Schedules, Batches, Channels and Pairing, Usage and Drive.
- Session/Thread SSE subscriptions with cancellation and explicit resumption; Realtime conversations with text/PCM audio, typed events, acknowledged close and History.
- Shared request options, timeouts, retries, pagination, response metadata and typed errors, with resource-lifecycle examples and API guides. Managed API support is planned for a future release and is not included in 0.1.0.

### Changed

- Promote the RC's API and runtime behavior without implementation changes; update installation, example and maintenance documentation for Maven Central consumption.

### Migration

- From `0.1.0-rc.1`, change only the dependency version to `0.1.0`.
- From the earlier internal `com.qoder:qca-sdk:0.1.0-SNAPSHOT`, use `com.qoder.sdk:qoder-cloud-agents-sdk:0.1.0`. Java imports remain under `com.qoder.cloudagents`.

## [0.1.0-rc.1] - 2026-10-21

First public release candidate for installation and integration testing ahead of `0.1.0`.

### Changed

- Use `com.qoder.sdk:qoder-cloud-agents-sdk` as the Maven coordinates. Java packages remain under `com.qoder.cloudagents`.

### Fixed

- Repeated default header settings now replace earlier values case-insensitively, while per-request headers retain precedence.
- Realtime handshake and connection failures now retain the underlying exception through `RealtimeException.getCause()` for diagnostics.
- Resumable SSE subscriptions honor valid `Retry-After` and `retry-after-ms` response headers after HTTP retries are exhausted, while cancellation and total deadlines still interrupt reconnect waits.
- Closing an active event subscription records cancellation before releasing its reader, preventing a race that could incorrectly report successful completion.

### Validation

- Manual Maven Central release workflow with source/CI checks, verified artifact digests, signing, retained deployment recovery, public package verification and matching GitHub Release notes. Publishing uses repository Actions secrets and proceeds after preflight without a separate environment approval.
- Disposable-key signing checks verify the release bundle without uploading it to Maven Central.
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

This release candidate is available from Maven Central. See the [maintenance guide](docs/maintaining.md) for checks and delivery boundaries.
