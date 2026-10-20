package com.qoder.cloudagents.core;

/** Explicit wire representation, independent of Java bean serialization. */
public interface JsonSerializable {
  JsonValue toJsonValue();
}
