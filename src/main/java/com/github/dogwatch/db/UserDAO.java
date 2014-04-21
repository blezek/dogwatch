package com.github.dogwatch.db;

import org.apache.shiro.subject.Subject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.github.dogwatch.core.User;

public class UserDAO extends SimpleDAO<User> {

  public UserDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public User getFromSubject(Subject subject) {
    if (subject == null || subject.getPrincipal() == null) {
      return null;
    }
    User user = (User) currentSession().createCriteria(User.class).add(Restrictions.eq("email", subject.getPrincipal().toString())).uniqueResult();
    return user;
  }

  public User findByHash(String hash) {
    return (User) currentSession().createCriteria(User.class).add(Restrictions.eq("activation_hash", hash)).uniqueResult();
  }

}
