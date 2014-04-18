package com.github.dogwatch;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.secnod.dropwizard.shiro.ShiroConfiguration;

import com.bazaarvoice.dropwizard.assets.AssetsBundleConfiguration;
import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dogwatch.configuration.EmailConfiguration;

public class DogWatchConfiguration extends Configuration implements AssetsBundleConfiguration {
  @Valid
  @NotNull
  @JsonProperty
  private DataSourceFactory database = new DataSourceFactory();

  @Valid
  @NotNull
  @JsonProperty
  public ShiroConfiguration shiro = new ShiroConfiguration();

  @Valid
  @NotNull
  @JsonProperty
  private final AssetsConfiguration assets = new AssetsConfiguration();

  @Valid
  @JsonProperty
  public String dbWeb = null;

  @Valid
  @NotNull
  @JsonProperty
  public final EmailConfiguration email = new EmailConfiguration();

  @Override
  public AssetsConfiguration getAssetsConfiguration() {
    return assets;
  }

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

}
