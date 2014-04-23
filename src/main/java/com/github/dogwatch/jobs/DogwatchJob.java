package com.github.dogwatch.jobs;

import static com.codahale.metrics.MetricRegistry.name;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.github.dogwatch.Singletons;

public abstract class DogwatchJob implements org.quartz.Job {

  static Logger logger = LoggerFactory.getLogger(LookoutJob.class);

  abstract public void doJob(JobExecutionContext context) throws JobExecutionException;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("dogwatch-jobs");
    Timer timer = metricRegistry.timer(name(getClass(), getClass().getName()));

    Context timerContext = timer.time();

    final Session session = Singletons.sessionFactory.openSession();
    try {
      // configureSession(session);
      ManagedSessionContext.bind(session);
      session.beginTransaction();
      try {
        doJob(context);
        Transaction transaction = session.getTransaction();
        if (transaction != null && transaction.isActive()) {
          transaction.commit();
        }
      } catch (Exception e) {
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.isActive()) {
          txn.rollback();
        }
        throw new JobExecutionException(e);
      }
    } finally {
      timerContext.stop();
      session.close();
      ManagedSessionContext.unbind(Singletons.sessionFactory);
    }

  }
}