package com.github.dogwatch.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailConfiguration {
  @NotNull
  @JsonProperty
  public String smtpHostname;

  @JsonProperty
  public Integer port = 25;

  @NotNull
  @JsonProperty
  public String username;

  @NotNull
  @JsonProperty
  public String password;

  @NotNull
  @JsonProperty
  public Boolean ssl = false;

  @NotNull
  @JsonProperty
  public String from;
}
