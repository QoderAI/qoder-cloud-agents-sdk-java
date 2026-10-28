package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ManagedClient;
import com.qoder.cloudagents.auth.AuthMode;
import com.qoder.cloudagents.auth.Region;
import com.qoder.cloudagents.auth.ServiceAccountCredential;

/** Example-only PAT, static SAT or SA Key configuration for the Managed API. */
public final class ManagedExampleClient implements AutoCloseable {
  private final ManagedClient client;
  private final ServiceAccountCredential credential;

  private ManagedExampleClient(ManagedClient client, ServiceAccountCredential credential) {
    this.client = client;
    this.credential = credential;
  }

  public static ManagedExampleClient fromEnvironment() {
    ManagedClient.Builder builder = ManagedClient.builder();
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
                .scopes("qca.access")
                .build();
        builder.credential(credential);
      }
    } else if (mode != null && !"pat".equals(mode))
      throw new IllegalArgumentException("Set QODER_AUTH_MODE to pat or sa");
    try {
      return new ManagedExampleClient(builder.build(), credential);
    } catch (RuntimeException error) {
      if (credential != null) credential.close();
      throw error;
    }
  }

  public ManagedClient client() {
    return client;
  }

  public void close() {
    client.close();
    if (credential != null) credential.close();
  }
}
