package com.github.dogwatch.managed;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.Managed;

import com.googlecode.flyway.core.Flyway;

public class FlywayMigration implements Managed {
  private DataSourceFactory dataSource;

  public FlywayMigration(DataSourceFactory dataSourceFactory) {
    this.dataSource = dataSourceFactory;
  }

  @Override
  public void start() throws Exception {
    Flyway flyway = new Flyway();
    flyway.setDataSource(this.dataSource.getUrl(), this.dataSource.getUser(), this.dataSource.getPassword());
    flyway.migrate();
  }

  @Override
  public void stop() throws Exception {

  }

}
