package com.qoder.cloudagents.examples;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(15)
class ClientExtensionsConsumerTest {
  @Test
  void consumerCanImplementTransportWithoutInternalSdkOrOkHttpTypes() throws Exception {
    assertTrue(
        ForwardClient.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()
            .endsWith(".jar"));
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      server.enqueue(
          new MockResponse().setHeader("X-Request-ID", "req_consumer").setBody("{\"data\":[]}"));
      server.enqueue(new MockResponse().setBody("{\"data\":[]}"));
      // This read-only adapter demonstrates public types. Production adapters also handle uploads.
      HttpTransport transport =
          request -> {
            CompletableFuture<HttpResponse> future = new CompletableFuture<>();
            executor.execute(
                () -> {
                  try {
                    HttpURLConnection connection =
                        (HttpURLConnection) request.uri().toURL().openConnection();
                    future.whenComplete(
                        (value, error) -> {
                          if (future.isCancelled()) connection.disconnect();
                        });
                    connection.setInstanceFollowRedirects(false);
                    connection.setConnectTimeout((int) request.connectTimeout().toMillis());
                    connection.setReadTimeout((int) request.readTimeout().toMillis());
                    connection.setRequestMethod(request.method());
                    request
                        .headers()
                        .forEach(
                            (name, values) ->
                                values.forEach(
                                    value -> connection.addRequestProperty(name, value)));
                    if (future.isCancelled()) {
                      connection.disconnect();
                      return;
                    }
                    int status = connection.getResponseCode();
                    Map<String, List<String>> headers =
                        new LinkedHashMap<>(connection.getHeaderFields());
                    headers.remove(null);
                    InputStream stream =
                        new FilterInputStream(connection.getInputStream()) {
                          @Override
                          public void close() throws IOException {
                            try {
                              super.close();
                            } finally {
                              connection.disconnect();
                            }
                          }
                        };
                    HttpResponse response = new HttpResponse(status, headers, stream);
                    if (!future.complete(response)) response.close();
                  } catch (Exception error) {
                    future.completeExceptionally(error);
                  }
                });
            return future;
          };
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ForwardClient client =
              ForwardClient.builder()
                  .pat("consumer-secret")
                  .baseUrl(server.url("/api/v1/forward").toString())
                  .build();
          PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
        ClientExtensionsExample.run(client, transport, output);
        client.models().list(); // Closing the derived client did not close the parent.
      }
      String text = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
      assertTrue(text.contains("models=0 request_id=req_consumer"));
      assertFalse(text.contains("consumer-secret"));
      RecordedRequest first = server.takeRequest();
      assertEquals("Bearer consumer-secret", first.getHeader("Authorization"));
      assertEquals("sdk-example", first.getHeader("X-Trace-Source"));
      assertNull(server.takeRequest().getHeader("X-Trace-Source"));
    } finally {
      executor.shutdownNow();
    }
  }
}
