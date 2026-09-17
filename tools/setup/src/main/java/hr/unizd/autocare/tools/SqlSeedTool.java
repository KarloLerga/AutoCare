package hr.unizd.autocare.tools;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Developer-only Azure SQL alat za validaciju, streaming import i završnu migraciju. */
public final class SqlSeedTool {
  private static final int BATCH = 1000;

  private SqlSeedTool() {}

  public static void run(String[] args) {
    try {
      switch (args[0]) {
        case "db-list" -> discover();
        case "sql-check" -> check();
        case "import-complete-catalog" -> importCompleteCatalog(args);
        case "apply-final-schema" -> applyFinalSchema(args);
        case "final-audit" -> finalAudit(args);
        default ->
            throw new IllegalArgumentException(
                "Naredbe: sql-check, db-list, import-complete-catalog, apply-final-schema, final-audit");
      }
    } catch (Exception exception) {
      throw new IllegalStateException("SQL alat nije dovršen: " + exception.getMessage(), exception);
    }
  }

  private static void discover() throws SQLException {
    try (Connection connection = SetupSqlSettings.discovery().connect(false);
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
    SetupSqlSettings settings = SetupSqlSettings.environment(false);
    try (Connection connection = settings.connect(false);
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

  private static void importCompleteCatalog(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Navedite vehicle_work_rules_complete.csv.gz.");
    }
    Path path = Path.of(args[1]).toAbsolutePath().normalize();
    if (!Files.isRegularFile(path) || Files.isSymbolicLink(path)) {
      throw new IllegalArgumentException("Datoteka kataloga nije valjana.");
    }
    boolean apply = has(args, "--apply");
    if (apply && !has(args, "--confirm-complete-catalog")) {
      throw new IllegalArgumentException(
          "Za stvarni import dodajte --apply --confirm-complete-catalog.");
    }

    SetupSqlSettings settings = SetupSqlSettings.environment(false);
    try (Connection connection = settings.connect(apply)) {
      verifyTables(connection);
      Map<String, Long> variants = loadIds(connection, "vehicle_variant");
      Map<String, Long> works = loadIds(connection, "work_definition");
      Map<String, String> categories = loadCategories(connection);
      long expected = validateFile(path, variants, works, categories);
      System.out.println(
          "Katalog je validan: " + expected + " pravila / " + variants.size() + " varijanti.");
      if (!apply) {
        System.out.println("DRY RUN: baza nije mijenjana.");
        return;
      }

      lock(connection);
      connection.setAutoCommit(false);
      try {
        rejectReferencedOtherWorks(connection);
        rejectRuleForeignKeys(connection);
        try (Statement delete = connection.createStatement()) {
          delete.executeUpdate("DELETE FROM dbo.vehicle_work_rule");
        }
        long inserted = insertRules(connection, path, variants, works, categories);
        if (inserted != expected) {
          throw new SQLException("Broj uvezenih pravila se promijenio tijekom importa.");
        }
        connection.commit();
        report(connection);
        System.out.println("Kompletni katalog je atomarno uvezen.");
      } catch (Exception failure) {
        try {
          connection.rollback();
        } catch (SQLException rollback) {
          failure.addSuppressed(rollback);
        }
        throw failure;
      }
    }
  }

  private static long validateFile(
      Path path, Map<String, Long> variants, Map<String, Long> works, Map<String, String> categories)
      throws IOException {
    Set<String> seenVariants = new HashSet<>();
    final long[] count = {0};
    SeedFiles.read(
        path,
        row -> {
          String variant = required(row, "variant_code");
          String work = required(row, "work_code");
          if (!variants.containsKey(variant)) {
            throw new IllegalArgumentException("Nepoznata varijanta: " + variant);
          }
          String category = categories.get(work);
          if (category == null || !works.containsKey(work)) {
            throw new IllegalArgumentException("Nepoznat rad: " + work);
          }
          if (work.startsWith("OTHER_")) {
            throw new IllegalArgumentException("OTHER rad nije dio finalnog kataloga: " + work);
          }
          BigDecimal price = decimal(row, "estimated_price");
          if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Cijena mora biti pozitivna: " + variant + "/" + work);
          }
          Integer km = positiveInteger(row, "interval_km");
          Integer months = positiveInteger(row, "interval_months");
          if ("MAINTENANCE".equals(category) && km == null && months == null) {
            throw new IllegalArgumentException("Održavanje nema interval: " + variant + "/" + work);
          }
          if ("REPAIR".equals(category) && (km != null || months != null)) {
            throw new IllegalArgumentException("Popravak ima interval: " + variant + "/" + work);
          }
          seenVariants.add(variant);
          count[0]++;
        });
    if (seenVariants.size() != variants.size()) {
      throw new IllegalArgumentException(
          "Neke varijante nemaju pravila: " + (variants.size() - seenVariants.size()));
    }
    return count[0];
  }

  private static long insertRules(
      Connection connection,
      Path path,
      Map<String, Long> variants,
      Map<String, Long> works,
      Map<String, String> categories)
      throws IOException, SQLException {
    long[] count = {0};
    try (PreparedStatement insert =
        connection.prepareStatement(
            "INSERT INTO dbo.vehicle_work_rule"
                + "(variant_id,work_id,interval_km,interval_months,estimated_price)"
                + " VALUES (?,?,?,?,?)")) {
      try {
        SeedFiles.read(
            path,
            row -> {
              try {
                insert.setLong(1, variants.get(required(row, "variant_code")));
                insert.setLong(2, works.get(required(row, "work_code")));
                setInteger(insert, 3, positiveInteger(row, "interval_km"));
                setInteger(insert, 4, positiveInteger(row, "interval_months"));
                insert.setBigDecimal(5, decimal(row, "estimated_price"));
                insert.addBatch();
                count[0]++;
                if (count[0] % BATCH == 0) {
                  insert.executeBatch();
                }
                if (count[0] % 100_000 == 0) {
                  System.out.println("Uvezeno " + count[0] + " pravila...");
                }
              } catch (SQLException exception) {
                throw new ImportFailure(exception);
              }
            });
      } catch (ImportFailure failure) {
        throw failure.sqlException;
      }
      insert.executeBatch();
    }
    return count[0];
  }

  private static void applyFinalSchema(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Navedite guarded SQL migraciju.");
    }
    if (!has(args, "--apply") || !has(args, "--confirm-final-schema")) {
      System.out.println("DRY RUN: migracija nije primijenjena; dodajte --apply --confirm-final-schema.");
      return;
    }
    Path path = Path.of(args[1]).toAbsolutePath().normalize();
    String sql = Files.readString(path, StandardCharsets.UTF_8);
    String applySql = sql.replace("DECLARE @Apply bit = 0;", "DECLARE @Apply bit = 1;");
    if (applySql.equals(sql)) {
      throw new IllegalArgumentException("Migracija nema očekivani @Apply guard.");
    }
    try (Connection connection = SetupSqlSettings.environment(false).connect(false);
        Statement statement = connection.createStatement()) {
      statement.execute(applySql);
      System.out.println("Završna schema migracija je primijenjena.");
    }
  }

  private static void finalAudit(String[] args) throws Exception {
    if (args.length < 2) {
      throw new IllegalArgumentException("Navedite SQL audit.");
    }
    String sql = Files.readString(Path.of(args[1]), StandardCharsets.UTF_8);
    try (Connection connection = SetupSqlSettings.environment(false).connect(false);
        Statement statement = connection.createStatement()) {
      boolean result = statement.execute(sql);
      while (true) {
        if (result) {
          printResult(statement.getResultSet());
        }
        if (statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT)) {
          result = true;
          continue;
        }
        int update = statement.getUpdateCount();
        if (update == -1) {
          break;
        }
        result = false;
      }
    }
  }

  private static void printResult(ResultSet result) throws SQLException {
    try (result) {
      ResultSetMetaData metadata = result.getMetaData();
      StringBuilder header = new StringBuilder();
      for (int i = 1; i <= metadata.getColumnCount(); i++) {
        if (i > 1) {
          header.append('\t');
        }
        header.append(metadata.getColumnLabel(i));
      }
      System.out.println(header);
      while (result.next()) {
        StringBuilder line = new StringBuilder();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
          if (i > 1) {
            line.append('\t');
          }
          line.append(result.getString(i));
        }
        System.out.println(line);
      }
    }
  }

  private static Map<String, Long> loadIds(Connection connection, String table) throws SQLException {
    Map<String, Long> ids = new HashMap<>();
    String sql = "SELECT id,code FROM dbo." + table;
    try (PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {
      while (result.next()) {
        ids.put(result.getString("code"), result.getLong("id"));
      }
    }
    return ids;
  }

  private static Map<String, String> loadCategories(Connection connection) throws SQLException {
    Map<String, String> categories = new HashMap<>();
    try (PreparedStatement statement =
            connection.prepareStatement("SELECT code,category FROM dbo.work_definition");
        ResultSet result = statement.executeQuery()) {
      while (result.next()) {
        categories.put(result.getString(1), result.getString(2));
      }
    }
    return categories;
  }

  private static void verifyTables(Connection connection) throws SQLException {
    for (String table : new String[] {"vehicle_variant", "work_definition", "vehicle_work_rule"}) {
      try (PreparedStatement statement =
              connection.prepareStatement(
                  "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='dbo'"
                      + " AND TABLE_NAME=?")) {
        statement.setString(1, table);
        try (ResultSet result = statement.executeQuery()) {
          result.next();
          if (result.getInt(1) != 1) {
            throw new SQLException("Nedostaje dbo." + table);
          }
        }
      }
    }
  }

  private static void rejectReferencedOtherWorks(Connection connection) throws SQLException {
    String sql =
        "SELECT "
            + "(SELECT COUNT_BIG(*) FROM dbo.service_item i JOIN dbo.work_definition w ON w.id=i.work_id"
            + " WHERE w.code IN ('OTHER_MAINTENANCE','OTHER_REPAIR')),"
            + "(SELECT COUNT_BIG(*) FROM dbo.problem p JOIN dbo.work_definition w ON w.id=p.suggested_repair_id"
            + " WHERE w.code IN ('OTHER_MAINTENANCE','OTHER_REPAIR'))";
    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
      result.next();
      if (result.getLong(1) != 0 || result.getLong(2) != 0) {
        throw new SQLException("OTHER radovi imaju korisničke reference; import je blokiran.");
      }
    }
  }

  private static void rejectRuleForeignKeys(Connection connection) throws SQLException {
    try (PreparedStatement statement =
            connection.prepareStatement(
                "SELECT COUNT_BIG(*) FROM sys.foreign_keys WHERE referenced_object_id=OBJECT_ID('dbo.vehicle_work_rule')");
        ResultSet result = statement.executeQuery()) {
      result.next();
      if (result.getLong(1) != 0) {
        throw new SQLException("Druga tablica referencira vehicle_work_rule; import je blokiran.");
      }
    }
  }

  private static void lock(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet result =
            statement.executeQuery(
                "SET NOCOUNT ON; DECLARE @r int; EXEC @r=sys.sp_getapplock"
                    + " @Resource=N'AutoCare-reference-import',@LockMode='Exclusive',@LockOwner='Session',@LockTimeout=0;"
                    + " SELECT @r")) {
      if (!result.next() || result.getInt(1) < 0) {
        throw new SQLException("Drugi import je aktivan.");
      }
    }
  }

  private static void report(Connection connection) throws SQLException {
    String sql =
        "SELECT "
            + "(SELECT COUNT_BIG(*) FROM dbo.vehicle_variant) AS variants,"
            + "(SELECT COUNT_BIG(*) FROM dbo.work_definition) AS works,"
            + "(SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule) AS rules,"
            + "(SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule r JOIN dbo.work_definition w ON w.id=r.work_id WHERE w.category='MAINTENANCE') AS maintenance_rules,"
            + "(SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule r JOIN dbo.work_definition w ON w.id=r.work_id WHERE w.category='REPAIR') AS repair_rules";
    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
      result.next();
      System.out.printf(
          "Baza nakon importa: %d varijanti / %d radova / %d pravila (%d održavanje, %d popravci).%n",
          result.getLong(1),
          result.getLong(2),
          result.getLong(3),
          result.getLong(4),
          result.getLong(5));
    }
  }

  private static String required(Map<String, String> row, String key) {
    String value = row.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje vrijednost: " + key);
    }
    return value.strip();
  }

  private static Integer positiveInteger(Map<String, String> row, String key) {
    String value = row.get(key);
    if (value == null || value.isBlank()) {
      return null;
    }
    int parsed;
    try {
      parsed = Integer.parseInt(value.strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Nevaljan cijeli broj u " + key + ".");
    }
    if (parsed <= 0) {
      throw new IllegalArgumentException("Vrijednost mora biti pozitivna u " + key + ".");
    }
    return parsed;
  }

  private static BigDecimal decimal(Map<String, String> row, String key) {
    String value = row.get(key);
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(value.strip());
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Nevaljan decimalni broj u " + key + ".");
    }
  }

  private static void setInteger(PreparedStatement statement, int index, Integer value)
      throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.INTEGER);
    } else {
      statement.setInt(index, value);
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

  private static final class ImportFailure extends RuntimeException {
    private final SQLException sqlException;

    private ImportFailure(SQLException sqlException) {
      super(sqlException);
      this.sqlException = sqlException;
    }
  }
}
