package com.github.dogwatch.core;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public long id;

  public String email;

  public Boolean activated = false;
  public String activation_hash;

  // Never pass password and salt out
  @JsonIgnore
  public String password;
  @JsonIgnore
  public String salt;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
  public Set<Role> roles = new HashSet<Role>();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
  public Set<Watch> watches = new HashSet<Watch>();

  public void setPassword(String password, int hashIterations) {
    Random rng = new SecureRandom();

    String salt = Long.toString(rng.nextLong());
    this.password = new Sha512Hash(password, ByteSource.Util.bytes(salt), hashIterations).toHex();
    // save the salt with the new account. The HashedCredentialsMatcher
    // will need it later when handling login attempts:
    this.salt = salt;
  }

}
