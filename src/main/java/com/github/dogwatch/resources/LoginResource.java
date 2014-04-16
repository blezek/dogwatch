package com.github.dogwatch.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.secnod.shiro.jaxrs.Auth;

import com.github.dogwatch.core.Role;
import com.github.dogwatch.core.User;
import com.github.dogwatch.db.SimpleDAO;

@Path("/login")
public class LoginResource {
  SimpleDAO<User> userDAO;
  int hashIterations;

  public LoginResource(SimpleDAO<User> userDAO, int hashIterations) {
    this.userDAO = userDAO;
    this.hashIterations = hashIterations;
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

    subject.login(new UsernamePasswordToken(username, password, remember));
    return Response.ok(username).build();
  }

  @UnitOfWork
  @POST
  @Path("/register")
  public String register(@FormParam("username") String username, @FormParam("password") String password, @FormParam("remember") Boolean remember, @Auth Subject subject) {
    UsernamePasswordToken t = new UsernamePasswordToken(username, password);

    // Note that a normal app would reference an attribute rather
    // than create a new RNG every time:
    Random rng = new SecureRandom();

    String salt = Long.toString(rng.nextLong());

    User user = new User();
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
    user = userDAO.create(user);
    return username;
  }
}
