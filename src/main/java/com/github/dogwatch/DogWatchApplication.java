package com.github.dogwatch;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.flywaydb.core.Flyway;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.github.dogwatch.bundle.CustomShiroBundle;
import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Role;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.SimpleDAO;
import com.github.dogwatch.db.UserDAO;
import com.github.dogwatch.db.WatchDAO;
import com.github.dogwatch.jobs.JobManager;
import com.github.dogwatch.managed.DBWebServer;
import com.github.dogwatch.resources.ActivateResource;
import com.github.dogwatch.resources.LoginResource;
import com.github.dogwatch.resources.LookoutResource;
import com.github.dogwatch.resources.RootResource;
import com.github.dogwatch.resources.WatchResource;

public class DogWatchApplication extends Application<DogWatchConfiguration> {
  static Logger logger = LoggerFactory.getLogger(DogWatchApplication.class);
  static int HashIterations = 100;

  private final HibernateBundle<DogWatchConfiguration> hibernate = new HibernateBundle<DogWatchConfiguration>(Watch.class, User.class, Role.class, Heartbeat.class) {
    @Override
    public DataSourceFactory getDataSourceFactory(DogWatchConfiguration configuration) {
      return configuration.getDataSourceFactory();
    }

    @Override
    protected void configure(org.hibernate.cfg.Configuration configuration) {
      super.configure(configuration);
      configuration.setProperty("hibernate.show_sql", "true");
      configuration.setProperty("show_sql", "true");
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
      realm.setDataSource(Singletons.dataSource);
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
    bootstrap.addBundle(new ConfiguredAssetsBundle("/public/", "/dogwatch/", "index.html"));
  }

  @Override
  public void run(DogWatchConfiguration configuration, Environment environment) throws Exception {

    // Register Joda time
    environment.getObjectMapper().registerModule(new JodaModule());
    environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Flyway
    Singletons.dataSource = configuration.getDataSourceFactory().build(environment.metrics(), "dogwatch");

    Flyway flyway = new Flyway();
    flyway.setDataSource(Singletons.dataSource);
    flyway.migrate();

    // Globals
    Singletons.threadPool = environment.lifecycle().executorService("dogwatch").build();
    Singletons.configuration = configuration;
    Singletons.environment = environment;
    Singletons.jobManager = new JobManager(configuration.getDataSourceFactory());
    Singletons.objectMapper = environment.getObjectMapper();
    Singletons.sessionFactory = hibernate.getSessionFactory();

    // Freemarker
    BeansWrapper.getDefaultInstance().setExposeFields(true);
    Configuration cfg = new Configuration();
    // Where do we load the templates from:

    ArrayList<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
    if (configuration.dogwatch.templatePath != null) {
      loaders.add(new FileTemplateLoader(new File(configuration.dogwatch.templatePath)));
    }
    loaders.add(new ClassTemplateLoader(this.getClass(), "/templates"));
    cfg.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[] {})));

    // Some other recommended settings:
    cfg.setIncompatibleImprovements(new Version(2, 3, 20));
    cfg.setDefaultEncoding("UTF-8");
    cfg.setLocale(Locale.US);
    DefaultObjectWrapper ow = new DefaultObjectWrapper();
    ow.setExposeFields(true);
    ow.setExposureLevel(BeansWrapper.EXPOSE_SAFE);
    cfg.setObjectWrapper(ow);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    Singletons.freemarkerConfiguration = cfg;

    environment.servlets().setSessionHandler(new SessionHandler());

    if (configuration.dbWeb != null) {
      environment.lifecycle().manage(new DBWebServer(configuration.dbWeb));
    }

    environment.lifecycle().manage(Singletons.jobManager);

    final WatchDAO watchDAO = new WatchDAO(hibernate.getSessionFactory());
    final UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
    SimpleDAO<Heartbeat> heartbeatDAO = new SimpleDAO<Heartbeat>(hibernate.getSessionFactory());

    environment.jersey().register(new WatchResource(watchDAO, userDAO));

    // Login
    environment.jersey().register(new LoginResource(userDAO, HashIterations));
    environment.jersey().register(new ActivateResource(userDAO));
    environment.jersey().register(new RootResource());
    environment.jersey().register(new LookoutResource(watchDAO, userDAO));
  }

}
