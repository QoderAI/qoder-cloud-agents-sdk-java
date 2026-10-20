package com.qoder.cloudagents.auth;

import java.util.concurrent.CompletableFuture;

/** Resolves a token per attempt. Implementations must be thread-safe and return promptly. */
@FunctionalInterface
public interface Credential {
  CompletableFuture<String> getToken();
}
