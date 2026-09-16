package hr.unizd.autocare.app;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Vanjska konfiguracija normalne aplikacije, bez izrade baze ili uvoza. */
public final class SqlSettings {

  private final String host;
  private final int port;
  private final String database;
  private final String username;
  private final String password;

  private SqlSettings(String host, int port, String database, String username, String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
  }

  public static SqlSettings fromEnvironment() {
    return from(System.getenv());
  }

  /** Cista provjera koja omogucuje testiranje bez otvaranja SQL veze. */
  public static SqlSettings from(Map<String, String> environment) {
    String host = required(environment, "AUTOCARE_DB_HOST").strip();
    String database = required(environment, "AUTOCARE_DB_NAME").strip();
    String username = required(environment, "AUTOCARE_DB_USER").strip();
    String password = required(environment, "AUTOCARE_DB_PASSWORD");

    if (!host.matches("[a-zA-Z0-9][a-zA-Z0-9.-]{0,252}")) {
      throw new IllegalArgumentException("Nevaljan naziv SQL servera.");
    }

    if (!database.matches("[a-zA-Z0-9_][a-zA-Z0-9_. -]{0,127}")) {
      throw new IllegalArgumentException("Nevaljan naziv baze.");
    }

    Set<String> systemDatabases = Set.of("master", "tempdb", "model", "msdb");

    if (systemDatabases.contains(database.toLowerCase(Locale.ROOT))) {
      throw new IllegalArgumentException("Aplikacija ne smije koristiti sistemsku bazu.");
    }

    int port;

    try {
      port = Integer.parseInt(environment.getOrDefault("AUTOCARE_DB_PORT", "1433").strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("SQL port mora biti broj.");
    }

    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("SQL port je izvan dopustenog raspona.");
    }

    return new SqlSettings(host, port, database, username, password);
  }

  public String getJdbcUrl() {
    return "jdbc:sqlserver://"
        + host
        + ":"
        + port
        + ";databaseName="
        + database
        + ";encrypt=true;trustServerCertificate=false"
        + ";loginTimeout=60;socketTimeout=120000;applicationName=AutoCare;";
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabase() {
    return database;
  }

  private static String required(Map<String, String> environment, String name) {
    String value = environment.get(name);

    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + ".");
    }

    return value;
  }

  @Override
  public String toString() {
    return "SQL Server " + host + ":" + port + " / " + database;
  }
}
