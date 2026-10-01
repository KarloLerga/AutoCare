package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Učitava lokalne postavke baze i otvara JPA persistence sloj. */
public class DatabaseConfig {

  private DatabaseConfig() {
  }

  /** Otvara EntityManagerFactory koristeći postavke iz database.properties u rootu projekta. */
  public static EntityManagerFactory open() {
    Properties settings = new Properties();

    try (FileInputStream input = new FileInputStream("database.properties")) {
      settings.load(input);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Ne mogu učitati database.properties iz root mape projekta.", exception);
    }

    String host = settings.getProperty("database.host");
    String port = settings.getProperty("database.port");
    String database = settings.getProperty("database.name");
    String username = settings.getProperty("database.username");
    String password = settings.getProperty("database.password");

    if (host == null || host.isBlank()
        || port == null || port.isBlank()
        || database == null || database.isBlank()
        || username == null || username.isBlank()
        || password == null || password.isBlank()) {
      throw new IllegalStateException(
          "Nedostaju postavke baze u database.properties.");
    }

    String jdbcUrl = "jdbc:sqlserver://" + host + ":" + port
        + ";databaseName=" + database
        + ";encrypt=true;trustServerCertificate=false;";

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", username);
    properties.put("jakarta.persistence.jdbc.password", password);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
