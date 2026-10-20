package com.qoder.cloudagents.examples;

import static com.qoder.cloudagents.examples.ForwardLiveCheck.*;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.errors.ApiStatusException;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Instant;
import java.util.*;

/** Verifies two newly issued tokens and two newly created Identities only. */
final class ForwardTokenLiveCheck {
  static void run(ForwardClient manager, String base, String key, PrintStream out) {
    List<ServiceAccountToken> pending = new ArrayList<>();
    RuntimeException failure = null;
    try {
      ServiceAccountTokenCreateParams params =
          ServiceAccountTokenCreateParams.builder()
              .ttlSeconds(600)
              .metadata(Collections.singletonMap("purpose", "java-sdk-permission-check"))
              .build();
      ServiceAccountToken admin = manager.serviceAccountTokens().create(key, params, options());
      pending.add(admin);
      validate(admin, null);
      log(out, "issued_admin_sat", "auth_token_id", admin.authTokenId());
      try (ForwardClient runtime = runtime(base, admin);
          IdentityFixture owned = new IdentityFixture(runtime, out);
          IdentityFixture other = new IdentityFixture(runtime, out)) {
        runtime.templates().list(TemplateListParams.builder().limit(1).build(), options());
        ServiceAccountToken bound =
            manager.identities().serviceAccountTokens().create(owned.id, key, params, options());
        pending.add(bound);
        validate(bound, owned.id);
        log(
            out,
            "issued_identity_sat",
            "auth_token_id",
            bound.authTokenId(),
            "identity_id",
            owned.id);
        try (ForwardClient identityClient = runtime(base, bound)) {
          configs(identityClient, owned.id);
          denied(() -> configs(identityClient, other.id), false, out, "cross_identity_denied");
          revoke(manager, key, bound, out);
          pending.remove(bound);
          denied(() -> configs(identityClient, owned.id), true, out, "revoked_identity_denied");
        }
      }
      // Keep the admin token usable until resource cleanup has finished.
      revoke(manager, key, admin, out);
      pending.remove(admin);
      try (ForwardClient runtime = runtime(base, admin)) {
        denied(
            () ->
                runtime.templates().list(TemplateListParams.builder().limit(1).build(), options()),
            true,
            out,
            "revoked_admin_denied");
      }
    } catch (RuntimeException error) {
      failure = error;
      throw error;
    } finally {
      RuntimeException cleanup = null;
      Collections.reverse(pending);
      for (ServiceAccountToken token : pending) {
        try {
          revoke(manager, key, token, out);
        } catch (RuntimeException error) {
          log(out, "cleanup_failed_sat", "auth_token_id", token.authTokenId());
          if (cleanup == null) cleanup = error;
          else cleanup.addSuppressed(error);
        }
      }
      if (cleanup != null) {
        if (failure != null) failure.addSuppressed(cleanup);
        else throw cleanup;
      }
    }
  }

  private static void configs(ForwardClient client, String identity) {
    client
        .identities()
        .configs()
        .list(identity, IdentityConfigListParams.builder().limit(1).build(), options());
  }

  private static ForwardClient runtime(String base, ServiceAccountToken token) {
    return ForwardClient.builder()
        .baseUrl(base)
        .authMode(AuthMode.SA)
        .accessToken(token.accessToken())
        .maxRetries(0)
        .build();
  }

  private static void validate(ServiceAccountToken token, String identity) {
    require(
        "service_account_token".equals(token.type()) && "Bearer".equals(token.tokenType()),
        "Unexpected SAT type");
    require(
        token.authTokenId() != null
            && token.credentialId() != null
            && token.serviceAccountId() != null
            && token.accessToken() != null
            && !token.accessToken().isEmpty(),
        "SAT lacks required fields");
    require(
        token.expiresIn() != null
            && token.expiresIn() > 0
            && token.expiresIn() <= 600
            && token.expiresAt() != null
            && Instant.parse(token.expiresAt()).isAfter(Instant.now()),
        "SAT expiry differs");
    require(
        (identity == null ? "admin" : "identity").equals(token.subjectType())
            && Objects.equals(identity, token.identityId()),
        "SAT subject binding differs");
  }

  private static void revoke(
      ForwardClient manager, String key, ServiceAccountToken token, PrintStream out) {
    ServiceAccountTokenRevokeParams.Builder params =
        ServiceAccountTokenRevokeParams.builder().reason("java-sdk-permission-check-complete");
    if (token.credentialId() != null) params.targetCredentialId(token.credentialId());
    if (token.expiresAt() != null) params.targetExpiresAt(token.expiresAt());
    require(
        Boolean.TRUE.equals(
            manager
                .serviceAccountTokens()
                .revoke(token.authTokenId(), key, params.build(), options())
                .revoked()),
        "SAT revoke not acknowledged");
    log(out, "revoked_sat", "auth_token_id", token.authTokenId());
  }

  private static void denied(Runnable call, boolean revoked, PrintStream out, String event) {
    try {
      call.run();
    } catch (ApiStatusException error) {
      require(
          error.statusCode() == 403 || (revoked && error.statusCode() == 401),
          "Unexpected denial status");
      log(out, event, "status", error.statusCode(), "request_id", error.requestId());
      return;
    }
    require(false, "Request should have been denied");
  }
}
