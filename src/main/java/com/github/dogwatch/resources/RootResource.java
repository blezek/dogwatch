package com.github.dogwatch.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RootResource {

  @GET
  public Response get() throws URISyntaxException {
    return Response.temporaryRedirect(new URI("/dogwatch/")).build();
  }
}
