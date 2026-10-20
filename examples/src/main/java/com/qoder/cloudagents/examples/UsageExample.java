package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.*;
import java.io.PrintStream;
import java.time.Duration;

/** Read-only grouped usage for an explicit Asia/Shanghai hourly window. */
public final class UsageExample {
  private UsageExample() {}

  public static void main(String[] args) {
    String start = System.getenv("QODER_USAGE_START_AT"), end = System.getenv("QODER_USAGE_END_AT");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), start, end, System.out);
    }
  }

  public static void run(ForwardClient client, String start, String end, PrintStream out) {
    UsageListParams p = UsageListParams.builder().startAt(start).endAt(end).limit(20).build();
    RequestOptions o = RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
    // These calls read only the first page. Iterate a response to fetch additional pages lazily.
    IdentityUsageListResponse identities = client.usage().listIdentities(p, o);
    for (IdentityUsage u : identities.data())
      out.println(
          "identity="
              + u.identityId()
              + " active_seconds="
              + u.activeSeconds()
              + " credits="
              + u.credits());
    TemplateUsageListResponse templates = client.usage().listTemplates(p, o);
    for (TemplateUsage u : templates.data())
      out.println(
          "template="
              + u.templateId()
              + " active_identities="
              + u.activeIdentities()
              + " credits="
              + u.credits());
    out.println(
        "identity_has_more=" + identities.hasMore() + " template_has_more=" + templates.hasMore());
  }
}
