package com.github.dogwatch.db;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Watch;

public class WatchDAO extends SimpleDAO<Watch> {

  public WatchDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Watch getByUID(String uid) {
    Query query = currentSession().createQuery("from Watch where uid = :uid");
    query.setString("uid", uid);
    return (Watch) query.uniqueResult();
  }

  @SuppressWarnings("unchecked")
  public List<Heartbeat> lastHeartbeats(Watch watch, int count) {
    Query query = currentSession().createQuery("from Heartbeat where watch_id = :id order by instant");
    query.setLong("id", watch.id);
    query.setMaxResults(count);
    return (List<Heartbeat>) query.list();
  }

  public Heartbeat saveHeartbeat(Heartbeat entity) {
    currentSession().saveOrUpdate(checkNotNull(entity));
    return entity;
  }

  public boolean haveHeartbeat(Watch watch) {
    String queryString = "select count(*) from Heartbeat where watch_id = :id and instant > :last_check and instant < :next_check";

    Query query = currentSession().createQuery(queryString);
    query.setLong("id", watch.id);

    Timestamp last = new Timestamp(0);
    if (watch.last_check != null) {
      last = new Timestamp(watch.last_check.getMillis());
    }

    query.setTimestamp("last_check", last);
    query.setTimestamp("next_check", new Timestamp(watch.next_check.getMillis()));
    long count = ((Long) query.iterate().next()).longValue();
    return (count > 0);
  }
}
