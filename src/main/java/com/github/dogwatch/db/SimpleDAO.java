package com.github.dogwatch.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.github.dogwatch.core.Watch;
import com.google.common.base.Optional;

public class SimpleDAO<T> extends AbstractDAO<T> {

  public SimpleDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Optional<T> findById(Long id) {
    return Optional.fromNullable(get(id));
  }

  public T create(T t) {
    return persist(t);
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll(Class<T> c) {
    return currentSession().createCriteria(c).list();
  }

  public T update(T t) {
    currentSession().saveOrUpdate(t);
    return t;
  }

  public void delete(Watch watch) {
    currentSession().delete(watch);
  }

  public void commit() {
    if (currentSession().getTransaction() != null && currentSession().getTransaction().isActive()) {
      currentSession().getTransaction().commit();
    }
  }
}
