package de.bsommerfeld.antiac.hibernate;

import de.bsommerfeld.antiac.AntiAC;
import de.bsommerfeld.antiac.AntiACConfig;
import de.bsommerfeld.antiac.hibernate.enums.DatabaseDialect;
import de.bsommerfeld.antiac.hibernate.enums.DatabaseDriver;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

public class HibernateConfig {

  public static StandardServiceRegistry getHibernateConfiguration() {
    AntiACConfig config = AntiAC.getInstance().getConfiguration();
    Map<String, Object> settings = getHibernateSettings(config);
    return buildServiceRegistry(settings);
  }

  private static Map<String, Object> getHibernateSettings(AntiACConfig config) {
    Map<String, Object> settings = new HashMap<>();
    settings.put(Environment.DRIVER, getDriverClass(config));
    settings.put(Environment.DIALECT, getDialectClass(config));
    settings.put(Environment.URL, config.getDatabaseUrl());
    settings.put(Environment.USER, config.getDatabaseUsername());
    settings.put(Environment.PASS, config.getDatabasePassword());
    settings.put(Environment.HBM2DDL_AUTO, "update");
    return settings;
  }

  private static String getDriverClass(AntiACConfig config) {
    return DatabaseDriver.valueOf(config.getDatabaseDriver().toUpperCase()).getDriverClass();
  }

  private static String getDialectClass(AntiACConfig config) {
    return DatabaseDialect.valueOf(config.getDatabaseDialect().toUpperCase()).getDialectClass();
  }

  private static StandardServiceRegistry buildServiceRegistry(Map<String, Object> settings) {
    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
    registryBuilder.applySettings(settings);
    return registryBuilder.build();
  }
}
