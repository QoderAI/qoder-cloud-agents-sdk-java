# Forward 真实环境验收

`ForwardLiveCheck` 位于独立的 `examples` Maven 工程，消费本地安装的 SDK JAR。普通 `test` / `verify` 只运行模拟服务测试，不执行线上请求。

先构建、安装并验证消费者：

```sh
./mvnw -B -ntp clean install
./mvnw -B -ntp -f examples/pom.xml clean verify
```

然后按 [示例认证说明](../examples/README.md) 配置 PAT、SA Key 自动置换或直接 SAT。设置 `QODER_FORWARD_BASE_URL`、`QODER_REGION` 和当前认证可访问的 `QODER_TEMPLATE_ID`；Realtime 分组需要该模板已经开启 Realtime。密钥由本机环境或 CI 密钥注入，不放入命令参数、仓库或日志。

```sh
export QODER_RUN_LIVE=true
./mvnw -B -ntp -f examples/pom.xml exec:java \
  -Dexec.mainClass=com.qoder.cloudagents.examples.ForwardLiveCheck \
  -Dexec.args=read
```

写入及执行分组必须显式启用：

```sh
export QODER_ALLOW_WRITE=true
export QODER_ALLOW_EXECUTION=true
# 用下表中的分组名替换 read。usage 与 read 一样只需要 QODER_RUN_LIVE。
# realtime-pcm 另外要求 QODER_PCM_FILE 指向不超过 30 秒的原始 PCM16LE / 16 kHz / mono。
```

| 分组 | 实际检查 | 副作用与清理 |
| --- | --- | --- |
| `read` | Model 列表、指定 Template、异步 Template 列表 | 只读 |
| `resources` | Environment/File 上传下载、Memory/Version、Skill/Version/Vault/Credential、Template 创建更新克隆、Identity Config、Drive 上传下载 | 复用已有生命周期示例，每个场景创建自己的测试资源，独立报告结果并执行清理 |
| `session` | 新 Template/Identity/Session、用户事件发送、精确回显、空闲事件、Session SSE checkpoint、Thread SSE 历史续订 | 使用现有 Environment，但不修改原 Template；归档新 Session/Template，clear/delete 新 Identity |
| `realtime-text` | Conversation、WS ready、文本输入、非空助手转写、PCM 输出、audio.done、pong、匹配关闭确认、History 两页 | 创建独立 Identity；关闭连接，归档其 Session，再清理 Identity |
| `realtime-pcm` | 同上，并按实时速度发送固定 PCM、检查非空用户转写 | 消耗 ASR/TTS 和模型额度；不播放音频，不声称验证了扬声器或麦克风 |
| `schedule` | 手动 Schedule 创建、Run 完成、Run 列表和对应 Session 精确回显及正常 idle | 新建 Identity/Template；归档 Schedule、Session、Template，清理 Identity |
| `batch` | 单条 JSONL 上传、`ignore_idle_window=true`、任务完成计数、Task 列表、输出下载和 Session 历史 | 新建 Identity/Template；结束后删除输入文件并清理资源；失败时先取消 Batch 并确认终态 |
| `usage` | 最近 24 个完整上海时区小时的 Identity/Template 聚合，最多两页，数值类型及非负校验、ID 筛选 | 只读；空数据会明确记录，不声称验证了计费准确性 |
| `sa-tokens` | 新签发 Admin/Identity SAT、绑定 Identity 访问、跨 Identity 403、吊销后 401/403 | 需要 SA Key；只操作新建的两个 Identity 和两个 600 秒 SAT；失败也尝试吊销 |
| `realtime-work` | 文本触发后台 `pwd`、相同 Work ID 的 accepted/completed、pong、关闭、Work 与 Message History 分页 | 使用独立 Identity；不要求修改文件或读取其内容，关闭连接后清理 |

SA Key 模式下，普通 HTTP/SSE 使用 `ServiceAccountCredential` 置换的平台 SAT。Realtime 分组额外调用 Forward 管理接口签发 600 秒临时 SAT，业务连接使用该 SAT；无论成功或失败都尝试吊销。直接 SAT 模式使用调用方提供的 Token，不负责吊销它。

各请求设置总超时；Session SSE 最长 3 分钟，Realtime 文本/PCM 连接最长 2 分钟，Work 最长 4 分钟。Schedule/Batch 轮询阶段最多 4 分钟，失败后 Batch 清理另外等待最多 1 分钟。单次请求有独立总超时，因此整个进程可能稍长。验收入口不自动重跑失败场景。复用的资源示例保持各自请求重试策略。资源分组会继续执行相互独立的场景，但任一失败都使整个分组返回非零。

Schedule Run 或 Batch 创建响应丢失、异步任务仍未确认终态时，不删除其输入和执行资源，日志记录 `retained_execution_resources` / `retained_batch_input` 及关联 ID、幂等键供人工核对。Schedule 归档只阻止后续触发，不等于取消已有 Run。已确认终态时，只取消/归档本次独立 Identity 下的 Session。Batch 输出通过独立、不携带 SDK Authorization、禁止自动重定向且限制两 MiB 的下载请求读取。

日志只记录请求/资源/事件 ID、类型、计数和清理结果，不输出 Token、SA Key、签名下载 URL、音频 Base64 或对话正文。HTTP 失败保留状态码、错误码和 request ID；清理异常不能掩盖业务异常。保存原始失败日志，再修正原因进行有标识的复验，不能只保留最后一次通过记录。

Session 流从发送响应返回的用户事件 ID 开始，Thread 流也携带属于该主 Thread 的用户事件游标。无游标时服务端从当前尾部开始，因此不能用它验证已经结束的一轮。检查 `session.status_idle` 及正常 stop reason；关闭订阅不会取消 Session。

Realtime 的 `no_active_response`、`no_content`、`saved`、`saved_interrupted` 都是契约允许的关闭结果。一个结果值本身不证明本轮内容已持久化，验收另查 History 并检查分页。没有物理删除 Conversation 的公开 API；Identity 清理后不再可访问不等于物理删除。Template 归档也会保留记录。

本入口不是 123 个操作的逐接口线上覆盖。Channel/Pairing/QR 真实流程、故障注入续订与长稳测试仍另行验收。每次运行应记录源码 SHA、Java 版本、区域、认证方式、分组结果和原始失败；某次通过不代表后续运行或其他环境也通过。
