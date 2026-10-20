# Usage、Forward SAT 管理与 Drive

本节覆盖 10 个 HTTP 操作：Usage 2 个、Forward SAT 管理 3 个、Drive 5 个。均提供同步、异步和 `withResponse()`，共用请求超时、取消、错误与响应元信息。默认测试使用本地模拟服务；CN/Global 的示例与功能验收按[线上测试指南](live-testing.md)显式运行。

## Usage

```java
UsageListParams params = UsageListParams.builder()
    .startAt("2026-09-14T09:00:00")
    .endAt("2026-09-14T12:00:00")
    .identityIds(Arrays.asList("idn_a", "idn_b"))
    .templateIds(Arrays.asList("tmpl_a", "tmpl_b"))
    .limit(20)
    .build();
IdentityUsageListResponse identities = client.usage().listIdentities(params);
TemplateUsageListResponse templates = client.usage().listTemplates(params);
for (IdentityUsage item : identities) {
  BigDecimal seconds = item.activeSeconds();
  BigDecimal credits = item.credits();
}
```

- 仅支持新版 `start_at` / `end_at`，格式为 `YYYY-MM-DDTHH:00:00`；CN、Global 都按 `Asia/Shanghai` 解释，不转换为 UTC。区间含开始、不含结束，最多 744 小时；当前日期上限由服务端判断。
- 不提供旧的 `start_time` / `end_time`、`duration_seconds` 或 `session_ids` 类型化字段。通过 RequestOptions 混入旧时间参数也会在本地拒绝。
- 多 ID 用逗号分隔 query 编码，不发送 JSON 数组或方括号参数。单一 ID、多 ID 和不同资源维度的组合交由服务端按契约筛选。
- `activeSeconds()`、`credits()` 返回 `BigDecimal`，不截断或重新舍入。Template 行另有 `activeIdentities()`；计数为 `Long`。
- 响应保留 `startAt()`、`endAt()`、空页和未知字段。常规游标分页按 `afterId` / `beforeId` 的方向延续，保留时间、过滤与请求选项；`getNextPageAsync()` 可取消，重复游标会报错。
- 迭代响应会懒加载下一页；只检查首屏请用 `.data()`。PAT 与管理员 SAT 可查询，绑定 Identity 的 SAT 不具备此权限。

## Forward SAT 管理

| 方法 | 用途 |
| --- | --- |
| `client.serviceAccountTokens().create(saKey, params)` | 创建管理员 SAT |
| `client.identities().serviceAccountTokens().create(identityId, saKey, params)` | 创建绑定 Identity 的 SAT |
| `client.serviceAccountTokens().revoke(authTokenId, saKey, params)` | 吊销同一 SA 下签发的 SAT |

SA Key 是独立的方法参数，放入这一次请求的 Authorization 头，不进入 Params 的 JSON。三个方法绕过普通 Credential 解析、强制零重试；请求级 `maxRetries`、幂等键和 `x-should-retry` 都不会开启重试。不会改变共享客户端的业务认证或 `ServiceAccountCredential` 平台置换逻辑。

仅调用 Token 管理时可以不配置业务凭证；普通业务调用若缺少凭证，仍在发送网络请求前报配置错误。模式冲突、非法显式凭据仍会在构建时被拒绝。不要把 SA Key 放进 `accessToken()`。

```java
String baseUrl = "https://api.qoder.com/api/v1/forward";
String saKey = System.getenv("QODER_SA_KEY");
try (ForwardClient manager = ForwardClient.builder()
        .baseUrl(baseUrl).authMode(AuthMode.SA).build()) {
  ServiceAccountToken token = manager.identities().serviceAccountTokens().create(
      identityId,
      saKey,
      ServiceAccountTokenCreateParams.builder().ttlSeconds(300).build());
  try {
    try (ForwardClient runtime = ForwardClient.builder()
            .baseUrl(baseUrl).authMode(AuthMode.SA)
            .accessToken(token.accessToken()).build()) {
      // 用签发的 Identity SAT 调用业务接口。
      IdentityConfigListResponse configs = runtime.identities().configs().list(identityId);
    }
  } finally {
    manager.serviceAccountTokens().revoke(
        token.authTokenId(), saKey,
        ServiceAccountTokenRevokeParams.builder()
            .targetCredentialId(token.credentialId())
            .targetExpiresAt(token.expiresAt())
            .reason("request-complete")
            .build());
  }
}
```

上面是简化示例；[完整示例](../examples/src/main/java/com/qoder/cloudagents/examples/ForwardTokenManagementExample.java)还保留原错误及清理错误，分别验证管理员与 Identity Token，并只吊销本次签发的 Token。`ttlSeconds` 省略时使用服务默认值，显式值范围为 1–43200。`targetCredentialId`、`targetExpiresAt` 可消除吊销目标歧义；后者保留 RFC 3339 字符串。

直接 SAT 不自动续期。创建请求失败可能已经在服务端签发；SDK 不自动重放，也不会用吊销所有 Token 的方式清理。默认对象输出不包含密钥或 Token，但 `accessToken()`、`rawJson()`、`toJsonValue()` 包含敏感响应，调用方应保护这些值。

## Drive（Beta）

| 方法 | 参数与返回 |
| --- | --- |
| `drives().listEntries(params)` | `DriveListEntriesParams` → `DriveListEntriesResponse` |
| `drives().createUploadUrl(params)` | `DriveCreateUploadUrlParams` → `DriveUploadUrl` |
| `drives().createDownloadUrl(params)` | `DriveCreateDownloadUrlParams` → `DriveDownloadUrl` |
| `drives().deleteEntry(params)` | `DriveDeleteEntryParams` → `DriveDeleteEntryResponse` |
| `drives().clear(params)` | `DriveClearParams` → `DriveClearResponse` |

每个方法均显式要求 `identityId`，包括使用 Identity SAT 的情况；它只进入 query，不混入 JSON body。Identity SAT 必须选择其绑定的 Identity。

```java
DriveListEntriesResponse page = client.drives().listEntries(
    DriveListEntriesParams.builder().identityId(identityId)
        .path("reports").limit(20).build());
while (true) {
  for (DriveEntry entry : page.entries()) {
    // 顺序与服务端一致；目录的 size() 可以为 null，零字节文件为 0。
  }
  if (!page.hasNextPage()) break;
  page = client.drives().listEntries(
      DriveListEntriesParams.builder().identityId(identityId)
          .path("reports").limit(20).pageToken(page.nextPageToken()).build());
}
```

- SDK 保留服务端顺序。`entries` / `next_page_token` 是独立分页协议，不套用普通 `data` / `has_more` 分页。令牌原样传入，沿用同一认证范围、Identity、目录；旧响应没有令牌时视为末页。
- 列表根目录可省略 `path` 或传空字符串。非空路径的 `/` 在最终 query 中保留，包括 RequestOptions 覆盖之后；中文与保留字符做表单编码，空格用 `+`。分页令牌正常编码，不修改其中的 `/`。
- 上传、下载、删除使用 body 中的相对 `path`；不自动归一化、排序或递归访问目录。非法路径由服务端校验；空文件路径在本地拒绝。
- 旧版删除响应缺少 `deleted_count` 时，`deletedCount()` 返回 null，原始字段仍保留。`size`、ETag、修改时间、下载内容类型等允许缺失；未知类型与额外字段可读取。
- 上传/下载方法只申请签名 URL，不自动传输文件。外部存储请求只使用返回的 method、headers，不复用 SDK Authorization、默认头、query 或重试。签名 URL 不应输出到日志。刷新过期授权使用新幂等键或省略幂等键，重放旧请求不会延长有效期。
- `deleteEntry` 删除指定文件或目录；`recursive(true)` 才表示递归删除非空目录。`clear` 清空整个 Identity Drive，只能在调用方明确需要时使用。503 部分删除错误的 `error.deleted_count` 保留在 `ApiStatusException.body()`；request ID 优先取响应头，再取顶层或 error 对象。

[DriveExample](../examples/src/main/java/com/qoder/cloudagents/examples/DriveExample.java)使用独立存储连接上传小文件、列目录、下载比对，然后只删除随机路径下的本次文件；不会清空整个 Drive，也不跟随存储重定向。

Template 的配套类型已存在，可以显式开启 Agent 的 Drive 托管工具：

```java
TemplateUpdateParams patch = TemplateUpdateParams.builder()
    .managedToolConfig(ManagedToolConfig.builder()
        .enabledTools(Arrays.asList("drive", "schedule")).build())
    .build();
client.templates().update(templateId, patch);
```

该配置会更新所选 Template 的工具列表，按需要保留其他工具；SDK 不暴露 `/forward-mcp/...` 内部工具路由。不要把 Drive 文件管理与 Agent 内部工具调用混为一类接口。

SDK 同时提供 [Session / Thread SSE](session-streaming.md) 和 [Realtime 与 History](realtime.md)。对应文档说明订阅、连接、取消和历史分页的使用方式；真实环境验收见[线上测试指南](live-testing.md)。
