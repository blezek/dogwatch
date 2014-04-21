package com.github.dogwatch.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "heartbeats")
public class Heartbeat {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @JsonIgnore
  @ManyToOne
  public Watch watch;

  @Column
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime instant;
  public String message = "";
  public String status = "";
}
