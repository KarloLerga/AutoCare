package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Otvara jednu tvornicu EntityManagera za životni vijek aplikacije. */
public final class DatabaseConfig {

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    Map<String, Object> properties = new HashMap<>();
    String host = required("AUTOCARE_DB_HOST");
    String database = required("AUTOCARE_DB_NAME");
    String username = required("AUTOCARE_DB_USER");
    String password = required("AUTOCARE_DB_PASSWORD");

    String jdbcUrl =
        "jdbc:sqlserver://"
            + host
            + ":1433;databaseName="
            + database
            + ";encrypt=true;trustServerCertificate=false;";

    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", username);
    properties.put("jakarta.persistence.jdbc.password", password);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + ".");
    }
    return value.strip();
  }
}
