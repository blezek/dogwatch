package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.text.ParseException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.subject.Subject;
import org.quartz.CronExpression;
import org.quartz.Scheduler;
import org.secnod.shiro.jaxrs.Auth;

import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.WatchDAO;
import com.github.dogwatch.jobs.JobManager;

@Path("/rest/watch")
public class WatchResource {

  private final WatchDAO watchDAO;
  Scheduler scheduler;

  public WatchResource(WatchDAO watchDAO, JobManager jobManager) {
    this.watchDAO = watchDAO;
    this.scheduler = jobManager.getScheduler();
  }

  @GET
  @UnitOfWork
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWatches(@Auth Subject subject) {
    return Response.ok(watchDAO.findAllForSubject(subject)).build();
  }

  @POST
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createWatch(@Auth Subject subject, Watch watch) {
    watch.uid = UUID.randomUUID().toString();

    watch.worry = Math.abs(watch.worry);
    // Try to parse the cron setting
    CronExpression expression;
    try {
      expression = new CronExpression(watch.cron);
      watch = watchDAO.create(watch);
      watch.scheduleCheck(scheduler, expression);
      watch = watchDAO.update(watch);
    } catch (ParseException e) {
      return Response.serverError().entity("Error parsing cron expression at character " + e.getErrorOffset() + " '" + e.getLocalizedMessage() + "'").build();
    } catch (Exception e) {
      return Response.serverError().entity("Error scheduling check '" + e.getLocalizedMessage() + "'").build();
    }

    return Response.ok(watch).build();
  }

  @PUT
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Watch updateWatch(@Auth Subject subject, Watch watch) {
    return watchDAO.update(watch);
  }

  @DELETE
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteWatch(@Auth Subject subject, Watch watch) {
    watchDAO.delete(watch);
    return Response.ok().build();
  }

}
