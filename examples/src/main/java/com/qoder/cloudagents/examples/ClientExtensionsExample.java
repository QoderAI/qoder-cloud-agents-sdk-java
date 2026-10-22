package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.ApiRequest;
import com.qoder.cloudagents.core.ApiResponse;
import com.qoder.cloudagents.core.JsonValue;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.core.http.HttpTransport;
import java.io.PrintStream;
import java.time.Duration;

/** Read-only model listing through a copied client, HTTP interceptor and generic JSON request. */
public final class ClientExtensionsExample {
  private ClientExtensionsExample() {}

  public static void main(String[] args) throws Exception {
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), null, System.out);
    }
  }

  /** Supply a caller-owned transport to use the application's HTTP implementation. */
  public static void run(ForwardClient client, HttpTransport transport, PrintStream output) {
    try (ForwardClient derived =
        client.withOptions(
            options -> {
              options
                  .timeout(Duration.ofSeconds(30))
                  .interceptor(
                      (request, chain) -> {
                        long started = System.nanoTime();
                        return chain
                            .proceed(request.withHeader("X-Trace-Source", "sdk-example"))
                            .whenComplete(
                                (response, error) ->
                                    output.println(
                                        "http_elapsed_ms="
                                            + (System.nanoTime() - started) / 1_000_000));
                      });
              if (transport != null) options.transport(transport);
            })) {
      ApiResponse<JsonValue> response =
          derived.request(
              ApiRequest.builder("GET", "models").build(),
              RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build());
      output.println(
          "models="
              + response.data().get("data").asArray().size()
              + " request_id="
              + response.requestId());
    }
  }
}
