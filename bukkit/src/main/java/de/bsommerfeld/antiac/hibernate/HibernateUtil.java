package de.bsommerfeld.antiac.hibernate;

import de.bsommerfeld.antiac.hibernate.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;

@Slf4j
public class HibernateUtil {

  private static SessionFactory sessionFactory;

  public static void setupHibernate() {
    try {
      StandardServiceRegistry registry = HibernateConfig.getHibernateConfiguration();
      sessionFactory = buildSessionFactory(registry);
    } catch (Exception e) {
      log.error("SessionFactory creation failed", e);
      throw new ExceptionInInitializerError("Initial SessionFactory creation failed: " + e);
    }
  }

  private static SessionFactory buildSessionFactory(StandardServiceRegistry registry) {
    MetadataSources sources = new MetadataSources(registry);
    sources.addAnnotatedClass(LogEntry.class);
    return sources.buildMetadata().buildSessionFactory();
  }

  public static Session openSession() {
    return sessionFactory.openSession();
  }

  public static void shutdown() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
  }
}
