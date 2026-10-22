package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

/** Webhook management using a PAT or administrator SAT. */
public final class WebhookEndpoints {
  private final AsyncWebhookEndpoints async;

  public WebhookEndpoints(AsyncWebhookEndpoints async) {
    this.async = async;
  }

  public WebhookEndpointCreateResponse create(WebhookEndpointCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public WebhookEndpointCreateResponse create(
      WebhookEndpointCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public WebhookEndpointListResponse list() {
    return list(WebhookEndpointListParams.builder().build());
  }

  public WebhookEndpointListResponse list(RequestOptions options) {
    return list(WebhookEndpointListParams.builder().build(), options);
  }

  public WebhookEndpointListResponse list(WebhookEndpointListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public WebhookEndpointListResponse list(
      WebhookEndpointListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public WebhookEndpoint retrieve(String endpointId) {
    return retrieve(endpointId, RequestOptions.DEFAULT);
  }

  public WebhookEndpoint retrieve(String endpointId, RequestOptions options) {
    return Futures.await(async.retrieve(endpointId, options));
  }

  public WebhookEndpoint update(String endpointId, WebhookEndpointUpdateParams params) {
    return update(endpointId, params, RequestOptions.DEFAULT);
  }

  public WebhookEndpoint update(
      String endpointId, WebhookEndpointUpdateParams params, RequestOptions options) {
    return Futures.await(async.update(endpointId, params, options));
  }

  public Void delete(String endpointId) {
    return delete(endpointId, RequestOptions.DEFAULT);
  }

  public Void delete(String endpointId, RequestOptions options) {
    return Futures.await(async.delete(endpointId, options));
  }

  public WebhookEndpoint enable(String endpointId) {
    return enable(endpointId, RequestOptions.DEFAULT);
  }

  public WebhookEndpoint enable(String endpointId, RequestOptions options) {
    return Futures.await(async.enable(endpointId, options));
  }

  public WebhookEndpoint disable(String endpointId) {
    return disable(endpointId, RequestOptions.DEFAULT);
  }

  public WebhookEndpoint disable(String endpointId, RequestOptions options) {
    return Futures.await(async.disable(endpointId, options));
  }

  public WebhookEndpointTestResponse test(String endpointId) {
    return test(endpointId, RequestOptions.DEFAULT);
  }

  public WebhookEndpointTestResponse test(String endpointId, RequestOptions options) {
    return Futures.await(async.test(endpointId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<WebhookEndpointCreateResponse> create(WebhookEndpointCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpointCreateResponse> create(
        WebhookEndpointCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<WebhookEndpointListResponse> list() {
      return list(WebhookEndpointListParams.builder().build());
    }

    public ApiResponse<WebhookEndpointListResponse> list(RequestOptions options) {
      return list(WebhookEndpointListParams.builder().build(), options);
    }

    public ApiResponse<WebhookEndpointListResponse> list(WebhookEndpointListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpointListResponse> list(
        WebhookEndpointListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<WebhookEndpoint> retrieve(String endpointId) {
      return retrieve(endpointId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpoint> retrieve(String endpointId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(endpointId, options));
    }

    public ApiResponse<WebhookEndpoint> update(
        String endpointId, WebhookEndpointUpdateParams params) {
      return update(endpointId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpoint> update(
        String endpointId, WebhookEndpointUpdateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().update(endpointId, params, options));
    }

    public ApiResponse<Void> delete(String endpointId) {
      return delete(endpointId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Void> delete(String endpointId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(endpointId, options));
    }

    public ApiResponse<WebhookEndpoint> enable(String endpointId) {
      return enable(endpointId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpoint> enable(String endpointId, RequestOptions options) {
      return Futures.await(async.withResponse().enable(endpointId, options));
    }

    public ApiResponse<WebhookEndpoint> disable(String endpointId) {
      return disable(endpointId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpoint> disable(String endpointId, RequestOptions options) {
      return Futures.await(async.withResponse().disable(endpointId, options));
    }

    public ApiResponse<WebhookEndpointTestResponse> test(String endpointId) {
      return test(endpointId, RequestOptions.DEFAULT);
    }

    public ApiResponse<WebhookEndpointTestResponse> test(
        String endpointId, RequestOptions options) {
      return Futures.await(async.withResponse().test(endpointId, options));
    }
  }
}
