# Skills, versions, Vaults and Credentials

These 19 Forward operations share the client's PAT/SA authentication, request options, errors and native asynchronous transport. `client.async()` exposes the same resources with `CompletableFuture` results; `withResponse()` exposes status, headers and request IDs. Vault Credentials store secrets for agent resources; they are separate from the SDK's `auth.Credential` used to authenticate API requests.

## Resource methods

| Resource | Methods | HTTP details |
| --- | --- | --- |
| `client.skills()` | `list`, `create`, `retrieve`, `update`, `delete` | Create is multipart; the legacy update uses PUT |
| `client.skills().versions()` | `list`, `create`, `retrieve`, `download`, `delete` | Version creation is multipart; download returns ZIP bytes |
| `client.vaults()` | `list`, `create`, `retrieve`, `delete` | JSON requests/responses |
| `client.vaults().credentials()` | `list`, `create`, `retrieve`, `update`, `delete` | Update uses POST and never retries automatically |

Every operation accepts final `RequestOptions`. Delete methods preserve the documented deletion JSON (`id`, `type`, `deleted`) and also accept an empty 204/205 response, returning null. No Search or additional archive/OAuth-management endpoint is included in this baseline.

## Upload a Skill

```java
Skill skill = client.skills().create(
    SkillCreateParams.builder()
        .addFile(Upload.path(Paths.get("review/SKILL.md"),
            "review/SKILL.md", "text/markdown"))
        .addFile(Upload.path(Paths.get("review/scripts/check.sh"),
            "review/scripts/check.sh", "text/plain"))
        .build(),
    RequestOptions.builder().idempotencyKey("create-review-001").build());
```

Both files are sent as repeated `files` parts, retaining their relative filenames. A single ZIP can instead be passed with `addFile(Upload.path(zipPath, "application/zip"))`. `Upload.bytes` snapshots content; `Upload.path` streams from disk. `Upload.stream` transfers ownership when the request is accepted: the SDK closes every claimed stream on completion, failure or cancellation, including cancellation while waiting for authentication. A custom stream must allow `close()` to unblock its own reads.

Only uploads consisting entirely of immutable byte sources are eligible for the shared retry policy. Any path/stream disables automatic replay, even with an idempotency key. The SDK does not extract packages or normalize relative filenames. The service validates a single top-level directory, its matching `SKILL.md` frontmatter name/description, path safety and the 50 MB compressed/uncompressed limits.

Creation accepts `metadata` (JSON form field) and `iconId`. The deprecated `file(Upload)`, `name`, `description` and `type` setters remain for baseline compatibility. When both `files` and legacy `file` are provided, `files` wins and the unused legacy stream remains caller-owned. A Version creation accepts only `files`. Idempotency keys and owner scope belong in `RequestOptions`, never multipart fields.

## Immutable versions and downloads

```java
SkillVersion created = client.skills().versions().create(skill.id(),
    SkillVersionCreateParams.builder()
        .addFile(Upload.path(Paths.get("review-v2.zip"), "application/zip"))
        .build());

try (FileDownload download = client.skills().versions()
        .download(skill.id(), created.version(),
            RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build())) {
    // Choose the output path yourself; do not trust Content-Disposition as a local path.
    java.nio.file.Files.copy(download.body(), Paths.get("downloaded-review.zip"));
    System.out.println(download.requestId());
}
```

A version is an opaque string: retain the service's timestamp or legacy counter without arithmetic. `latest` is not supported by the API. Retrieve the Skill and use its `latestVersion()`; this may be null when no versions remain. `SkillVersion` exposes size, SHA-256, canonical name, directory, description and status. Unknown response fields/values remain available. The version size/SHA-256 describe the stored canonical ZIP. The content endpoint wraps it in the skill-name directory; verify the downloaded member contents rather than comparing its transport bytes to the canonical hash.

`download` returns an owned `FileDownload` as soon as headers arrive; the ZIP is not parsed as JSON or buffered entirely. It uses the authenticated API endpoint directly, unlike File's two-hop storage download. `requestId()` and `withResponse().requestId()` refer to this API call; `storageRequestId()` is the same final response ID for a direct download. Close the download or its body after use. EOF also releases it.

Header/read-idle timeouts and `totalTimeout` remain effective during streaming. Cancelling the pending future aborts a request before handoff; after handoff close the download. Closing the owning client terminates active streams and blocked readers. GET may retry before a stream is delivered; body failures after delivery never replay. API redirects are not followed with credentials.

`skills().update(...)` is retained and marked deprecated, matching the official contract. Its metadata replaces the whole map and cannot be null; it is not a merge patch. New content should be uploaded with `versions().create(...)`. The deprecated `SkillRetrieveParams.includeContent(true)` remains available; read the `Deprecation` header through `withResponse()`.

## Vault and secret creation

```java
Vault vault = client.vaults().create(
    VaultCreateParams.builder().displayName("application secrets").build(),
    RequestOptions.builder().idempotencyKey("app-vault-001").build());

VaultCredential credential = client.vaults().credentials().create(vault.id(),
    VaultCredentialCreateParams.builder()
        .auth(VaultCredentialCreateAuth.builder()
            .type("environment_variable")
            .secretName("UPSTREAM_API_KEY")
            .secretValue(System.getenv("UPSTREAM_API_KEY"))
            .build())
        .build());
```

`VaultCredentialCreateAuth` provides typed setters for the public creation shapes:

- `static_bearer`: `mcpServerUrl` and `token`.
- `environment_variable`: `secretName` and `secretValue`.
- `mcp_oauth`: `mcpServerUrl`, `accessToken`, optional `expiresAt` and `VaultCredentialOAuthRefresh` (`refreshToken`, `clientId`, `tokenEndpoint`, `scope`, `tokenEndpointAuth`). The endpoint-auth value is JSON because its shape depends on the OAuth provider.

These builders do not contact an OAuth provider or manage its refresh lifecycle. Forward owns stored-secret behavior. Responses use a separate `VaultCredentialAuth` containing public `type`, optional `mcpServerUrl` or `secretName`; secrets are write-only. The response preserves `archivedAt` and unknown fields. Default `toString()` on request objects/builders omits contents; explicitly calling `rawJson()`, `toJsonValue()` or a secret getter still exposes the values to the caller and must not be logged.

The official shared Credential schema and create page differ in their OAuth examples. This implementation follows the operation-specific create/update contracts (access token plus nested refresh), consistent with existing public SDK behavior; it does not reinterpret client credentials as a public OAuth-registration call. Additional fields can be passed explicitly for a confirmed service contract. A legacy `displayName` setter is available for existing SDK compatibility; Forward does not persist it.

## Rotate secrets and merge metadata

```java
Map<String, JsonValue> patch = new LinkedHashMap<>();
patch.put("obsolete", JsonValue.of(null));
patch.put("rotated_by", JsonValue.of("my-service"));

VaultCredential updated = client.vaults().credentials().update(vault.id(), credential.id(),
    VaultCredentialUpdateParams.builder()
        .auth(VaultCredentialUpdateAuth.builder()
            .type("environment_variable")
            .secretValue(System.getenv("NEW_UPSTREAM_API_KEY"))
            .build())
        .metadata(patch)
        .build());
```

Only supplied fields are sent. A metadata key with null deletes that key; `.metadata(null)` clears all metadata, while omitting the setter preserves it. For OAuth updates, `expiresAt(null)` and refresh `scope(null)` clear those fields. Update types deliberately omit immutable MCP URL, secret name, refresh client ID and token endpoint setters. The server checks the current credential type and whether an existing OAuth refresh configuration can be modified. `created_by` is reserved in metadata.

**Credential update always disables automatic retries**, including 429, 503, connection failures and a server `x-should-retry` instruction. Per-call retry counts/idempotency keys cannot override this. If the response is lost, a redacted GET cannot prove which secret is stored: record the request ID and resolve the ambiguous outcome before issuing a new rotation. SDK methods never synthesize a successful result or replay a 401 write.

## Pagination and owner scope

Skill, Vault and Credential lists use `page`/`next_page`, also supporting explicitly selected `afterId` or `beforeId`; these selectors are mutually exclusive. Subsequent pages preserve owner scope, filters and request options. Skill `source` cannot be combined with `beforeId`. Skill Version lists support only `page`; the opaque continuation is never derived from a version ID. Server order is preserved. Check `hasNextPage()` before manual `getNextPage()`; exhausted/repeated cursors raise `PaginationException`.

PAT callers can select an Identity using `RequestOptions.builder().query("identity_id", id).build()` or a list/retrieve query builder. The option remains in query for every request and inherited-resource operation. SAT ownership comes from its credential; omit `identity_id` entirely, including an empty value. The SDK does not silently remove an explicit parameter or change authentication mode.

For an isolated workflow with cleanup and an exact downloaded ZIP member/content check, see [SkillVaultExample](../examples/src/main/java/com/qoder/cloudagents/examples/SkillVaultExample.java) and the [example instructions](../examples/README.md). See [live testing](live-testing.md) for resource lifecycle checks using the packaged SDK.
