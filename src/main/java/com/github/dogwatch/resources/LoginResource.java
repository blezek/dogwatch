package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.mail.HtmlEmail;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dogwatch.Singletons;
import com.github.dogwatch.core.Role;
import com.github.dogwatch.core.User;
import com.github.dogwatch.db.UserDAO;

@Path("/login")
public class LoginResource {
  static Logger logger = LoggerFactory.getLogger(LoginResource.class);
  UserDAO userDAO;
  int hashIterations;
  Random rng = new SecureRandom();

  public LoginResource(UserDAO userDAO, int hashIterations) {
    this.userDAO = userDAO;
    this.hashIterations = hashIterations;
  }

  @GET
  @UnitOfWork
  public Response checkLogin(@Auth Subject subject) {
    User user = userDAO.getFromSubject(subject);
    ObjectNode json = Singletons.objectMapper.createObjectNode();
    json.putPOJO("user", user);
    json.put("isAuthenticated", subject.isAuthenticated());
    json.put("isRemembered", subject.isRemembered());
    json.put("host", Singletons.configuration.dogwatch.host);
    return Response.ok(json).build();
  }

  @POST
  @UnitOfWork
  @Path("/logout")
  public Response logout(@Auth Subject subject) {
    subject.logout();
    return Response.ok().build();
  }

  @POST
  @UnitOfWork
  public Response login(@FormParam("username") String username, @FormParam("password") String password, @FormParam("remember") Boolean remember, @Auth Subject subject) {
    if (remember == null) {
      remember = false;
    }
    if (username == null || password == null) {
      return Response.serverError().entity("username and password required").build();
    }
    try {
      subject.login(new UsernamePasswordToken(username, password, remember));
    } catch (AuthenticationException e) {
      return Response.serverError().entity(new SimpleResponse("message", "Login failed")).build();
    }
    return Response.ok(new SimpleResponse("message", username)).build();
  }

  @UnitOfWork
  @POST
  @Path("/lostpassword")
  public String lostPassword(@FormParam("username") String email) {

    final User user = userDAO.findByEmail(email);
    if (user == null) {
      return "Email sent!";
    }
    user.activation_hash = UUID.randomUUID().toString();
    userDAO.update(user);
    Singletons.threadPool.execute(new Runnable() {

      @Override
      public void run() {
        try {
          HtmlEmail email = Singletons.newEmail();
          Map<String, Object> input = new HashMap<String, Object>();
          input.put("configuration", Singletons.configuration);
          input.put("user", user);
          Singletons.renderEmail(email, "forgot", input);
          email.setSubject("Forgotten password for Dogwatch");
          email.addTo(user.email);
          email.send();
        } catch (Exception e) {
          LoggerFactory.getLogger(LoginResource.class).error("error sending email", e);
        }
      }
    });
    return "Email sent!";
  }

  @UnitOfWork
  @POST
  @Path("/register")
  public Response register(@FormParam("username") String username, @FormParam("email") String email, @FormParam("password") String password, @Auth Subject subject) {
    // Note that a normal app would reference an attribute rather
    // than create a new RNG every time:

    if (username == null || email == null || password == null) {
      return Response.status(Status.BAD_REQUEST).entity(new SimpleResponse("message", "Must provide username, email and password")).build();
    }

    String salt = Long.toString(rng.nextLong());

    User user = new User();
    user.username = username;
    user.email = username;
    // Now hash the plain-text password with the random salt and multiple
    // iterations and then Base64-encode the value (requires less space than
    // Hex):
    user.password = new Sha512Hash(password, ByteSource.Util.bytes(salt), hashIterations).toHex();
    // save the salt with the new account. The HashedCredentialsMatcher
    // will need it later when handling login attempts:
    user.salt = salt;
    user.activated = false;
    user.activation_hash = UUID.randomUUID().toString();

    // Add the user in the user role
    Role role = new Role();
    role.role = "user";
    role.user = user;
    user.roles.add(role);
    try {
      user = userDAO.create(user);
    } catch (Exception e) {
      return Response.status(Status.BAD_REQUEST).entity(new SimpleResponse("message", "Failed to create user")).build();
    }
    sendActivationEmail(user);
    userDAO.commit();
    try {
      subject.login(new UsernamePasswordToken(username, password));
    } catch (AuthenticationException e) {
      logger.error("Error registering in", e);
    }

    return Response.ok().build();
  }

  @UnitOfWork
  @POST
  @Path("/changepassword")
  public Response changePassword(@FormParam("hash") String hash, @FormParam("password") String password, @FormParam("password_match") String passwordMatch, @Auth Subject subject) {
    SimpleResponse r = new SimpleResponse();
    if (!password.equals(passwordMatch)) {
      r.put("updated", false);
      r.put("message", "Passwords do not match");
      return Response.serverError().entity(r).build();
    }
    if (hash == null && subject.isAuthenticated()) {
      User user = userDAO.getFromSubject(subject);

      user.setPassword(password, hashIterations);
      user.activated = true;
      userDAO.update(user);
      r.put("updated", true);
      return Response.ok(r).build();
    }
    if (hash != null) {
      User user = userDAO.findByHash(hash);
      if (user == null) {
        r.put("updated", false);
        r.put("message", "Could not find hash");
        return Response.serverError().entity(r).build();
      }
      user.setPassword(password, hashIterations);
      user.activated = true;
      userDAO.update(user);
      r.put("updated", true);
      return Response.ok(r).build();
    }
    r.put("updated", false);
    r.put("message", "Unable to process");
    return Response.serverError().entity(r).build();
  }

  void sendActivationEmail(final User user) {
    Singletons.threadPool.execute(new Runnable() {

      @Override
      public void run() {
        try {
          HtmlEmail email = Singletons.newEmail();
          Map<String, Object> input = new HashMap<String, Object>();
          input.put("configuration", Singletons.configuration);
          input.put("user", user);
          Singletons.renderEmail(email, "activate", input);
          email.setSubject("Welcome to DogWatch");
          email.addTo(user.email);
          email.send();
        } catch (Exception e) {
          LoggerFactory.getLogger(LoginResource.class).error("error sending email", e);
        }
      }
    });

  }

}
