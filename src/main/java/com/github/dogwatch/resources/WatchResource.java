package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.dogwatch.core.Watch;
import com.github.dogwatch.db.SimpleDAO;

@Path("/watch")
public class WatchResource {

  private final SimpleDAO<Watch> watchDAO;

  public WatchResource(SimpleDAO<Watch> watchDAO) {
    this.watchDAO = watchDAO;
  }

  @GET
  @UnitOfWork
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWatches() {
    return Response.ok(watchDAO.findAll(Watch.class)).build();
  }

  @POST
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Watch createWatch(Watch watch) {
    watch.uid = UUID.randomUUID().toString();
    return watchDAO.create(watch);
  }

  @PUT
  @UnitOfWork
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Watch updateWatch(Watch watch) {
    return watchDAO.update(watch);
  }

}
