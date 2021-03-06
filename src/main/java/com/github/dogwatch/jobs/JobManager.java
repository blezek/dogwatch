package com.github.dogwatch.jobs;

import static org.quartz.JobBuilder.newJob;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.Managed;

import java.util.HashMap;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.PoolingConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobManager implements Managed {

  private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
  protected Scheduler scheduler;
  Map<JobDetail, String> jobs = new HashMap<JobDetail, String>();

  public JobManager(DataSourceFactory dsFactory) throws Exception {

    DBConnectionManager.getInstance().addConnectionProvider("internal", new PoolingConnectionProvider(dsFactory.getDriverClass(), dsFactory.getUrl(), dsFactory.getUser(), dsFactory.getPassword(), 10, "VALUES 1"));

    JobStoreTX jdbcJobStore = new JobStoreTX();
    jdbcJobStore.setDataSource("internal");

    SimpleThreadPool threadPool = new SimpleThreadPool(10, Thread.NORM_PRIORITY);
    DirectSchedulerFactory.getInstance().createScheduler(threadPool, jdbcJobStore);
    scheduler = DirectSchedulerFactory.getInstance().getScheduler();

  }

  @Override
  public void start() throws Exception {
    scheduler.start();
    for (JobDetail job : jobs.keySet()) {
      String cron = jobs.get(job);
      // Trigger the job to run now, and then every 40 seconds
      Trigger trigger = TriggerBuilder.newTrigger().startNow().withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
      scheduler.scheduleJob(job, trigger);
    }
  }

  @Override
  public void stop() throws Exception {
    scheduler.shutdown(true);
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  public void cronSchedule(Class<LookoutJob> jobClass, String cronExpression, Object... objects) throws Exception {
    // define the job and tie it to our DumbJob class
    JobDataMap data = new JobDataMap();
    if (objects.length % 2 != 0) {
      throw new Exception("data must be key-value pairs!");
    }
    for (int i = 0; i < objects.length; i += 2) {
      data.put((String) objects[i], objects[i + 1]);
    }
    JobDetail jobBuilder = newJob(jobClass).usingJobData(data).build();
    jobs.put(jobBuilder, cronExpression);
  }
}
