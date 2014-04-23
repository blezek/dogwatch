package com.github.dogwatch.jobs;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.hibernate.Session;
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

public class LookoutJob extends DogwatchJob {
  static Logger logger = LoggerFactory.getLogger(LookoutJob.class);

  @Override
  public void doJob(JobExecutionContext context) {

    Long id = (Long) context.getJobDetail().getJobDataMap().get("id");
    logger.info("Looking at Job: " + context.getJobDetail().getJobDataMap().get("id"));
    WatchDAO watchDAO = new WatchDAO(Singletons.sessionFactory);
    final Optional<Watch> watchOptional = watchDAO.findById(id);
    if (!watchOptional.isPresent()) {
      logger.info("Could not find watch " + id);
      return;
    }
    final Watch watch = watchOptional.get();
    // Did we have a heartbeat within the worry time?
    if (!watchDAO.haveHeartbeat(watch)) {
      watch.consecutive_failed_checks++;

      Singletons.threadPool.execute(new Runnable() {

        @Override
        public void run() {
          try {
            User user = watch.user;
            HtmlEmail email = Singletons.newEmail();
            Map<String, Object> input = new HashMap<String, Object>();
            input.put("watch", watch);
            input.put("configuration", Singletons.configuration);
            input.put("user", user);
            Singletons.renderEmail(email, "missing", input);
            email.setSubject("[Dogwatch] Missing checkin for " + watch.name);
            email.addTo(user.email);
            email.send();
          } catch (EmailException e) {
            LoggerFactory.getLogger(LoginResource.class).error("error sending email", e);
          }
        }
      });
    } else {
      watch.consecutive_failed_checks = 0;
    }
    try {
      if (watch.consecutive_failed_checks < 10) {
        watch.scheduleCheck(context.getScheduler(), new CronExpression(watch.cron));
      }
      watchDAO.update(watch);
    } catch (ParseException e) {
      logger.error("Error parsing cron!", e);
    } catch (Exception e) {
      logger.error("Error scheduling next check!", e);
    }
  }

}
