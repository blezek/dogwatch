package com.github.dogwatch;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dogwatch.jobs.JobManager;

public class Singletons {
  static Logger logger = LoggerFactory.getLogger(Singletons.class);
  public static ExecutorService threadPool;
  public static DataSource dataSource;
  public static DogWatchConfiguration configuration;
  public static Environment environment;
  public static JobManager jobManager;
  public static ObjectMapper objectMapper;
  public static Configuration freemarkerConfiguration;
  public static SessionFactory sessionFactory;

  public static HtmlEmail newEmail() throws EmailException {
    HtmlEmail email = new ImageHtmlEmail();
    email.setHostName(configuration.email.smtpHostname);
    email.setSmtpPort(configuration.email.port);
    email.setAuthenticator(new DefaultAuthenticator(configuration.email.username, configuration.email.password));
    email.setSSLOnConnect(configuration.email.ssl);
    email.setFrom(configuration.email.from);
    return email;
  }

  /**
   * Render the given template to the email. First looking for
   * template.html.ftl, then template.text.html, and falling back to
   * template.ftl.
   * 
   * @param email
   * @param template
   * @param input
   * @return
   * @throws IOException
   * @throws TemplateException
   */
  public static void renderEmail(HtmlEmail email, String name, Map<String, Object> input) {

    // Try the HTML
    try {
      Template template = freemarkerConfiguration.getTemplate(name + ".html.ftl");
      StringWriter writer = new StringWriter();
      template.process(input, writer);
      email.setHtmlMsg(writer.toString());
    } catch (Exception e) {
      logger.error("Error processing the html template", e);
    }

    // Try the Text
    boolean haveText = false;
    try {
      Template template = freemarkerConfiguration.getTemplate(name + ".text.ftl");
      StringWriter writer = new StringWriter();
      template.process(input, writer);
      email.setTextMsg(writer.toString());
      haveText = true;
    } catch (Exception e) {
      logger.error("Error processing the text template", e);
    }

    // Try the HTML
    try {
      if (!haveText) {
        Template template = freemarkerConfiguration.getTemplate(name + ".ftl");
        StringWriter writer = new StringWriter();
        template.process(input, writer);
        email.setMsg(writer.toString());
      }
    } catch (Exception e) {
      logger.error("Error processing the text template", e);
    }

  }
}
