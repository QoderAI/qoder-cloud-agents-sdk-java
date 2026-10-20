package com.qoder.cloudagents.forward;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class AsyncVaultCredentials {
  private final ClientEngine engine;

  public AsyncVaultCredentials(ClientEngine engine) {
    this.engine = engine;
  }

  public CompletableFuture<VaultCredentialListResponse> list(String vaultId) {
    return list(vaultId, VaultCredentialListParams.builder().build());
  }

  public CompletableFuture<VaultCredentialListResponse> list(
      String vaultId, RequestOptions options) {
    return list(vaultId, VaultCredentialListParams.builder().build(), options);
  }

  public CompletableFuture<VaultCredentialListResponse> list(
      String vaultId, VaultCredentialListParams params) {
    return list(vaultId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultCredentialListResponse> list(
      String vaultId, VaultCredentialListParams params, RequestOptions options) {
    return Futures.map(withResponse().list(vaultId, params, options), ApiResponse::data);
  }

  public CompletableFuture<VaultCredential> create(
      String vaultId, VaultCredentialCreateParams params) {
    return create(vaultId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultCredential> create(
      String vaultId, VaultCredentialCreateParams params, RequestOptions options) {
    return Futures.map(withResponse().create(vaultId, params, options), ApiResponse::data);
  }

  public CompletableFuture<VaultCredential> retrieve(String vaultId, String credentialId) {
    return retrieve(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultCredential> retrieve(
      String vaultId, String credentialId, RequestOptions options) {
    return Futures.map(withResponse().retrieve(vaultId, credentialId, options), ApiResponse::data);
  }

  public CompletableFuture<VaultCredential> update(
      String vaultId, String credentialId, VaultCredentialUpdateParams params) {
    return update(vaultId, credentialId, params, RequestOptions.DEFAULT);
  }

  public CompletableFuture<VaultCredential> update(
      String vaultId,
      String credentialId,
      VaultCredentialUpdateParams params,
      RequestOptions options) {
    return Futures.map(
        withResponse().update(vaultId, credentialId, params, options), ApiResponse::data);
  }

  public CompletableFuture<DeletedVaultCredential> delete(String vaultId, String credentialId) {
    return delete(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public CompletableFuture<DeletedVaultCredential> delete(
      String vaultId, String credentialId, RequestOptions options) {
    return Futures.map(withResponse().delete(vaultId, credentialId, options), ApiResponse::data);
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public CompletableFuture<ApiResponse<VaultCredentialListResponse>> list(String vaultId) {
      return list(vaultId, VaultCredentialListParams.builder().build());
    }

    public CompletableFuture<ApiResponse<VaultCredentialListResponse>> list(
        String vaultId, RequestOptions options) {
      return list(vaultId, VaultCredentialListParams.builder().build(), options);
    }

    public CompletableFuture<ApiResponse<VaultCredentialListResponse>> list(
        String vaultId, VaultCredentialListParams params) {
      return list(vaultId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultCredentialListResponse>> list(
        String vaultId, VaultCredentialListParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return CursorRequests.list(
          engine,
          new String[] {"vaults", vaultId, "credentials"},
          params.query(),
          options,
          VaultCredentialListResponse::new);
    }

    public CompletableFuture<ApiResponse<VaultCredential>> create(
        String vaultId, VaultCredentialCreateParams params) {
      return create(vaultId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultCredential>> create(
        String vaultId, VaultCredentialCreateParams params, RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"vaults", vaultId, "credentials"},
              Collections.emptyMap(),
              params.toJsonValue(),
              true,
              options),
          r -> r.map(v -> new VaultCredential(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<VaultCredential>> retrieve(
        String vaultId, String credentialId) {
      return retrieve(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultCredential>> retrieve(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "GET",
              new String[] {"vaults", vaultId, "credentials", credentialId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> new VaultCredential(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<VaultCredential>> update(
        String vaultId, String credentialId, VaultCredentialUpdateParams params) {
      return update(vaultId, credentialId, params, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<VaultCredential>> update(
        String vaultId,
        String credentialId,
        VaultCredentialUpdateParams params,
        RequestOptions options) {
      if (params == null) throw new ConfigurationException("Request parameters are required");
      return Futures.map(
          engine.request(
              "POST",
              new String[] {"vaults", vaultId, "credentials", credentialId},
              Collections.emptyMap(),
              params.toJsonValue(),
              false,
              options.toBuilder().maxRetries(0).build()),
          r -> r.map(v -> new VaultCredential(v, r.requestId())));
    }

    public CompletableFuture<ApiResponse<DeletedVaultCredential>> delete(
        String vaultId, String credentialId) {
      return delete(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public CompletableFuture<ApiResponse<DeletedVaultCredential>> delete(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.map(
          engine.request(
              "DELETE",
              new String[] {"vaults", vaultId, "credentials", credentialId},
              Collections.emptyMap(),
              null,
              true,
              options),
          r -> r.map(v -> v.isNull() ? null : new DeletedVaultCredential(v, r.requestId())));
    }
  }
}
