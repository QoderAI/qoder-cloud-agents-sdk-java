package com.qoder.cloudagents.managed;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.core.internal.*;
import java.util.*;

public final class VaultCredentials {
  private final AsyncVaultCredentials async;

  public VaultCredentials(AsyncVaultCredentials async) {
    this.async = async;
  }

  public VaultCredential archive(String vaultId, String credentialId) {
    return archive(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public VaultCredential archive(String vaultId, String credentialId, RequestOptions options) {
    return Futures.await(async.archive(vaultId, credentialId, options));
  }

  public VaultCredential create(String vaultId, VaultCredentialCreateParams params) {
    return create(vaultId, params, RequestOptions.DEFAULT);
  }

  public VaultCredential create(
      String vaultId, VaultCredentialCreateParams params, RequestOptions options) {
    return Futures.await(async.create(vaultId, params, options));
  }

  public DeletedVaultCredential delete(String vaultId, String credentialId) {
    return delete(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public DeletedVaultCredential delete(
      String vaultId, String credentialId, RequestOptions options) {
    return Futures.await(async.delete(vaultId, credentialId, options));
  }

  public VaultCredential retrieve(String vaultId, String credentialId) {
    return retrieve(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public VaultCredential retrieve(String vaultId, String credentialId, RequestOptions options) {
    return Futures.await(async.retrieve(vaultId, credentialId, options));
  }

  public VaultCredentialListResponse list(String vaultId) {
    return list(vaultId, VaultCredentialListParams.builder().build());
  }

  public VaultCredentialListResponse list(String vaultId, RequestOptions options) {
    return list(vaultId, VaultCredentialListParams.builder().build(), options);
  }

  public VaultCredentialListResponse list(String vaultId, VaultCredentialListParams params) {
    return list(vaultId, params, RequestOptions.DEFAULT);
  }

  public VaultCredentialListResponse list(
      String vaultId, VaultCredentialListParams params, RequestOptions options) {
    return Futures.await(async.list(vaultId, params, options));
  }

  public VaultCredential update(
      String vaultId, String credentialId, VaultCredentialUpdateParams params) {
    return update(vaultId, credentialId, params, RequestOptions.DEFAULT);
  }

  public VaultCredential update(
      String vaultId,
      String credentialId,
      VaultCredentialUpdateParams params,
      RequestOptions options) {
    return Futures.await(async.update(vaultId, credentialId, params, options));
  }

  public VaultCredentialValidation validate(String vaultId, String credentialId) {
    return validate(vaultId, credentialId, RequestOptions.DEFAULT);
  }

  public VaultCredentialValidation validate(
      String vaultId, String credentialId, RequestOptions options) {
    return Futures.await(async.validate(vaultId, credentialId, options));
  }

  public WithResponse withResponse() {
    return new WithResponse();
  }

  public final class WithResponse {
    public ApiResponse<VaultCredential> archive(String vaultId, String credentialId) {
      return archive(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredential> archive(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.await(async.withResponse().archive(vaultId, credentialId, options));
    }

    public ApiResponse<VaultCredential> create(String vaultId, VaultCredentialCreateParams params) {
      return create(vaultId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredential> create(
        String vaultId, VaultCredentialCreateParams params, RequestOptions options) {
      return Futures.await(async.withResponse().create(vaultId, params, options));
    }

    public ApiResponse<DeletedVaultCredential> delete(String vaultId, String credentialId) {
      return delete(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public ApiResponse<DeletedVaultCredential> delete(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.await(async.withResponse().delete(vaultId, credentialId, options));
    }

    public ApiResponse<VaultCredential> retrieve(String vaultId, String credentialId) {
      return retrieve(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredential> retrieve(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.await(async.withResponse().retrieve(vaultId, credentialId, options));
    }

    public ApiResponse<VaultCredentialListResponse> list(String vaultId) {
      return list(vaultId, VaultCredentialListParams.builder().build());
    }

    public ApiResponse<VaultCredentialListResponse> list(String vaultId, RequestOptions options) {
      return list(vaultId, VaultCredentialListParams.builder().build(), options);
    }

    public ApiResponse<VaultCredentialListResponse> list(
        String vaultId, VaultCredentialListParams params) {
      return list(vaultId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredentialListResponse> list(
        String vaultId, VaultCredentialListParams params, RequestOptions options) {
      return Futures.await(async.withResponse().list(vaultId, params, options));
    }

    public ApiResponse<VaultCredential> update(
        String vaultId, String credentialId, VaultCredentialUpdateParams params) {
      return update(vaultId, credentialId, params, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredential> update(
        String vaultId,
        String credentialId,
        VaultCredentialUpdateParams params,
        RequestOptions options) {
      return Futures.await(async.withResponse().update(vaultId, credentialId, params, options));
    }

    public ApiResponse<VaultCredentialValidation> validate(String vaultId, String credentialId) {
      return validate(vaultId, credentialId, RequestOptions.DEFAULT);
    }

    public ApiResponse<VaultCredentialValidation> validate(
        String vaultId, String credentialId, RequestOptions options) {
      return Futures.await(async.withResponse().validate(vaultId, credentialId, options));
    }
  }
}
