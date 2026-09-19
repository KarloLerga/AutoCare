package hr.unizd.autocare.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Konfiguracija developerskih SQL naredbi. */
final class SetupSqlSettings {
  private final String host;
  private final int port;
  private final String database;
  private final String username;
  private final String password;

  private SetupSqlSettings(
      String host, int port, String database, String username, String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
  }

  static SetupSqlSettings environment() {
    return from(System.getenv(), false);
  }

  static SetupSqlSettings discovery() {
    return from(System.getenv(), true);
  }

  private static SetupSqlSettings from(Map<String, String> environment, boolean discovery) {
    String host = required(environment, "AUTOCARE_DB_HOST").strip();
    if (!host.matches("[a-zA-Z0-9][a-zA-Z0-9.-]{0,252}")) {
      throw new IllegalArgumentException("Nevaljan naziv SQL servera.");
    }

    int port;
    try {
      port = Integer.parseInt(environment.getOrDefault("AUTOCARE_DB_PORT", "1433").strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("SQL port mora biti broj.");
    }
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("SQL port je izvan dopuštenog raspona.");
    }

    String database = discovery ? "master" : required(environment, "AUTOCARE_DB_NAME").strip();
    if (!database.matches("[a-zA-Z0-9_][a-zA-Z0-9_. -]{0,127}")) {
      throw new IllegalArgumentException("Nevaljan naziv baze.");
    }
    String lowerName = database.toLowerCase(Locale.ROOT);
    if (!discovery
        && (lowerName.equals("master")
            || lowerName.equals("tempdb")
            || lowerName.equals("model")
            || lowerName.equals("msdb"))) {
      throw new IllegalArgumentException("Aplikacija ne smije koristiti sistemsku bazu.");
    }

    return new SetupSqlSettings(
        host,
        port,
        database,
        required(environment, "AUTOCARE_DB_USER").strip(),
        required(environment, "AUTOCARE_DB_PASSWORD"));
  }

  Connection connect() throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", username);
    properties.setProperty("password", password);
    return DriverManager.getConnection(url(), properties);
  }

  private String url() {
    return "jdbc:sqlserver://"
        + host
        + ":"
        + port
        + ";databaseName="
        + database
        + ";encrypt=true;trustServerCertificate=false"
        + ";loginTimeout=60;socketTimeout=900000;applicationName=AutoCare-Setup;";
  }

  private static String required(Map<String, String> environment, String name) {
    String value = environment.get(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + ".");
    }
    return value;
  }
}
