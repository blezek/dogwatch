package com.github.dogwatch;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Collection;
import java.util.HashSet;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.github.dogwatch.bundle.CustomShiroBundle;
import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Role;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.SimpleDAO;
import com.github.dogwatch.db.WatchDAO;
import com.github.dogwatch.jobs.JobManager;
import com.github.dogwatch.managed.DBWebServer;
import com.github.dogwatch.resources.LoginResource;
import com.github.dogwatch.resources.WatchResource;
import com.googlecode.flyway.core.Flyway;

public class DogWatchApplication extends Application<DogWatchConfiguration> {
  static Logger logger = LoggerFactory.getLogger(DogWatchApplication.class);
  static int HashIterations = 100;

  private final HibernateBundle<DogWatchConfiguration> hibernate = new HibernateBundle<DogWatchConfiguration>(Watch.class, User.class, Role.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(DogWatchConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }
  };

  private final CustomShiroBundle<DogWatchConfiguration> shiro = new CustomShiroBundle<DogWatchConfiguration>() {

    @Override
    protected ShiroConfiguration narrow(DogWatchConfiguration configuration) {
      return configuration.shiro;
    }

    @Override
    protected Collection<Realm> createRealms(DogWatchConfiguration configuration, Environment environment) throws Exception {
      JdbcRealm realm = new JdbcRealm();
      realm.setDataSource(configuration.getDataSourceFactory().build(environment.metrics(), "shiro"));
      realm.setSaltStyle(SaltStyle.COLUMN);
      realm.setAuthenticationQuery("select password, salt from users where email = ?");
      realm.setUserRolesQuery("select roles.role from roles, user_role, users where roles.id = user_role.id and users.id = user_role.user_id and users.email = ?");
      realm.setPermissionsQuery("select role_permission.permission from role_permission, roles where roles.role = ?");
      HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Sha512Hash.ALGORITHM_NAME);
      matcher.setHashIterations(HashIterations);
      // matcher.setStoredCredentialsHexEncoded(false);
      realm.setCredentialsMatcher(matcher);
      Collection<Realm> realms = new HashSet<Realm>();
      realms.add(realm);
      return realms;
    }
  };

  @Override
  public void initialize(Bootstrap<DogWatchConfiguration> bootstrap) {
    bootstrap.addBundle(hibernate);
    bootstrap.addBundle(shiro);
    bootstrap.addBundle(new ConfiguredAssetsBundle("/public/", "/dogwatch", "index.html"));
  }

  @Override
  public void run(DogWatchConfiguration configuration, Environment environment) throws Exception {
    environment.servlets().setSessionHandler(new SessionHandler());
    Flyway flyway = new Flyway();
    flyway.setDataSource(configuration.getDataSourceFactory().getUrl(), configuration.getDataSourceFactory().getUser(), configuration.getDataSourceFactory().getPassword());
    flyway.migrate();

    if (configuration.dbWeb != null) {
      environment.lifecycle().manage(new DBWebServer(configuration.dbWeb));
    }

    JobManager jobManager = new JobManager(configuration.getDataSourceFactory());

    environment.lifecycle().manage(jobManager);

    final WatchDAO watchDAO = new WatchDAO(hibernate.getSessionFactory());
    SimpleDAO<Heartbeat> heartbeatDAO = new SimpleDAO<Heartbeat>(hibernate.getSessionFactory());
    environment.jersey().register(new WatchResource(watchDAO, jobManager));

    // Login
    final SimpleDAO<User> userDAO = new SimpleDAO<User>(hibernate.getSessionFactory());
    environment.jersey().register(new LoginResource(userDAO, HashIterations));

  }

  public static void main(String[] args) throws Exception {
    new DogWatchApplication().run(args);
  }

}
