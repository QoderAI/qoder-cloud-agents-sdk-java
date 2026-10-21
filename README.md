# Qoder Cloud Agents Java SDK

Java 8+ SDK for the **Forward API**, with typed synchronous and asynchronous clients. Supports PAT/SA authentication, pagination, retries, Session/Thread SSE and Realtime text/audio. Managed APIs are outside this SDK's scope.

[Changelog](CHANGELOG.md) · [Releases](https://github.com/QoderAI/qoder-cloud-agents-sdk-java/releases) · [Maven Central](https://central.sonatype.com/artifact/com.qoder.sdk/qoder-cloud-agents-sdk)

## Installation

Add the Maven dependency; published versions require no local SDK build or additional repository configuration.

```xml
<dependency>
  <groupId>com.qoder.sdk</groupId>
  <artifactId>qoder-cloud-agents-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

Compatible with Java 8, 11, 17, 21 and 25. See the [changelog](CHANGELOG.md) for upgrading from the RC or earlier internal coordinates.

## Quick start with PAT

Set `QODER_PAT`, then create a client:

```java
import com.qoder.cloudagents.ForwardClient;

try (ForwardClient client = ForwardClient.builder()
        .pat(System.getenv("QODER_PAT"))
        .build()) {
    System.out.println(client.models().list().data().size());
}
```

The default endpoint is Global: `https://api.qoder.com/api/v1/forward`. For CN, add `.baseUrl("https://api.qoder.com.cn/api/v1/forward")` to the client builder.

## Service Account

Set `QODER_SA_KEY` and explicitly configure a credential:

```java
import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.Region;
import com.qoder.cloudagents.auth.ServiceAccountCredential;

try (ServiceAccountCredential credential = ServiceAccountCredential.builder()
        .saKey(System.getenv("QODER_SA_KEY"))
        .region(Region.GLOBAL)
        .scopes("qca.access", "forward.access")
        .build();
     ForwardClient client = ForwardClient.builder().credential(credential).build()) {
    System.out.println(client.models().list().data().size());
}
```

The credential exchanges the SA Key for a SAT, then caches and refreshes it. For CN, use both `Region.CN` and the CN BaseURL. See [client configuration](docs/client-configuration.md) for static SATs, authentication precedence, timeouts, retries and cancellation.

## Documentation

- [Runnable resource examples and shared authentication setup](examples/README.md)
- [Forward API coverage and resource guides](docs/forward-api-coverage.md)
- [Session/Thread SSE](docs/session-streaming.md) and [Realtime text, audio and History](docs/realtime.md)
- [Usage, Forward SAT management and Drive](docs/usage-sat-drive.md)
- [Live testing](docs/live-testing.md)

For source builds and contributions, see [CONTRIBUTING.md](CONTRIBUTING.md). Maintainers can follow the [maintenance](docs/maintaining.md) and [release](docs/releasing.md) guides.
