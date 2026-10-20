package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.errors.ApiStatusException;
import java.io.*;
import java.util.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;

@Timeout(10)
class ForwardTokenLiveCheckTest {
  @Test
  void scopesRevocationAndFailureCleanupAreChecked() throws Exception {
    for (boolean fail : new boolean[] {false, true}) {
      Set<String> revoked = new HashSet<>();
      List<String> deleted = new ArrayList<>();
      try (MockWebServer server = new MockWebServer()) {
        server.setDispatcher(
            new Dispatcher() {
              int identities;

              @Override
              public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath(), method = request.getMethod();
                if (path.endsWith("/revoke")) {
                  String id = path.contains("auth-bound") ? "bound" : "admin";
                  revoked.add(id);
                  return ok("{\"revoked\":true}");
                }
                if (path.endsWith("/service_account_tokens")) {
                  boolean bound = path.contains("/identities/");
                  return ok(
                      "{\"type\":\"service_account_token\",\"token_type\":\"Bearer\",\"access_token\":\""
                          + (bound ? "bound" : "admin")
                          + "\",\"auth_token_id\":\"auth-"
                          + (bound ? "bound" : "admin")
                          + "\",\"credential_id\":\"cred\",\"service_account_id\":\"sa\",\"expires_in\":600,\"expires_at\":\"2999-01-01T00:00:00Z\",\"subject_type\":\""
                          + (bound ? "identity\",\"identity_id\":\"idn-1" : "admin")
                          + "\"}");
                }
                if (path.endsWith("/identities") && "POST".equals(method))
                  return ok("{\"id\":\"idn-" + ++identities + "\"}");
                if (path.contains("/identities/") && path.endsWith("/templates")) {
                  assertEquals("Bearer bound", request.getHeader("Authorization"));
                  if (revoked.contains("bound"))
                    return new MockResponse().setResponseCode(401).setBody("{}");
                  if (fail) return new MockResponse().setResponseCode(503).setBody("{}");
                  if (path.contains("idn-2"))
                    return new MockResponse().setResponseCode(403).setBody("{}");
                  return ok("{\"data\":[],\"has_more\":false}");
                }
                if (path.endsWith("/templates") && revoked.contains("admin"))
                  return new MockResponse().setResponseCode(401).setBody("{}");
                if (path.endsWith("/clear")) return ok("{\"status\":\"completed\"}");
                if ("DELETE".equals(method)) {
                  deleted.add(path);
                  return ok("{}");
                }
                return ok("{\"data\":[],\"has_more\":false}");
              }
            });
        server.start();
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        try (ForwardClient client =
                ForwardClient.builder()
                    .baseUrl(server.url("/api/v1/forward").toString())
                    .pat("synthetic")
                    .maxRetries(0)
                    .build();
            PrintStream out = new PrintStream(log)) {
          if (fail)
            assertEquals(
                503,
                assertThrows(
                        ApiStatusException.class,
                        () ->
                            ForwardTokenLiveCheck.run(
                                client,
                                server.url("/api/v1/forward").toString(),
                                "synthetic-sa-key",
                                out))
                    .statusCode());
          else {
            ForwardTokenLiveCheck.run(
                client, server.url("/api/v1/forward").toString(), "synthetic-sa-key", out);
            assertTrue(log.toString("UTF-8").contains("revoked_admin_denied"));
            assertTrue(log.toString("UTF-8").contains("cross_identity_denied"));
          }
        }
        assertEquals(new HashSet<>(Arrays.asList("admin", "bound")), revoked);
        assertEquals(2, deleted.size());
        assertFalse(log.toString("UTF-8").contains("synthetic-sa-key"));
      }
    }
  }

  private static MockResponse ok(String body) {
    return new MockResponse().setBody(body);
  }
}
