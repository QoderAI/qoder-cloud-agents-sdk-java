# Channels, Pairing and QR authorization

The baseline Channel group has nine HTTP operations, with synchronous, native asynchronous and `withResponse()` forms. They reuse the shared PAT / SA credentials, error types, request options and cancellation behavior. This implementation has offline verification only.

| Resource | Methods |
| --- | --- |
| `client.channels()` | `list`, `create`, `retrieve`, `update`, `delete` |
| `client.channelPairings()` | `create`, `delete` |
| `client.channels().qrSessions()` | `create`, `retrieve` |

`client.async()` exposes the same resources using `CompletableFuture`. QR retrieval uses `/qr_sessions/{session_key}`; it is not nested under a Channel URL. QR creation has no required body, so it accepts just the Channel ID and optional `RequestOptions`, without an empty parameters model. Idempotency keys are headers supplied through request options.

## Keep the two binding steps separate

QR authorization connects Forward to the IM platform. Channel Pairing chooses the Identity and Template for a message scope. Confirming a QR does not create a Pairing or activate an Identity.

- `identity_resolution.mode=fixed` (default) binds one Identity and Template at Channel creation. The builder requires both IDs.
- `mode=pairing` omits both IDs, including explicit null values. After IM authorization, a real incoming message can produce a pairing code; the caller can authorize that message scope using the Pairing API.
- Identity resolution mode is immutable after creation. Channel update exposes no typed setter for it. Switching mode requires creating another Channel according to the service contract.
- `enabled` is a manual switch; `bindingStatus` is server-managed authorization state. Enabling a Channel does not make an unbound or expired connection usable. Inbound processing requires both enabled and bound.
- Pairing scope fields are `scopeType` (`direct`/`room`), `scopeExternalId`, and `scopeDisplayName`. Display names are not authorization identifiers. A room shares a Pairing, while Sessions/replies can remain conversation/thread-specific. Messages sent before pairing are not automatically replayed.

## Create and update

```java
Channel channel = client.channels().create(
    ChannelCreateParams.builder()
        .channelType("feishu")
        .identityResolution(ChannelIdentityResolution.builder().mode("pairing").build())
        .name("Example connection")
        .enabled(false)
        .build(),
    RequestOptions.builder().idempotencyKey(createKey).build());
```

This creates a disabled Channel without IM credentials. The lifecycle example keeps it disabled and deletes only the Channel it created. For an actual QR flow, choose an enabled test Channel; QR creation on a disabled Channel can return 409.

```java
Channel updated = client.channels().update(channelId,
    ChannelUpdateParams.builder()
        .enabled(false)
        .channelConfig(ChannelConfigParams.builder()
            .responseOptions(ChannelResponseOptions.builder()
                .includeToolCalls(false)
                .includeThinking(false)
                .build())
            .build())
        .build(),
    RequestOptions.builder().idempotencyKey(updateKey).build());
```

Updates preserve omitted fields. Explicit `false` is serialized, not omitted. Explicit null is also preserved; it is not a general reset instruction, and the server decides which fields permit it. `identityId` / `templateId` updates are for fixed Channels only. `channelType`, resolution mode and server-maintained `bindingStatus` are not typed update fields.

Direct IM credentials are a separate configuration from SDK PAT/SAT/SA authentication:

| Platform | `ChannelCredentials` fields |
| --- | --- |
| Feishu | `appId`, `appSecret` |
| DingTalk | `clientId`, `clientSecret` |
| WeCom | `botId`, `secret` |
| Teams (Global) | `appId`, `tenantId`, `clientSecret` |
| WeChat | QR authorization |

Set them through `ChannelConfigParams.credentials(...)` on creation or credential rotation. Channel response `ChannelConfig` exposes response options, without typed credential getters. A redacted response acknowledges the request; it cannot prove a stored secret's value. Builders/models omit data in default `toString()`, but `rawJson()`/`toJsonValue()` intentionally expose supplied data and must not be logged for credential-bearing requests. Unknown response fields are preserved rather than silently discarded.

HTTP writes follow the common replay policy. Use a stable, unique idempotency key for the same logical operation; use different keys for different operations. Do not import the Vault Credential API's special zero-retry behavior into Channel methods, whose official contract supports idempotency headers.

## QR and pairing in an application

```java
ChannelQRSession qr = client.channels().qrSessions().create(channelId,
    RequestOptions.builder().idempotencyKey(qrCreateKey).build());
// Deliver qr.qrCodeContent() or qr.qrCodeImageBase64() to your authorized UI.
// Keep qr.sessionKey() private; do not write it or the QR data to logs.

ChannelQRSession current = client.channels().qrSessions().retrieve(qr.sessionKey());
if ("confirmed".equals(current.status())) {
    Channel refreshed = client.channels().retrieve(current.channelId());
    // Check refreshed.enabled() and refreshed.bindingStatus().
}
```

The SDK performs one request per call, without hidden polling or an external-browser login. An application can poll until `confirmed`, `expired`, `denied` or `error`; it should bound total polling time and expose cancellation. `waiting` and `scanned` are intermediate states. Unknown future states remain strings, not parse errors. `pollIntervalSeconds` is an optional compatibility field, not a guaranteed response field; current guidance uses 3 seconds if absent. Only apply a positive, reasonable returned interval. A terminal failure should be shown to the user rather than silently restarting authorization.

Supported QR platforms in the current guide are WeChat, WeCom, Feishu and DingTalk. Teams uses direct credentials. SDK does not guess a platform from the Channel ID or perform an extra lookup to enforce this; the service validates support. `errCode` / `errMsg` can be absent or null; confirmed responses need not contain QR data. A 404 remains a typed API error, not a fabricated `expired` response.

```java
ChannelPairing pairing = client.channelPairings().create(
    ChannelPairingCreateParams.builder()
        .code(pairingCode)
        .identityId(identityId)
        .templateId(templateId)
        .build(),
    RequestOptions.builder().idempotencyKey(pairingKey).build());

// Alternative: .channelPairingId(pendingPairingId) instead of .code(...).
// Exactly one selector is required; never send both.

// Explicitly revoke a binding selected by the caller:
DeletedChannelPairing removed = client.channelPairings().delete(pairing.id());
```

Use the returned Pairing ID for revocation and Schedule sinks. Current API supports completion by pending Pairing ID as well as code; code-only older SDK types are not copied. An ID-based completion can be retried against the same binding without depending on code expiry. Used/expired codes cannot be treated as fresh pairing requests. Permission, ownership and concurrent-change errors remain server decisions.

The development SDK also exposes nullable `createdAt()` and `updatedAt()` strings on `ChannelPairing`, preserving timestamps returned by the service. These getters are not included in the published `0.1.0` JAR.

`delete` removes the Channel connection; it is distinct from `channelPairings().delete`, which revokes one binding. Already absent/unpaired bindings can return `deleted=true`. SDK keeps the declared deletion response and accepts an empty 204 without inventing a JSON body; use `withResponse()` for metadata in that case.

## Lists and examples

Channel listing accepts `channelType`, `enabled`, `bindingStatus`, `identityId`, `templateId`, `limit` (1–100), and mutually exclusive `afterId` / `beforeId`. It uses cursor pagination, preserves server order and filters, and performs no prefetch. `getNextPage()` requires `hasNextPage()`; exhausted/repeated cursors fail explicitly. Page-token parameters are not supported.

- [ChannelLifecycleExample](../examples/src/main/java/com/qoder/cloudagents/examples/ChannelLifecycleExample.java) is runnable with an explicit write opt-in; it never enables, authorizes or pairs the created Channel.
- [ChannelAuthorizationExample](../examples/src/main/java/com/qoder/cloudagents/examples/ChannelAuthorizationExample.java) provides UI-callable helpers. QR data is returned, not printed; a confirmed poll refreshes Channel details. Pairing/revocation are explicit calls and no binding is automatically removed.
- Offline tests send no actual IM messages and use synthetic credentials only. Real Channel/Pairing/QR acceptance requires a separately authorized external connection.

## Current contract and additional endpoints

The Channel operation inventory follows the fixed SDK baseline in [coverage](forward-api-coverage.md).

The current docs also contain Pairing list, retrieve and update endpoints absent from the 123-operation SDK baseline. They are outside the current Java SDK scope. The nine implemented endpoints include current request/response fields (ID-based pairing, scope data, QR errors/optional polling interval).
