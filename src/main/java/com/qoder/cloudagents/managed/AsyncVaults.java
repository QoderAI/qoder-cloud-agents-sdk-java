package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncVaults {
  private final ClientEngine engine;

  public AsyncVaults(ClientEngine engine) {
    this.engine = engine;
  }

  public AsyncVaultCredentials credentials() {
    return new AsyncVaultCredentials(engine);
  }

  public CompletableFuture<Vault> archive(String vaultId) {
    return archive(vaultId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Vault> archive(String vaultId, RequestOptions options) {
    return Futures.map(withResponse().archive(vaultId, options), ApiResponse::data);
  }

  public CompletableFuture<Vault> create(VaultCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Vault> create(VaultCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedVault> delete(String vaultId) {
    return delete(vaultId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedVault> delete(String vaultId, RequestOptions options) {
    return Futures.map(withResponse().delete(vaultId, options), ApiResponse::data);
  }

  public CompletableFuture<Vault> retrieve(String vaultId) {
    return retrieve(vaultId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<Vault> retrieve(String vaultId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(vaultId, options), ApiResponse::data);
  }

  public CompletableFuture<VaultListResponse> list() {
    return list(VaultListParams.builder().build());
  }

  public CompletableFuture<VaultListResponse> list(RequestOptions options) {
    return list(VaultListParams.builder().build(), options);
  }

  public CompletableFuture<VaultListResponse> list(VaultListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultListResponse> list(VaultListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(params, options), ApiResponse::data);
  }

  public CompletableFuture<VaultListResponse> search() {
    return search(VaultSearchParams.builder().build());
  }

  public CompletableFuture<VaultListResponse> search(RequestOptions options) {
    return search(VaultSearchParams.builder().build(), options);
  }

  public CompletableFuture<VaultListResponse> search(VaultSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultListResponse> search(
      VaultSearchParams params, RequestOptions options) {
    return Futures.map(withResponse().search(params, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<Vault>> archive(String vaultId) {
      return archive(vaultId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Vault>> archive(String vaultId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"vaults", vaultId, "archive"},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Vault(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Vault>> create(VaultCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Vault>> create(
        VaultCreateParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"vaults"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new Vault(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedVault>> delete(String vaultId) {
      return delete(vaultId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedVault>> delete(
        String vaultId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"vaults", vaultId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedVault(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<Vault>> retrieve(String vaultId) {
      return retrieve(vaultId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<Vault>> retrieve(String vaultId, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      return Futures.map(
          engine.request(
              "GET", new String[] {"vaults", vaultId}, Collections.emptyMap(), null, true, options),
          r -> r.map(v -> v.isNull() ? null : new Vault(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> list() {
      return list(VaultListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> list(RequestOptions options) {
      return list(VaultListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> list(VaultListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> list(
        VaultListParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine, new String[] {"vaults"}, params.query(), options, VaultListResponse::new);
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> search() {
      return search(VaultSearchParams.builder().build());
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> search(RequestOptions options) {
      return search(VaultSearchParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> search(VaultSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultListResponse>> search(
        VaultSearchParams params, RequestOptions options) {
      if (options == null) throw new ConfigurationException("Request options must not be null");
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return SearchRequests.search(
          engine,
          new String[] {"vaults", "search"},
          params.toJsonValue(),
          Collections.emptyMap(),
          options,
          VaultListResponse::new);
    }
  }
}
