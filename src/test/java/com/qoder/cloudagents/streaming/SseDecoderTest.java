package com.qoder.cloudagents.streaming;

import static org.junit.jupiter.api.Assertions.*;

import com.qoder.cloudagents.errors.ResponseValidationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SseDecoderTest {
  SseDecoder decoder(String text) {
    return new SseDecoder(
        new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), "initial");
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r"})
  void lineEndingsBomCommentsAndMultilineData(String nl) throws Exception {
    SseDecoder d =
        decoder(
            "\ufeff: heartbeat"
                + nl
                + nl
                + "id: evt_1"
                + nl
                + "event: future.event"
                + nl
                + "unknown: value"
                + nl
                + "retry: 100"
                + nl
                + "data: {"
                + nl
                + "data: \"text\":\"你好😀\"}"
                + nl
                + nl);
    SseDecoder.Frame frame = d.next();
    assertEquals("evt_1", frame.id);
    assertEquals("future.event", frame.type);
    assertEquals("{\n\"text\":\"你好😀\"}", frame.data);
    assertNull(d.next());
  }

  @Test
  void utf8SplitAtEveryByte() throws Exception {
    byte[] bytes = "data: 你好😀\n\n".getBytes(StandardCharsets.UTF_8);
    InputStream oneByte =
        new ByteArrayInputStream(bytes) {
          @Override
          public synchronized int read(byte[] b, int off, int len) {
            return super.read(b, off, Math.min(len, 1));
          }
        };
    assertEquals("你好😀", new SseDecoder(oneByte, null).next().data);
  }

  @Test
  void emptyIdResetsAndNulIdIsIgnored() throws Exception {
    SseDecoder d = decoder("id: one\ndata: {}\n\nid:\ndata: {}\n\nid: invalid\0id\ndata: {}\n\n");
    assertEquals("one", d.next().id);
    assertEquals("", d.next().id);
    assertEquals("", d.next().id);
  }

  @Test
  void idOnlyFrameCannotSkipUndeliveredEvent() throws Exception {
    SseDecoder d = decoder("id: undelivered\n\ndata: {}\n\n");
    assertEquals("initial", d.next().id);
  }

  @ParameterizedTest
  @ValueSource(strings = {"id: lost\ndata: {}", "id: lost\ndata: {}\n", ": heartbeat\n", ""})
  void incompleteFramesNotDispatched(String input) throws Exception {
    assertNull(decoder(input).next());
  }

  @Test
  void sizeLimitForLineAndAccumulatedFrame() {
    for (String text :
        new String[] {
          "data: " + String.join("", java.util.Collections.nCopies(100, "a")),
          "data: 1234\ndata: 5678\ndata: 9012\n\n"
        }) {
      SseDecoder d =
          new SseDecoder(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), null, 24);
      assertThrows(ResponseValidationException.class, d::next);
    }
  }

  @Test
  void invalidUtf8IsNotSilentlyReplaced() {
    byte[] bytes = new byte[] {'d', 'a', 't', 'a', ':', ' ', (byte) 0xc3, 0x28, '\n', '\n'};
    assertThrows(
        java.nio.charset.CharacterCodingException.class,
        () -> new SseDecoder(new ByteArrayInputStream(bytes), null).next());
  }
}
