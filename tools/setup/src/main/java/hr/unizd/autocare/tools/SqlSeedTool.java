package hr.unizd.autocare.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/** Developer-only alat za provjeru veze, završnu migraciju i audit Azure SQL baze. */
public final class SqlSeedTool {
  private SqlSeedTool() {}

  public static void run(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Nedostaje naredba.");
    }

    try {
      if (args[0].equals("db-list")) {
        discover();
      } else if (args[0].equals("sql-check")) {
        check();
      } else if (args[0].equals("apply-final-schema")) {
        applyFinalSchema(args);
      } else if (args[0].equals("final-audit")) {
        finalAudit(args);
      } else {
        throw new IllegalArgumentException(
            "Naredbe: sql-check, db-list, apply-final-schema, final-audit");
      }
    } catch (Exception exception) {
      throw new IllegalStateException("SQL alat nije dovršen: " + exception.getMessage(), exception);
    }
  }

  private static void discover() throws SQLException {
    try (Connection connection = SetupSqlSettings.discovery().connect();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT name FROM sys.databases WHERE database_id>4 ORDER BY name");
        ResultSet result = statement.executeQuery()) {
      while (result.next()) {
        System.out.println(result.getString(1));
      }
    }
  }

  private static void check() throws SQLException {
    SetupSqlSettings settings = SetupSqlSettings.environment();
    try (Connection connection = settings.connect();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT DB_NAME(), CAST(SERVERPROPERTY('EngineEdition') AS int), SUSER_SNAME()")) {
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          System.out.println(
              "SQL veza OK | baza="
                  + result.getString(1)
                  + " | EngineEdition="
                  + result.getInt(2)
                  + " | korisnik="
                  + result.getString(3)
                  + " | encrypt=true, trustServerCertificate=false");
        }
      }
    }
  }

  private static void applyFinalSchema(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Navedite guarded SQL migraciju.");
    }

    Path path = Path.of(args[1]).toAbsolutePath().normalize();
    if (!Files.isRegularFile(path)) {
      throw new IllegalArgumentException("SQL migracija ne postoji.");
    }

    String sql = Files.readString(path, StandardCharsets.UTF_8);
    boolean apply = has(args, "--apply");
    if (apply && !has(args, "--confirm-final-schema")) {
      throw new IllegalArgumentException(
          "Za stvarnu migraciju dodajte --apply --confirm-final-schema.");
    }

    String command = sql;
    if (apply) {
      command = sql.replace("DECLARE @Apply bit = 0;", "DECLARE @Apply bit = 1;");
      if (command.equals(sql)) {
        throw new IllegalArgumentException("Migracija nema očekivani @Apply guard.");
      }
    }

    try (Connection connection = SetupSqlSettings.environment().connect();
        Statement statement = connection.createStatement()) {
      printAll(statement, command);
    }

    if (apply) {
      System.out.println("Završna schema migracija je primijenjena.");
    } else {
      System.out.println("DRY RUN: izvršen je samo read-only dio migracije; baza nije mijenjana.");
    }
  }

  private static void finalAudit(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Navedite SQL audit.");
    }

    Path path = Path.of(args[1]).toAbsolutePath().normalize();
    if (!Files.isRegularFile(path)) {
      throw new IllegalArgumentException("SQL audit ne postoji.");
    }

    String sql = Files.readString(path, StandardCharsets.UTF_8);
    try (Connection connection = SetupSqlSettings.environment().connect();
        Statement statement = connection.createStatement()) {
      printAll(statement, sql);
    }
  }

  private static void printAll(Statement statement, String sql) throws SQLException {
    boolean result = statement.execute(sql);
    while (true) {
      if (result) {
        printResult(statement.getResultSet());
      }
      if (statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT)) {
        result = true;
        continue;
      }
      if (statement.getUpdateCount() == -1) {
        break;
      }
      result = false;
    }
  }

  private static void printResult(ResultSet result) throws SQLException {
    try (result) {
      ResultSetMetaData metadata = result.getMetaData();
      StringBuilder header = new StringBuilder();
      for (int index = 1; index <= metadata.getColumnCount(); index++) {
        if (index > 1) {
          header.append('\t');
        }
        header.append(metadata.getColumnLabel(index));
      }
      System.out.println(header);

      while (result.next()) {
        StringBuilder line = new StringBuilder();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
          if (index > 1) {
            line.append('\t');
          }
          line.append(result.getString(index));
        }
        System.out.println(line);
      }
    }
  }

  private static boolean has(String[] args, String value) {
    for (String arg : args) {
      if (value.equals(arg)) {
        return true;
      }
    }
    return false;
  }
}
