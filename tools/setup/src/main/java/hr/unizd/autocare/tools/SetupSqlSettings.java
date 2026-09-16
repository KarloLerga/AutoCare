package hr.unizd.autocare.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Konfiguracija developerskih SQL naredbi; normalni runtime je ne moze koristiti. */
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

  static SetupSqlSettings environment(boolean test) {
    return from(System.getenv(), test, false);
  }

  static SetupSqlSettings discovery() {
    return from(System.getenv(), false, true);
  }

  private static SetupSqlSettings from(
      Map<String, String> environment, boolean test, boolean discovery) {
    String prefix = test ? "AUTOCARE_TEST_" : "AUTOCARE_DB_";
    String host = required(environment, prefix + "HOST").strip();

    if (!host.matches("[a-zA-Z0-9][a-zA-Z0-9.-]{0,252}")) {
      throw new IllegalArgumentException("Nevaljan naziv SQL servera.");
    }

    int port;

    try {
      port = Integer.parseInt(environment.getOrDefault(prefix + "PORT", "1433").strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("SQL port mora biti broj.");
    }

    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("SQL port je izvan dopustenog raspona.");
    }

    String database = discovery ? "master" : required(environment, prefix + "NAME").strip();

    if (!database.matches("[a-zA-Z0-9_][a-zA-Z0-9_. -]{0,127}")) {
      throw new IllegalArgumentException("Nevaljan naziv baze.");
    }

    Set<String> systemDatabases = Set.of("master", "tempdb", "model", "msdb");

    if (!discovery && systemDatabases.contains(database.toLowerCase(Locale.ROOT))) {
      throw new IllegalArgumentException("Aplikacija ne smije koristiti sistemsku bazu.");
    }

    if (test) {
      if (!database.endsWith("_test")) {
        throw new IllegalArgumentException("Test zahtijeva zasebnu bazu s nastavkom _test.");
      }

      if (database.equalsIgnoreCase(environment.get("AUTOCARE_DB_NAME"))) {
        throw new IllegalArgumentException("Testna i aplikacijska baza moraju biti odvojene.");
      }
    }

    return new SetupSqlSettings(
        host,
        port,
        database,
        required(environment, prefix + "USER").strip(),
        required(environment, prefix + "PASSWORD"));
  }

  String database() {
    return database;
  }

  String url() {
    return "jdbc:sqlserver://"
        + host
        + ":"
        + port
        + ";databaseName="
        + database
        + ";encrypt=true;trustServerCertificate=false"
        + ";loginTimeout=60;socketTimeout=120000;applicationName=AutoCare-Setup;";
  }

  Connection connect(boolean bulkCopy) throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", username);
    properties.setProperty("password", password);

    if (bulkCopy) {
      properties.setProperty("useBulkCopyForBatchInsert", "true");
      properties.setProperty("bulkCopyForBatchInsertBatchSize", "1000");
    }

    return DriverManager.getConnection(url(), properties);
  }

  void requireSchemaConsent(Map<String, String> environment) {
    if (!database.equals(environment.get("AUTOCARE_SCHEMA_TARGET"))) {
      throw new IllegalArgumentException(
          "Za schema-update postavite AUTOCARE_SCHEMA_TARGET na tocno ime odabrane razvojne baze.");
    }
  }

  private static String required(Map<String, String> environment, String name) {
    String value = environment.get(name);

    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + name + ".");
    }

    return value;
  }
}
