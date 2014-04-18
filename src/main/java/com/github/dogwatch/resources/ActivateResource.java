package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.github.dogwatch.core.User;
import com.github.dogwatch.db.UserDAO;

@Path("/activate")
public class ActivateResource {

  private UserDAO userDAO;

  public ActivateResource(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  @UnitOfWork
  @GET
  @Path("/{hash}")
  public Response activate(@PathParam("hash") String hash) throws URISyntaxException {

    User user = userDAO.findByHash(hash);
    if (user == null || user.activated) {
      return Response.temporaryRedirect(new URI("/dogwatch/#/hash/error")).build();
    }
    user.activated = true;
    userDAO.update(user);
    return Response.temporaryRedirect(new URI("/dogwatch/#/hash")).build();
  }
}
