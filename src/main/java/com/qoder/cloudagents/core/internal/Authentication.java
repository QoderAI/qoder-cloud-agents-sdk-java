package com.qoder.cloudagents.core.internal;

import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.auth.Credential;
import com.qoder.cloudagents.auth.ServiceAccountCredential;
import com.qoder.cloudagents.errors.ConfigurationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Internal configuration resolver shared by all Forward transports. */
public final class Authentication {
  private Authentication() {}

  public static Credential resolve(
      AuthMode requested,
      String pat,
      String accessToken,
      Credential credential,
      String host,
      Function<String, String> environment) {
    boolean nativeSA = credential instanceof ServiceAccountCredential;
    AuthMode mode = requested != null ? requested : nativeSA ? AuthMode.SA : AuthMode.PAT;
    if ((pat != null ? 1 : 0) + (accessToken != null ? 1 : 0) + (credential != null ? 1 : 0) > 1)
      throw new ConfigurationException("Choose one explicit credential source");
    if (mode == AuthMode.SA && pat != null)
      throw new ConfigurationException("PAT cannot be used in SA mode");
    if (nativeSA) {
      if (mode != AuthMode.SA)
        throw new ConfigurationException("ServiceAccountCredential requires SA mode");
      ((ServiceAccountCredential) credential).validateRegion(host);
    }
    if (credential != null) return credential;
    String token = accessToken != null ? accessToken : pat;
    if (token == null) {
      token = nonEmpty(environment.apply("QODER_ACCESS_TOKEN"));
      if (token == null && mode == AuthMode.PAT) token = nonEmpty(environment.apply("QODER_PAT"));
    }
    // Token management can use a per-call SA Key without a business credential.
    // Ordinary requests still fail locally before sending any unauthenticated request.
    if (token == null)
      return () ->
          Futures.failed(
              new ConfigurationException(
                  "Configure accessToken, credential or QODER_ACCESS_TOKEN (QODER_PAT in PAT"
                      + " mode)"));
    final String resolved = validateToken(token);
    return () -> CompletableFuture.completedFuture(resolved);
  }

  private static String nonEmpty(String value) {
    return value == null || value.isEmpty() ? null : value;
  }

  public static String validateToken(String token) {
    if (token == null || token.isEmpty())
      throw new ConfigurationException(
          "Configure accessToken, credential or QODER_ACCESS_TOKEN (QODER_PAT in PAT mode)");
    for (int i = 0; i < token.length(); i++) {
      char c = token.charAt(i);
      if (c <= 32 || c >= 127)
        throw new ConfigurationException("Token contains invalid header characters");
    }
    return token;
  }
}
