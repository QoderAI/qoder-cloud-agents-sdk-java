# Managed 真实环境验收

`ManagedLiveCheck` 是独立 examples 工程中的可选测试入口，使用已解析的 SDK JAR；普通测试不访问线上。开发构建先执行根项目 `./mvnw clean install`，再执行 `./mvnw -f examples/pom.xml clean verify`。验证内网 QA 包或公网发布包时，在独立消费者中锁定实际下载的包和摘要，不能用本地安装包替代。

配置 `QODER_BASE_URL`（CN 或 Global 的 `/api/v1/cloud`）、`QODER_AUTH_MODE=pat|sa`，以及 PAT、SA Key 或 SAT。SA Key 额外需要 `QODER_REGION=cn|global`，由 `ManagedExampleClient` 申请 `qca.access`。密钥通过环境注入，不写入命令或日志。可设置 `QODER_MODEL`，默认 `auto`。

```sh
export QODER_RUN_LIVE=true
./mvnw -B -ntp -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ManagedLiveCheck \
  -Dexec.args=read
```

| 分组 | 验证内容 | 额外开关 |
| --- | --- | --- |
| `read` | Model、八类 Search、Dream list、异步响应 ID | 无 |
| `resources` | Agent 乐观版本更新及历史版本、Environment、Memory/Version、File 上传及 downloadable 权限（允许时下载）、Skill/Version、Vault/Credential archive/delete | `QODER_ALLOW_WRITE=true` |
| `session` | Agent + cloud Environment 创建 Session，预算与 metadata，动态 Agent 配置，短模型请求、SSE 回显与 idle、Credits、Thread 列表、取消预算 | write + `QODER_ALLOW_EXECUTION=true` |
| `deployment` | 手动 Deployment、pause/unpause、Run 的 scoped/global 读取、初始事件回显 | write + execution |
| `work` | 独立 self_hosted Environment 的 Work poll/ack/heartbeat/update/get/list/stats/stop | write + execution |

写入分组仅创建 `sdk-contract-managed-java-` 前缀资源。通过 ID 仅清理本次创建的资源；Agent/Deployment archive 会保留历史记录。Session 不在终态时先取消，再尝试删除。Work 分组验证租约协议，不启动客户 worker，也不声称验证自托管容器执行。

每次 HTTP 请求最长 120 秒；模型回复只发一条短消息。记录方法、路径、状态和请求/资源 ID，日志不打印请求体、密钥、完整 URL query 或模型文本。失败退出非零，清理单独尝试并记录 `cleanup_failed`，不会掩盖业务异常。外部运行器应为每个分组设置进程超时，并保留首轮、后续复验及未清理资源记录。

需要分别记录 CN/Global × PAT/SA 的结果。五个 JDK 的离线回归不能代替真实链路结果。当前测试入口不是 108 项逐接口线上覆盖：OAuth 用户授权、OSS 挂载、Dream 模型执行、长稳及故障注入需专用配置和单独验收。已有 Forward 内网测试不会自动执行本入口；同步内部控制脚本时要显式加入相应分组，默认 `promote=false`。

上传文件不一定可下载；当响应 `downloadable=false` 时，资源分组检查 `/content` 返回官方规定的 403，并明确记录该路径未进行实际文件传输。Work 的 `block_ms` 必须在 1–999 之间；当前示例使用 999。
