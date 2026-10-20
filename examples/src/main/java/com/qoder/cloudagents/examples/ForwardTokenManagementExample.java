package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;
import java.util.*;

/** Issue two temporary SATs, make read-only business calls, then revoke only those SATs. */
public final class ForwardTokenManagementExample {
  private ForwardTokenManagementExample() {}

  public static void main(String[] args) {
    if (!"true".equals(System.getenv("QODER_ALLOW_WRITE")))
      throw new IllegalArgumentException("Set QODER_ALLOW_WRITE=true");
    String base = required("QODER_FORWARD_BASE_URL"),
        key = required("QODER_SA_KEY"),
        identity = required("QODER_IDENTITY_ID");
    try (ForwardClient manager =
        ForwardClient.builder().baseUrl(base).authMode(AuthMode.SA).build()) {
      run(manager, base, key, identity, System.out);
    }
  }

  private static String required(String name) {
    String v = System.getenv(name);
    if (v == null || v.isEmpty()) throw new IllegalArgumentException("Set " + name);
    return v;
  }

  public static void run(
      ForwardClient manager, String base, String key, String identity, PrintStream out) {
    List<ServiceAccountToken> issued = new ArrayList<>();
    RuntimeException failure = null;
    try {
      ServiceAccountTokenCreateParams p =
          ServiceAccountTokenCreateParams.builder()
              .ttlSeconds(300)
              .metadata(Collections.singletonMap("purpose", "java-sdk-example"))
              .build();
      ServiceAccountToken admin = manager.serviceAccountTokens().create(key, p, options());
      issued.add(admin);
      try (ForwardClient runtime =
          ForwardClient.builder()
              .baseUrl(base)
              .authMode(AuthMode.SA)
              .accessToken(admin.accessToken())
              .build()) {
        out.println("admin_model_count=" + runtime.models().list(options()).data().size());
      }
      ServiceAccountToken bound =
          manager.identities().serviceAccountTokens().create(identity, key, p, options());
      issued.add(bound);
      try (ForwardClient runtime =
          ForwardClient.builder()
              .baseUrl(base)
              .authMode(AuthMode.SA)
              .accessToken(bound.accessToken())
              .build()) {
        out.println(
            "identity_config_count="
                + runtime
                    .identities()
                    .configs()
                    .list(identity, IdentityConfigListParams.builder().limit(10).build(), options())
                    .data()
                    .size());
      }
    } catch (RuntimeException e) {
      failure = e;
      throw e;
    } finally {
      RuntimeException cleanup = null;
      for (ServiceAccountToken token : issued) {
        try {
          ServiceAccountTokenRevokeParams.Builder b =
              ServiceAccountTokenRevokeParams.builder().reason("java-sdk-example-complete");
          if (token.credentialId() != null) b.targetCredentialId(token.credentialId());
          if (token.expiresAt() != null) b.targetExpiresAt(token.expiresAt());
          manager.serviceAccountTokens().revoke(token.authTokenId(), key, b.build(), options());
          out.println("revoked_token_id=" + token.authTokenId());
        } catch (RuntimeException e) {
          out.println("cleanup_failed_token_id=" + token.authTokenId());
          if (cleanup == null) cleanup = e;
          else cleanup.addSuppressed(e);
        }
      }
      if (cleanup != null) {
        if (failure != null) failure.addSuppressed(cleanup);
        else throw cleanup;
      }
    }
  }

  private static RequestOptions options() {
    return RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
  }
}
