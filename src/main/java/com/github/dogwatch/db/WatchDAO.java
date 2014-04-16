package com.github.dogwatch.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.github.dogwatch.core.Watch;
import com.google.common.base.Optional;

public class WatchDAO extends AbstractDAO<Watch> {

  public WatchDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Optional<Watch> findById(Long id) {
    return Optional.fromNullable(get(id));
  }

  public Watch create(Watch watch) {
    return persist(watch);
  }

  @SuppressWarnings("unchecked")
  public List<Watch> findAll() {
    return currentSession().createCriteria(Watch.class).list();
  }

  public Watch update(Watch watch) {
    return update(watch);
  }
}
