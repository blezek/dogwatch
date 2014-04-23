package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.User;
import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.UserDAO;
import com.github.dogwatch.db.WatchDAO;

@Path("/lookout/{id}")
public class LookoutResource {

  private WatchDAO watchDAO;
  private UserDAO userDAO;

  public LookoutResource(WatchDAO watchDAO, UserDAO userDAO) {
    this.watchDAO = watchDAO;
    this.userDAO = userDAO;
  }

  @GET
  @UnitOfWork
  public Response get(@PathParam("id") String id, @Context UriInfo uriInfo) {
    Watch watch = watchDAO.getByUID(id);
    User user = userDAO.getByUID(id);
    if (watch == null && user == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    // Try to find a named watch
    if (watch == null) {
      if (uriInfo.getQueryParameters().containsKey("watch")) {
        String name = uriInfo.getQueryParameters().getFirst("watch");
        for (Watch w : user.watches) {
          if (w.name.equals(name)) {
            watch = w;
          }
        }
      }
      if (watch == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
    }
    Heartbeat heartbeat = new Heartbeat();
    heartbeat.instant = new DateTime();

    for (String p : new String[] { "m", "message" }) {
      if (uriInfo.getQueryParameters().containsKey(p)) {
        heartbeat.message = uriInfo.getQueryParameters().getFirst(p);
      }
    }
    for (String p : new String[] { "s", "status" }) {
      if (uriInfo.getQueryParameters().containsKey(p)) {
        heartbeat.status = uriInfo.getQueryParameters().getFirst(p);
      }
    }

    heartbeat.watch = watch;
    watchDAO.saveHeartbeat(heartbeat);
    return Response.ok("Land Ho!", MediaType.TEXT_PLAIN).build();
  }
}
