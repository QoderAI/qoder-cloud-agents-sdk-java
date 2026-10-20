package com.qoder.cloudagents.auth;

public enum Region {
  CN("https://openapi.qoder.com.cn/api/v1/serviceToken/exchange"),
  GLOBAL("https://openapi.qoder.sh/api/v1/serviceToken/exchange");
  private final String exchangeUrl;

  Region(String exchangeUrl) {
    this.exchangeUrl = exchangeUrl;
  }

  String exchangeUrl() {
    return exchangeUrl;
  }
}
