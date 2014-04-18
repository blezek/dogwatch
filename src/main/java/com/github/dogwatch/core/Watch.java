package com.github.dogwatch.core;

import static org.quartz.JobBuilder.newJob;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
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

import org.joda.time.DateTime;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.github.dogwatch.jobs.LookoutJob;

@Entity
@Table(name = "watches")
public class Watch {

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
  public int worry;
  public boolean active;
  public Timestamp next_check;
  public Timestamp last_check;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(name = "watch_user", joinColumns = { @JoinColumn(name = "watch_id") }, inverseJoinColumns = { @JoinColumn(name = "user_id") })
  public Set<User> users = new HashSet<User>();

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
    JobDetail jobBuilder = newJob(LookoutJob.class).usingJobData(jobData).build();
    Date triggerStartTime;
    if (last_check != null) {
      triggerStartTime = expression.getNextValidTimeAfter(last_check);
    } else {
      triggerStartTime = expression.getNextValidTimeAfter(new Date());
    }
    // Add our watch amount...
    DateTime t = new DateTime(triggerStartTime.getTime());
    t.plusMinutes(worry);

    Trigger trigger = TriggerBuilder.newTrigger().startAt(t.toDate()).forJob(jobBuilder).build();
    scheduler.scheduleJob(jobBuilder, trigger);
    next_check = new Timestamp(t.getMillis());
  }
}
