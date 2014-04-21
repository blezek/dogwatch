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
}
