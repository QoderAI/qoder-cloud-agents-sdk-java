package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(15)
class SkillVaultConsumerTest {
  final List<String> calls = new ArrayList<>();
  final List<String> secrets = new ArrayList<>();

  MockResponse ok(String s) {
    return new MockResponse().setBody(s).setHeader("x-request-id", "example-request");
  }

  MockResponse error(int s) {
    return ok("{\"error\":{\"message\":\"test failure\"}}").setResponseCode(s);
  }

  Dispatcher dispatch(boolean corrupt, boolean rotateFails, boolean cleanupFails) throws Exception {
    // These describe the canonical stored ZIP, not the rewrapped download bytes.
    String version =
        "{\"id\":\"skillver_created\",\"version\":\"1786973491821648\",\"content_size\":4,\"content_sha256\":\"canonical-object-hash\"}";
    return new Dispatcher() {
      String name;

      public MockResponse dispatch(RecordedRequest r) {
        String path = r.getRequestUrl().encodedPath().substring("/api/v1/forward".length()),
            method = r.getMethod();
        calls.add(method + " " + path);
        if (!"Bearer example-token".equals(r.getHeader("Authorization"))) return error(401);
        if (path.equals("/skills")) {
          if (method.equals("GET")) return ok("{\"data\":[],\"has_more\":false}");
          String body = r.getBody().readUtf8();
          if (!body.contains("name=\"files\"")
              || !body.contains("application/zip")
              || r.getHeader("Idempotency-Key") == null) return error(400);
          Matcher match = Pattern.compile("filename=\"([^\"]+)\\.zip\"").matcher(body);
          if (!match.find()) return error(400);
          name = match.group(1);
          return ok("{\"id\":\"skill_created\",\"latest_version\":\"1\"}").setResponseCode(201);
        }
        if (path.equals("/skills/skill_created"))
          return ok(
              method.equals("DELETE")
                  ? "{\"id\":\"skill_created\",\"deleted\":true}"
                  : "{\"id\":\"skill_created\"}");
        if (path.equals("/skills/skill_created/versions"))
          return method.equals("POST")
              ? ok(version).setResponseCode(201)
              : ok("{\"data\":[],\"has_more\":false}");
        if (path.equals("/skills/skill_created/versions/1786973491821648"))
          return ok(
              method.equals("DELETE") ? "{\"id\":\"1786973491821648\",\"deleted\":true}" : version);
        if (path.endsWith("/content"))
          return new MockResponse()
              .setHeader("Content-Type", "application/zip")
              .setHeader("x-request-id", "zip-request")
              .setBody(
                  new okio.Buffer().write(corrupt ? new byte[] {1, 2} : downloadedPackage(name)));
        if (path.equals("/vaults"))
          return ok(
              method.equals("POST")
                  ? "{\"id\":\"vault_created\"}"
                  : "{\"data\":[],\"has_more\":false}");
        if (path.equals("/vaults/vault_created"))
          return ok(
              method.equals("DELETE")
                  ? "{\"id\":\"vault_created\",\"deleted\":true}"
                  : "{\"id\":\"vault_created\"}");
        if (path.equals("/vaults/vault_created/credentials")) {
          if (method.equals("GET")) return ok("{\"data\":[],\"has_more\":false}");
          JsonValue auth = JsonValue.parse(r.getBody().readUtf8()).get("auth");
          secrets.add(auth.get("secret_value").asString());
          if (!auth.get("secret_name").asString().equals("SDK_JAVA_EXAMPLE")) return error(400);
          return ok("{\"id\":\"vcred_created\"}");
        }
        if (path.equals("/vaults/vault_created/credentials/vcred_created")) {
          if (method.equals("POST")) {
            secrets.add(
                JsonValue.parse(r.getBody().readUtf8()).get("auth").get("secret_value").asString());
            if (rotateFails) return error(503);
          }
          if (method.equals("DELETE"))
            return cleanupFails ? error(409) : ok("{\"id\":\"vcred_created\",\"deleted\":true}");
          return ok(
              "{\"id\":\"vcred_created\",\"auth\":{\"type\":\"environment_variable\",\"secret_name\":\"SDK_JAVA_EXAMPLE\"}}");
        }
        return error(404);
      }
    };
  }

  ForwardClient client(MockWebServer api) {
    return ForwardClient.builder()
        .baseUrl(api.url("/api/v1/forward").toString())
        .pat("example-token")
        .maxRetries(0)
        .build();
  }

  @Test
  void packagedExampleVerifiesDownloadRotatesAndCleansOwnResources() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    try (MockWebServer api = new MockWebServer()) {
      api.setDispatcher(dispatch(false, false, false));
      api.start();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
        SkillVaultExample.run(c, out);
      }
      assertEquals(18, calls.size());
      assertEquals(
          Arrays.asList(
              "DELETE /vaults/vault_created/credentials/vcred_created",
              "DELETE /vaults/vault_created",
              "DELETE /skills/skill_created"),
          calls.subList(calls.size() - 3, calls.size()));
      assertEquals(2, secrets.size());
      assertNotEquals(secrets.get(0), secrets.get(1));
      String output = bytes.toString("UTF-8");
      for (String secret : secrets) assertFalse(output.contains(secret));
      assertFalse(output.contains("example-token"));
      assertTrue(output.contains("verified_skill_bytes="));
      assertTrue(output.contains("credential_rotation_acknowledged"));
    }
  }

  private static byte[] downloadedPackage(String name) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
        zip.putNextEntry(new ZipEntry(name + "/SKILL.md"));
        zip.write(
            ("---\nname: "
                    + name
                    + "\n"
                    + "description: Updated example\n"
                    + "---\n"
                    + "This is an isolated SDK packaging example.\n")
                .getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void packageVerificationRejectsChangedContentAndUnexpectedPaths() throws Exception {
    assertThrows(
        IllegalStateException.class,
        () ->
            SkillVaultExample.verifyPackage(
                new ByteArrayInputStream(downloadedPackage("test")),
                "test",
                "wrong".getBytes(StandardCharsets.UTF_8)));
    assertThrows(
        IllegalStateException.class,
        () ->
            SkillVaultExample.verifyPackage(
                new ByteArrayInputStream(downloadedPackage("../outside")), "test", new byte[0]));
  }

  @Test
  void badDownloadStopsBeforeVaultCreationAndCleansSkill() throws Exception {
    try (MockWebServer api = new MockWebServer()) {
      api.setDispatcher(dispatch(true, false, false));
      api.start();
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        assertThrows(IllegalStateException.class, () -> SkillVaultExample.run(c, out));
      }
      assertFalse(calls.contains("POST /vaults"));
      assertEquals("DELETE /skills/skill_created", calls.get(calls.size() - 1));
    }
  }

  @Test
  void failedRotationPreservesCleanupErrorsAndAttemptsAllResources() throws Exception {
    try (MockWebServer api = new MockWebServer()) {
      api.setDispatcher(dispatch(false, true, true));
      api.start();
      try (ForwardClient c = client(api);
          PrintStream out = new PrintStream(new ByteArrayOutputStream())) {
        ApiStatusException e =
            assertThrows(ApiStatusException.class, () -> SkillVaultExample.run(c, out));
        assertEquals(503, e.statusCode());
        assertEquals(1, e.getSuppressed().length);
        assertEquals(409, ((ApiStatusException) e.getSuppressed()[0]).statusCode());
      }
      assertEquals(
          1, Collections.frequency(calls, "POST /vaults/vault_created/credentials/vcred_created"));
      assertTrue(calls.contains("DELETE /vaults/vault_created"));
      assertTrue(calls.contains("DELETE /skills/skill_created"));
    }
  }
}
