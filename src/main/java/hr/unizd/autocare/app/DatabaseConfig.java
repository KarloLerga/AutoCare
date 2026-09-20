package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Učitava lokalnu konfiguraciju baze i otvara EntityManagerFactory. */
public final class DatabaseConfig {
  private static final String CONFIG_PROPERTY = "autocare.config";
  private static final Path PROJECT_CONFIG = Path.of("connection.local.properties");
  private static final Path PRIVATE_CONFIG = Path.of("C:\\private-autocare\\connection.local.json");

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    ConfigValues config = loadConfig();
    Map<String, Object> properties = new HashMap<>();
    String host = required(config, "host");
    String database = required(config, "database");
    String username = required(config, "user");
    String password = required(config, "password");
    String port = config.value("port");
    if (port == null || port.isBlank()) {
      port = "1433";
    }

    String jdbcUrl =
        "jdbc:sqlserver://"
            + host
            + ":"
            + port.strip()
            + ";databaseName="
            + database
            + ";encrypt=true;trustServerCertificate=false;";

    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", username);
    properties.put("jakarta.persistence.jdbc.password", password);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }

  private static ConfigValues loadConfig() {
    Path path = configPath();
    try {
      if (path.toString().endsWith(".properties")) {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
          properties.load(input);
        }
        return new ConfigValues(properties, null);
      }
      return new ConfigValues(null, Files.readString(path, StandardCharsets.UTF_8));
    } catch (IOException exception) {
      throw new IllegalArgumentException("Nedostaje konfiguracija baze: " + path);
    }
  }

  private static Path configPath() {
    String configured = System.getProperty(CONFIG_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return Path.of(configured.strip());
    }
    if (Files.isRegularFile(PROJECT_CONFIG)) {
      return PROJECT_CONFIG;
    }
    return PRIVATE_CONFIG;
  }

  private static String required(ConfigValues config, String name) {
    String value = config.value(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + " u konfiguraciji baze.");
    }
    return value.strip();
  }

  private static final class ConfigValues {
    private final Properties properties;
    private final String json;

    private ConfigValues(Properties properties, String json) {
      this.properties = properties;
      this.json = json;
    }

    private String value(String name) {
      if (properties != null) {
        return properties.getProperty(name);
      }
      return jsonValue(json, name);
    }
  }

  private static String jsonValue(String json, String name) {
    String marker = "\"" + name + "\"";
    int keyIndex = json.indexOf(marker);
    if (keyIndex < 0) {
      return null;
    }

    int colon = json.indexOf(':', keyIndex + marker.length());
    int firstQuote = json.indexOf('\"', colon + 1);
    if (colon < 0 || firstQuote < 0) {
      return null;
    }

    StringBuilder value = new StringBuilder();
    boolean escaped = false;
    for (int index = firstQuote + 1; index < json.length(); index++) {
      char character = json.charAt(index);
      if (escaped) {
        value.append(character);
        escaped = false;
      } else if (character == '\\') {
        escaped = true;
      } else if (character == '\"') {
        return value.toString();
      } else {
        value.append(character);
      }
    }
    return null;
  }
}
