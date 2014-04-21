package com.github.dogwatch.core;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

}
