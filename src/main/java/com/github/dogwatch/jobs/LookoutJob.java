package com.github.dogwatch.jobs;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookoutJob extends Job {
  static Logger logger = LoggerFactory.getLogger(LookoutJob.class);

  @Override
  public void doJob(JobExecutionContext context) {
    // Check on any missed watches
    logger.info("Looking at Job: " + context.getJobDetail().getJobDataMap().get("id"));
    // logger.info("Looking for missing heartbeats");
  }

}
