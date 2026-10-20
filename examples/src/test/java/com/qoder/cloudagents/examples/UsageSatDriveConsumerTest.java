package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class UsageSatDriveConsumerTest {
  MockWebServer api, storage;
  ForwardClient client;
  ByteArrayOutputStream output;
  PrintStream out;

  @BeforeEach
  void setup() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    api = new MockWebServer();
    storage = new MockWebServer();
    api.start();
    storage.start();
    client =
        ForwardClient.builder()
            .baseUrl(api.url("/api/v1/forward").toString())
            .pat("private-pat")
            .defaultHeader("X-Private", "private-header")
            .maxRetries(0)
            .build();
    output = new ByteArrayOutputStream();
    out = new PrintStream(output, true, "UTF-8");
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    api.close();
    storage.close();
    out.close();
  }

  MockResponse json(String value) {
    return new MockResponse().setHeader("Content-Type", "application/json").setBody(value);
  }

  RecordedRequest take(MockWebServer s) throws Exception {
    RecordedRequest r = s.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    return r;
  }

  @Test
  void usageReadsBothGroupsWithoutImplicitFullAccountTraversal() throws Exception {
    api.enqueue(
        json(
            "{\"data\":[{\"identity_id\":\"idn\",\"active_seconds\":0.0001,\"credits\":1.23}],\"has_more\":true,\"last_id\":\"next\"}"));
    api.enqueue(
        json(
            "{\"data\":[{\"template_id\":\"tmpl\",\"active_identities\":2,\"credits\":3.45}],\"has_more\":false}"));
    UsageExample.run(client, "2026-09-14T09:00:00", "2026-09-14T12:00:00", out);
    assertEquals(2, api.getRequestCount());
    assertEquals("2026-09-14T09:00:00", take(api).getRequestUrl().queryParameter("start_at"));
    assertEquals("/api/v1/forward/usage/templates", take(api).getRequestUrl().encodedPath());
    String text = output.toString("UTF-8");
    assertTrue(text.contains("active_seconds=0.0001"));
    assertTrue(text.contains("identity_has_more=true"));
    assertFalse(text.contains("private-pat"));
  }

  String issued(String id, String token, String subject) {
    return "{\"auth_token_id\":\""
        + id
        + "\",\"access_token\":\""
        + token
        + "\",\"subject_type\":\""
        + subject
        + "\",\"credential_id\":\"issuer-key-id\",\"expires_at\":\"2026-10-18T12:00:00Z\"}";
  }

  @Test
  void satExampleUsesIssuedTokensForBusinessAndRevokesBothOnCompletion() throws Exception {
    api.enqueue(json(issued("admin-id", "private-admin-sat", "admin")));
    api.enqueue(json("{\"data\":[]}"));
    api.enqueue(json(issued("identity-id", "private-identity-sat", "identity")));
    api.enqueue(json("{\"data\":[],\"has_more\":false}"));
    api.enqueue(json("{\"revoked\":true}"));
    api.enqueue(json("{\"revoked\":true}"));
    ForwardTokenManagementExample.run(
        client, api.url("/api/v1/forward").toString(), "private-key", "idn", out);
    assertEquals("Bearer private-key", take(api).getHeader("Authorization"));
    assertEquals("Bearer private-admin-sat", take(api).getHeader("Authorization"));
    RecordedRequest bound = take(api);
    assertEquals("/api/v1/forward/identities/idn/service_account_tokens", bound.getPath());
    assertEquals("Bearer private-key", bound.getHeader("Authorization"));
    assertEquals("Bearer private-identity-sat", take(api).getHeader("Authorization"));
    for (String id : Arrays.asList("admin-id", "identity-id")) {
      RecordedRequest revoke = take(api);
      assertEquals("/api/v1/forward/service_account_tokens/" + id + "/revoke", revoke.getPath());
      assertEquals("Bearer private-key", revoke.getHeader("Authorization"));
      JsonValue body = JsonValue.parse(revoke.getBody().readUtf8());
      assertEquals("issuer-key-id", body.get("target_credential_id").asString());
      assertEquals("2026-10-18T12:00:00Z", body.get("target_expires_at").asString());
    }
    assertEquals(6, api.getRequestCount());
    assertFalse(output.toString("UTF-8").contains("private-"));
  }

  @Test
  void satFailureKeepsOriginalAndStillAttemptsBothRevocations() throws Exception {
    api.enqueue(json(issued("admin-id", "private-admin-sat", "admin")));
    api.enqueue(json("{\"data\":[]}"));
    api.enqueue(json(issued("identity-id", "private-identity-sat", "identity")));
    api.enqueue(json("{\"error\":{\"code\":\"permission_denied\"}}").setResponseCode(403));
    api.enqueue(json("{}").setResponseCode(503));
    api.enqueue(json("{\"revoked\":true}"));
    ApiStatusException e =
        assertThrows(
            ApiStatusException.class,
            () ->
                ForwardTokenManagementExample.run(
                    client, api.url("/api/v1/forward").toString(), "private-key", "idn", out));
    assertEquals(403, e.statusCode());
    assertEquals(1, e.getSuppressed().length);
    assertEquals(503, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
    assertEquals(6, api.getRequestCount());
    assertTrue(output.toString("UTF-8").contains("cleanup_failed_token_id=admin-id"));
  }

  void grants() {
    api.enqueue(
        json(
            "{\"url\":\""
                + storage.url("/upload?signature=private-upload")
                + "\",\"method\":\"PUT\",\"headers\":{\"Content-Type\":\"text/plain\",\"X-Storage\":\"required\"}}"));
    api.enqueue(json("{\"entries\":[]}"));
    api.enqueue(
        json(
            "{\"url\":\""
                + storage.url("/download?signature=private-download")
                + "\",\"method\":\"GET\",\"headers\":{\"X-Storage\":\"required\"}}"));
  }

  @Test
  void driveRoundTripUsesSeparateStorageConnectionAndDeletesOnlyOwnedFile() throws Exception {
    grants();
    api.enqueue(json("{\"deleted_count\":1}"));
    storage.enqueue(new MockResponse().setResponseCode(200));
    storage.enqueue(new MockResponse().setBody("QCA Java Drive round trip\n文件\n"));
    DriveExample.run(client, "idn", out);
    RecordedRequest create = take(api);
    String path = JsonValue.parse(create.getBody().readUtf8()).get("path").asString();
    assertTrue(path.startsWith("sdk-java/"));
    assertEquals("idn", create.getRequestUrl().queryParameter("identity_id"));
    RecordedRequest listing = take(api);
    assertEquals(
        path.substring(0, path.lastIndexOf('/')), listing.getRequestUrl().queryParameter("path"));
    assertEquals(path, JsonValue.parse(take(api).getBody().readUtf8()).get("path").asString());
    RecordedRequest delete = take(api);
    assertEquals("DELETE", delete.getMethod());
    assertEquals("/api/v1/forward/drives/entries", delete.getRequestUrl().encodedPath());
    JsonValue body = JsonValue.parse(delete.getBody().readUtf8());
    assertEquals(path, body.get("path").asString());
    assertFalse(body.get("recursive").asBoolean());
    for (String verb : Arrays.asList("PUT", "GET")) {
      RecordedRequest r = take(storage);
      assertEquals(verb, r.getMethod());
      assertEquals("required", r.getHeader("X-Storage"));
      assertNull(r.getHeader("Authorization"));
      assertNull(r.getHeader("X-Private"));
      assertNull(r.getHeader("X-Qoder-Lang"));
      if (verb.equals("PUT"))
        assertEquals("QCA Java Drive round trip\n文件\n", r.getBody().readUtf8());
    }
    assertEquals(4, api.getRequestCount());
    assertFalse(output.toString("UTF-8").contains("private-"));
    assertTrue(output.toString("UTF-8").contains("verified_drive_bytes="));
  }

  @Test
  void storageRedirectIsNotFollowedAndAmbiguousUploadIsCleanedUp() throws Exception {
    api.enqueue(
        json("{\"url\":\"" + storage.url("/upload") + "\",\"method\":\"PUT\",\"headers\":{}}"));
    api.enqueue(json("{\"deleted_count\":0}"));
    storage.enqueue(
        new MockResponse()
            .setResponseCode(307)
            .setHeader("Location", storage.url("/must-not-follow").toString()));
    IOException e = assertThrows(IOException.class, () -> DriveExample.run(client, "idn", out));
    assertTrue(e.getMessage().contains("307"));
    assertEquals(1, storage.getRequestCount());
    assertEquals(2, api.getRequestCount());
    take(api);
    assertEquals("DELETE", take(api).getMethod());
  }

  @Test
  void driveMismatchRetainsCleanupFailureAndRecoveryPath() throws Exception {
    grants();
    api.enqueue(
        json("{\"error\":{\"code\":\"drive_delete_partial\",\"deleted_count\":0}}")
            .setResponseCode(503));
    storage.enqueue(new MockResponse());
    storage.enqueue(new MockResponse().setBody("different content"));
    IOException e = assertThrows(IOException.class, () -> DriveExample.run(client, "idn", out));
    assertEquals("Drive contents differ", e.getMessage());
    assertEquals(1, e.getSuppressed().length);
    assertEquals(503, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
    assertTrue(output.toString("UTF-8").contains("cleanup_failed_drive_path=sdk-java/"));
    assertEquals(4, api.getRequestCount());
  }
}
