package com.qoder.cloudagents.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class Version {
  static final String CURRENT = load();

  private Version() {}

  private static String load() {
    try (InputStream stream =
        Version.class.getResourceAsStream("/com/qoder/cloudagents/version.properties")) {
      if (stream == null) throw new IllegalStateException("Missing SDK version resource");
      Properties values = new Properties();
      values.load(stream);
      return values.getProperty("version");
    } catch (IOException error) {
      throw new IllegalStateException("Cannot read SDK version", error);
    }
  }
}
