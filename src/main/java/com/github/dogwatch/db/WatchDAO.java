package com.github.dogwatch.db;

import java.util.List;

import org.apache.shiro.subject.Subject;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Watch;

public class WatchDAO extends SimpleDAO<Watch> {

  public WatchDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public List<Watch> findAllForSubject(Subject subject) {
    Criteria c = currentSession().createCriteria(Watch.class, "w");
    c.addOrder(Order.desc("w.timestamp"));
    c.createAlias("users", "u", JoinType.INNER_JOIN, Restrictions.eq("email", subject.getPrincipal()));
    return c.list();
  }

  List<Heartbeat> lastHeartbeats(Watch watch, int count) {
    return null;
  }

}
