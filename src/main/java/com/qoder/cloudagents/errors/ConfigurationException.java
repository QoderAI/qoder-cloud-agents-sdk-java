package com.qoder.cloudagents.errors;

/** Invalid local client or credential configuration. Messages never include credentials. */
public final class ConfigurationException extends QcaException {
  public ConfigurationException(String message) {
    super(message);
  }
}
