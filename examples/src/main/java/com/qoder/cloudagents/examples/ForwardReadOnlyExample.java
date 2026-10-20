package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.RequestOptions;
import com.qoder.cloudagents.forward.Model;
import com.qoder.cloudagents.forward.Template;
import com.qoder.cloudagents.forward.TemplateListParams;
import com.qoder.cloudagents.forward.TemplateListResponse;
import java.io.PrintStream;
import java.time.Duration;

/**
 * Lists models/templates and optionally retrieves one template; does not create or delete
 * resources.
 */
public final class ForwardReadOnlyExample {
  private ForwardReadOnlyExample() {}

  public static void main(String[] args) throws Exception {
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      run(setup.client(), System.getenv("QODER_TEMPLATE_ID"), System.out);
    }
  }

  public static void run(ForwardClient client, String templateId, PrintStream output)
      throws Exception {
    RequestOptions options = RequestOptions.builder().totalTimeout(Duration.ofSeconds(30)).build();
    for (Model model : client.models().list(options).data())
      output.println("model=" + model.id() + " name=" + model.displayName());
    TemplateListResponse page =
        client
            .async()
            .templates()
            .list(TemplateListParams.builder().limit(10).build(), options)
            .get();
    for (Template template : page.data())
      output.println("template=" + template.id() + " name=" + template.name());
    output.println("has_more=" + page.hasMore() + " request_id=" + page.requestId());
    if (templateId != null && !templateId.isEmpty()) {
      Template template = client.templates().retrieve(templateId, options);
      output.println("selected=" + template.id() + " request_id=" + template.requestId());
    }
  }
}
