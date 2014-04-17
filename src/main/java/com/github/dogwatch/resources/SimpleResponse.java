package com.github.dogwatch.resources;

import java.util.HashMap;

public class SimpleResponse extends HashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  public SimpleResponse() {
    super();
  }

  public SimpleResponse(String key, Object value) {
    super();
    put(key, value);
  }
}
