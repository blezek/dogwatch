package com.github.dogwatch.jobs;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.quartz.CronExpression;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dogwatch.Singletons;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.WatchDAO;
import com.github.dogwatch.resources.LoginResource;
import com.google.common.base.Optional;

public class LookoutJob extends Job {
  static Logger logger = LoggerFactory.getLogger(LookoutJob.class);

  @Override
  public void doJob(JobExecutionContext context) {
    // Check on any missed watches
    Long id = (Long) context.getJobDetail().getJobDataMap().get("id");
    logger.info("Looking at Job: " + context.getJobDetail().getJobDataMap().get("id"));
    WatchDAO watchDAO = new WatchDAO(Singletons.sessionFactory);
    final Optional<Watch> watch = watchDAO.findById(id);
    if (!watch.isPresent()) {
      logger.info("Could not find watch " + id);
      return;
    }
    // Did we have a heartbeat within the worry time?
    if (!watchDAO.haveHeartbeat(watch.get())) {
      Singletons.threadPool.execute(new Runnable() {

        @Override
        public void run() {
          try {
            User user = watch.get().user;
            HtmlEmail email = Singletons.newEmail();
            Map<String, Object> input = new HashMap<String, Object>();
            input.put("watch", watch);
            input.put("configuration", Singletons.configuration);
            input.put("user", user);
            Singletons.renderEmail(email, "missed", input);
            email.setSubject("[Dogwatch] Missing checkin for " + watch.get().name);
            email.addTo(user.email);
            email.send();
          } catch (EmailException e) {
            LoggerFactory.getLogger(LoginResource.class).error("error sending email", e);
          }
        }
      });
      try {
        watch.get().scheduleCheck(context.getScheduler(), new CronExpression(watch.get().cron));
        watchDAO.update(watch.get());
      } catch (ParseException e) {
        logger.error("Error parsing cron!", e);
      } catch (Exception e) {
        logger.error("Error scheduling next check!", e);
      }

    }
  }
}
