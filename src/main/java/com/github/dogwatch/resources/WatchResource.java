package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.redhogs.cronparser.CronExpressionDescriptor;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.secnod.shiro.jaxrs.Auth;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dogwatch.Singletons;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.UserDAO;
import com.github.dogwatch.db.WatchDAO;
import com.google.common.base.Optional;

@Path("/rest/watch")
public class WatchResource {

  private final WatchDAO watchDAO;
  private final UserDAO userDAO;
  Scheduler scheduler;

  public WatchResource(WatchDAO watchDAO, UserDAO userDAO) {
    this.watchDAO = watchDAO;
    this.userDAO = userDAO;
    this.scheduler = Singletons.jobManager.getScheduler();
  }

  @Path("/test")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response testDate() {
    return Response.ok(new SimpleResponse("time", new DateTime())).build();
  }

  @GET
  @UnitOfWork
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWatches(@Auth Subject subject) {
    User user = userDAO.getFromSubject(subject);
    if (user == null) {
      return Response.serverError().entity(new SimpleResponse("message", "Could not find user")).build();
    }
    // Force Hibernate to fetch
    user.watches.size();
    return Response.ok(user.watches).build();
  }

  // Make this an admin resource
  @GET
  @Path("/scheduled")
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response listJobs() throws SchedulerException {
    ObjectNode json = Singletons.objectMapper.createObjectNode();

    ArrayNode jobs = json.putArray("jobs");
    // enumerate each job group
    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
      JobDetail detail = scheduler.getJobDetail(jobKey);
      ObjectNode job = jobs.addObject();

      job.put("key", detail.getKey().toString());
      JobDataMap map = detail.getJobDataMap();
      job.put("name", map.containsKey("name") ? map.getString("name") : "unknown");
      job.put("email", map.containsKey("email") ? map.getString("email") : "unknown");
      // List<Trigger> triggerList = (List<Trigger>) ;
      ArrayNode triggers = job.putArray("triggers");
      for (Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
        ObjectNode triggerObject = triggers.addObject();
        triggerObject.put("next", trigger.getNextFireTime().toString());
      }
    }
    return Response.ok(json).build();
  }

  @Path("/validate")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response validateWatch(Watch watch) {
    Map<String, Object> r = watch.validate();
    return Response.ok(r).build();
  }

  @POST
  @RequiresUser
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createWatch(@Auth Subject subject, Watch watch) {
    User user = userDAO.getFromSubject(subject);

    Map<String, Object> r = watch.validate();
    boolean valid = (Boolean) r.get("valid");
    if (!valid) {
      return Response.serverError().entity(r).build();
    }

    watch.uid = UUID.randomUUID().toString();
    watch.user = user;
    // user.watches.add(watch);

    watch.worry = Math.abs(watch.worry);
    // Try to parse the cron setting
    CronExpression expression;
    try {
      expression = new CronExpression(watch.cron);
      watch.explanation = CronExpressionDescriptor.getDescription(watch.cron);
      watch = watchDAO.create(watch);
      watch.scheduleCheck(scheduler);
      watch = watchDAO.update(watch);
    } catch (ParseException e) {
      return Response.serverError().entity("Error parsing cron expression at character " + e.getErrorOffset() + " '" + e.getLocalizedMessage() + "'").build();
    } catch (Exception e) {
      return Response.serverError().entity("Error scheduling check '" + e.getLocalizedMessage() + "'").build();
    }

    return Response.ok(watch).build();
  }

  @PUT
  @RequiresUser
  @UnitOfWork
  @Path("/{id: [1-9][0-9]*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateWatch(@Auth Subject subject, Watch watchUpdate, @PathParam("id") Long id) {
    User user = userDAO.getFromSubject(subject);
    if (user == null) {
      return Response.serverError().build();
    }
    Watch watch = watchDAO.findById(id).get();
    if (watch == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    watch.update(watchUpdate);

    Map<String, Object> r = watch.validate();
    boolean valid = (Boolean) r.get("valid");
    if (!valid) {
      return Response.serverError().entity(r).build();
    }

    try {
      CronExpression expression = new CronExpression(watch.cron);
      watch.explanation = CronExpressionDescriptor.getDescription(watch.cron);
      watch.scheduleCheck(scheduler);
    } catch (ParseException e) {
      return Response.serverError().entity("Error parsing cron expression at character " + e.getErrorOffset() + " '" + e.getLocalizedMessage() + "'").build();
    } catch (Exception e) {
      return Response.serverError().entity("Error scheduling check '" + e.getLocalizedMessage() + "'").build();
    }
    watch.consecutive_failed_checks = 0;
    return Response.ok(watchDAO.update(watch)).build();
  }

  @DELETE
  @RequiresUser
  @Path("/{id: [1-9][0-9]*}")
  @UnitOfWork
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteWatch(@Auth Subject subject, @PathParam("id") long id) {
    User user = userDAO.getFromSubject(subject);
    Watch watch = watchDAO.findById(id).get();
    if (watch == null) {
      return Response.serverError().entity(new SimpleResponse("message", "unknown watch")).build();
    }
    if (watch.user.id != user.id) {
      return Response.serverError().entity(new SimpleResponse("message", "unknown watch")).build();
    }
    watchDAO.delete(watch);
    return Response.ok().build();
  }

  // Lookouts
  @GET
  @Path("/{id: [1-9][0-9]*}/lookout")
  @RequiresUser
  @UnitOfWork
  @Produces(MediaType.APPLICATION_JSON)
  public Response getLookouts(@Auth Subject subject, @PathParam("id") long id) {
    User user = userDAO.getFromSubject(subject);
    Optional<Watch> watch = watchDAO.findById(id);
    if (!watch.isPresent()) {
      return Response.serverError().entity(new SimpleResponse("message", "unknown watch")).build();
    }
    if (watch.get().user.id == user.id) {
      return Response.ok(watchDAO.lastHeartbeats(watch.get(), 10)).build();
    } else {
      return Response.serverError().entity(new SimpleResponse("message", "unknown watch")).build();
    }
  }

  @GET
  @RequiresUser
  @Path("/tz")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTZ(@QueryParam("timezone") String timezone) {
    if (timezone == null) {
      timezone = "";
    }
    timezone = "(.*)" + timezone.toLowerCase() + "(.*)";
    ObjectNode json = Singletons.objectMapper.createObjectNode();
    ArrayNode timezones = json.putArray("timezones");
    for (String tz : Singletons.Timezones) {
      if (tz.toLowerCase().matches(timezone)) {
        timezones.add(tz);
      }
    }
    return Response.ok(json).build();
  }
}
