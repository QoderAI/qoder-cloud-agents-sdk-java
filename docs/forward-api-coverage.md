# Forward API 覆盖基线

契约核对：2026-10-28。**0.2.0** 覆盖 **146 个 HTTP 操作及 Realtime WebSocket**：首版 123 项、Webhook 8 项、Forward Search / Dream / Pairing / Decision 15 项。同一版本还提供 [107 个 Managed 操作](managed-api-coverage.md)。离线验证与真实环境验收分别记录，不能把操作覆盖数当作线上通过率。

HTTP BaseURL 为 CN `https://api.qoder.com.cn/api/v1/forward` 或 Global `https://api.qoder.com/api/v1/forward`；表内路径均相对于 BaseURL。

来源：TypeScript main `597ff047be4fb0afb72fc6bd024ce957cf3e1fc1` 的 `src/forward/api-inventory.json`（120 项），补充 `service-account-token.ts` 和 `identity-service-account-token.ts` 的 3 项；operation ID 集合与 Go main `735f13c8bf5888bf70b5a93b9ba89bd0e078d4b9` 的 `forward/testdata/api-contracts.json`（123 项）交叉核对一致。

首版基线共 **123 个 HTTP 操作**：121 个普通 HTTP 操作、2 个 SSE 订阅操作；另有 1 个 WebSocket 连接入口。Java 资源方法见下表，0.2.0 新增的 23 项列在本文末尾；Maven 接入方式见 [README](../README.md#installation)，已发布版本见 [Releases](https://github.com/QoderAI/qoder-cloud-agents-sdk-java/releases)。相同方法的同步/异步形式只计一个协议操作。

这里只盘点协议入口；参数、响应、分页、认证和重试行为由对应资源指南与契约测试说明。

0.2.0 已按 Go `a996a88`、Python `2f8c625`、TypeScript `f4e3a4f` 的 main 补齐 ImageSource `file_id`、创建 Memory Store 时的 mount `access`、Schedule/Run `initial_events_version` 和 ChannelPairing 时间戳。这些字段不新增协议操作；客户端 HTTP 扩展、通用 JSON 请求和配置复制见[客户端配置](client-configuration.md)。

## 使用指南

- [客户端认证、超时、重试与取消](client-configuration.md)
- [Template](templates.md)、[Identity / Config](identities.md)、[Session / Thread](sessions.md) 和 [SSE](session-streaming.md)
- [Memory Store](memory-stores.md)、[Environment / File](environments-files.md)、[Skill / Vault](skills-vaults.md)
- [Schedule / Batch](schedules-batches.md)、[Channel / Pairing / QR](channels.md)、[Usage / SAT / Drive](usage-sat-drive.md)
- [Realtime](realtime.md) 和 [可运行示例](../examples/README.md)

## HTTP 操作

| # | operation ID | Java 资源方法（省略 client 前缀） | HTTP | 相对路径 | 返回形式 | 状态 |
| ---: | --- | --- | --- | --- | --- | --- |
| 1 | `listBatches` | `batches().list(...)` | `GET` | `/batches` | 分页 | 已实现、离线验证 |
| 2 | `createBatch` | `batches().create(...)` | `POST` | `/batches` | 类型化响应 | 已实现、离线验证 |
| 3 | `getBatch` | `batches().retrieve(...)` | `GET` | `/batches/{batch_id}` | 类型化响应 | 已实现、离线验证 |
| 4 | `cancelBatch` | `batches().cancel(...)` | `POST` | `/batches/{batch_id}/cancel` | 类型化响应 | 已实现、离线验证 |
| 5 | `getBatchError` | `batches().getError(...)` | `GET` | `/batches/{batch_id}/error` | 类型化响应 | 已实现、离线验证 |
| 6 | `getBatchOutput` | `batches().getOutput(...)` | `GET` | `/batches/{batch_id}/output` | 类型化响应 | 已实现、离线验证 |
| 7 | `listBatchTasks` | `batches().tasks().list(...)` | `GET` | `/batches/{batch_id}/tasks` | 分页 | 已实现、离线验证 |
| 8 | `listChannels` | `channels().list(...)` | `GET` | `/channels` | 分页 | 已实现、离线验证 |
| 9 | `createChannel` | `channels().create(...)` | `POST` | `/channels` | 类型化响应 | 已实现、离线验证 |
| 10 | `getChannel` | `channels().retrieve(...)` | `GET` | `/channels/{channel_id}` | 类型化响应 | 已实现、离线验证 |
| 11 | `updateChannel` | `channels().update(...)` | `POST` | `/channels/{channel_id}` | 类型化响应 | 已实现、离线验证 |
| 12 | `deleteChannel` | `channels().delete(...)` | `DELETE` | `/channels/{channel_id}` | 类型化响应 | 已实现、离线验证 |
| 13 | `pairChannel` | `channelPairings().create(...)` | `POST` | `/channel_pairings` | 类型化响应 | 已实现、离线验证 |
| 14 | `unpairChannel` | `channelPairings().delete(...)` | `DELETE` | `/channel_pairings/{pairing_id}` | 类型化响应 | 已实现、离线验证 |
| 15 | `createChannelQrSession` | `channels().qrSessions().create(...)` | `POST` | `/channels/{channel_id}/qr_sessions` | 类型化响应 | 已实现、离线验证 |
| 16 | `getChannelQrSession` | `channels().qrSessions().retrieve(...)` | `GET` | `/qr_sessions/{session_key}` | 类型化响应 | 已实现、离线验证 |
| 17 | `listEnvironment` | `environments().list(...)` | `GET` | `/environments` | 分页 | 已实现、离线验证 |
| 18 | `createEnvironment` | `environments().create(...)` | `POST` | `/environments` | 类型化响应 | 已实现、离线验证 |
| 19 | `getEnvironment` | `environments().retrieve(...)` | `GET` | `/environments/{id}` | 类型化响应 | 已实现、离线验证 |
| 20 | `updateEnvironment` | `environments().update(...)` | `POST` | `/environments/{id}` | 类型化响应 | 已实现、离线验证 |
| 21 | `archiveEnvironment` | `environments().archive(...)` | `POST` | `/environments/{id}/archive` | 类型化响应 | 已实现、离线验证 |
| 22 | `deleteEnvironment` | `environments().delete(...)` | `DELETE` | `/environments/{id}` | 删除 JSON / 204 | 已实现、离线验证 |
| 23 | `listFile` | `files().list(...)` | `GET` | `/files` | 分页 | 已实现、离线验证 |
| 24 | `uploadFile` | `files().upload(...)` | `POST` | `/files` | 类型化响应 | 已实现、离线验证 |
| 25 | `getFile` | `files().getMetadata(...)` | `GET` | `/files/{file_id}` | 类型化响应 | 已实现、离线验证 |
| 26 | `deleteFile` | `files().delete(...)` | `DELETE` | `/files/{file_id}` | 删除 JSON / 204 | 已实现、离线验证 |
| 27 | `downloadFile` | `files().download(...)` | `GET` | `/files/{file_id}/content` | 可关闭下载流 / URL helper | 已实现、离线验证 |
| 28 | `listIdentities` | `identities().list(...)` | `GET` | `/identities` | 分页 | 已实现、离线验证 |
| 29 | `createIdentity` | `identities().create(...)` | `POST` | `/identities` | 类型化响应 | 已实现、离线验证 |
| 30 | `ensureAdminIdentity` | `identities().ensureAdmin(...)` | `POST` | `/identities/admin/ensure` | 类型化响应 | 已实现、离线验证 |
| 31 | `getIdentityStats` | `identities().stats(...)` | `GET` | `/identities/stats` | 类型化响应 | 已实现、离线验证 |
| 32 | `getIdentity` | `identities().retrieve(...)` | `GET` | `/identities/{identity_id}` | 类型化响应 | 已实现、离线验证 |
| 33 | `updateIdentity` | `identities().update(...)` | `POST` | `/identities/{identity_id}` | 类型化响应 | 已实现、离线验证 |
| 34 | `deleteIdentity` | `identities().delete(...)` | `DELETE` | `/identities/{identity_id}` | 类型化响应 | 已实现、离线验证 |
| 35 | `listIdentityAgents` | `identities().listTemplates(...)` | `GET` | `/identities/{identity_id}/agents` | 类型化响应 | 已实现、离线验证 |
| 36 | `clearIdentity` | `identities().clear(...)` | `POST` | `/identities/{identity_id}/clear` | 类型化响应 | 已实现、离线验证 |
| 37 | `disableIdentity` | `identities().disable(...)` | `POST` | `/identities/{identity_id}/disable` | 类型化响应 | 已实现、离线验证 |
| 38 | `enableIdentity` | `identities().enable(...)` | `POST` | `/identities/{identity_id}/enable` | 类型化响应 | 已实现、离线验证 |
| 39 | `listIdentityConfigs` | `identities().configs().list(...)` | `GET` | `/identities/{identity_id}/templates` | 分页 | 已实现、离线验证 |
| 40 | `getIdentityConfig` | `identities().configs().retrieve(...)` | `GET` | `/identities/{identity_id}/templates/{template_id}/config` | 类型化响应 | 已实现、离线验证 |
| 41 | `upsertIdentityConfig` | `identities().configs().upsert(...)` | `POST` | `/identities/{identity_id}/templates/{template_id}/config` | 类型化响应 | 已实现、离线验证 |
| 42 | `getEffectiveConfig` | `identities().configs().getEffective(...)` | `GET` | `/identities/{identity_id}/templates/{template_id}/effective` | 类型化响应 | 已实现、离线验证 |
| 43 | `listMemoryStoreMounts` | `identities().memoryStores().list(...)` | `GET` | `/identities/{identity_id}/templates/{template_id}/memory_stores` | 类型化响应 | 已实现、离线验证 |
| 44 | `mountMemoryStore` | `identities().memoryStores().mount(...)` | `POST` | `/identities/{identity_id}/templates/{template_id}/memory_stores` | 类型化响应 | 已实现、离线验证 |
| 45 | `detachMemoryStore` | `identities().memoryStores().detach(...)` | `DELETE` | `/identities/{identity_id}/templates/{template_id}/memory_stores/{memory_store_id}` | 类型化响应 | 已实现、离线验证 |
| 46 | `listMemoryStore` | `memoryStores().list(...)` | `GET` | `/memory_stores` | 分页 | 已实现、离线验证 |
| 47 | `createMemoryStore` | `memoryStores().create(...)` | `POST` | `/memory_stores` | 类型化响应 | 已实现、离线验证 |
| 48 | `getMemoryStore` | `memoryStores().retrieve(...)` | `GET` | `/memory_stores/{memory_store_id}` | 类型化响应 | 已实现、离线验证 |
| 49 | `updateMemoryStore` | `memoryStores().update(...)` | `POST` | `/memory_stores/{memory_store_id}` | 类型化响应 | 已实现、离线验证 |
| 50 | `deleteMemoryStore` | `memoryStores().delete(...)` | `DELETE` | `/memory_stores/{memory_store_id}` | 类型化响应 | 已实现、离线验证 |
| 51 | `archiveMemoryStore` | `memoryStores().archive(...)` | `POST` | `/memory_stores/{memory_store_id}/archive` | 类型化响应 | 已实现、离线验证 |
| 52 | `listMemoryStoreMemoryMemories` | `memoryStores().memories().list(...)` | `GET` | `/memory_stores/{memory_store_id}/memories` | 分页 | 已实现、离线验证 |
| 53 | `createMemoryStoreMemory` | `memoryStores().memories().create(...)` | `POST` | `/memory_stores/{memory_store_id}/memories` | 类型化响应 | 已实现、离线验证 |
| 54 | `getMemoryStoreMemory` | `memoryStores().memories().retrieve(...)` | `GET` | `/memory_stores/{memory_store_id}/memories/{memory_id}` | 类型化响应 | 已实现、离线验证 |
| 55 | `updateMemoryStoreMemory` | `memoryStores().memories().update(...)` | `POST` | `/memory_stores/{memory_store_id}/memories/{memory_id}` | 类型化响应 | 已实现、离线验证 |
| 56 | `deleteMemoryStoreMemory` | `memoryStores().memories().delete(...)` | `DELETE` | `/memory_stores/{memory_store_id}/memories/{memory_id}` | 类型化响应 | 已实现、离线验证 |
| 57 | `listMemoryStoreMemoryVersion` | `memoryStores().memoryVersions().list(...)` | `GET` | `/memory_stores/{memory_store_id}/memory_versions` | 分页 | 已实现、离线验证 |
| 58 | `getMemoryStoreMemoryVersion` | `memoryStores().memoryVersions().retrieve(...)` | `GET` | `/memory_stores/{memory_store_id}/memory_versions/{memory_version_id}` | 类型化响应 | 已实现、离线验证 |
| 59 | `redactMemoryStoreMemoryVersion` | `memoryStores().memoryVersions().redact(...)` | `POST` | `/memory_stores/{memory_store_id}/memory_versions/{memory_version_id}/redact` | 类型化响应 | 已实现、离线验证 |
| 60 | `listModels` | `models().list(...)` | `GET` | `/models` | 类型化响应 | 已实现、离线验证 |
| 61 | `listSchedules` | `schedules().list(...)` | `GET` | `/schedules` | 分页 | 已实现、离线验证 |
| 62 | `createSchedule` | `schedules().create(...)` | `POST` | `/schedules` | 类型化响应 | 已实现、离线验证 |
| 63 | `archiveSchedules` | `schedules().archiveMany(...)` | `POST` | `/schedules/archive` | 类型化响应 | 已实现、离线验证 |
| 64 | `getSchedule` | `schedules().retrieve(...)` | `GET` | `/schedules/{schedule_id}` | 类型化响应 | 已实现、离线验证 |
| 65 | `updateSchedule` | `schedules().update(...)` | `POST` | `/schedules/{schedule_id}` | 类型化响应 | 已实现、离线验证 |
| 66 | `archiveSchedule` | `schedules().archive(...)` | `POST` | `/schedules/{schedule_id}/archive` | 类型化响应 | 已实现、离线验证 |
| 67 | `pauseSchedule` | `schedules().pause(...)` | `POST` | `/schedules/{schedule_id}/pause` | 类型化响应 | 已实现、离线验证 |
| 68 | `runSchedule` | `schedules().run(...)` | `POST` | `/schedules/{schedule_id}/run` | 类型化响应 | 已实现、离线验证 |
| 69 | `unpauseSchedule` | `schedules().unpause(...)` | `POST` | `/schedules/{schedule_id}/unpause` | 类型化响应 | 已实现、离线验证 |
| 70 | `listScheduleRuns` | `scheduleRuns().list(...)` | `GET` | `/schedule_runs` | 分页 | 已实现、离线验证 |
| 71 | `getScheduleRun` | `scheduleRuns().retrieve(...)` | `GET` | `/schedule_runs/{run_id}` | 类型化响应 | 已实现、离线验证 |
| 72 | `listSessions` | `sessions().list(...)` | `GET` | `/sessions` | 分页 | 已实现、离线验证 |
| 73 | `createSession` | `sessions().create(...)` | `POST` | `/sessions` | 类型化响应 | 已实现、离线验证 |
| 74 | `getSession` | `sessions().retrieve(...)` | `GET` | `/sessions/{session_id}` | 类型化响应 | 已实现、离线验证 |
| 75 | `updateSession` | `sessions().update(...)` | `POST` | `/sessions/{session_id}` | 类型化响应 | 已实现、离线验证 |
| 76 | `archiveSession` | `sessions().archive(...)` | `POST` | `/sessions/{session_id}/archive` | 类型化响应 | 已实现、离线验证 |
| 77 | `cancelSession` | `sessions().cancel(...)` | `POST` | `/sessions/{session_id}/cancel` | 类型化响应 | 已实现、离线验证 |
| 78 | `listSessionEvents` | `sessions().events().list(...)` | `GET` | `/sessions/{session_id}/events` | 分页 | 已实现、离线验证 |
| 79 | `sendSessionEvents` | `sessions().events().send(...)` | `POST` | `/sessions/{session_id}/events` | 类型化响应 | 已实现、离线验证 |
| 80 | `streamSessionEvents` | `sessions().events().streamEvents(...)` | `GET` | `/sessions/{session_id}/events/stream` | SSE | 已实现、离线验证 |
| 81 | `addSessionResource` | `sessions().resources().add(...)` | `POST` | `/sessions/{session_id}/resources` | 类型化响应 | 已实现、离线验证 |
| 82 | `listSessionThreads` | `sessions().threads().list(...)` | `GET` | `/sessions/{session_id}/threads` | 分页 | 已实现、离线验证 |
| 83 | `getSessionThread` | `sessions().threads().retrieve(...)` | `GET` | `/sessions/{session_id}/threads/{thread_id}` | 类型化响应 | 已实现、离线验证 |
| 84 | `archiveSessionThread` | `sessions().threads().archive(...)` | `POST` | `/sessions/{session_id}/threads/{thread_id}/archive` | 类型化响应 | 已实现、离线验证 |
| 85 | `listSessionThreadEvents` | `sessions().threads().events().list(...)` | `GET` | `/sessions/{session_id}/threads/{thread_id}/events` | 分页 | 已实现、离线验证 |
| 86 | `streamSessionThreadEvents` | `sessions().threads().events().streamEvents(...)` | `GET` | `/sessions/{session_id}/threads/{thread_id}/stream` | SSE | 已实现、离线验证 |
| 87 | `listSkill` | `skills().list(...)` | `GET` | `/skills` | 分页 | 已实现、离线验证 |
| 88 | `createSkill` | `skills().create(...)` | `POST` | `/skills` | 类型化响应 | 已实现、离线验证 |
| 89 | `getSkill` | `skills().retrieve(...)` | `GET` | `/skills/{id}` | 类型化响应 | 已实现、离线验证 |
| 90 | `updateSkill` | `skills().update(...)` | `PUT` | `/skills/{id}` | 类型化响应 | 已实现、离线验证 |
| 91 | `deleteSkill` | `skills().delete(...)` | `DELETE` | `/skills/{id}` | 删除 JSON / 204 | 已实现、离线验证 |
| 92 | `listSkillVersion` | `skills().versions().list(...)` | `GET` | `/skills/{id}/versions` | 分页 | 已实现、离线验证 |
| 93 | `createSkillVersion` | `skills().versions().create(...)` | `POST` | `/skills/{id}/versions` | 类型化响应 | 已实现、离线验证 |
| 94 | `getSkillVersion` | `skills().versions().retrieve(...)` | `GET` | `/skills/{id}/versions/{version}` | 类型化响应 | 已实现、离线验证 |
| 95 | `deleteSkillVersion` | `skills().versions().delete(...)` | `DELETE` | `/skills/{id}/versions/{version}` | 类型化响应 | 已实现、离线验证 |
| 96 | `downloadSkillVersion` | `skills().versions().download(...)` | `GET` | `/skills/{id}/versions/{version}/content` | 可关闭 ZIP 字节流 | 已实现、离线验证 |
| 97 | `listTemplates` | `templates().list(...)` | `GET` | `/templates` | 分页、懒加载迭代 | 已实现、离线验证 |
| 98 | `createTemplate` | `templates().create(...)` | `POST` | `/templates` | 类型化响应 | 已实现、离线验证 |
| 99 | `getTemplate` | `templates().retrieve(...)` | `GET` | `/templates/{template_id}` | 类型化完整配置 | 已实现、离线验证 |
| 100 | `updateTemplate` | `templates().update(...)` | `POST` | `/templates/{template_id}` | 类型化响应 | 已实现、离线验证 |
| 101 | `archiveTemplate` | `templates().archive(...)` | `POST` | `/templates/{template_id}/archive` | 类型化响应 | 已实现、离线验证 |
| 102 | `cloneTemplate` | `templates().clone(...)` | `POST` | `/templates/{template_id}/clone` | 类型化响应 | 已实现、离线验证 |
| 103 | `listVault` | `vaults().list(...)` | `GET` | `/vaults` | 分页 | 已实现、离线验证 |
| 104 | `createVault` | `vaults().create(...)` | `POST` | `/vaults` | 类型化响应 | 已实现、离线验证 |
| 105 | `getVault` | `vaults().retrieve(...)` | `GET` | `/vaults/{id}` | 类型化响应 | 已实现、离线验证 |
| 106 | `deleteVault` | `vaults().delete(...)` | `DELETE` | `/vaults/{id}` | 删除 JSON / 204 | 已实现、离线验证 |
| 107 | `listVaultCredential` | `vaults().credentials().list(...)` | `GET` | `/vaults/{id}/credentials` | 分页 | 已实现、离线验证 |
| 108 | `createVaultCredential` | `vaults().credentials().create(...)` | `POST` | `/vaults/{id}/credentials` | 类型化响应 | 已实现、离线验证 |
| 109 | `getVaultCredential` | `vaults().credentials().retrieve(...)` | `GET` | `/vaults/{id}/credentials/{cred_id}` | 类型化响应 | 已实现、离线验证 |
| 110 | `deleteVaultCredential` | `vaults().credentials().delete(...)` | `DELETE` | `/vaults/{id}/credentials/{cred_id}` | 删除 JSON / 204 | 已实现、离线验证 |
| 111 | `updateVaultCredential` | `vaults().credentials().update(...)` | `POST` | `/vaults/{id}/credentials/{cred_id}` | 类型化响应 | 已实现、离线验证 |
| 112 | `listIdentityUsage` | `usage().listIdentities(...)` | `GET` | `/usage/identities` | 分页 | 已实现、离线验证 |
| 113 | `listTemplateUsage` | `usage().listTemplates(...)` | `GET` | `/usage/templates` | 分页 | 已实现、离线验证 |
| 114 | `createRealtimeConversation` | `realtime().conversations().create(...)` | `POST` | `/realtime/conversations` | 类型化响应 | 已实现 |
| 115 | `getRealtimeConversationHistory` | `realtime().conversations().history(...)` | `GET` | `/realtime/conversations/{conversation_id}/history` | 独立历史分页 | 已实现 |
| 116 | `listDriveEntries` | `drives().listEntries(...)` | `GET` | `/drives/entries` | 类型化响应 | 已实现、离线验证 |
| 117 | `createDriveUploadURL` | `drives().createUploadUrl(...)` | `POST` | `/drives/upload-url` | 类型化响应 | 已实现、离线验证 |
| 118 | `createDriveDownloadURL` | `drives().createDownloadUrl(...)` | `POST` | `/drives/download-url` | 类型化响应 | 已实现、离线验证 |
| 119 | `deleteDriveEntry` | `drives().deleteEntry(...)` | `DELETE` | `/drives/entries` | 类型化响应 | 已实现、离线验证 |
| 120 | `clearDrive` | `drives().clear(...)` | `DELETE` | `/drives` | 类型化响应 | 已实现、离线验证 |
| 121 | `createServiceAccountToken` | `serviceAccountTokens().create(...)` | `POST` | `/service_account_tokens` | 类型化响应 | 已实现、离线验证 |
| 122 | `createIdentityServiceAccountToken` | `identities().serviceAccountTokens().create(...)` | `POST` | `/identities/{identity_id}/service_account_tokens` | 类型化响应 | 已实现、离线验证 |
| 123 | `revokeServiceAccountToken` | `serviceAccountTokens().revoke(...)` | `POST` | `/service_account_tokens/{auth_token_id}/revoke` | 类型化响应 | 已实现、离线验证 |

基础层、普通 HTTP、Session/Thread SSE、Realtime 创建、History 和 WebSocket 均已实现。默认测试验证本地模拟服务与独立 JAR 消费；线上测试需按[真实环境测试](live-testing.md)另行执行。

## WebSocket 与不增加 HTTP 路由的辅助能力

| 能力 | 建议入口 | 必须保持的语义 | 状态 |
| --- | --- | --- | --- |
| Realtime WebSocket | `client.realtime().connect(...)` | `GET /realtime?conversation_id=...` 升级；鉴权、ready 等待、取消、有界收发 | 已实现 |
| Realtime 客户端事件 | `sendText / appendAudio / sendEvent / interrupt / playback* / ping` | 按现有协议发送；不伪造播放完成，不自动重发输入 | 已实现 |
| Realtime 服务端事件 | `receive / receiveAsync` | 已知事件类型、未知事件、Work、音频、转写；保留原始错误 | 已实现 |
| Realtime 关闭 | `closeGracefully / close` | 业务确认与传输释放分别表达 | 已实现 |
| Session / Thread 续订 | `events().resumableStream(...)` | 显式开启，沿用原参数/options，基于已交付的完整 SSE 帧恢复 Last-Event-ID | 已实现、离线验证 |
| 常规分页 | `hasNextPage / getNextPage / autoPagingIterable` | cursor 与 page 分别处理；保留筛选和请求选项，末页行为明确 | 已实现 |
| Drive 翻页 | `entries / nextPageToken` | 原样游标、服务端顺序，不套普通 data/has_more | 已实现 |
| History 翻页 | `nextBefore` | 历史持久化 ID，不能混用 WS sequence 或 SSE id | 已实现 |
| File 下载 | `files().download(...)` | API 获取授权后独立存储请求，流式读取且不泄漏 QCA Token | 已实现 |
| SA Credential | `ServiceAccountCredential` | 平台 Token 置换、缓存、按需续期，与 Forward SAT 管理 API 分开 | 已实现 |
| 同步/异步与响应元信息 | `ForwardClient / AsyncForwardClient / withResponse` | 同一协议核心；取消和资源所有权明确；保留 request ID | 已实现 |

## 当前未提供

- Realtime Template 专用配置 GET/POST；普通 Template 的 managed_tool_config 已实现。
- Agent 运行时内部工具调用和免 Token 的内部服务认证模式。
- 新增 Drive 本地文件同步框架、采集/播放音频设备实现。

这些范围外项目不应放入验收分母，也不应以 skipped 用例伪装已提供功能。

## 实施时补充的逐项证据

每个 operation ID 关联 Java resource 和 Params/Response、独立 wire fixture、同步/异步契约测试、文档示例与适用的真实环境结果。线上覆盖按认证、资源生命周期和业务场景安排，不承诺所有操作都适用于所有身份。受权限或 fixture 限制时注明原因，不能以 HTTP 假服务通过代替真实环境验收。

维护检查见[维护指南](maintaining.md)，线上验证入口见[真实环境测试](live-testing.md)。

## 0.2.0 新增的 Webhooks 与 Forward 操作

Webhook 的 8 个操作已实现同步/异步、分页、原始请求体验签及示例，详见 [Webhook 指南](webhooks.md)。本轮补齐之前记录为范围外的三个 Pairing 操作，以及 Search、Dream 和 Decision。

| # | HTTP | 相对路径 | Java 入口 |
| ---: | --- | --- | --- |
| 124 | `POST` | `/webhook/endpoints` | `webhooks().endpoints().create(...)` |
| 125 | `GET` | `/webhook/endpoints` | `webhooks().endpoints().list(...)` |
| 126 | `GET` | `/webhook/endpoints/{endpoint_id}` | `webhooks().endpoints().retrieve(...)` |
| 127 | `PUT` | `/webhook/endpoints/{endpoint_id}` | `webhooks().endpoints().update(...)` |
| 128 | `DELETE` | `/webhook/endpoints/{endpoint_id}` | `webhooks().endpoints().delete(...)` |
| 129 | `POST` | `/webhook/endpoints/{endpoint_id}/enable` | `webhooks().endpoints().enable(...)` |
| 130 | `POST` | `/webhook/endpoints/{endpoint_id}/disable` | `webhooks().endpoints().disable(...)` |
| 131 | `POST` | `/webhook/endpoints/{endpoint_id}/test` | `webhooks().endpoints().test(...)` |
| 132 | `POST` | `/environments/search` | `environments().search(...)` |
| 133 | `POST` | `/skills/search` | `skills().search(...)` |
| 134 | `POST` | `/vaults/search` | `vaults().search(...)` |
| 135 | `POST` | `/files/search` | `files().search(...)` |
| 136 | `POST` | `/dreams` | `dreams().create(...)` |
| 137 | `GET` | `/dreams` | `dreams().list(...)` |
| 138 | `GET` | `/dreams/{id}` | `dreams().retrieve(...)` |
| 139 | `POST` | `/dreams/{id}/cancel` | `dreams().cancel(...)` |
| 140 | `POST` | `/dreams/{id}/archive` | `dreams().archive(...)` |
| 141 | `GET` | `/channel_pairings` | `channelPairings().list(...)` |
| 142 | `GET` | `/channel_pairings/{pairing_id}` | `channelPairings().retrieve(...)` |
| 143 | `POST` | `/channel_pairings/{pairing_id}` | `channelPairings().update(...)` |
| 144 | `POST` | `/decisions` | `decisions().create(...)` |
| 145 | `GET` | `/decisions` | `decisions().list(...)` |
| 146 | `GET` | `/decisions/{decision_id}` | `decisions().retrieve(...)` |

最新公开契约及字段迁移说明见 [Forward 补齐指南](forward-parity.md)。固定官方来源为 2026-10-28 文档快照；Go/Python/TS 0.13.0 作为行为对照，不把其扩展字段自动视为官方契约。

本轮独立请求 fixture 位于 `src/test/resources/forward-parity-routes.json`，覆盖 15 个操作的同步/异步及 withResponse 入口。`ForwardParityTest`、`ForwardPaginationParityTest` 验证字段、metadata 冲突、精度、分页、幂等重试与取消。`ForwardCapabilitiesConsumerTest` 通过独立 examples 工程消费实际 JAR。真实环境和 QA 包尚须按 [验收指南](live-testing.md) 执行；目前没有这些新增功能的线上通过声明。
