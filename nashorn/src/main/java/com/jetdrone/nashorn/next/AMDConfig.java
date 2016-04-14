package com.jetdrone.nashorn.next;

import java.util.Collections;
import java.util.Map;

final class AMDConfig {

  private final String baseUrl;
  private final Map<String, String> paths;

  AMDConfig(String baseUrl, Map<String, String> paths) {
    this.baseUrl = baseUrl;
    this.paths = paths != null ? paths : Collections.emptyMap();
  }


  String getBaseUrl() {
    return baseUrl;
  }

  Map<String, String> getPaths() {
    return paths;
  }
}
