package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.managed.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Timeout(15)
class ManagedRoutesTest {
  private MockWebServer server;
  private ManagedClient client;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    client =
        ManagedClient.builder()
            .baseUrl(server.url("/api/v1/cloud").toString())
            .pat("test-pat")
            .maxRetries(0)
            .build();
  }

  @AfterEach
  void close() throws Exception {
    client.close();
    server.close();
  }

  static Stream<Arguments> routes() throws Exception {
    try (BufferedReader r =
        new BufferedReader(
            new InputStreamReader(
                ManagedRoutesTest.class.getResourceAsStream("/managed-routes.json"),
                StandardCharsets.UTF_8))) {
      return JsonValue.parse(r.lines().collect(Collectors.joining("\n"))).asArray().stream()
          .flatMap(
              f ->
                  IntStream.range(0, 4)
                      .mapToObj(
                          mode ->
                              Arguments.of(
                                  f.get("resource").asString()
                                      + "."
                                      + f.get("operation").asString(),
                                  mode,
                                  f)));
    }
  }

  @ParameterizedTest(name = "{0}, mode={1}")
  @MethodSource("routes")
  void officialEndpointsUseTheirDocumentedMethodAndParameterLocation(
      String name, int mode, JsonValue f) throws Exception {
    boolean download = "download".equals(f.get("operation").asString());
    server.enqueue(
        new MockResponse()
            .setHeader("X-Request-ID", "req_managed")
            .setBody(download ? "zip-content" : f.get("response").toJson()));
    Object service = (mode % 2 == 1) ? client.async() : client;
    for (String step : f.get("resource").asString().split("\\."))
      service = service.getClass().getMethod(step).invoke(service);
    if (mode >= 2) service = service.getClass().getMethod("withResponse").invoke(service);
    List<Class<?>> types = new ArrayList<>();
    List<Object> args = new ArrayList<>();
    for (JsonValue id : f.get("ids").asArray()) {
      types.add(String.class);
      args.add(id.asString());
    }
    if (!f.get("param").isNull()) {
      Class<?> type = Class.forName("com.qoder.cloudagents.managed." + f.get("param").asString());
      types.add(type);
      if ("query".equals(f.get("mode").asString())) {
        Object builder = type.getMethod("builder").invoke(null);
        for (Map.Entry<String, JsonValue> e : f.get("query").asObject().entrySet()) {
          String t = f.get("query_types").get(e.getKey()).asString();
          Class<?> jt =
              "long".equals(t) ? long.class : "boolean".equals(t) ? boolean.class : String.class;
          Object v =
              "long".equals(t)
                  ? e.getValue().asLong()
                  : "boolean".equals(t) ? e.getValue().asBoolean() : e.getValue().asString();
          builder.getClass().getMethod(camel(e.getKey()), jt).invoke(builder, v);
        }
        args.add(builder.getClass().getMethod("build").invoke(builder));
      } else if ("multipart".equals(f.get("mode").asString())) {
        Upload upload =
            Upload.bytes("hello".getBytes(StandardCharsets.UTF_8), "qa/SKILL.md", "text/plain");
        if (type == FileUploadParams.class)
          args.add(FileUploadParams.builder().file(upload).name("qa.txt").build());
        else if (type == SkillCreateParams.class)
          args.add(
              SkillCreateParams.builder()
                  .files(Collections.singletonList(upload))
                  .displayTitle("QA")
                  .build());
        else
          args.add(
              SkillVersionCreateParams.builder().files(Collections.singletonList(upload)).build());
      } else args.add(type.getMethod("fromJson", JsonValue.class).invoke(null, f.get("body")));
    }
    Object result =
        service
            .getClass()
            .getMethod(f.get("operation").asString(), types.toArray(new Class<?>[0]))
            .invoke(service, args.toArray());
    if (result instanceof CompletableFuture)
      result = ((CompletableFuture<?>) result).get(3, TimeUnit.SECONDS);
    if (result instanceof ApiResponse) {
      assertEquals("req_managed", ((ApiResponse<?>) result).requestId());
      result = ((ApiResponse<?>) result).data();
    }
    if (result instanceof FileDownload) {
      try (FileDownload file = (FileDownload) result) {
        assertEquals('z', file.body().read());
      }
    }
    RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
    assertNotNull(r);
    assertEquals(f.get("method").asString(), r.getMethod());
    assertEquals(f.get("path").asString(), r.getRequestUrl().encodedPath());
    assertEquals("Bearer test-pat", r.getHeader("Authorization"));
    assertEquals("java", r.getHeader("X-Qoder-Lang"));
    assertEquals(f.get("query").asObject().size(), r.getRequestUrl().querySize());
    for (Map.Entry<String, JsonValue> e : f.get("query").asObject().entrySet())
      assertEquals(
          e.getValue().isString() ? e.getValue().asString() : e.getValue().toJson(),
          r.getRequestUrl().queryParameter(e.getKey()));
    if ("search".equals(f.get("mode").asString()))
      assertEquals("search-2026-08-31", r.getHeader("x-qoder-beta"));
    String body = r.getBody().readUtf8();
    if ("multipart".equals(f.get("mode").asString())) {
      assertTrue(r.getHeader("Content-Type").startsWith("multipart/form-data"));
      assertTrue(body.contains("hello"));
      assertTrue(body.contains("qa/SKILL.md"));
    } else if (f.get("body").isNull()) assertEquals("", body);
    else {
      JsonValue expected = f.get("body"), actual = JsonValue.parse(body);
      if ("sessions.create".equals(name)) {
        Map<String, JsonValue> metadata = new LinkedHashMap<>(expected.get("metadata").asObject());
        metadata.put("__qca_sdk_created", JsonValue.of("true"));
        metadata.put("__qca_sdk_language", JsonValue.of("java"));
        metadata.put("__qca_sdk_version", JsonValue.of(r.getHeader("X-Qoder-Package-Version")));
        Map<String, JsonValue> copy = new LinkedHashMap<>(expected.asObject());
        copy.put("metadata", JsonValue.of(metadata));
        expected = JsonValue.of(copy);
      }
      assertEquals(expected.toJson(), actual.toJson());
    }
  }

  static String camel(String s) {
    String[] p = s.split("[^a-zA-Z0-9]+");
    StringBuilder r = new StringBuilder(p[0]);
    for (int i = 1; i < p.length; i++)
      if (!p[i].isEmpty())
        r.append(Character.toUpperCase(p[i].charAt(0))).append(p[i].substring(1));
    return r.toString();
  }
}
