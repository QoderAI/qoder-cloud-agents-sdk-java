# Schedules, Runs and Batches

These Forward resources share `ForwardClient` / `AsyncForwardClient`, PAT and SA authentication, request options, typed errors and request IDs. The SDK provides 18 HTTP operations in these groups. Default tests verify local contracts and packaged consumers; opt-in [live tests](live-testing.md) check execution in a selected environment.

## Schedules and Runs

```java
Schedule schedule = client.schedules().create(
    ScheduleCreateParams.builder()
        .identityId(identityId)
        .templateId(templateId)
        .environmentId(environmentId)
        .name("Manual report")
        .initialEvents(Collections.singletonList(
            ScheduleInitialEvent.builder()
                .type("user.message")
                .content("Summarize today's report.")
                .build()))
        .triggerPolicy(ScheduleTriggerPolicyParams.builder().type("manual").build())
        .execution(ScheduleExecution.builder().maxAttempts(1L).build())
        .build(),
    RequestOptions.builder().idempotencyKey(createKey).build());
```

Schedule initial events use string `content` as documented by this endpoint; ordinary Session events have their own content-block model. `ScheduleTriggerPolicyParams` exposes request fields (`type`, `expression`, `timezone`), while `ScheduleTriggerPolicy` includes computed response fields such as `upcomingRunsAt` and `lastRunAt`. Cron/timezone, once-time and interval rules are validated by the server.

Since 0.2.0, `initialEventsVersion()` is available on both `Schedule` and `ScheduleRun`. It returns a nullable `Long`: an absent version remains unknown, distinct from an explicit zero.

- `schedules().list/create/retrieve/update/archive/archiveMany/pause/unpause/run` cover the nine Schedule operations.
- `scheduleRuns().list/retrieve` cover Run queries. Listing requires `ScheduleRunListParams.identityId`; retrieval accepts optional `ScheduleRunRetrieveParams.identityId`.
- An omitted trigger policy or explicit `null` creates a manual Schedule. Updating `triggerPolicy(null)` switches to manual. Updating `sinks(null)` or an empty list clears delivery targets; omitting a field retains it. Execution fields merge. Metadata values of JSON `null` remove keys.
- `ScheduleSink` supports either a Channel plus `target`, or `channelPairingId`; the API currently allows at most one sink. The example creates no delivery target.
- `maxAttempts` accepts 1 or 2. This controls server execution attempts, separately from SDK HTTP retries. It does not promise that every failure can be retried.
- `source` and `sourceSessionId` describe where the Schedule was created. Run `sessionId` is the execution Session; `triggerContext.type` is this Run's trigger. These are different concepts.
- Run `status` and `pushStatus` are independent: computation can complete while delivery fails. Future status values and unmodeled fields remain available.

Explicit execution:

```java
ScheduleRun run = client.schedules().run(schedule.id(),
    RequestOptions.builder().idempotencyKey(runKey).build());
ScheduleRun current = client.scheduleRuns().retrieve(run.id());
```

A Run executes asynchronously and may consume credits. `archive` prevents future triggers; it does not cancel an existing pending/running Run. There is no Schedule Run cancel/delete method in this API group. Applications can inspect its Session using the existing Session APIs where authorized.

Batch archive is explicitly bounded:

```java
ScheduleArchiveManyResponse result = client.schedules().archiveMany(
    ScheduleArchiveManyParams.builder().scheduleIds(scheduleIds).build(),
    RequestOptions.builder().idempotencyKey(archiveKey).build());
```

The request contains **only `schedule_ids`**. The builder snapshots and deduplicates IDs in insertion order and requires 1–50 unique, nonempty IDs. The current official contract rejects `scope` and other extra fields. This differs from the older TypeScript baseline's `scope: by_schedule_ids` implementation; Java follows the current contract. The endpoint permits PAT or administrator SAT; an Identity-bound SAT cannot perform this administrator operation. SDK does not infer permissions from token text.

## Batch creation and inspection

First upload UTF-8 JSONL using `files().upload` with `purpose("session_resource")`. Each line contains `custom_id`, `template_id`, `identity_id` and `body.input`; optional `body.resources` contains file resources. Use distinct custom IDs and existing accessible resources. Default `user_upload` purpose is unsuitable for Batch input.

```java
Batch batch = client.batches().create(
    BatchCreateParams.builder()
        .inputFileId(inputFileId)
        .completionWindow("24h")
        .ignoreIdleWindow(true)
        .build(),
    RequestOptions.builder().idempotencyKey(batchKey).build());
```

- `ignoreIdleWindow` is optional, defaults to false on the server, and accepts a boolean, not null. True removes the idle-window restriction; it does not override owner/global capacity, FIFO ordering or other scheduling conditions.
- Read `status` and `queueReason`. Creation can return `validating` with real initial counts, including invalid input rows. Do not treat an HTTP 200 as completion. Completion windows are `24h`, `48h`, `72h` from creation.
- `requestCounts` preserves each count. Older top-level counts remain separate accessors; SDK does not invent totals or infer missing counts. `usage.totalCredits` uses `BigDecimal` to preserve precision and distinguish zero from absent/null usage.
- `batches().retrieve/cancel/getOutput/getError` complete the ordinary operations. Cancellation is asynchronous; inspect until terminal (`completed`, `failed`, `cancelled`, `expired`) rather than deleting input immediately after cancel.
- `batches().tasks().list` returns task summaries, usage, artifacts and structured errors. Task identity is `customId`, not a fabricated `id`.
- Keep the input and row-level resources available while work is pending/running. Example submission logs its operation key and file ID for reconciliation and retains the file even if Batch creation returns an ambiguous error. Delete only files you own after verifying they are no longer needed.

## Pagination, async calls and result files

Schedule, Run and Batch lists support `afterId` / `beforeId`, with `limit` 1–100. Batch tasks support only `afterId`. They do not use page tokens. Continuation preserves filters and server order; no background prefetch occurs. `getNextPage()` requires `hasNextPage()`; exhaustion and repeated cursors raise `PaginationException`.

```java
CompletableFuture<BatchTaskListResponse> pending = client.async().batches().tasks().list(
    batchId, BatchTaskListParams.builder().limit(20).build());
// pending.cancel(true) propagates to the active HTTP call.

ApiResponse<Batch> response = client.batches().withResponse().retrieve(batchId);
String requestId = response.requestId();
```

`getOutput` and `getError` return a `BatchFile` signed URL grant (`url`, `expiresAt`). They do not automatically download data. Use a separate HTTP client without PAT/SAT/SA Key or QCA default headers to consume that URL before expiry. Do not log the URL or its raw JSON. The model's default `toString` omits fields.

Batch `outputFileId` and `errorFileId` are **not** generic Files API resources. Fetch their grants via the Batch endpoints. Task artifact `fileId` values, by contrast, refer to the Files API. Calling output before readiness can return `400 batch_not_ready`; absent error files may return 404 and expired files 410. These remain typed `ApiStatusException` responses with request IDs; SDK does not conceal them with polling.

Runnable examples: [ScheduleLifecycleExample](../examples/src/main/java/com/qoder/cloudagents/examples/ScheduleLifecycleExample.java) and [BatchExample](../examples/src/main/java/com/qoder/cloudagents/examples/BatchExample.java). The former defaults to no execution; the latter requires separate write and execution opt-ins for submission, and can inspect an existing Batch read-only.

## Contract sources

Endpoint inventory and resource naming follow the pinned SDK baselines in [coverage](forward-api-coverage.md).

These resources use shared PAT/SA authentication and do not introduce a local PAT-only restriction. Service-side permissions are checked separately by the opt-in live tests.
