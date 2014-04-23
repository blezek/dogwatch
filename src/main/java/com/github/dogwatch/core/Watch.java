package com.github.dogwatch.core;

import static org.quartz.JobBuilder.newJob;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.redhogs.cronparser.CronExpressionDescriptor;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dogwatch.jobs.LookoutJob;
import com.github.dogwatch.resources.SimpleResponse;

@Entity
@Table(name = "watches")
public class Watch {
  static Logger logger = LoggerFactory.getLogger(Watch.class);

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String name;
  public String uid;
  public String cron;
  public String explanation;
  public String status;
  public String description;
  public int consecutive_failed_checks;
  public int worry;
  public boolean active;

  @Column
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime last_check;

  @Column
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime next_check;

  @Column
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime expected;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(name = "watch_user", joinColumns = { @JoinColumn(name = "watch_id") }, inverseJoinColumns = { @JoinColumn(name = "user_id") })
  public Set<User> users = new HashSet<User>();

  @JsonIgnore
  @ManyToOne
  public User user;

  public void scheduleCheck(Scheduler scheduler, CronExpression expression) throws Exception {
    // First cancel any possible pending checks
    JobKey jobKey = new JobKey(Long.toString(id), "lookout");
    if (scheduler.checkExists(jobKey)) {
      scheduler.deleteJob(jobKey);
    }
    // Create a map for this job
    JobDataMap jobData = new JobDataMap();
    jobData.put("id", id);
    JobDetail jobBuilder = newJob(LookoutJob.class).withIdentity(jobKey).usingJobData(jobData).build();
    Date triggerStartTime;
    triggerStartTime = expression.getNextValidTimeAfter(new Date());
    // Add our watch amount...
    expected = new DateTime(triggerStartTime.getTime());
    next_check = new DateTime(expected.plusMinutes(worry).getMillis());
    logger.info("Scheduled job for " + name + "(" + id + ") next scheduled time is " + expected + " will begin to worry at " + next_check);
    Trigger trigger = TriggerBuilder.newTrigger().startAt(expected.toDate()).forJob(jobBuilder).build();
    scheduler.scheduleJob(jobBuilder, trigger);

  }

  public Map<String, Object> validate() {
    List<String> messages = new ArrayList<String>();
    SimpleResponse r = new SimpleResponse("explanation", "");
    r.put("valid", true);
    if (name == null) {
      messages.add("Please give your watch a name");
      r.put("valid", false);
    }
    if (worry < 1) {
      messages.add("Worry time must be greater than 1 minute");
      r.put("valid", false);
    }
    try {
      new CronExpression(cron);
      r.put("explanation", CronExpressionDescriptor.getDescription(cron));
    } catch (ParseException e) {
      messages.add("Error parsing cron expression at character " + e.getErrorOffset() + " '" + e.getLocalizedMessage() + "'");
      r.put("valid", false);
    }
    r.put("messages", messages);
    return r;
  }

}
