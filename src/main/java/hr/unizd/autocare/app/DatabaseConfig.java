package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Učitava lokalnu konfiguraciju baze i otvara EntityManagerFactory. */
public final class DatabaseConfig {
  private static final String CONFIG_FILE = "connection.local.properties";

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    Properties config = loadConfig();
    String host = required(config, "host");
    String database = required(config, "database");
    String username = required(config, "user");
    String password = required(config, "password");
    String port = config.getProperty("port", "1433").strip();

    String jdbcUrl =
        "jdbc:sqlserver://"
            + host
            + ":"
            + port
            + ";databaseName="
            + database
            + ";encrypt=true;trustServerCertificate=false;";

    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", username);
    properties.put("jakarta.persistence.jdbc.password", password);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }

  private static Properties loadConfig() {
    Properties config = new Properties();
    Path path = Path.of(CONFIG_FILE);

    try (InputStream input = Files.newInputStream(path)) {
      config.load(input);
    } catch (IOException exception) {
      throw new IllegalArgumentException(
          "Nedostaje " + CONFIG_FILE + " u glavnom direktoriju projekta.");
    }

    return config;
  }

  private static String required(Properties config, String name) {
    String value = config.getProperty(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + " u " + CONFIG_FILE + ".");
    }
    return value.strip();
  }
}
