package com.qoder.cloudagents.streaming;

import com.qoder.cloudagents.errors.ResponseValidationException;
import java.io.*;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Internal incremental decoder. An incomplete frame never changes the last dispatched ID. */
final class SseDecoder {
  private final Reader reader;
  private final int limit;
  private boolean first = true, skipLf;
  private String lastId;

  SseDecoder(InputStream source, String initialId) {
    this(source, initialId, 8 * 1024 * 1024);
  }

  SseDecoder(InputStream source, String initialId, int limit) {
    reader =
        new BufferedReader(
            new InputStreamReader(
                source,
                StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)));
    lastId = initialId;
    this.limit = limit;
  }

  Frame next() throws IOException {
    String id = lastId, type = "message";
    StringBuilder data = new StringBuilder();
    boolean hasData = false;
    int size = 0;
    for (; ; ) {
      String line = line();
      if (line == null) return null;
      size += line.length() + 1;
      if (size > limit) throw new ResponseValidationException("SSE frame exceeds size limit");
      if (line.isEmpty()) {
        if (hasData) {
          lastId = id;
          return new Frame(id, type, data.substring(0, data.length() - 1));
        }
        id = lastId;
        type = "message";
        size = 0;
        continue;
      }
      int colon = line.indexOf(':');
      String key = colon < 0 ? line : line.substring(0, colon);
      String value = colon < 0 ? "" : line.substring(colon + 1);
      if (value.startsWith(" ")) value = value.substring(1);
      switch (key) {
        case "data":
          data.append(value).append('\n');
          hasData = true;
          break;
        case "event":
          type = value;
          break;
        case "id":
          if (value.indexOf('\0') < 0) id = value;
          break;
        default:
          break; // Heartbeats, retry hints and future fields do not yield events.
      }
    }
  }

  private String line() throws IOException {
    StringBuilder line = new StringBuilder();
    for (; ; ) {
      int ch = reader.read();
      if (ch < 0) return null; // No blank-line delimiter: the frame is incomplete.
      if (first) {
        first = false;
        if (ch == '\ufeff') continue;
      }
      if (skipLf) {
        skipLf = false;
        if (ch == '\n') continue;
      }
      if (ch == '\r') {
        skipLf = true;
        return line.toString();
      }
      if (ch == '\n') return line.toString();
      line.append((char) ch);
      if (line.length() > limit)
        throw new ResponseValidationException("SSE line exceeds size limit");
    }
  }

  static final class Frame {
    final String id, type, data;

    Frame(String id, String type, String data) {
      this.id = id;
      this.type = type;
      this.data = data;
    }
  }
}
