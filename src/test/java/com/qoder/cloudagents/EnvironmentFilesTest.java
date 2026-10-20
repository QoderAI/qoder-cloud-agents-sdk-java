package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.*;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class EnvironmentFilesTest {
  MockWebServer api, storage;
  ForwardClient client;

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    storage = new MockWebServer();
    api.start();
    storage.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("test-token")
            .defaultHeader("X-Private", "default-secret")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
    storage.close();
  }

  MockResponse json(String s) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(s);
  }

  RecordedRequest take(MockWebServer s) throws Exception {
    RecordedRequest r = s.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  RequestOptions options() {
    return RequestOptions.builder()
        .idempotencyKey("test-key")
        .query("identity_id", "idn_test")
        .header("X-Caller", "caller-secret")
        .build();
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {
        "envList", "GET", "/environments", "", "{\"data\":[],\"has_more\":false,\"next_page\":null}"
      },
      {
        "envCreate",
        "POST",
        "/environments",
        "{\"name\":\"Test\",\"config\":{\"type\":\"cloud\",\"packages\":{\"pip\":[\"PyYAML==6.0.1\"]},\"setup_script\":\"echo"
            + " ready\"}}",
        "{\"id\":\"env_1\",\"config\":{\"type\":\"cloud\"}}"
      },
      {"envGet", "GET", "/environments/id%2F%3F%23%25", "", "{\"id\":\"env_1\"}"},
      {
        "envUpdate",
        "POST",
        "/environments/id%2F%3F%23%25",
        "{\"metadata\":{\"team\":\"sdk\"}}",
        "{\"id\":\"env_1\"}"
      },
      {
        "envArchive",
        "POST",
        "/environments/id%2F%3F%23%25/archive",
        "",
        "{\"id\":\"env_1\",\"archived_at\":\"now\"}"
      },
      {
        "envDelete",
        "DELETE",
        "/environments/id%2F%3F%23%25",
        "",
        "{\"id\":\"env_1\",\"type\":\"environment_deleted\",\"deleted\":true}"
      },
      {"fileList", "GET", "/files", "", "{\"data\":[],\"has_more\":false,\"next_page\":null}"},
      {"fileGet", "GET", "/files/id%2F%3F%23%25", "", "{\"id\":\"file_1\"}"},
      {
        "fileDelete",
        "DELETE",
        "/files/id%2F%3F%23%25",
        "",
        "{\"id\":\"file_1\",\"type\":\"file_deleted\",\"deleted\":true}"
      },
      {
        "fileUrl",
        "GET",
        "/files/id%2F%3F%23%25/content",
        "",
        "{\"url\":\"https://storage.example/a?signed=1\",\"expires_at\":\"later\",\"filename\":\"a.txt\"}"
      }
    };
    return Arrays.stream(rows)
        .flatMap(
            row ->
                Stream.of(false, true)
                    .map(a -> Arguments.of(row[0], row[1], row[2], row[3], row[4], a)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void routeContracts(
      String op, String method, String path, String body, String reply, boolean async)
      throws Exception {
    int status = op.equals("envCreate") ? 201 : 200;
    api.enqueue(json(reply).setResponseCode(status).setHeader("x-request-id", "api-id"));
    ApiResponse<?> response = invoke(op, async);
    assertEquals(status, response.statusCode());
    assertEquals("api-id", response.requestId());
    assertEquals(reply, ((JsonModel) response.data()).rawJson());
    RecordedRequest req = take(api);
    assertEquals(method, req.getMethod());
    assertEquals("/api/v1/forward" + path, req.getRequestUrl().encodedPath());
    assertEquals("idn_test", req.getRequestUrl().queryParameter("identity_id"));
    assertEquals("test-key", req.getHeader("Idempotency-Key"));
    assertEquals("Bearer test-token", req.getHeader("Authorization"));
    assertEquals(body, req.getBody().readUtf8());
    assertEquals(0, storage.getRequestCount());
  }

  ApiResponse<?> invoke(String op, boolean async) throws Exception {
    String id = "id/?#%";
    RequestOptions o = options();
    Environments.WithResponse e = client.environments().withResponse();
    AsyncEnvironments.WithResponse ae = client.async().environments().withResponse();
    com.qoder.cloudagents.forward.Files.WithResponse f = client.files().withResponse();
    AsyncFiles.WithResponse af = client.async().files().withResponse();
    switch (op) {
      case "envList":
        return async
            ? ae.list(EnvironmentListParams.builder().build(), o).get()
            : e.list(EnvironmentListParams.builder().build(), o);
      case "envCreate":
        EnvironmentCreateParams create =
            EnvironmentCreateParams.builder()
                .name("Test")
                .config(
                    EnvironmentConfig.builder()
                        .type("cloud")
                        .packages(
                            EnvironmentPackages.builder()
                                .pip(Collections.singletonList("PyYAML==6.0.1"))
                                .build())
                        .setupScript("echo ready")
                        .build())
                .build();
        return async ? ae.create(create, o).get() : e.create(create, o);
      case "envGet":
        return async ? ae.retrieve(id, o).get() : e.retrieve(id, o);
      case "envUpdate":
        EnvironmentUpdateParams update =
            EnvironmentUpdateParams.builder()
                .metadata(Collections.singletonMap("team", "sdk"))
                .build();
        return async ? ae.update(id, update, o).get() : e.update(id, update, o);
      case "envArchive":
        return async ? ae.archive(id, o).get() : e.archive(id, o);
      case "envDelete":
        return async ? ae.delete(id, o).get() : e.delete(id, o);
      case "fileList":
        return async
            ? af.list(FileListParams.builder().build(), o).get()
            : f.list(FileListParams.builder().build(), o);
      case "fileGet":
        return async ? af.getMetadata(id, o).get() : f.getMetadata(id, o);
      case "fileDelete":
        return async ? af.delete(id, o).get() : f.delete(id, o);
      case "fileUrl":
        return async ? af.getDownloadUrl(id, o).get() : f.getDownloadUrl(id, o);
      default:
        throw new AssertionError(op);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void multipartEncodingAndImmutableBytes(boolean async) throws Exception {
    byte[] bytes = "中文\nfile contents".getBytes(StandardCharsets.UTF_8);
    Upload upload = Upload.bytes(bytes, "test\"name.txt", "text/plain");
    Arrays.fill(bytes, (byte) 'x');
    FileUploadParams p =
        FileUploadParams.builder()
            .file(upload)
            .name("Display")
            .purpose("session_resource")
            .metadata(Collections.singletonMap("team", "sdk"))
            .build();
    api.enqueue(
        json("{\"id\":\"file_new\",\"size_bytes\":20}")
            .setResponseCode(201)
            .setHeader("x-request-id", "upload-id"));
    ApiResponse<FileMetadata> response =
        async
            ? client.async().files().withResponse().upload(p, options()).get()
            : client.files().withResponse().upload(p, options());
    assertEquals("file_new", response.data().id());
    assertEquals(201, response.statusCode());
    assertEquals("upload-id", response.data().requestId());
    RecordedRequest request = take(api);
    String content = request.getBody().readUtf8();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getHeader("Content-Type").startsWith("multipart/form-data; boundary="));
    assertTrue(content.contains("name=\"file\"; filename=\"test%22name.txt\""));
    assertTrue(content.contains("Content-Type: text/plain"));
    assertTrue(content.contains("中文\nfile contents"));
    assertTrue(content.contains("name=\"metadata\"\r\n\r\n{\"team\":\"sdk\"}"));
    assertTrue(content.contains("name=\"purpose\"\r\n\r\nsession_resource"));
    assertFalse(content.contains("Idempotency-Key"));
    assertFalse(content.contains("identity_id"));
  }

  @Test
  void bytesRetryWithSameMultipartAndKeyButStreamsDoNotReplay() throws Exception {
    api.enqueue(json("{}").setResponseCode(503).setHeader("retry-after-ms", "1"));
    api.enqueue(json("{\"id\":\"file_new\"}"));
    client
        .files()
        .upload(
            FileUploadParams.builder()
                .file(Upload.bytes(new byte[] {1, 2, 3}, "a.txt", "text/plain"))
                .build(),
            options().toBuilder().maxRetries(1).header("Content-Type", "wrong/type").build());
    RecordedRequest first = take(api), second = take(api);
    assertEquals(first.getBody().readByteString(), second.getBody().readByteString());
    assertEquals(first.getHeader("Idempotency-Key"), second.getHeader("Idempotency-Key"));
    assertTrue(first.getHeader("Content-Type").startsWith("multipart/form-data"));
    AtomicBoolean closed = new AtomicBoolean();
    InputStream input =
        new ByteArrayInputStream(new byte[] {1}) {
          public void close() throws IOException {
            closed.set(true);
            super.close();
          }
        };
    Upload oneShot = Upload.stream(input, "a.txt", "text/plain");
    api.enqueue(json("{}").setResponseCode(503).setHeader("x-should-retry", "true"));
    assertThrows(
        ApiStatusException.class,
        () ->
            client
                .files()
                .upload(
                    FileUploadParams.builder().file(oneShot).build(),
                    options().toBuilder().maxRetries(3).build()));
    assertTrue(closed.get());
    assertEquals(3, api.getRequestCount());
    assertThrows(
        ConfigurationException.class,
        () -> client.files().upload(FileUploadParams.builder().file(oneShot).build()));
  }

  @Test
  void pathUploadAndCancellationBeforeCredentialClosesStream() throws Exception {
    Path file = java.nio.file.Files.createTempFile("qca-upload-", ".txt");
    try {
      java.nio.file.Files.write(file, "from disk".getBytes(StandardCharsets.UTF_8));
      api.enqueue(json("{\"id\":\"file_1\"}"));
      client
          .files()
          .upload(FileUploadParams.builder().file(Upload.path(file, "text/plain")).build());
      assertTrue(take(api).getBody().readUtf8().contains("from disk"));
    } finally {
      java.nio.file.Files.delete(file);
    }
    CompletableFuture<String> token = new CompletableFuture<>();
    AtomicBoolean closed = new AtomicBoolean();
    InputStream input =
        new ByteArrayInputStream(new byte[0]) {
          public void close() {
            closed.set(true);
          }
        };
    try (ForwardClient pendingClient =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .credential(() -> token)
            .build()) {
      CompletableFuture<FileMetadata> pending =
          pendingClient
              .async()
              .files()
              .upload(
                  FileUploadParams.builder()
                      .file(Upload.stream(input, "empty.txt", "text/plain"))
                      .build());
      assertTrue(pending.cancel(true));
      assertTrue(closed.get());
      assertFalse(token.isCancelled());
      token.complete("test-token");
      assertEquals(1, api.getRequestCount());
    }
  }

  @Test
  void typedFieldsAndPartialUpdatesRemainDistinct() throws Exception {
    api.enqueue(
        json(
            "{\"id\":\"env_1\",\"config\":{\"type\":\"cloud\",\"setup_script\":\"echo"
                + " hello\",\"packages\":{\"type\":\"packages\",\"apt\":[],\"cargo\":[],\"npm\":[\"pnpm@9\"],\"future\":[]}},\"metadata\":{\"a\":\"b\"},\"archived_at\":null,\"identity_id\":\"idn_1\",\"icon_url\":\"icon\",\"binding_info\":{\"agent_template_count\":9007199254740993},\"future\":1}"));
    Environment env = client.environments().retrieve("env_1");
    assertEquals("echo hello", env.config().setupScript());
    assertEquals("pnpm@9", env.config().packages().npm().get(0));
    assertEquals(9007199254740993L, env.bindingInfo().agentTemplateCount());
    assertTrue(env.field("archived_at").isNull());
    assertEquals(1, env.field("future").asLong());
    assertTrue(EnvironmentUpdateParams.builder().build().field("config").isMissing());
    assertTrue(EnvironmentUpdateParams.builder().config(null).build().field("config").isNull());
    assertEquals(
        "{}",
        EnvironmentUpdateParams.builder()
            .metadata(Collections.emptyMap())
            .build()
            .field("metadata")
            .toJson());
    api.enqueue(
        json(
            "{\"id\":\"file_1\",\"size_bytes\":9007199254740993,\"mime_type\":\"text/plain\",\"downloadable\":true,\"scope\":{\"id\":\"sess_1\",\"type\":\"session\"},\"binding_info\":{\"agent_template_count\":2}}"));
    FileMetadata f = client.files().getMetadata("file_1");
    assertEquals(9007199254740993L, f.sizeBytes());
    assertEquals("sess_1", f.scope().id());
    assertTrue(f.downloadable());
  }

  @Test
  void jsonAnd204DeleteResponsesAreSupported() throws Exception {
    api.enqueue(new MockResponse().setResponseCode(204));
    api.enqueue(new MockResponse().setResponseCode(204));
    assertNull(client.environments().delete("env_1"));
    assertNull(client.async().files().delete("file_1").get());
  }

  @Test
  void pageTokensRetainOptionsAndBackwardCursorDoesNotUseNextPage() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"z\"}],\"has_more\":true,\"last_id\":\"not-the-token\",\"next_page\":\"opaque+/=\"}"));
    api.enqueue(json("{\"data\":[{\"id\":\"a\"}],\"has_more\":false,\"next_page\":null}"));
    EnvironmentListResponse p =
        client.environments().list(EnvironmentListParams.builder().limit(1).build(), options());
    assertEquals("opaque+/=", p.nextPage());
    assertEquals("a", p.getNextPageAsync().get().data().get(0).id());
    take(api);
    RecordedRequest next = take(api);
    assertEquals("opaque+/=", next.getRequestUrl().queryParameter("page"));
    assertNull(next.getRequestUrl().queryParameter("after_id"));
    assertEquals("idn_test", next.getRequestUrl().queryParameter("identity_id"));
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"file_2\"}],\"has_more\":true,\"first_id\":\"file_2\",\"next_page\":\"ignored\"}"));
    api.enqueue(json("{\"data\":[],\"has_more\":false,\"next_page\":null}"));
    client
        .files()
        .list(FileListParams.builder().beforeId("file_3").name("a b").build())
        .getNextPage();
    take(api);
    next = take(api);
    assertEquals("file_2", next.getRequestUrl().queryParameter("before_id"));
    assertEquals("a b", next.getRequestUrl().queryParameter("name"));
  }

  @Test
  void cursorConflictsRepeatsAndScopeContinuationAreExplicit() throws Exception {
    assertThrows(
        ConfigurationException.class,
        () -> EnvironmentListParams.builder().page("a").afterId("b").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .files()
                .list(
                    FileListParams.builder().page("a").build(),
                    RequestOptions.builder().query("before_id", "b").build()));
    assertThrows(
        ConfigurationException.class,
        () -> FileListParams.builder().scopeId("sess_1").afterId("file_1").build());
    api.enqueue(
        json("{\"data\":[{\"id\":\"file_1\"}],\"has_more\":true,\"next_page\":\"repeat\"}"));
    FileListResponse p = client.files().list(FileListParams.builder().page("repeat").build());
    assertThrows(PaginationException.class, p::getNextPage);
    api.enqueue(
        json("{\"data\":[{\"id\":\"file_1\"}],\"has_more\":true,\"next_page\":\"unused\"}"));
    FileListResponse scoped =
        client.files().list(FileListParams.builder().scopeId("sess_1").build());
    assertThrows(PaginationException.class, scoped::getNextPage);
    assertEquals(2, api.getRequestCount());
  }

  void grant() {
    api.enqueue(
        json("{\"url\":"
                + JsonValue.of(storage.url("/blob?signature=secret").toString()).toJson()
                + "}")
            .setHeader("x-request-id", "grant-id"));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void downloadIsStreamingAndStorageIsCredentialIsolated(boolean async) throws Exception {
    grant();
    storage.enqueue(
        new MockResponse().setBody("binary\u0000中文").setHeader("x-request-id", "storage-id"));
    try (FileDownload downloaded =
        async
            ? client.async().files().download("file/1", options()).get()
            : client.files().download("file/1", options())) {
      assertEquals(
          "binary\u0000中文", new String(readAll(downloaded.body()), StandardCharsets.UTF_8));
      assertEquals("grant-id", downloaded.requestId());
      assertEquals(-1, downloaded.body().read());
      assertEquals("storage-id", downloaded.storageRequestId());
    }
    RecordedRequest first = take(api), second = take(storage);
    assertEquals("/api/v1/forward/files/file%2F1/content", first.getRequestUrl().encodedPath());
    assertEquals("Bearer test-token", first.getHeader("Authorization"));
    assertEquals("/blob?signature=secret", second.getPath());
    for (String header :
        Arrays.asList(
            "Authorization",
            "X-Private",
            "X-Caller",
            "Idempotency-Key",
            "X-Qoder-Lang",
            "X-Qoder-Retry-Count")) assertNull(second.getHeader(header), header);
  }

  static byte[] readAll(InputStream input) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int count;
    while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
    return bytes.toByteArray();
  }

  @Test
  void storageRetriesAndRedirectsKeepIsolation() throws Exception {
    grant();
    storage.enqueue(new MockResponse().setResponseCode(503).setHeader("retry-after-ms", "1"));
    storage.enqueue(
        new MockResponse().setResponseCode(302).setHeader("Location", "/final?other=signed"));
    storage.enqueue(new MockResponse().setBody("ok"));
    try (FileDownload d =
        client.files().download("file_1", options().toBuilder().maxRetries(1).build())) {
      assertEquals("ok", new String(readAll(d.body()), StandardCharsets.UTF_8));
    }
    assertEquals(1, api.getRequestCount());
    assertEquals(3, storage.getRequestCount());
    for (int i = 0; i < 3; i++) assertNull(take(storage).getHeader("Authorization"));
  }

  @Test
  void grantAndStorageErrorsStayVisibleAndUnsafeUrlsAreRejected() throws Exception {
    api.enqueue(json("{}").setResponseCode(403));
    assertEquals(
        403,
        assertThrows(ApiStatusException.class, () -> client.files().download("file_1"))
            .statusCode());
    api.enqueue(json("{\"url\":\"http://user:secret@localhost/a\"}"));
    assertThrows(ResponseValidationException.class, () -> client.files().download("file_1"));
    api.enqueue(json("{\"url\":\"file:///tmp/not-a-download\"}"));
    assertThrows(ResponseValidationException.class, () -> client.files().download("file_1"));
    assertEquals(0, storage.getRequestCount());
    grant();
    storage.enqueue(
        json("{\"error\":{\"message\":\"expired\"}}")
            .setResponseCode(403)
            .setHeader("x-request-id", "storage-error"));
    ApiStatusException e =
        assertThrows(ApiStatusException.class, () -> client.files().download("file_1"));
    assertEquals(403, e.statusCode());
    assertEquals("storage-error", e.requestId());
  }

  @Test
  void totalDeadlineCoversBodyAfterHeadersAndDoesNotReplay() throws Exception {
    grant();
    storage.enqueue(new MockResponse().setBody("late").setBodyDelay(2, TimeUnit.SECONDS));
    try (FileDownload d =
        client
            .files()
            .download(
                "file_1",
                RequestOptions.builder()
                    .totalTimeout(Duration.ofMillis(200))
                    .maxRetries(2)
                    .build())) {
      assertThrows(ApiTimeoutException.class, () -> readAll(d.body()));
    }
    assertEquals(1, storage.getRequestCount());
  }

  @Test
  void bodyReadFailureDoesNotRestartDownload() throws Exception {
    grant();
    storage.enqueue(
        new MockResponse()
            .setBody("incomplete")
            .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
    try (FileDownload d =
        client.files().download("file_1", RequestOptions.builder().maxRetries(3).build())) {
      assertThrows(ApiConnectionException.class, () -> readAll(d.body()));
    }
    assertEquals(1, storage.getRequestCount());
  }

  @Test
  void cancelPendingDownloadAndClientCloseReleaseActiveBody() throws Exception {
    grant();
    storage.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<FileDownload> pending = client.async().files().download("file_1");
    take(storage);
    assertTrue(pending.cancel(true));
    assertTrue(pending.isCancelled());
    grant();
    storage.enqueue(new MockResponse().setBody("pending").setBodyDelay(2, TimeUnit.SECONDS));
    FileDownload d = client.files().download("file_1");
    client.close();
    assertThrows(ApiUserAbortException.class, () -> d.body().read());
    d.close();
  }
}
