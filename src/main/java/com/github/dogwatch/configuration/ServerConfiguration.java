package com.github.dogwatch.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerConfiguration {
  @Valid
  @NotNull
  @JsonProperty
  public String host;

  @Valid
  @JsonProperty
  public String templatePath;

  public String getHost() {
    return host;
  }

  public String getTemplatePath() {
    return templatePath;
  }
}
