package com.github.dogwatch.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailConfiguration {
  @Valid
  @NotNull
  @JsonProperty
  public String smtpHostname;

  @Valid
  @JsonProperty
  public Integer port = 25;

  @Valid
  @NotNull
  @JsonProperty
  public String username;

  @Valid
  @NotNull
  @JsonProperty
  public String password;

  @Valid
  @JsonProperty
  public Boolean ssl = false;

  @Valid
  @NotNull
  @JsonProperty
  public String from;
}
