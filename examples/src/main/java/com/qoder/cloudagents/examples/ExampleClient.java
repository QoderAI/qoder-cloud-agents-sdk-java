package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.auth.Region;
import com.qoder.cloudagents.auth.ServiceAccountCredential;

/**
 * Example-only auth selection. Business examples share this setup instead of duplicating PAT/SA
 * flows.
 */
public final class ExampleClient implements AutoCloseable {
  private final ForwardClient client;
  private final ServiceAccountCredential credential;

  private ExampleClient(ForwardClient client, ServiceAccountCredential credential) {
    this.client = client;
    this.credential = credential;
  }

  public static ExampleClient fromEnvironment() {
    ForwardClient.Builder builder = ForwardClient.builder();
    ServiceAccountCredential credential = null;
    String mode = System.getenv("QODER_AUTH_MODE");
    if ("sa".equals(mode)) {
      builder.authMode(AuthMode.SA);
      String key = System.getenv("QODER_SA_KEY");
      if (key != null && !key.isEmpty()) {
        String region = System.getenv("QODER_REGION");
        if (!"cn".equals(region) && !"global".equals(region))
          throw new IllegalArgumentException("Set QODER_REGION to cn or global");
        credential =
            ServiceAccountCredential.builder()
                .saKey(key)
                .region("cn".equals(region) ? Region.CN : Region.GLOBAL)
                .scopes("qca.access", "forward.access")
                .build();
        builder.credential(credential);
      }
    } else if (mode != null && !"pat".equals(mode)) {
      throw new IllegalArgumentException("Set QODER_AUTH_MODE to pat or sa");
    }
    try {
      return new ExampleClient(builder.build(), credential);
    } catch (RuntimeException error) {
      if (credential != null) credential.close();
      throw error;
    }
  }

  public ForwardClient client() {
    return client;
  }

  @Override
  public void close() {
    client.close();
    if (credential != null) credential.close();
  }
}
