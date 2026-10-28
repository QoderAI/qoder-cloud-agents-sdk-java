package com.qoder.cloudagents.examples;

import com.qoder.cloudagents.ForwardClient;
import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.util.*;

/** Search is read-only; Decision and Dream creation require explicit execution opt-in. */
public final class ForwardCapabilitiesExample {
  private ForwardCapabilitiesExample() {}

  public static FileListResponse searchFiles(ForwardClient client, String name, String identityId) {
    FileSearchParams.Builder params = FileSearchParams.builder().name(name).limit(5);
    if (identityId != null) params.identityId(identityId);
    return client.files().search(params.build());
  }

  public static Decision decide(ForwardClient client, String idempotencyKey) {
    return client
        .decisions()
        .create(
            DecisionCreateParams.builder()
                .state(Collections.singletonMap("resolved", JsonValue.of(true)))
                .questions(
                    Collections.singletonList(
                        DecisionQuestion.builder()
                            .type("choice")
                            .instructions("Is resolved true in the supplied state?")
                            .choices(
                                Arrays.asList(
                                    DecisionChoice.builder().value(true).build(),
                                    DecisionChoice.builder().value(false).build()))
                            .build()))
                .build(),
            RequestOptions.builder().idempotencyKey(idempotencyKey).build());
  }

  public static Dream createDream(
      ForwardClient client, String memoryStoreId, String idempotencyKey) {
    return client
        .dreams()
        .create(
            DreamCreateParams.builder()
                .inputs(
                    Collections.singletonList(
                        DreamInput.builder()
                            .type("memory_store")
                            .memoryStoreId(memoryStoreId)
                            .build()))
                .build(),
            RequestOptions.builder().idempotencyKey(idempotencyKey).build());
  }

  public static void main(String[] args) {
    String action = args.length == 0 ? "search" : args[0];
    if (!Arrays.asList("search", "pairings", "decision", "dream").contains(action))
      throw new IllegalArgumentException("Use search, pairings, decision or dream");
    if (action.equals("decision") || action.equals("dream")) {
      if (!"true".equals(System.getenv("QODER_ALLOW_WRITE"))
          || !"true".equals(System.getenv("QODER_ALLOW_EXECUTION")))
        throw new IllegalArgumentException(
            "Set QODER_ALLOW_WRITE=true and QODER_ALLOW_EXECUTION=true");
    }
    String store = System.getenv("QODER_MEMORY_STORE_ID");
    if (action.equals("dream") && (store == null || store.isEmpty()))
      throw new IllegalArgumentException("Set QODER_MEMORY_STORE_ID");
    try (ExampleClient setup = ExampleClient.fromEnvironment()) {
      ForwardClient client = setup.client();
      if (action.equals("search")) {
        for (FileMetadata file :
            searchFiles(
                    client,
                    args.length > 1 ? args[1] : "report",
                    System.getenv("QODER_IDENTITY_ID"))
                .data()) System.out.println("file=" + file.id());
      } else if (action.equals("pairings")) {
        for (ChannelPairing pairing :
            client
                .channelPairings()
                .list(ChannelPairingListParams.builder().limit(5).build())
                .data())
          System.out.println("pairing=" + pairing.id() + " status=" + pairing.status());
      } else {
        String key = UUID.randomUUID().toString();
        System.out.println("idempotency_key=" + key);
        if (action.equals("decision")) {
          Decision decision = decide(client, key);
          System.out.println("decision=" + decision.id() + " status=" + decision.status());
        } else {
          Dream dream = createDream(client, store, key);
          System.out.println("dream=" + dream.id() + " status=" + dream.status());
          // Keep the task and its output for inspection; do not delete or replace the input Store.
        }
      }
    }
  }
}
