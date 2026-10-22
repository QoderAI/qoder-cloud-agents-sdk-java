package com.qoder.cloudagents;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.core.*;
import com.qoder.cloudagents.forward.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.Test;

class ForwardFieldParityTest {
  @Test
  void imageFileAndCreationMountAccessReachTheWire() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.start();
      try (ForwardClient client =
          ForwardClient.builder()
              .pat("test-pat")
              .baseUrl(server.url("/api/v1/forward").toString())
              .build()) {
        server.enqueue(new MockResponse().setBody("{\"data\":[]}"));
        ImageSource image = ImageSource.builder().type("file").fileId("file_image").build();
        client
            .sessions()
            .events()
            .send(
                "sess_example",
                SessionEventSendParams.builder()
                    .events(
                        Collections.singletonList(
                            SessionEventParam.builder()
                                .type("user.message")
                                .content(
                                    EventContent.blocks(
                                        Collections.singletonList(
                                            ContentBlock.builder()
                                                .type("image")
                                                .source(image)
                                                .build())))
                                .build()))
                    .build());
        JsonValue event =
            JsonValue.parse(server.takeRequest().getBody().readUtf8())
                .get("events")
                .asArray()
                .get(0);
        assertEquals(
            "file_image",
            event.get("content").asArray().get(0).get("source").get("file_id").asString());
        assertEquals(
            "file_image",
            ImageSource.fromJson(event.get("content").asArray().get(0).get("source")).fileId());

        server.enqueue(new MockResponse().setBody("{\"id\":\"memstore_example\"}"));
        client
            .async()
            .memoryStores()
            .create(
                MemoryStoreCreateParams.builder()
                    .name("memory")
                    .mount(
                        MemoryStoreCreateMount.builder()
                            .identityId("idn_example")
                            .templateId("tmpl_example")
                            .access("read_write")
                            .build())
                    .build(),
                RequestOptions.builder().idempotencyKey("create-memory").build())
            .get(3, TimeUnit.SECONDS);
        RecordedRequest request = server.takeRequest();
        assertEquals("create-memory", request.getHeader("Idempotency-Key"));
        assertEquals(
            "read_write",
            JsonValue.parse(request.getBody().readUtf8()).get("mount").get("access").asString());
      }
    }
  }

  @Test
  void missingVersionIsUnknownAndPairingTimestampsRemainOptional() {
    JsonValue json =
        JsonValue.parse(
            "{\"initial_events_version\":0,\"created_at\":\"2026-10-22T00:00:00Z\",\"updated_at\":null}");
    assertEquals(Long.valueOf(0), new Schedule(json, null).initialEventsVersion());
    assertEquals(Long.valueOf(0), new ScheduleRun(json, null).initialEventsVersion());
    assertNull(new Schedule(JsonValue.parse("{}"), null).initialEventsVersion());
    assertNull(new ScheduleRun(JsonValue.parse("{}"), null).initialEventsVersion());
    ChannelPairing pairing = new ChannelPairing(json, null);
    assertEquals("2026-10-22T00:00:00Z", pairing.createdAt());
    assertNull(pairing.updatedAt());
    assertTrue(ImageSource.builder().type("file").build().field("file_id").isMissing());
    assertTrue(ImageSource.builder().type("file").fileId(null).build().field("file_id").isNull());
    assertTrue(
        MemoryStoreCreateMount.builder()
            .identityId("i")
            .templateId("t")
            .build()
            .field("access")
            .isMissing());
  }
}
