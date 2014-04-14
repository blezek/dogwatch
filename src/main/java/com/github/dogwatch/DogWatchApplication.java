package com.github.dogwatch;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.SimpleDAO;
import com.github.dogwatch.jobs.JobManager;
import com.github.dogwatch.jobs.LookoutJob;
import com.github.dogwatch.managed.FlywayMigration;
import com.github.dogwatch.resources.WatchResource;

public class DogWatchApplication extends Application<DogWatchConfiguration> {
  static Logger logger = LoggerFactory.getLogger(DogWatchApplication.class);

  private final HibernateBundle<DogWatchConfiguration> hibernate = new HibernateBundle<DogWatchConfiguration>(Watch.class, User.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(DogWatchConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }
  };

  @Override
  public void initialize(Bootstrap<DogWatchConfiguration> bootstrap) {
    bootstrap.addBundle(hibernate);
  }

  @Override
  public void run(DogWatchConfiguration configuration, Environment environment) throws Exception {

    final FlywayMigration migration = new FlywayMigration(configuration.getDataSourceFactory());
    environment.lifecycle().manage(migration);

    final SimpleDAO<Watch> watchDAO = new SimpleDAO<Watch>(hibernate.getSessionFactory());
    SimpleDAO<Heartbeat> heartbeatDAO = new SimpleDAO<Heartbeat>(hibernate.getSessionFactory());
    environment.jersey().register(new WatchResource(watchDAO));

    JobManager jobManager = new JobManager();
    jobManager.cronSchedule(LookoutJob.class, "0/15 * * * * ?", "watchDAO", watchDAO, "heartbeatDAO", heartbeatDAO);
    environment.lifecycle().manage(jobManager);

  }

  public static void main(String[] args) throws Exception {
    new DogWatchApplication().run(args);
  }

}
