package com.github.dogwatch.db;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.github.dogwatch.core.User;

public class UserDAO extends SimpleDAO<User> {

  public UserDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public User findByHash(String hash) {
    return (User) currentSession().createCriteria(User.class).add(Restrictions.eq("activation_hash", hash)).uniqueResult();
  }

}
