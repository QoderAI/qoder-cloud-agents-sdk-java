package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class Vaults {
  private final AsyncVaults async;

  public Vaults(AsyncVaults async) {
    this.async = async;
  }

  public VaultCredentials credentials() {
    return new VaultCredentials(async.credentials());
  }

  public Vault archive(String vaultId) {
    return archive(vaultId, RequestOptions.DEFAULT);
  }

  public Vault archive(String vaultId, RequestOptions options) {
    return Futures.await(async.archive(vaultId, options));
  }

  public Vault create(VaultCreateParams params) {
    return create(params, RequestOptions.DEFAULT);
  }

  public Vault create(VaultCreateParams params, RequestOptions options) {
    return Futures.await(async.create(params, options));
  }

  public DeletedVault delete(String vaultId) {
    return delete(vaultId, RequestOptions.DEFAULT);
  }

  public DeletedVault delete(String vaultId, RequestOptions options) {
    return Futures.await(async.delete(vaultId, options));
  }

  public Vault retrieve(String vaultId) {
    return retrieve(vaultId, RequestOptions.DEFAULT);
  }

  public Vault retrieve(String vaultId, RequestOptions options) {
    return Futures.await(async.retrieve(vaultId, options));
  }

  public VaultListResponse list() {
    return list(VaultListParams.builder().build());
  }

  public VaultListResponse list(RequestOptions options) {
    return list(VaultListParams.builder().build(), options);
  }

  public VaultListResponse list(VaultListParams params) {
    return list(params, RequestOptions.DEFAULT);
  }

  public VaultListResponse list(VaultListParams params, RequestOptions options) {
    return Futures.await(async.list(params, options));
  }

  public VaultListResponse search() {
    return search(VaultSearchParams.builder().build());
  }

  public VaultListResponse search(RequestOptions options) {
    return search(VaultSearchParams.builder().build(), options);
  }

  public VaultListResponse search(VaultSearchParams params) {
    return search(params, RequestOptions.DEFAULT);
  }

  public VaultListResponse search(VaultSearchParams params, RequestOptions options) {
    return Futures.await(async.search(params, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<Vault> archive(String vaultId) {
      return archive(vaultId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Vault> archive(String vaultId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(vaultId, options));
    }

    public ApiResponse<Vault> create(VaultCreateParams params) {
      return create(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<Vault> create(VaultCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(params, options));
    }

    public ApiResponse<DeletedVault> delete(String vaultId) {
      return delete(vaultId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedVault> delete(String vaultId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(vaultId, options));
    }

    public ApiResponse<Vault> retrieve(String vaultId) {
      return retrieve(vaultId, RequestOptions.DEFAULT);
    }

    public ApiResponse<Vault> retrieve(String vaultId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(vaultId, options));
    }

    public ApiResponse<VaultListResponse> list() {
      return list(VaultListParams.builder().build());
    }

    public ApiResponse<VaultListResponse> list(RequestOptions options) {
      return list(VaultListParams.builder().build(), options);
    }

    public ApiResponse<VaultListResponse> list(VaultListParams params) {
      return list(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultListResponse> list(VaultListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(params, options));
    }

    public ApiResponse<VaultListResponse> search() {
      return search(VaultSearchParams.builder().build());
    }

    public ApiResponse<VaultListResponse> search(RequestOptions options) {
      return search(VaultSearchParams.builder().build(), options);
    }

    public ApiResponse<VaultListResponse> search(VaultSearchParams params) {
      return search(params, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultListResponse> search(VaultSearchParams params, RequestOptions options) {
      return Futures.await(async.withResponse().search(params, options));
    }
  }
}
