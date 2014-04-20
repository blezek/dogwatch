package com.github.dogwatch.db;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Watch;

public class WatchDAO extends SimpleDAO<Watch> {

  public WatchDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  public List<Heartbeat> lastHeartbeats(Watch watch, int count) {
    Query query = currentSession().createQuery("from Heartbeat where watch_id = :id order by instant");
    query.setLong("id", watch.id);
    query.setMaxResults(count);
    return (List<Heartbeat>) query.list();
  }
}
