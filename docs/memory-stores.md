# Memory Stores, Memories and Versions

The Forward client exposes 14 operations, each with synchronous, asynchronous and `withResponse()` forms:

| Resource | Methods |
| --- | --- |
| `client.memoryStores()` | `list`, `create`, `retrieve`, `update`, `delete`, `archive` |
| `client.memoryStores().memories()` | `list`, `create`, `retrieve`, `update`, `delete` |
| `client.memoryStores().memoryVersions()` | `list`, `retrieve`, `redact` |

Nested operations take the Store ID first. Memory operations also take the Memory ID where applicable; version operations take the Version ID. Versions are scoped directly to the Store, not to a nested Memory URL. They are created automatically when Memories change.

These endpoints use the client's existing PAT or administrator SAT. Identity-bound SAT permissions remain enforced by the server. No resource reads credentials separately.

## Create a Store

The server requires an **Idempotency-Key** for Store creation. Supply it through `RequestOptions`, outside the JSON body. Reuse the same key and body when retrying the same logical creation; give different operations different keys. The SDK does not invent a new key or enforce this header locally, so a missing key is returned as the service's HTTP 400. Client default headers also work, but a per-operation key avoids accidental reuse.

```java
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;

String createKey = java.util.UUID.randomUUID().toString();
MemoryStore store = client.memoryStores().create(
    MemoryStoreCreateParams.builder()
        .name("Project knowledge")
        .metadata(java.util.Collections.singletonMap("team", "sdk"))
        .build(),
    RequestOptions.builder().idempotencyKey(createKey).build());

System.out.println(store.id());
System.out.println(store.requestId());
```

Optional creation-time mounting uses:

```java
MemoryStoreCreateMount mount = MemoryStoreCreateMount.builder()
    .identityId("idn_example")
    .templateId("tmpl_example")
    .build();
// Supply .mount(mount) on MemoryStoreCreateParams.builder().
```

Both IDs are required when supplying `mount`. This is a read-only explicit mount, matching the separate Identity mount API. Store `bindingInfo().identityTemplateCount()` reports active mounts; unknown binding fields remain available through `field(...)`.

Store metadata is a string map with at most 15 user keys. `created_by` is reserved and added by the service. Memory metadata allows 16 keys and does not reserve `created_by`. Key/value size rules and other business constraints are enforced by the service. The SDK does not insert reserved metadata.

## Write and update a Memory

```java
Memory memory = client.memoryStores().memories().create(
    store.id(),
    MemoryCreateParams.builder()
        .path("notes/decision.md")
        .content("# Decision\nUse the public SDK.")
        .build());

Memory current = client.memoryStores().memories().retrieve(store.id(), memory.id());
Memory updated = client.memoryStores().memories().update(
    store.id(), memory.id(),
    MemoryUpdateParams.builder()
        .content("# Decision\nUse the Java SDK.")
        .contentSha256(current.contentSha256())
        .build());
```

`content` is required UTF-8 text, **not base64**, with a service limit of 100 KiB. An empty string is distinct from missing content; builders reject missing/null content. `path` is a case-sensitive relative path and cannot be changed by update. The service validates path segments and disallowed control characters; the SDK does not normalize paths or transform content.

`contentSha256(...)` is the expected current hash for optimistic concurrency. A stale hash produces HTTP 409. The SDK retains that conflict and does not retry, fetch a newer hash, or overwrite automatically. Omitting the hash omits the concurrency check. Applications that require conflict protection should ensure the retrieved hash is present before updating; the runnable example does so.

For both Store and Memory updates, `metadata` **replaces the whole map**. Omit it to keep existing metadata; use an empty map to clear user metadata. Store metadata still includes the service's reserved key. Like other builders, explicit null is serialized as null, not omission; the service decides which fields accept it. Store update rejects null values. Request maps are immutable snapshots.

## List and inspect versions

```java
MemoryListResponse memories = client.memoryStores().memories().list(
    store.id(), MemoryListParams.builder().pathPrefix("notes/").limit(20).build());

MemoryVersionListResponse versions = client.memoryStores().memoryVersions().list(
    store.id(), MemoryVersionListParams.builder().memoryId(memory.id()).build());
for (MemoryVersion summary : versions) {
    MemoryVersion detail = client.memoryStores().memoryVersions()
        .retrieve(store.id(), summary.id());
    // Read detail.content() as needed; avoid logging memory contents.
}
```

All three list resources support `limit(1..100)` and either `afterId` or `beforeId`. Lists preserve server order. `MemoryStoreListParams.systemManaged(false)` selects user-created Stores; `true` selects system Stores; omitting it leaves both visible. `pathPrefix("a/b")` is a raw string prefix and also matches `a/bc.md`; the SDK does not add a slash. Version listing retains its `memoryId` filter across pages.

Use `.data()` for the current page, iteration for lazy synchronous pagination, or `.getNextPageAsync()` for asynchronous pagination. Check `.hasNextPage()` before manual fetches. Nested paths, filters and request options are retained.

Memory and Version lists omit `content`; retrieval returns it. Redacted versions have explicit JSON null. Distinguish these states with `field("content").isMissing()` and `.isNull()`; `content()` returns null for either and returns `""` for empty content. Counts use `Long`. Unknown operations and response fields remain accessible.

## Delete, archive and redact have different effects

| Operation | Effect |
| --- | --- |
| `memories().delete(storeId, memoryId)` | Deletes the current Memory and records a deleted version; prior version content remains. |
| `memoryVersions().redact(storeId, versionId)` | Permanently removes only that version's content; retains its metadata, current Memory and other versions. Repeating it returns 409. |
| `memoryStores().archive(storeId)` | Keeps the Store readable, but disallows ordinary Memory writes and deletion. Existing mounts must be detached first. Redaction remains available. |
| `memoryStores().delete(storeId)` | Deletes the unbound active Store, its Memories and history. Bound or archived Stores return 409. |
| `identities().memoryStores().detach(...)` | Removes only a mount; does not delete the Store or Memories. |

Archive and redact use bodyless POSTs. Delete methods return typed `DeletedMemoryStore` / `DeletedMemory` JSON responses. The SDK does not perform hidden cleanup or automatically unmount resources after conflicts.

## Packaged example and verification

[MemoryStoreLifecycleExample](../examples/src/main/java/com/qoder/cloudagents/examples/MemoryStoreLifecycleExample.java) creates an isolated, unmounted Store, writes and updates a Memory with its retrieved hash, lists/retrieves versions, and deletes only the newly created Store and test data. It does not archive or redact as part of cleanup. It preserves business and cleanup errors and prints the unique name, creation key and created ID for recovery.

See [example commands](../examples/README.md) and [opt-in live testing](live-testing.md). Default tests use mock services and the installed SDK JAR.
