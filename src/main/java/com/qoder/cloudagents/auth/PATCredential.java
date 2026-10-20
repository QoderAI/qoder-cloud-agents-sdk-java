package com.qoder.cloudagents.auth;

import com.qoder.cloudagents.core.internal.Authentication;
import java.util.concurrent.CompletableFuture;

public final class PATCredential implements Credential {
  private final String token;

  public PATCredential(String token) {
    this.token = Authentication.validateToken(token);
  }

  public static PATCredential fromEnv() {
    return new PATCredential(System.getenv("QODER_PAT"));
  }

  @Override
  public CompletableFuture<String> getToken() {
    return CompletableFuture.completedFuture(token);
  }

  @Override
  public String toString() {
    return "PATCredential([redacted])";
  }
}
