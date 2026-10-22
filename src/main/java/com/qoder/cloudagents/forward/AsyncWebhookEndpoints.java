package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Webhook management using a PAT or administrator SAT. */
public final class AsyncWebhookEndpoints {
  private final ClientEngine engine;

  public AsyncWebhookEndpoints(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<WebhookEndpointCreateResponse> create(
      WebhookEndpointCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpointCreateResponse> create(
      WebhookEndpointCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpointListResponse> list() {
    return list(WebhookEndpointListParams.builder().build());
  }

  public CompletableFuture<WebhookEndpointListResponse> list(RequestOptions options) {
    return list(WebhookEndpointListParams.builder().build(), options);
  }

  public CompletableFuture<WebhookEndpointListResponse> list(WebhookEndpointListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpointListResponse> list(
      WebhookEndpointListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpoint> retrieve(String endpointId) {
    return retrieve(endpointId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpoint> retrieve(String endpointId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(endpointId, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpoint> update(
      String endpointId, WebhookEndpointUpdateParams params) {
    return update(endpointId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpoint> update(
      String endpointId, WebhookEndpointUpdateParams params, RequestOptions options) {
    return Futures.map(withResponse().update(endpointId, params, options), ApiResponse::data);
  }

  public CompletableFuture<Void> delete(String endpointId) {
    return delete(endpointId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Void> delete(String endpointId, RequestOptions options) {
    return Futures.map(withResponse().delete(endpointId, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpoint> enable(String endpointId) {
    return enable(endpointId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpoint> enable(String endpointId, RequestOptions options) {
    return Futures.map(withResponse().enable(endpointId, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpoint> disable(String endpointId) {
    return disable(endpointId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpoint> disable(String endpointId, RequestOptions options) {
    return Futures.map(withResponse().disable(endpointId, options), ApiResponse::data);
  }

  public CompletableFuture<WebhookEndpointTestResponse> test(String endpointId) {
    return test(endpointId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<WebhookEndpointTestResponse> test(
      String endpointId, RequestOptions options) {
    return Futures.map(withResponse().test(endpointId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<WebhookEndpointCreateResponse>> create(
        WebhookEndpointCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpointCreateResponse>> create(
        WebhookEndpointCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"webhook", "endpoints"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new WebhookEndpointCreateResponse(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WebhookEndpointListResponse>> list() {
      return list(WebhookEndpointListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<WebhookEndpointListResponse>> list(
        RequestOptions options) {
      return list(WebhookEndpointListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<WebhookEndpointListResponse>> list(
        WebhookEndpointListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpointListResponse>> list(
        WebhookEndpointListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"webhook", "endpoints"},
          params.query(),
          options,
          WebhookEndpointListResponse::new);
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> retrieve(String endpointId) {
      return retrieve(endpointId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> retrieve(
        String endpointId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"webhook", "endpoints", endpointId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new WebhookEndpoint(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> update(
        String endpointId, WebhookEndpointUpdateParams params) {
      return update(endpointId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> update(
        String endpointId, WebhookEndpointUpdateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "PUT",
              new String[] {"webhook", "endpoints", endpointId},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new WebhookEndpoint(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Void>> delete(String endpointId) {
      return delete(endpointId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Void>> delete(String endpointId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"webhook", "endpoints", endpointId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> null));
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> enable(String endpointId) {
      return enable(endpointId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> enable(
        String endpointId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"webhook", "endpoints", endpointId, "enable"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new WebhookEndpoint(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> disable(String endpointId) {
      return disable(endpointId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpoint>> disable(
        String endpointId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"webhook", "endpoints", endpointId, "disable"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new WebhookEndpoint(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<WebhookEndpointTestResponse>> test(String endpointId) {
      return test(endpointId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<WebhookEndpointTestResponse>> test(
        String endpointId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"webhook", "endpoints", endpointId, "test"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new WebhookEndpointTestResponse(v, r.requestId())));
    }
  }
}
