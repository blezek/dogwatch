package com.github.dogwatch;

import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dogwatch.jobs.JobManager;

public class Singletons {
  public static ExecutorService threadPool;
  public static DataSource dataSource;
  public static DogWatchConfiguration configuration;
  public static Environment environment;
  public static JobManager jobManager;
  public static ObjectMapper objectMapper;

  public static HtmlEmail newEmail() throws EmailException {
    HtmlEmail email = new ImageHtmlEmail();
    email.setHostName(configuration.email.smtpHostname);
    email.setSmtpPort(configuration.email.port);
    email.setAuthenticator(new DefaultAuthenticator(configuration.email.username, configuration.email.password));
    email.setSSLOnConnect(configuration.email.ssl);
    email.setFrom(configuration.email.from);

    return email;
  }
}
