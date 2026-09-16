package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Otvara jednu tvornicu EntityManagera za zivotni vijek aplikacije. */
public final class DatabaseConfig {

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    SqlSettings settings = SqlSettings.fromEnvironment();
    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", settings.getJdbcUrl());
    properties.put("jakarta.persistence.jdbc.user", settings.getUsername());
    properties.put("jakarta.persistence.jdbc.password", settings.getPassword());

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
