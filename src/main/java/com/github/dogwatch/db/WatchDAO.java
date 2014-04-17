package com.github.dogwatch.db;

import java.util.List;

import org.apache.shiro.subject.Subject;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.github.dogwatch.core.Heartbeat;
import com.github.dogwatch.core.Watch;

public class WatchDAO extends SimpleDAO<Watch> {

  public WatchDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public List<Watch> findAllForSubject(Subject subject) {
    Criteria c = currentSession().createCriteria(Watch.class, "w");
    c.createAlias("w.user", "user");
    c.add(Restrictions.eq("user.email", subject.getPrincipal()));
    //
    // List cats = sess.createCriteria(Cat.class).createAlias("kittens",
    // "kt").createAlias("mate", "mt").add(Restrictions.eqProperty("kt.name",
    // "mt.name")).list();
    //
    // Criteria c = session.createCriteria(W.class, "dokument");
    // c.createAlias("dokument.role", "role"); // inner join by default
    // c.createAlias("role.contact", "contact");
    // c.add(Restrictions.eq("contact.lastName", "Test"));
    // return c.list();
    //
    // Query qry =
    // currentSession().createQuery("select v.vendorName, c.customerName from Vendor v Left Join v.children c");
    return c.list();
  }

  List<Heartbeat> lastHeartbeats(Watch watch, int count) {
    return null;
  }

}
