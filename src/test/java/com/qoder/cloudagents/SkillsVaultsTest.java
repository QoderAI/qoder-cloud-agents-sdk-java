package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

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
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class SkillsVaultsTest {
  MockWebServer api;
  ForwardClient client;
  static final String ID = "id/?#%", VER = "1786973491821648";

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    api.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("test-token")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
  }

  MockResponse json(String v) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(v);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = api.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  RequestOptions options() {
    return RequestOptions.builder()
        .query("identity_id", "idn_test")
        .idempotencyKey("test-key")
        .header("X-Caller", "test")
        .build();
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {"skillList", "GET", "/skills", "", "{\"data\":[],\"has_more\":false}"},
      {
        "skillGet",
        "GET",
        "/skills/id%2F%3F%23%25",
        "",
        "{\"id\":\"skill_1\",\"latest_version\":null}"
      },
      {
        "skillUpdate",
        "PUT",
        "/skills/id%2F%3F%23%25",
        "{\"description\":\"updated\",\"icon_id\":null,\"metadata\":{\"team\":\"sdk\"}}",
        "{\"id\":\"skill_1\"}"
      },
      {
        "skillDelete",
        "DELETE",
        "/skills/id%2F%3F%23%25",
        "",
        "{\"id\":\"skill_1\",\"type\":\"skill_deleted\",\"deleted\":true}"
      },
      {
        "versionList",
        "GET",
        "/skills/id%2F%3F%23%25/versions",
        "",
        "{\"data\":[],\"has_more\":false}"
      },
      {
        "versionGet",
        "GET",
        "/skills/id%2F%3F%23%25/versions/1786973491821648",
        "",
        "{\"id\":\"skillver_1\",\"version\":\"1786973491821648\"}"
      },
      {
        "versionDelete",
        "DELETE",
        "/skills/id%2F%3F%23%25/versions/1786973491821648",
        "",
        "{\"id\":\"1786973491821648\",\"type\":\"skill_version_deleted\",\"deleted\":true}"
      },
      {"vaultList", "GET", "/vaults", "", "{\"data\":[],\"has_more\":false}"},
      {
        "vaultCreate",
        "POST",
        "/vaults",
        "{\"display_name\":\"test\",\"metadata\":{\"project\":\"sdk\"}}",
        "{\"id\":\"vault_1\"}"
      },
      {"vaultGet", "GET", "/vaults/id%2F%3F%23%25", "", "{\"id\":\"vault_1\"}"},
      {
        "vaultDelete",
        "DELETE",
        "/vaults/id%2F%3F%23%25",
        "",
        "{\"id\":\"vault_1\",\"type\":\"vault_deleted\",\"deleted\":true}"
      },
      {
        "credList",
        "GET",
        "/vaults/id%2F%3F%23%25/credentials",
        "",
        "{\"data\":[],\"has_more\":false}"
      },
      {
        "credCreate",
        "POST",
        "/vaults/id%2F%3F%23%25/credentials",
        "{\"auth\":{\"type\":\"static_bearer\",\"mcp_server_url\":\"https://mcp.example\",\"token\":\"secret-new\"}}",
        "{\"id\":\"vcred_1\",\"auth\":{\"type\":\"static_bearer\",\"mcp_server_url\":\"https://mcp.example\"}}"
      },
      {
        "credGet",
        "GET",
        "/vaults/id%2F%3F%23%25/credentials/id%2F%3F%23%25",
        "",
        "{\"id\":\"vcred_1\"}"
      },
      {
        "credUpdate",
        "POST",
        "/vaults/id%2F%3F%23%25/credentials/id%2F%3F%23%25",
        "{\"auth\":{\"type\":\"environment_variable\",\"secret_value\":\"rotated\"},\"metadata\":{\"remove\":null}}",
        "{\"id\":\"vcred_1\",\"auth\":{\"type\":\"environment_variable\",\"secret_name\":\"API_KEY\"},\"archived_at\":null}"
      },
      {
        "credDelete",
        "DELETE",
        "/vaults/id%2F%3F%23%25/credentials/id%2F%3F%23%25",
        "",
        "{\"id\":\"vcred_1\",\"type\":\"vault_credential_deleted\",\"deleted\":true}"
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
  void contracts(String op, String method, String path, String body, String reply, boolean async)
      throws Exception {
    int status = op.endsWith("Create") ? 201 : 200;
    api.enqueue(
        json(reply)
            .setResponseCode(status)
            .setHeader("x-request-id", "req-contract")
            .setHeader("Deprecation", "true"));
    ApiResponse<?> r = invoke(op, async);
    assertEquals(status, r.statusCode());
    assertEquals("req-contract", r.requestId());
    assertEquals("true", r.header("deprecation"));
    assertEquals(reply, ((JsonModel) r.data()).rawJson());
    assertEquals("req-contract", ((JsonModel) r.data()).requestId());
    RecordedRequest req = take();
    assertEquals(method, req.getMethod());
    assertEquals("/api/v1/forward" + path, req.getRequestUrl().encodedPath());
    assertEquals("idn_test", req.getRequestUrl().queryParameter("identity_id"));
    assertEquals("Bearer test-token", req.getHeader("Authorization"));
    assertEquals("test-key", req.getHeader("Idempotency-Key"));
    assertEquals("test", req.getHeader("X-Caller"));
    if (body.isEmpty()) assertEquals(0, req.getBodySize());
    else
      assertEquals(
          JsonValue.parse(body).toJson(), JsonValue.parse(req.getBody().readUtf8()).toJson());
    if (op.equals("skillGet"))
      assertEquals("true", req.getRequestUrl().queryParameter("include_content"));
  }

  ApiResponse<?> invoke(String op, boolean async) throws Exception {
    RequestOptions o = options();
    SkillUpdateParams su =
        SkillUpdateParams.builder()
            .description("updated")
            .iconId(null)
            .metadata(Collections.singletonMap("team", "sdk"))
            .build();
    VaultCreateParams vc =
        VaultCreateParams.builder()
            .displayName("test")
            .metadata(Collections.singletonMap("project", JsonValue.of("sdk")))
            .build();
    VaultCredentialCreateParams cc =
        VaultCredentialCreateParams.builder()
            .auth(
                VaultCredentialCreateAuth.builder()
                    .type("static_bearer")
                    .mcpServerUrl("https://mcp.example")
                    .token("secret-new")
                    .build())
            .build();
    VaultCredentialUpdateParams cu =
        VaultCredentialUpdateParams.builder()
            .auth(
                VaultCredentialUpdateAuth.builder()
                    .type("environment_variable")
                    .secretValue("rotated")
                    .build())
            .metadata(Collections.singletonMap("remove", null))
            .build();
    SkillRetrieveParams sr = SkillRetrieveParams.builder().includeContent(true).build();
    switch (op) {
      case "skillList":
        return async
            ? client.async().skills().withResponse().list(o).get()
            : client.skills().withResponse().list(o);
      case "skillGet":
        return async
            ? client.async().skills().withResponse().retrieve(ID, sr, o).get()
            : client.skills().withResponse().retrieve(ID, sr, o);
      case "skillUpdate":
        return async
            ? client.async().skills().withResponse().update(ID, su, o).get()
            : client.skills().withResponse().update(ID, su, o);
      case "skillDelete":
        return async
            ? client.async().skills().withResponse().delete(ID, o).get()
            : client.skills().withResponse().delete(ID, o);
      case "versionList":
        return async
            ? client.async().skills().versions().withResponse().list(ID, o).get()
            : client.skills().versions().withResponse().list(ID, o);
      case "versionGet":
        return async
            ? client.async().skills().versions().withResponse().retrieve(ID, VER, o).get()
            : client.skills().versions().withResponse().retrieve(ID, VER, o);
      case "versionDelete":
        return async
            ? client.async().skills().versions().withResponse().delete(ID, VER, o).get()
            : client.skills().versions().withResponse().delete(ID, VER, o);
      case "vaultList":
        return async
            ? client.async().vaults().withResponse().list(o).get()
            : client.vaults().withResponse().list(o);
      case "vaultCreate":
        return async
            ? client.async().vaults().withResponse().create(vc, o).get()
            : client.vaults().withResponse().create(vc, o);
      case "vaultGet":
        return async
            ? client.async().vaults().withResponse().retrieve(ID, o).get()
            : client.vaults().withResponse().retrieve(ID, o);
      case "vaultDelete":
        return async
            ? client.async().vaults().withResponse().delete(ID, o).get()
            : client.vaults().withResponse().delete(ID, o);
      case "credList":
        return async
            ? client.async().vaults().credentials().withResponse().list(ID, o).get()
            : client.vaults().credentials().withResponse().list(ID, o);
      case "credCreate":
        return async
            ? client.async().vaults().credentials().withResponse().create(ID, cc, o).get()
            : client.vaults().credentials().withResponse().create(ID, cc, o);
      case "credGet":
        return async
            ? client.async().vaults().credentials().withResponse().retrieve(ID, ID, o).get()
            : client.vaults().credentials().withResponse().retrieve(ID, ID, o);
      case "credUpdate":
        return async
            ? client.async().vaults().credentials().withResponse().update(ID, ID, cu, o).get()
            : client.vaults().credentials().withResponse().update(ID, ID, cu, o);
      case "credDelete":
        return async
            ? client.async().vaults().credentials().withResponse().delete(ID, ID, o).get()
            : client.vaults().credentials().withResponse().delete(ID, ID, o);
      default:
        throw new AssertionError(op);
    }
  }

  Upload bytes(String name, String content) {
    return Upload.bytes(content.getBytes(StandardCharsets.UTF_8), name, "text/plain");
  }

  @ParameterizedTest
  @CsvSource({"false,false", "false,true", "true,false", "true,true"})
  void multipartHasRepeatedFilesAndRelativePaths(boolean version, boolean async) throws Exception {
    List<Upload> uploads =
        Arrays.asList(
            bytes("code-review/SKILL.md", "---\nname: code-review\ndescription: test\n---\n"),
            bytes("code-review/scripts/run.sh", "echo verified"));
    api.enqueue(
        json(version ? "{\"id\":\"skillver_new\"}" : "{\"id\":\"skill_new\"}")
            .setResponseCode(201));
    RequestOptions o = options().toBuilder().header("Content-Type", "application/json").build();
    if (version) {
      SkillVersionCreateParams p = SkillVersionCreateParams.builder().files(uploads).build();
      assertEquals(
          "skillver_new",
          async
              ? client.async().skills().versions().create(ID, p, o).get().id()
              : client.skills().versions().create(ID, p, o).id());
    } else {
      SkillCreateParams p =
          SkillCreateParams.builder()
              .files(uploads)
              .metadata(Collections.singletonMap("team", JsonValue.of("sdk")))
              .iconId("pic_test")
              .build();
      assertEquals(
          "skill_new",
          async
              ? client.async().skills().create(p, o).get().id()
              : client.skills().create(p, o).id());
    }
    RecordedRequest r = take();
    assertEquals("POST", r.getMethod());
    assertEquals(
        "/api/v1/forward/skills" + (version ? "/id%2F%3F%23%25/versions" : ""),
        r.getRequestUrl().encodedPath());
    assertTrue(r.getHeader("Content-Type").startsWith("multipart/form-data; boundary="));
    String body = r.getBody().readUtf8();
    assertEquals(2, body.split("name=\"files\"", -1).length - 1);
    assertTrue(body.contains("filename=\"code-review/SKILL.md\""));
    assertTrue(body.contains("filename=\"code-review/scripts/run.sh\""));
    assertTrue(body.contains("echo verified"));
    assertFalse(body.contains("Idempotency-Key"));
    assertFalse(body.contains("identity_id"));
    if (!version) {
      assertTrue(body.contains("{\"team\":\"sdk\"}"));
      assertTrue(body.contains("pic_test"));
    }
  }

  @Test
  void legacyUploadAndFilesPrecedenceAreExplicit() throws Exception {
    api.enqueue(json("{\"id\":\"skill_legacy\"}").setHeader("Deprecation", "true"));
    SkillCreateParams p =
        SkillCreateParams.builder().file(bytes("legacy.zip", "zip")).name("ignored").build();
    assertEquals("true", client.skills().withResponse().create(p).header("Deprecation"));
    String legacy = take().getBody().readUtf8();
    assertTrue(legacy.contains("name=\"file\""));
    assertTrue(legacy.contains("ignored"));
    AtomicBoolean unusedClosed = new AtomicBoolean();
    InputStream unused = tracked(unusedClosed);
    api.enqueue(json("{\"id\":\"skill_new\"}"));
    client
        .skills()
        .create(
            SkillCreateParams.builder()
                .file(Upload.stream(unused, "legacy.zip", "application/zip"))
                .addFile(bytes("skill/SKILL.md", "current"))
                .build());
    String body = take().getBody().readUtf8();
    assertFalse(body.contains("legacy.zip"));
    assertFalse(unusedClosed.get());
    unused.close();
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void directVersionDownloadPreservesBinaryAndResponseHeaders(boolean async) throws Exception {
    byte[] zip = {80, 75, 3, 4, 0, (byte) 255, 2, 13, 10};
    api.enqueue(
        new MockResponse()
            .setBody(new Buffer().write(zip))
            .setHeader("Content-Type", "application/zip")
            .setHeader("Content-Disposition", "attachment; filename=review.zip")
            .setHeader("x-request-id", "zip-id"));
    ApiResponse<FileDownload> r =
        async
            ? client.async().skills().versions().withResponse().download(ID, VER, options()).get()
            : client.skills().versions().withResponse().download(ID, VER, options());
    try (FileDownload d = r.data()) {
      assertArrayEquals(zip, read(d.body()));
      assertEquals(-1, d.body().read());
      assertEquals("zip-id", d.requestId());
      assertEquals("application/zip", d.header("content-type"));
    }
    RecordedRequest req = take();
    assertEquals(
        "/api/v1/forward/skills/id%2F%3F%23%25/versions/" + VER + "/content",
        req.getRequestUrl().encodedPath());
    assertEquals("Bearer test-token", req.getHeader("Authorization"));
    assertEquals("idn_test", req.getRequestUrl().queryParameter("identity_id"));
    assertEquals("application/zip", req.getHeader("Accept"));
    assertEquals(1, api.getRequestCount());
  }

  byte[] read(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] b = new byte[128];
    int n;
    while ((n = in.read(b)) != -1) out.write(b, 0, n);
    return out.toByteArray();
  }

  @ParameterizedTest
  @ValueSource(ints = {429, 503})
  void secretRotationNeverRetriesEvenWithKeyAndServerInstruction(int status) throws Exception {
    api.enqueue(
        json("{\"error\":{\"message\":\"rotation failed\"}}")
            .setResponseCode(status)
            .setHeader("x-should-retry", "true")
            .setHeader("Retry-After", "0"));
    api.enqueue(json("{\"id\":\"must-not-be-used\"}"));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () ->
                client
                    .vaults()
                    .credentials()
                    .update(
                        "vault",
                        "cred",
                        VaultCredentialUpdateParams.builder().metadata(null).build(),
                        options().toBuilder().maxRetries(5).build()));
    assertEquals(status, e.statusCode());
    assertEquals(1, api.getRequestCount());
    assertEquals("{\"metadata\":null}", take().getBody().readUtf8());
  }

  @Test
  void oauthAndEnvironmentSecretsSerializeAndPrintSafely() throws Exception {
    VaultCredentialCreateAuth auth =
        VaultCredentialCreateAuth.builder()
            .type("mcp_oauth")
            .mcpServerUrl("https://mcp.example")
            .accessToken("secret-a")
            .expiresAt("later")
            .refresh(
                VaultCredentialOAuthRefresh.builder()
                    .clientId("client")
                    .tokenEndpoint("https://auth.example/token")
                    .refreshToken("secret-r")
                    .scope("read")
                    .tokenEndpointAuth(
                        JsonValue.parse(
                            "{\"type\":\"client_secret_basic\",\"client_secret\":\"secret-c\"}"))
                    .build())
            .build();
    api.enqueue(
        json(
            "{\"id\":\"c\",\"auth\":{\"type\":\"mcp_oauth\",\"mcp_server_url\":\"https://mcp.example\"}}"));
    VaultCredential result =
        client
            .vaults()
            .credentials()
            .create("v", VaultCredentialCreateParams.builder().auth(auth).build());
    JsonValue actual = JsonValue.parse(take().getBody().readUtf8()).get("auth");
    assertEquals("secret-r", actual.get("refresh").get("refresh_token").asString());
    assertEquals(
        "secret-c",
        actual.get("refresh").get("token_endpoint_auth").get("client_secret").asString());
    assertFalse(auth.toString().contains("secret"));
    assertFalse(auth.toString().contains("https:"));
    assertTrue(result.auth().field("access_token").isMissing());
    VaultCredentialUpdateParams patch =
        VaultCredentialUpdateParams.builder()
            .auth(
                VaultCredentialUpdateAuth.builder()
                    .type("mcp_oauth")
                    .expiresAt(null)
                    .refresh(
                        VaultCredentialOAuthRefreshUpdate.builder()
                            .scope(null)
                            .refreshToken("rotated")
                            .build())
                    .build())
            .build();
    assertTrue(patch.auth().field("expires_at").isNull());
    assertTrue(patch.auth().refresh().field("scope").isNull());
    assertTrue(patch.auth().field("access_token").isMissing());
    assertFalse(patch.toString().contains("rotated"));
    api.enqueue(
        json(
            "{\"id\":\"envcred\",\"auth\":{\"type\":\"environment_variable\",\"secret_name\":\"API_KEY\"}}"));
    VaultCredential env =
        client
            .vaults()
            .credentials()
            .create(
                "v",
                VaultCredentialCreateParams.builder()
                    .auth(
                        VaultCredentialCreateAuth.builder()
                            .type("environment_variable")
                            .secretName("API_KEY")
                            .secretValue("secret-env")
                            .build())
                    .build());
    assertEquals(
        "secret-env",
        JsonValue.parse(take().getBody().readUtf8()).get("auth").get("secret_value").asString());
    assertNull(env.auth().mcpServerUrl());
    assertEquals("API_KEY", env.auth().secretName());
  }

  @Test
  void responseCompatibilityAndImmutableInputs() {
    Skill s =
        new Skill(
            JsonValue.parse(
                "{\"id\":\"s\",\"latest_version\":null,\"source\":\"future\",\"new_field\":99,\"binding_info\":{\"agent_template_count\":2}}"),
            "r");
    assertNull(s.latestVersion());
    assertEquals("future", s.source());
    assertEquals(99, s.field("new_field").asLong());
    assertEquals(2, s.bindingInfo().agentTemplateCount());
    SkillVersion v =
        new SkillVersion(
            JsonValue.parse("{\"version\":\"2\",\"content_size\":9007199254740993}"), null);
    assertEquals("2", v.version());
    assertEquals(9007199254740993L, v.contentSize());
    Map<String, JsonValue> map = new LinkedHashMap<>();
    map.put("remove", null);
    VaultCredentialUpdateParams p = VaultCredentialUpdateParams.builder().metadata(map).build();
    map.put("new", JsonValue.of(1));
    assertTrue(p.field("metadata").get("remove").isNull());
    assertTrue(p.field("metadata").get("new").isMissing());
    assertThrows(ConfigurationException.class, () -> VaultCredentialUpdateParams.builder().build());
    assertThrows(ConfigurationException.class, () -> VaultCredentialUpdateAuth.builder().build());
    assertThrows(ConfigurationException.class, () -> SkillCreateParams.builder().build());
    assertThrows(ConfigurationException.class, () -> SkillVersionCreateParams.builder().build());
    assertThrows(
        ConfigurationException.class, () -> SkillUpdateParams.builder().metadata(null).build());
  }

  @Test
  void pageTokensKeepOwnerFiltersAndRejectContradictorySelectors() throws Exception {
    api.enqueue(
        json("{\"data\":[{\"id\":\"skill_1\"}],\"has_more\":true,\"next_page\":\"opaque /+=\"}"));
    api.enqueue(json("{\"data\":[{\"id\":\"skill_2\"}],\"has_more\":false}"));
    SkillListResponse first =
        client
            .skills()
            .list(
                SkillListParams.builder().displayTitle("review").source("custom").limit(1).build(),
                options());
    assertEquals("skill_2", first.getNextPage().data().get(0).id());
    take();
    RecordedRequest next = take();
    assertEquals("opaque /+=", next.getRequestUrl().queryParameter("page"));
    assertEquals("review", next.getRequestUrl().queryParameter("display_title"));
    assertEquals("custom", next.getRequestUrl().queryParameter("source"));
    assertEquals("idn_test", next.getRequestUrl().queryParameter("identity_id"));
    assertThrows(
        ConfigurationException.class,
        () -> SkillListParams.builder().source("custom").beforeId("s").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .skills()
                .list(
                    SkillListParams.builder().source("custom").build(),
                    RequestOptions.builder().query("before_id", "s").build()));
    assertThrows(
        ConfigurationException.class,
        () -> VaultListParams.builder().page("x").afterId("y").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .skills()
                .versions()
                .list("s", RequestOptions.builder().query("after_id", "v").build()));
  }

  @Test
  void versionAndCredentialPaginationUseOpaquePageOrExplicitBackwardCursor() throws Exception {
    api.enqueue(json("{\"data\":[{\"id\":\"v1\"}],\"has_more\":true,\"next_page\":\"vtoken\"}"));
    api.enqueue(json("{\"data\":[],\"has_more\":false}"));
    SkillVersionListResponse page = client.skills().versions().list("s");
    page.getNextPage();
    take();
    assertEquals("vtoken", take().getRequestUrl().queryParameter("page"));
    api.enqueue(
        json(
            "{\"data\":[{\"id\":\"c2\"}],\"has_more\":true,\"first_id\":\"c2\",\"next_page\":\"wrong-direction\"}"));
    api.enqueue(json("{\"data\":[],\"has_more\":false}"));
    client
        .vaults()
        .credentials()
        .list("v", VaultCredentialListParams.builder().beforeId("c3").name("mcp").build())
        .getNextPage();
    take();
    RecordedRequest req = take();
    assertEquals("c2", req.getRequestUrl().queryParameter("before_id"));
    assertEquals("mcp", req.getRequestUrl().queryParameter("name"));
    assertNull(req.getRequestUrl().queryParameter("page"));
  }

  @Test
  void emptyDeleteResponsesAndOldVersionStringsAreSupported() throws Exception {
    for (int i = 0; i < 4; i++) api.enqueue(new MockResponse().setResponseCode(204));
    assertNull(client.skills().delete("s"));
    assertNull(client.skills().versions().delete("s", "1"));
    assertNull(client.vaults().delete("v"));
    assertNull(client.vaults().credentials().delete("v", "c"));
    take();
    assertTrue(take().getPath().endsWith("/versions/1"));
  }

  InputStream tracked(AtomicBoolean closed) {
    return new ByteArrayInputStream(new byte[] {1, 2, 3}) {
      public void close() throws IOException {
        closed.set(true);
        super.close();
      }
    };
  }

  @Test
  void cancellingBeforeCredentialResolutionClosesEveryUpload() throws Exception {
    CompletableFuture<String> token = new CompletableFuture<>();
    AtomicBoolean a = new AtomicBoolean(), b = new AtomicBoolean();
    try (ForwardClient slow =
        ForwardClient.builder().baseUrl(api.url("/").toString()).credential(() -> token).build()) {
      CompletableFuture<?> pending =
          slow.async()
              .skills()
              .create(
                  SkillCreateParams.builder()
                      .addFile(Upload.stream(tracked(a), "s/SKILL.md", "text/plain"))
                      .addFile(Upload.stream(tracked(b), "s/a.txt", "text/plain"))
                      .build());
      assertTrue(pending.cancel(true));
      assertTrue(a.get());
      assertTrue(b.get());
      assertFalse(token.isCancelled());
      token.complete("unused");
      assertEquals(0, api.getRequestCount());
    }
  }

  @Test
  void multipartByteRetriesButAnyStreamDisablesReplay() throws Exception {
    RequestOptions retry = options().toBuilder().maxRetries(2).build();
    api.enqueue(json("{}").setResponseCode(503).setHeader("retry-after-ms", "1"));
    api.enqueue(json("{\"id\":\"s\"}"));
    client
        .skills()
        .create(
            SkillCreateParams.builder()
                .addFile(bytes("s/SKILL.md", "content"))
                .addFile(bytes("s/a.txt", "a"))
                .build(),
            retry);
    assertEquals(take().getBody().readUtf8(), take().getBody().readUtf8());
    AtomicBoolean closed = new AtomicBoolean();
    api.enqueue(json("{}").setResponseCode(429).setHeader("x-should-retry", "true"));
    assertThrows(
        ApiStatusException.class,
        () ->
            client
                .skills()
                .versions()
                .create(
                    "s",
                    SkillVersionCreateParams.builder()
                        .addFile(bytes("s/SKILL.md", "content"))
                        .addFile(Upload.stream(tracked(closed), "s/a.txt", "text/plain"))
                        .build(),
                    retry));
    assertTrue(closed.get());
    assertEquals(3, api.getRequestCount());
  }

  @Test
  void pathUploadPreservesRelativeFilename(@org.junit.jupiter.api.io.TempDir Path temp)
      throws Exception {
    Path path = temp.resolve("SKILL.md");
    java.nio.file.Files.write(path, "content".getBytes(StandardCharsets.UTF_8));
    api.enqueue(json("{\"id\":\"s\"}"));
    client
        .skills()
        .create(
            SkillCreateParams.builder()
                .addFile(Upload.path(path, "s/SKILL.md", "text/markdown"))
                .build());
    assertTrue(take().getBody().readUtf8().contains("filename=\"s/SKILL.md\""));
  }

  @Test
  void binaryErrorsAndPreBodyRetriesUseSharedPolicy() throws Exception {
    api.enqueue(
        json("{\"error\":{\"message\":\"denied\"}}")
            .setResponseCode(403)
            .setHeader("x-request-id", "denied-id"));
    ApiStatusException e =
        assertThrows(ApiStatusException.class, () -> client.skills().versions().download("s", "1"));
    assertEquals(403, e.statusCode());
    assertEquals("denied-id", e.requestId());
    take();
    api.enqueue(json("{}").setResponseCode(503).setHeader("retry-after-ms", "1"));
    api.enqueue(new MockResponse().setBody("PK zip"));
    try (FileDownload d =
        client
            .skills()
            .versions()
            .download("s", "1", RequestOptions.builder().maxRetries(1).build())) {
      assertEquals("PK zip", new String(read(d.body()), StandardCharsets.UTF_8));
    }
    take();
    assertEquals("1", take().getHeader("X-Qoder-Retry-Count"));
  }

  @Test
  void binaryTotalDeadlineIncludesBodyAndNeverReplaysAfterHandoff() throws Exception {
    api.enqueue(new MockResponse().setBody("PK zip").setBodyDelay(2, TimeUnit.SECONDS));
    try (FileDownload d =
        client
            .skills()
            .versions()
            .download(
                "s",
                "1",
                RequestOptions.builder()
                    .maxRetries(3)
                    .totalTimeout(Duration.ofMillis(200))
                    .build())) {
      assertThrows(ApiTimeoutException.class, () -> read(d.body()));
    }
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void binaryIdleTimeoutAndClientCloseReleaseStreams() throws Exception {
    api.enqueue(new MockResponse().setBody("PK zip").setBodyDelay(2, TimeUnit.SECONDS));
    try (FileDownload d =
        client
            .skills()
            .versions()
            .download(
                "s",
                "1",
                RequestOptions.builder().maxRetries(2).timeout(Duration.ofMillis(200)).build())) {
      assertThrows(ApiTimeoutException.class, () -> read(d.body()));
    }
    api.enqueue(new MockResponse().setBody("PK zip").setBodyDelay(2, TimeUnit.SECONDS));
    FileDownload d = client.async().skills().versions().download("s", "1").get();
    client.close();
    assertThrows(ApiUserAbortException.class, () -> read(d.body()));
    d.close();
    assertEquals(2, api.getRequestCount());
  }

  @Test
  void binaryCancelBeforeHeadersAndRedirectsDoNotLeakAuthentication() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<FileDownload> future = client.async().skills().versions().download("s", "1");
    take();
    assertTrue(future.cancel(true));
    try (MockWebServer other = new MockWebServer()) {
      other.start();
      api.enqueue(
          new MockResponse()
              .setResponseCode(302)
              .setHeader("Location", other.url("/zip").toString())
              .setBody("redirect"));
      assertEquals(
          302,
          assertThrows(
                  ApiStatusException.class, () -> client.skills().versions().download("s", "1"))
              .statusCode());
      assertEquals(0, other.getRequestCount());
    }
  }

  @Test
  void binaryClientCloseInterruptsAnActiveReader() throws Exception {
    api.enqueue(new MockResponse().setBody("PK long response").setBodyDelay(3, TimeUnit.SECONDS));
    FileDownload d = client.skills().versions().download("s", "1");
    ExecutorService reader = Executors.newSingleThreadExecutor();
    CountDownLatch started = new CountDownLatch(1);
    try {
      Future<?> reading =
          reader.submit(
              () -> {
                started.countDown();
                assertThrows(ApiUserAbortException.class, () -> read(d.body()));
              });
      assertTrue(started.await(1, TimeUnit.SECONDS));
      client.close();
      reading.get(2, TimeUnit.SECONDS);
    } finally {
      d.close();
      reader.shutdownNow();
    }
  }

  @Test
  void brokenBinaryBodyNeverRestartsDownload() throws Exception {
    api.enqueue(
        new MockResponse()
            .setBody("a deliberately incomplete body")
            .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));
    try (FileDownload d =
        client
            .skills()
            .versions()
            .download("s", "1", RequestOptions.builder().maxRetries(3).build())) {
      assertThrows(ApiConnectionException.class, () -> read(d.body()));
    }
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void secretRotationConnectionFailureNeverReplays() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
    assertThrows(
        ApiConnectionException.class,
        () ->
            client
                .vaults()
                .credentials()
                .update(
                    "v",
                    "c",
                    VaultCredentialUpdateParams.builder().metadata(null).build(),
                    options().toBuilder().maxRetries(5).build()));
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void multipartClaimFailureClosesPreviouslyClaimedStreams() {
    AtomicBoolean closed = new AtomicBoolean();
    Upload one = Upload.stream(tracked(closed), "s/SKILL.md", "text/plain");
    assertThrows(
        ConfigurationException.class,
        () ->
            client.skills().create(SkillCreateParams.builder().addFile(one).addFile(one).build()));
    assertTrue(closed.get());
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void multipartAcceptsListsThatDisallowNullQueriesAndSnapshotsThem() {
    // Java 9+ List.of rejects contains(null); this fixture exercises that contract on Java 8 too.
    List<Upload> values =
        new AbstractList<Upload>() {
          public Upload get(int i) {
            if (i != 0) throw new IndexOutOfBoundsException();
            return bytes("s/SKILL.md", "test");
          }

          public int size() {
            return 1;
          }

          public boolean contains(Object v) {
            return super.contains(Objects.requireNonNull(v));
          }
        };
    SkillCreateParams skill = SkillCreateParams.builder().files(values).build();
    SkillVersionCreateParams version = SkillVersionCreateParams.builder().files(values).build();
    assertEquals(1, skill.files().size());
    assertEquals(1, version.files().size());
    assertThrows(UnsupportedOperationException.class, () -> skill.files().clear());
    assertThrows(
        ConfigurationException.class,
        () -> SkillCreateParams.builder().files(Arrays.asList((Upload) null)));
  }
}
