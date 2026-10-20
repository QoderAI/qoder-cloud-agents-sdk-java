package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.auth.*;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.*;
import com.qoder.cloudagents.forward.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class UsageSatDrivesTest {
  MockWebServer api;
  ForwardClient client;
  static final String KEY = "synthetic-sa-key", ID = "opaque/?#%";

  @BeforeEach
  void setup() throws Exception {
    api = new MockWebServer();
    api.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("test-pat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
  }

  MockResponse json(String body) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
  }

  RecordedRequest take() throws Exception {
    RecordedRequest r = api.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  static UsageListParams usage() {
    return UsageListParams.builder()
        .startAt("2026-09-14T09:00:00")
        .endAt("2026-09-14T12:00:00")
        .build();
  }

  static ServiceAccountTokenCreateParams issue() {
    return ServiceAccountTokenCreateParams.builder()
        .ttlSeconds(3600)
        .metadata(Collections.singletonMap("purpose", "test"))
        .build();
  }

  static Stream<Arguments> routes() {
    String[][] rows = {
      {
        "identities",
        "GET",
        "/usage/identities",
        "",
        "{\"data\":[{\"identity_id\":\"idn_1\",\"active_seconds\":720.222,\"credits\":3.1}],\"has_more\":false,\"start_at\":\"2026-09-14T09:00:00\",\"end_at\":\"2026-09-14T12:00:00\"}"
      },
      {
        "templates",
        "GET",
        "/usage/templates",
        "",
        "{\"data\":[{\"template_id\":\"tmpl_1\",\"active_identities\":2,\"session_count\":3}],\"has_more\":false}"
      },
      {
        "admin",
        "POST",
        "/service_account_tokens",
        "{\"ttl_seconds\":3600,\"metadata\":{\"purpose\":\"test\"}}",
        "{\"access_token\":\"secret-sat\",\"auth_token_id\":\"auth_1\",\"subject_type\":\"admin\",\"expires_in\":3600}"
      },
      {
        "identity",
        "POST",
        "/identities/opaque%2F%3F%23%25/service_account_tokens",
        "{\"ttl_seconds\":3600,\"metadata\":{\"purpose\":\"test\"}}",
        "{\"access_token\":\"secret-sat\",\"auth_token_id\":\"auth_1\",\"subject_type\":\"identity\",\"identity_id\":\"idn_1\"}"
      },
      {
        "revoke",
        "POST",
        "/service_account_tokens/opaque%2F%3F%23%25/revoke",
        "{\"target_credential_id\":\"sak_1\",\"target_expires_at\":\"2026-10-17T12:00:00Z\",\"reason\":\"test\"}",
        "{\"revoked\":true}"
      },
      {
        "entries",
        "GET",
        "/drives/entries",
        "",
        "{\"entries\":[{\"path\":\"z\",\"type\":\"file\",\"size\":0},{\"path\":\"a\",\"type\":\"directory\"}],\"next_page_token\":\"a/b+c=\"}"
      },
      {
        "upload",
        "POST",
        "/drives/upload-url",
        "{\"path\":\"docs/a.txt\",\"content_type\":\"text/plain\"}",
        "{\"url\":\"https://storage.invalid/upload?signature=secret\",\"method\":\"PUT\",\"headers\":{\"Content-Type\":\"text/plain\"},\"expires_at\":\"2026-10-17T12:00:00Z\"}"
      },
      {
        "download",
        "POST",
        "/drives/download-url",
        "{\"path\":\"docs/a.txt\"}",
        "{\"url\":\"https://storage.invalid/download?signature=secret\",\"method\":\"GET\",\"headers\":{},\"size\":5,\"etag\":\"etag\",\"content_type\":\"text/plain\"}"
      },
      {
        "delete",
        "DELETE",
        "/drives/entries",
        "{\"path\":\"docs/a.txt\",\"recursive\":false}",
        "{\"deleted_count\":0}"
      },
      {"clear", "DELETE", "/drives", "", "{\"deleted_count\":12}"}
    };
    return Arrays.stream(rows)
        .flatMap(
            r -> Stream.of(false, true).map(a -> Arguments.of(r[0], r[1], r[2], r[3], r[4], a)));
  }

  @ParameterizedTest
  @MethodSource("routes")
  void contracts(String op, String method, String path, String body, String reply, boolean async)
      throws Exception {
    boolean sat = Arrays.asList("admin", "identity", "revoke").contains(op);
    int status = op.equals("admin") || op.equals("identity") ? 201 : 200;
    api.enqueue(
        json(reply)
            .setResponseCode(status)
            .setHeader("x-request-id", "req-operation")
            .setHeader("X-Contract", "retained"));
    ApiResponse<?> response = invoke(op, async);
    assertEquals(status, response.statusCode());
    assertEquals("req-operation", response.requestId());
    assertEquals("retained", response.header("X-Contract"));
    JsonModel model = (JsonModel) response.data();
    assertEquals(reply, model.rawJson());
    assertEquals("req-operation", model.requestId());
    RecordedRequest r = take();
    assertEquals(method, r.getMethod());
    assertEquals("/api/v1/forward" + path, r.getRequestUrl().encodedPath());
    assertEquals("Bearer " + (sat ? KEY : "test-pat"), r.getHeader("Authorization"));
    assertEquals("contract-key", r.getHeader("Idempotency-Key"));
    assertEquals("custom", r.getRequestUrl().queryParameter("extension"));
    if (body.isEmpty()) assertEquals(0, r.getBodySize());
    else
      assertEquals(
          JsonValue.parse(body).toJson(), JsonValue.parse(r.getBody().readUtf8()).toJson());
    if (op.equals("identities") || op.equals("templates")) {
      assertEquals("2026-09-14T09:00:00", r.getRequestUrl().queryParameter("start_at"));
      assertEquals("2026-09-14T12:00:00", r.getRequestUrl().queryParameter("end_at"));
    }
    if (Arrays.asList("entries", "upload", "download", "delete", "clear").contains(op))
      assertEquals("idn_1", r.getRequestUrl().queryParameter("identity_id"));
    assertFalse(model.toString().contains("secret"));
  }

  ApiResponse<?> invoke(String op, boolean a) throws Exception {
    RequestOptions o =
        RequestOptions.builder()
            .idempotencyKey("contract-key")
            .query("extension", "custom")
            .build();
    ServiceAccountTokenRevokeParams rev =
        ServiceAccountTokenRevokeParams.builder()
            .targetCredentialId("sak_1")
            .targetExpiresAt("2026-10-17T12:00:00Z")
            .reason("test")
            .build();
    DriveListEntriesParams list =
        DriveListEntriesParams.builder().identityId("idn_1").path("docs/sub").limit(2).build();
    DriveCreateUploadUrlParams up =
        DriveCreateUploadUrlParams.builder()
            .identityId("idn_1")
            .path("docs/a.txt")
            .contentType("text/plain")
            .build();
    DriveCreateDownloadUrlParams down =
        DriveCreateDownloadUrlParams.builder().identityId("idn_1").path("docs/a.txt").build();
    DriveDeleteEntryParams del =
        DriveDeleteEntryParams.builder()
            .identityId("idn_1")
            .path("docs/a.txt")
            .recursive(false)
            .build();
    DriveClearParams clear = DriveClearParams.builder().identityId("idn_1").build();
    switch (op) {
      case "identities":
        return a
            ? client.async().usage().withResponse().listIdentities(usage(), o).get()
            : client.usage().withResponse().listIdentities(usage(), o);
      case "templates":
        return a
            ? client.async().usage().withResponse().listTemplates(usage(), o).get()
            : client.usage().withResponse().listTemplates(usage(), o);
      case "admin":
        return a
            ? client.async().serviceAccountTokens().withResponse().create(KEY, issue(), o).get()
            : client.serviceAccountTokens().withResponse().create(KEY, issue(), o);
      case "identity":
        return a
            ? client
                .async()
                .identities()
                .serviceAccountTokens()
                .withResponse()
                .create(ID, KEY, issue(), o)
                .get()
            : client.identities().serviceAccountTokens().withResponse().create(ID, KEY, issue(), o);
      case "revoke":
        return a
            ? client.async().serviceAccountTokens().withResponse().revoke(ID, KEY, rev, o).get()
            : client.serviceAccountTokens().withResponse().revoke(ID, KEY, rev, o);
      case "entries":
        return a
            ? client.async().drives().withResponse().listEntries(list, o).get()
            : client.drives().withResponse().listEntries(list, o);
      case "upload":
        return a
            ? client.async().drives().withResponse().createUploadUrl(up, o).get()
            : client.drives().withResponse().createUploadUrl(up, o);
      case "download":
        return a
            ? client.async().drives().withResponse().createDownloadUrl(down, o).get()
            : client.drives().withResponse().createDownloadUrl(down, o);
      case "delete":
        return a
            ? client.async().drives().withResponse().deleteEntry(del, o).get()
            : client.drives().withResponse().deleteEntry(del, o);
      case "clear":
        return a
            ? client.async().drives().withResponse().clear(clear, o).get()
            : client.drives().withResponse().clear(clear, o);
      default:
        throw new AssertionError(op);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void usagePaginationKeepsWindowFiltersAndDirection(boolean backwards) throws Exception {
    List<String> ids = new ArrayList<>(Arrays.asList("idn_1", "idn_2"));
    UsageListParams.Builder b =
        UsageListParams.builder()
            .startAt("2026-09-14T09:00:00")
            .endAt("2026-09-14T12:00:00")
            .identityIds(ids)
            .templateIds(Arrays.asList("t1", "t2"))
            .limit(2);
    if (backwards) b.beforeId("previous");
    UsageListParams p = b.build();
    ids.add("late");
    api.enqueue(
        json(
            "{\"data\":[{\"identity_id\":\"z\",\"active_seconds\":0.0001,\"credits\":1.23}],\"has_more\":true,\"first_id\":\"first\",\"last_id\":\"last\"}"));
    api.enqueue(json("{\"data\":[],\"has_more\":false,\"first_id\":null,\"last_id\":null}"));
    IdentityUsageListResponse first =
        client.usage().listIdentities(p, RequestOptions.builder().query("extension", "x").build());
    assertEquals(new BigDecimal("0.0001"), first.data().get(0).activeSeconds());
    assertEquals(new BigDecimal("1.23"), first.data().get(0).credits());
    IdentityUsageListResponse last = first.getNextPageAsync().get();
    assertFalse(last.hasNextPage());
    assertThrows(PaginationException.class, last::getNextPage);
    take();
    RecordedRequest second = take();
    assertEquals("idn_1,idn_2", second.getRequestUrl().queryParameter("identity_ids"));
    assertEquals("t1,t2", second.getRequestUrl().queryParameter("template_ids"));
    assertEquals("x", second.getRequestUrl().queryParameter("extension"));
    assertEquals("2026-09-14T09:00:00", second.getRequestUrl().queryParameter("start_at"));
    assertEquals(
        backwards ? "first" : "last",
        second.getRequestUrl().queryParameter(backwards ? "before_id" : "after_id"));
    assertNull(second.getRequestUrl().queryParameter(backwards ? "after_id" : "before_id"));
  }

  @Test
  void usageRejectsLegacyAndInvalidWindowsEvenFromRequestOverrides() {
    for (String v :
        Arrays.asList(
            "2026-02-30T00:00:00",
            "2026-09-14T09:01:00",
            "2026-09-14T09:00:00Z",
            "2026-09-14T13:00:00"))
      assertThrows(
          ConfigurationException.class,
          () -> UsageListParams.builder().startAt(v).endAt("2026-09-14T12:00:00").build());
    assertThrows(
        ConfigurationException.class,
        () ->
            UsageListParams.builder()
                .startAt("2026-01-01T00:00:00")
                .endAt("2026-02-02T00:00:00")
                .build());
    for (String key : Arrays.asList("start_time", "end_time", "timezone", "page", "start_at"))
      assertThrows(
          ConfigurationException.class,
          () ->
              client
                  .usage()
                  .listIdentities(usage(), RequestOptions.builder().query(key, "").build()));
    assertThrows(
        ConfigurationException.class,
        () ->
            client
                .usage()
                .listTemplates(
                    usage(),
                    RequestOptions.builder()
                        .query("after_id", "a")
                        .query("before_id", "b")
                        .build()));
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void driveKeepsOrderAndOpaqueTokensAndEncodesOnlyPathSlashesSpecially() throws Exception {
    api.enqueue(
        json(
            "{\"entries\":[{\"path\":\"z\",\"size\":0},{\"path\":\"a\",\"type\":\"directory\"}],\"next_page_token\":\"a/b+c=&%\"}"));
    api.enqueue(json("{\"entries\":[]}"));
    DriveListEntriesResponse first =
        client
            .drives()
            .listEntries(
                DriveListEntriesParams.builder().identityId("idn").path("original").build(),
                RequestOptions.builder().query("path", "目录/a b+~*").build());
    assertEquals("z", first.entries().get(0).path());
    assertEquals(Long.valueOf(0), first.entries().get(0).size());
    assertNull(first.entries().get(1).size());
    assertTrue(first.hasNextPage());
    DriveListEntriesResponse last =
        client
            .drives()
            .listEntries(
                DriveListEntriesParams.builder()
                    .identityId("idn")
                    .path("目录/a b+~*")
                    .pageToken(first.nextPageToken())
                    .build());
    assertFalse(last.hasNextPage());
    RecordedRequest r = take();
    assertTrue(r.getPath().contains("path=%E7%9B%AE%E5%BD%95/a+b%2B%7E*"));
    r = take();
    assertEquals("a/b+c=&%", r.getRequestUrl().queryParameter("page_token"));
    assertTrue(r.getPath().contains("page_token=a%2Fb%2Bc%3D%26%25"));
  }

  @Test
  void driveOptionalFieldsAndLegacyDeleteMessagesRemainReadable() throws Exception {
    api.enqueue(json("{\"message\":\"deleted\",\"future\":true}"));
    DriveDeleteEntryResponse r =
        client
            .drives()
            .deleteEntry(DriveDeleteEntryParams.builder().identityId("idn").path("a").build());
    assertNull(r.deletedCount());
    assertTrue(r.field("future").asBoolean());
    DriveDownloadUrl grant =
        new DriveDownloadUrl(
            JsonValue.parse("{\"url\":\"private-url\",\"method\":\"GET\",\"headers\":{}}"), "r");
    assertNull(grant.size());
    assertNull(grant.etag());
    assertFalse(grant.toString().contains("private-url"));
  }

  @Test
  void drivePartialDeletionPreservesErrorDetailsWithoutRetry() throws Exception {
    api.enqueue(
        json("{\"request_id\":\"req-partial\",\"error\":{\"code\":\"drive_delete_partial\",\"deleted_count\":7}}")
            .setResponseCode(503));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () -> client.drives().clear(DriveClearParams.builder().identityId("idn").build()));
    assertEquals("drive_delete_partial", e.code());
    assertEquals(7, e.body().get("error").get("deleted_count").asLong());
    assertEquals("req-partial", e.requestId());
    assertEquals(1, api.getRequestCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {429, 503})
  void tokenManagementNeverRetriesEvenWithOverrides(int status) throws Exception {
    for (String op : Arrays.asList("admin", "identity", "revoke")) {
      api.enqueue(
          json("{\"error\":{\"message\":\"private-key-echo\"}}")
              .setResponseCode(status)
              .setHeader("x-should-retry", "true")
              .setHeader("Retry-After", "0"));
      RequestOptions o =
          RequestOptions.builder().maxRetries(4).idempotencyKey("force-retry").build();
      assertThrows(
          ApiStatusException.class,
          () -> {
            if (op.equals("admin")) client.serviceAccountTokens().create(KEY, issue(), o);
            else if (op.equals("identity"))
              client.identities().serviceAccountTokens().create("idn", KEY, issue(), o);
            else
              client
                  .serviceAccountTokens()
                  .revoke("token", KEY, ServiceAccountTokenRevokeParams.builder().build(), o);
          });
      take();
    }
    assertEquals(3, api.getRequestCount());
  }

  @Test
  void perCallKeyBypassesProviderAndDoesNotAffectLaterBusinessAuthentication() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    Credential credential =
        () -> {
          calls.incrementAndGet();
          return CompletableFuture.completedFuture("business-sat");
        };
    try (ForwardClient c =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .authMode(AuthMode.SA)
            .credential(credential)
            .build()) {
      api.enqueue(json("{\"access_token\":\"issued\",\"subject_type\":\"admin\"}"));
      api.enqueue(json("{\"data\":[]}"));
      c.serviceAccountTokens().create(KEY, issue());
      assertEquals(0, calls.get());
      c.models().list();
      assertEquals(1, calls.get());
      assertEquals("Bearer " + KEY, take().getHeader("Authorization"));
      assertEquals("Bearer business-sat", take().getHeader("Authorization"));
    }
  }

  @Test
  void missingBusinessCredentialStillAllowsExplicitKeyButFailsBusinessLocally() throws Exception {
    Credential absent =
        Authentication.resolve(AuthMode.SA, null, null, null, "localhost", name -> null);
    try (ForwardClient c =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .credential(absent)
            .build()) {
      api.enqueue(json("{\"access_token\":\"issued\"}"));
      c.serviceAccountTokens().create(KEY, ServiceAccountTokenCreateParams.builder().build());
      assertThrows(ConfigurationException.class, () -> c.models().list());
      assertEquals(1, api.getRequestCount());
      assertEquals("{}", take().getBody().readUtf8());
    }
  }

  @Test
  void keysNeverSerializeAndInvalidKeysFailBeforeNetwork() {
    for (String key : Arrays.asList("", "bad\r\nheader", "has space", null)) {
      ConfigurationException e =
          assertThrows(
              ConfigurationException.class,
              () -> client.serviceAccountTokens().create(key, issue()));
      assertFalse(e.getMessage().contains("bad\r"));
    }
    assertFalse(issue().rawJson().contains(KEY));
    assertFalse(issue().toString().contains("purpose"));
    assertThrows(
        ConfigurationException.class,
        () -> ServiceAccountTokenCreateParams.builder().ttlSeconds(0));
    assertThrows(
        ConfigurationException.class,
        () -> ServiceAccountTokenCreateParams.builder().ttlSeconds(43201));
    assertThrows(
        ConfigurationException.class,
        () -> client.identities().serviceAccountTokens().create("", KEY, issue()));
    assertEquals(0, api.getRequestCount());
  }

  @Test
  void asyncTokenCancellationAndCloseReachTheSharedTransport() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<ServiceAccountToken> f =
        client.async().serviceAccountTokens().create(KEY, issue());
    take();
    assertTrue(f.cancel(true));
    assertTrue(f.isCancelled());
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    CompletableFuture<DriveClearResponse> d =
        client.async().drives().clear(DriveClearParams.builder().identityId("idn").build());
    take();
    client.close();
    ExecutionException e = assertThrows(ExecutionException.class, () -> d.get(2, TimeUnit.SECONDS));
    assertTrue(e.getCause() instanceof ApiUserAbortException);
  }

  @Test
  void metadataAndDriveBodiesAreSnapshotsAndPreserveExplicitFalseNull() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("audit", Collections.singletonMap("nested", true));
    ServiceAccountTokenCreateParams p =
        ServiceAccountTokenCreateParams.builder().metadata(m).build();
    m.put("late", "change");
    assertTrue(p.field("metadata").get("late").isMissing());
    DriveCreateUploadUrlParams upload =
        DriveCreateUploadUrlParams.builder().identityId("idn").path("a").contentType(null).build();
    assertTrue(upload.field("content_type").isNull());
    assertTrue(upload.field("identity_id").isMissing());
    assertThrows(
        ConfigurationException.class,
        () -> DriveCreateDownloadUrlParams.builder().identityId("idn").path("").build());
    assertThrows(
        ConfigurationException.class, () -> DriveListEntriesParams.builder().path("").build());
  }

  @Test
  void tokenRequestsWithDifferentKeysDoNotShareAuthentication() throws Exception {
    api.enqueue(json("{\"auth_token_id\":\"one\"}"));
    api.enqueue(json("{\"auth_token_id\":\"two\"}"));
    CompletableFuture<ServiceAccountToken> a =
        client.async().serviceAccountTokens().create("key-one", issue());
    CompletableFuture<ServiceAccountToken> b =
        client.async().identities().serviceAccountTokens().create("idn", "key-two", issue());
    a.get();
    b.get();
    Set<String> keys = new HashSet<>();
    keys.add(take().getHeader("Authorization"));
    keys.add(take().getHeader("Authorization"));
    assertEquals(new HashSet<>(Arrays.asList("Bearer key-one", "Bearer key-two")), keys);
  }

  @Test
  void tokenConnectionFailureIsNotReplayed() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
    assertThrows(
        ApiConnectionException.class,
        () ->
            client
                .serviceAccountTokens()
                .create(
                    KEY,
                    issue(),
                    RequestOptions.builder().maxRetries(5).idempotencyKey("key").build()));
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void tokenTotalDeadlineIsEnforced() throws Exception {
    api.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
    assertThrows(
        ApiTimeoutException.class,
        () ->
            client
                .serviceAccountTokens()
                .create(
                    KEY,
                    issue(),
                    RequestOptions.builder().totalTimeout(Duration.ofMillis(120)).build()));
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void usageRepeatedCursorDoesNotLoop() throws Exception {
    api.enqueue(
        json("{\"data\":[{\"template_id\":\"t\"}],\"has_more\":true,\"last_id\":\"same\"}"));
    TemplateUsageListResponse p =
        client
            .usage()
            .listTemplates(
                UsageListParams.builder()
                    .startAt("2026-09-14T09:00:00")
                    .endAt("2026-09-14T12:00:00")
                    .afterId("same")
                    .build());
    assertThrows(PaginationException.class, p::getNextPage);
    assertEquals(1, api.getRequestCount());
  }

  @Test
  void driveUsesStaticSatWithExplicitIdentityAndDoesNotContactStorage() throws Exception {
    try (ForwardClient c =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .authMode(AuthMode.SA)
            .accessToken("identity-sat")
            .build()) {
      api.enqueue(
          json("{\"url\":\"https://storage.invalid/private\",\"method\":\"PUT\",\"headers\":{}}"));
      c.drives()
          .createUploadUrl(
              DriveCreateUploadUrlParams.builder().identityId("idn-bound").path("owned/a").build());
      RecordedRequest r = take();
      assertEquals("Bearer identity-sat", r.getHeader("Authorization"));
      assertEquals("idn-bound", r.getRequestUrl().queryParameter("identity_id"));
      assertEquals(1, api.getRequestCount());
    }
  }

  @Test
  void nestedErrorRequestIdIsAvailableAndHeaderTakesPrecedence() {
    JsonValue body =
        JsonValue.parse(
            "{\"error\":{\"request_id\":\"nested\",\"code\":\"idempotency_key_reused\"}}");
    assertEquals(
        "nested",
        new ApiStatusException(new ApiResponse<>(body, 409, Collections.emptyMap())).requestId());
    assertEquals(
        "header",
        new ApiStatusException(
                new ApiResponse<>(
                    body,
                    409,
                    Collections.singletonMap("x-request-id", Collections.singletonList("header"))))
            .requestId());
  }
}
