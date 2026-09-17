package hr.unizd.autocare.tools;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Developer-only SQL Server importer. Existing Hibernate tables, bounded staging batches, no MERGE
 * or destructive reload.
 */
public final class SqlSeedTool {
  private static final int BATCH = 1000;

  private SqlSeedTool() {}

  public static void run(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Nedostaje SQL/seed naredba.");
    }
    String command = args[0];
    if (command.equals("db-list")) {
      try {
        discover();
      } catch (Exception ex) {
        throw new IllegalStateException("Read-only db-list nije uspio: " + ex.getMessage(), ex);
      }
      return;
    }
    if (command.equals("sql-check")) {
      try {
        check();
      } catch (Exception ex) {
        throw new IllegalStateException("SQL provjera nije uspjela: " + ex.getMessage(), ex);
      }
      return;
    }
    try {
      if (args.length < 2) {
        throw new IllegalArgumentException("Navedite folder seed podataka.");
      }
      Path dir = Path.of(args[1]);
      validate(dir);
      if (command.equals("seed-validate") || !Arrays.asList(args).contains("--apply")) {
        System.out.println("DRY RUN zavrsen. Baza nije kontaktirana.");
        return;
      }
      if (!command.equals("seed-all")) {
        throw new IllegalArgumentException("Nepoznata seed naredba.");
      }
      if (!Arrays.asList(args).contains("--acknowledge-model-estimates")) {
        throw new IllegalArgumentException(
            "Potrebno --acknowledge-model-estimates: cijene su modelirane, ne provjereni trzisni"
                + " prosjeci.");
      }
      SetupSqlSettings settings = SetupSqlSettings.environment(false);
      if (!settings.database().equals(System.getenv("AUTOCARE_SEED_TARGET"))) {
        throw new IllegalArgumentException(
            "AUTOCARE_SEED_TARGET mora tocno odgovarati AUTOCARE_DB_NAME.");
      }
      boolean intervals = Arrays.asList(args).contains("--with-referenced-intervals");
      boolean diagnostics = Arrays.asList(args).contains("--with-diagnostics");
      boolean plain = Arrays.asList(args).contains("--plain-jdbc");
      try (Connection c = settings.connect(!plain)) {
        verifySchema(c);
        lock(c);
        c.setAutoCommit(false);
        try {
          works(c, dir);
          variants(c, dir);
          rules(c, dir);
          if (intervals) {
            intervals(c, dir);
          }
          if (diagnostics) {
            diagnostics(c, dir);
          }
          c.commit();
          report(c);
          System.out.println(
              "Uvoz dovrsen. Cijene/povijest drugih izvora nisu prepisane. Ponovni uvoz je"
                  + " dopusten.");
        } catch (Exception ex) {
          try {
            c.rollback();
          } catch (SQLException rollback) {
            ex.addSuppressed(rollback);
          }
          throw ex;
        }
      }
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Seed nije dovrsen: "
              + ex.getMessage()
              + " Raniji paketi mogu biti spremljeni; nakon popravka isti uvoz smije se ponoviti."
              + " Nije dopusteno TRUNCATE ni iskljucivanje TLS/FK.",
          ex);
    }
  }

  public static void discover() throws SQLException {
    try (Connection c = SetupSqlSettings.discovery().connect(false);
        PreparedStatement s =
            c.prepareStatement("SELECT name FROM sys.databases WHERE database_id>4 ORDER BY name");
        ResultSet r = s.executeQuery()) {
      while (r.next()) {
        System.out.println(r.getString(1));
      }
    }
    // Deliberately does not select, create, resize or change billing for any database.
  }

  public static void check() throws SQLException {
    SetupSqlSettings settings = SetupSqlSettings.environment(false);
    try (Connection c = settings.connect(false);
        PreparedStatement s =
            c.prepareStatement(
                "SELECT DB_NAME(), CAST(SERVERPROPERTY('EngineEdition') AS int), SUSER_SNAME()");
        ResultSet r = s.executeQuery()) {
      if (r.next()) {
        System.out.println(
            "SQL veza OK | baza="
                + r.getString(1)
                + " | EngineEdition="
                + r.getInt(2)
                + " | korisnik="
                + r.getString(3)
                + " | encrypt=true, trustServerCertificate=false");
      }
    }
  }

  private static void lock(Connection c) throws SQLException {
    try (Statement s = c.createStatement();
        ResultSet r =
            s.executeQuery(
                "SET NOCOUNT ON; DECLARE @r int; EXEC @r=sys.sp_getapplock"
                    + " @Resource=N'AutoCare-reference-import',@LockMode='Exclusive',@LockOwner='Session',@LockTimeout=0;"
                    + " SELECT @r")) {
      if (!r.next() || r.getInt(1) < 0) {
        throw new SQLException("Drugi seed proces je aktivan.");
      }
    }
  }

  private static void verifySchema(Connection c) throws SQLException {
    for (String table :
        List.of("vehicle_variant", "work_definition", "vehicle_work_rule", "diagnostic_rule")) {
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT COUNT(*) FROM sys.tables WHERE name=? AND schema_id=SCHEMA_ID(N'dbo')")) {
        s.setString(1, table);
        try (ResultSet r = s.executeQuery()) {
          r.next();
          if (r.getInt(1) != 1) {
            throw new SQLException("Nedostaje dbo." + table + "; prvo Hibernate schema-update.");
          }
        }
      }
    }
  }

  /** Offline full streaming validation; a digest proves integrity, not automotive accuracy. */
  public static void validate(Path dir) throws IOException {
    SeedFiles.verifyManifest(dir);
    Set<String> variants = new HashSet<>(), works = new HashSet<>();
    Map<String, String> categories = new HashMap<>();
    long v =
        SeedFiles.read(
            dir.resolve("vehicle_variants.csv"),
            r -> {
              String code = SeedFiles.text(r, "code", 80, true);
              if (!variants.add(code)) {
                throw new IllegalArgumentException("Duplicirana varijanta.");
              }
              for (String key : List.of("make", "model", "generation", "engine_label")) {
                SeedFiles.text(
                    r,
                    key,
                    switch (key) {
                      case "make" -> 100;
                      case "model" -> 150;
                      case "generation" -> 200;
                      default -> 240;
                    },
                    true);
              }
              Integer from = SeedFiles.integer(r, "year_from", 1886, 2100),
                  to = SeedFiles.integer(r, "year_to", 1886, 2100);
              if (from == null || (to != null && to < from)) {
                throw new IllegalArgumentException("Nevaljan raspon godina.");
              }
              SeedFiles.integer(r, "power_hp", 1, 10000);
              SeedFiles.text(r, "body_type", 100, false);
              SeedFiles.text(r, "fuel_type", 80, false);
              SeedFiles.text(r, "transmission", 120, false);
              String image = SeedFiles.text(r, "image_path", 255, false);
              if (image != null
                  && (!image.startsWith("/images/vehicles/")
                      || image.contains("..")
                      || image.contains("\\"))) {
                throw new IllegalArgumentException("Nevaljana lokalna slika.");
              }
            });
    long w =
        SeedFiles.read(
            dir.resolve("work_definitions.csv"),
            r -> {
              String code = SeedFiles.text(r, "code", 80, true);
              if (!works.add(code)) {
                throw new IllegalArgumentException("Duplicirani rad.");
              }
              SeedFiles.text(r, "name", 160, true);
              String cat = SeedFiles.text(r, "category", 20, true);
              if (!Set.of("MAINTENANCE", "REPAIR").contains(cat)) {
                throw new IllegalArgumentException("Nevaljana kategorija.");
              }
              categories.put(code, cat);
              SeedFiles.text(r, "estimate_note", 1000, false);
            });
    Set<String> ended = new HashSet<>();
    Set<String> currentWorks = new HashSet<>();
    String[] last = {null};
    long rules =
        SeedFiles.read(
            dir.resolve("vehicle_work_rules.csv.gz"),
            r -> {
              String variant = SeedFiles.text(r, "variant_code", 80, true),
                  work = SeedFiles.text(r, "work_code", 80, true);
              if (!variants.contains(variant) || !works.contains(work)) {
                throw new IllegalArgumentException("Nepostojeci kod u pravilu.");
              }
              if (!variant.equals(last[0])) {
                if (last[0] != null) {
                  ended.add(last[0]);
                }
                if (ended.contains(variant)) {
                  throw new IllegalArgumentException("Pravila nisu grupirana po varijanti.");
                }
                last[0] = variant;
                currentWorks.clear();
              }
              if (!currentWorks.add(work)) {
                throw new IllegalArgumentException("Duplicirani par varijanta/rad.");
              }
              SeedFiles.price(r, "estimated_price");
              Integer km = SeedFiles.integer(r, "interval_km", 1, 1000000),
                  months = SeedFiles.integer(r, "interval_months", 1, 1200);
              if (km != null || months != null) {
                throw new IllegalArgumentException(
                    "Osnovni seed ne smije neprimjetno aktivirati intervale; odvojeni su u"
                        + " referenced_intervals.csv.");
              }
              SeedFiles.text(r, "estimate_note", 1000, true);
              SeedFiles.text(r, "interval_source", 1000, false);
            });
    Set<String> pairs = new HashSet<>();
    long intervals =
        SeedFiles.read(
            dir.resolve("referenced_intervals.csv"),
            r -> {
              String vc = SeedFiles.text(r, "variant_code", 80, true),
                  wc = SeedFiles.text(r, "work_code", 80, true);
              if (!variants.contains(vc)
                  || !"MAINTENANCE".equals(categories.get(wc))
                  || !pairs.add(vc + "/" + wc)) {
                throw new IllegalArgumentException("Nevaljan/dupliciran interval.");
              }
              Integer km = SeedFiles.integer(r, "interval_km", 1, 1000000),
                  m = SeedFiles.integer(r, "interval_months", 1, 1200);
              if (km == null && m == null) {
                throw new IllegalArgumentException("Interval nema kriterij.");
              }
              SeedFiles.text(r, "interval_source", 1000, true);
            });
    Set<String> codes = new HashSet<>();
    long diagnostics =
        SeedFiles.read(
            dir.resolve("diagnostic_rules.csv"),
            r -> {
              String code = SeedFiles.text(r, "code", 80, true),
                  work = SeedFiles.text(r, "work_code", 80, true);
              if (!codes.add(code) || !"REPAIR".equals(categories.get(work))) {
                throw new IllegalArgumentException("Nevaljana dijagnostika.");
              }
              SeedFiles.text(r, "phrase", 160, true);
              if (SeedFiles.integer(r, "weight", 1, 100) == null) {
                throw new IllegalArgumentException("Tezina nedostaje.");
              }
            });
    System.out.printf(
        "Offline seed: %d varijanti, %d radova, %d pravila, %d referenciranih intervala, %d"
            + " tekstualnih pravila.%n",
        v, w, rules, intervals, diagnostics);
  }

  @FunctionalInterface
  private interface BatchAction {
    void accept(List<Map<String, String>> rows) throws Exception;
  }

  private static void batches(Path path, BatchAction action) throws Exception {
    List<Map<String, String>> rows = new ArrayList<>(BATCH);
    long[] processed = {0};
    try {
      SeedFiles.read(
          path,
          row -> {
            rows.add(row);
            if (rows.size() == BATCH) {
              try {
                action.accept(rows);
                processed[0] += rows.size();
                rows.clear();
                if (processed[0] % 50000 == 0) {
                  System.out.println(path.getFileName() + ": " + processed[0]);
                }
              } catch (Exception ex) {
                throw new BatchFailure(ex);
              }
            }
          });
    } catch (BatchFailure ex) {
      throw (Exception) ex.getCause();
    }
    if (!rows.isEmpty()) {
      action.accept(rows);
    }
  }

  private static final class BatchFailure extends RuntimeException {
    BatchFailure(Exception cause) {
      super(cause);
    }
  }

  private static void execute(Connection c, String sql) throws SQLException {
    try (Statement s = c.createStatement()) {
      s.setQueryTimeout(120);
      s.execute(sql);
    }
  }

  private static void strings(
      PreparedStatement s, int start, Map<String, String> row, String... keys) throws SQLException {
    for (int i = 0; i < keys.length; i++) {
      String v = row.get(keys[i]);
      if (v == null || v.isBlank()) {
        s.setNull(start + i, Types.NVARCHAR);
      } else {
        s.setNString(start + i, v);
      }
    }
  }

  private static void num(PreparedStatement s, int index, Map<String, String> row, String key)
      throws SQLException {
    String v = row.get(key);
    if (v == null || v.isBlank()) {
      s.setNull(index, Types.INTEGER);
    } else {
      s.setInt(index, Integer.parseInt(v));
    }
  }

  private static void money(PreparedStatement s, int index, Map<String, String> row, String key)
      throws SQLException {
    String v = row.get(key);
    if (v == null || v.isBlank()) {
      s.setNull(index, Types.DECIMAL);
    } else {
      s.setBigDecimal(index, new BigDecimal(v));
    }
  }

  private static void works(Connection c, Path dir) throws Exception {
    execute(
        c,
        "CREATE TABLE #ac_w(code nvarchar(80) NOT NULL,name nvarchar(160) NOT NULL,category"
            + " nvarchar(20) NOT NULL,estimate_note nvarchar(1000) NULL)");
    batches(
        dir.resolve("work_definitions.csv"),
        rows -> {
          try (PreparedStatement s = c.prepareStatement("INSERT INTO #ac_w VALUES(?,?,?,?)")) {
            for (var r : rows) {
              strings(s, 1, r, "code", "name", "category", "estimate_note");
              s.addBatch();
            }
            s.executeBatch();
          }
          execute(
              c,
              "IF EXISTS(SELECT 1 FROM #ac_w s JOIN dbo.work_definition t ON t.code=s.code WHERE"
                  + " t.category<>s.category) THROW 51000,'Postojeci work code ima drugo"
                  + " znacenje/kategoriju.',1");
          execute(
              c,
              "INSERT INTO"
                  + " dbo.work_definition(code,name,category,default_estimated_price,estimate_note)"
                  + " SELECT s.code,s.name,s.category,NULL,s.estimate_note FROM #ac_w s WHERE NOT"
                  + " EXISTS(SELECT 1 FROM dbo.work_definition t WHERE t.code=s.code)");
          execute(c, "DELETE FROM #ac_w");
          c.commit();
        });
    execute(c, "DROP TABLE #ac_w");
  }

  private static void variants(Connection c, Path dir) throws Exception {
    execute(
        c,
        "CREATE TABLE #ac_v(code nvarchar(80),make nvarchar(100),model nvarchar(150),generation"
            + " nvarchar(200),engine_label nvarchar(240),body_type nvarchar(100),fuel_type"
            + " nvarchar(80),power_hp int,transmission nvarchar(120),year_from int,year_to"
            + " int,image_path nvarchar(255))");
    batches(
        dir.resolve("vehicle_variants.csv"),
        rows -> {
          try (PreparedStatement s =
              c.prepareStatement("INSERT INTO #ac_v VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")) {
            for (var r : rows) {
              strings(
                  s,
                  1,
                  r,
                  "code",
                  "make",
                  "model",
                  "generation",
                  "engine_label",
                  "body_type",
                  "fuel_type");
              num(s, 8, r, "power_hp");
              strings(s, 9, r, "transmission");
              num(s, 10, r, "year_from");
              num(s, 11, r, "year_to");
              strings(s, 12, r, "image_path");
              s.addBatch();
            }
            s.executeBatch();
          }
          execute(
              c,
              "IF EXISTS(SELECT 1 FROM #ac_v s JOIN dbo.vehicle_variant t ON t.code=s.code WHERE"
                  + " t.make<>s.make OR t.model<>s.model OR t.generation<>s.generation OR"
                  + " t.engine_label<>s.engine_label OR t.year_from<>s.year_from OR"
                  + " ISNULL(t.year_to,-1)<>ISNULL(s.year_to,-1)) THROW 51000,'Kataloski code ima"
                  + " drugo znacenje; potreban review.',1");
          execute(
              c,
              "INSERT INTO"
                  + " dbo.vehicle_variant(code,make,model,generation,engine_label,body_type,fuel_type,power_hp,transmission,year_from,year_to,image_path)"
                  + " SELECT"
                  + " s.code,s.make,s.model,s.generation,s.engine_label,s.body_type,s.fuel_type,s.power_hp,s.transmission,s.year_from,s.year_to,s.image_path"
                  + " FROM #ac_v s WHERE NOT EXISTS(SELECT 1 FROM dbo.vehicle_variant t WHERE"
                  + " t.code=s.code)");
          execute(c, "DELETE FROM #ac_v");
          c.commit();
        });
    execute(c, "DROP TABLE #ac_v");
  }

  private static void rules(Connection c, Path dir) throws Exception {
    execute(
        c,
        "CREATE TABLE #ac_r(variant_code nvarchar(80),work_code nvarchar(80),estimated_price"
            + " decimal(9,2),estimate_note nvarchar(1000),interval_source nvarchar(1000))");
    batches(
        dir.resolve("vehicle_work_rules.csv.gz"),
        rows -> {
          try (PreparedStatement s = c.prepareStatement("INSERT INTO #ac_r VALUES(?,?,?,?,?)")) {
            for (var r : rows) {
              strings(s, 1, r, "variant_code", "work_code");
              money(s, 3, r, "estimated_price");
              strings(s, 4, r, "estimate_note", "interval_source");
              s.addBatch();
            }
            s.executeBatch();
          }
          execute(
              c,
              "IF EXISTS(SELECT 1 FROM #ac_r s LEFT JOIN dbo.vehicle_variant v ON"
                  + " v.code=s.variant_code LEFT JOIN dbo.work_definition w ON w.code=s.work_code"
                  + " WHERE v.id IS NULL OR w.id IS NULL) THROW 51000,'Nedostaje varijanta ili"
                  + " zahvat.',1");
          execute(
              c,
              "INSERT INTO"
                  + " dbo.vehicle_work_rule(variant_id,work_id,estimated_price,estimate_note,interval_source)"
                  + " SELECT"
                  + " v.id,w.id,s.estimated_price,s.estimate_note,s.interval_source"
                  + " FROM #ac_r s JOIN dbo.vehicle_variant v ON v.code=s.variant_code JOIN"
                  + " dbo.work_definition w ON w.code=s.work_code WHERE NOT EXISTS(SELECT 1 FROM"
                  + " dbo.vehicle_work_rule t WHERE t.variant_id=v.id AND t.work_id=w.id)");
          // A deliberate NULL with an existing reviewed note is NOT missing data to overwrite.
          execute(
              c,
              "UPDATE t SET t.estimated_price=s.estimated_price,t.estimate_note=s.estimate_note FROM"
                  + " dbo.vehicle_work_rule t JOIN dbo.vehicle_variant v ON v.id=t.variant_id JOIN"
                  + " dbo.work_definition w ON w.id=t.work_id JOIN #ac_r s ON s.variant_code=v.code"
                  + " AND s.work_code=w.code WHERE t.estimated_price IS NULL AND t.estimate_note IS"
                  + " NULL AND s.estimated_price IS NOT NULL");
          execute(c, "DELETE FROM #ac_r");
          c.commit();
        });
    execute(c, "DROP TABLE #ac_r");
  }

  private static void intervals(Connection c, Path dir) throws Exception {
    execute(
        c,
        "CREATE TABLE #ac_i(variant_code nvarchar(80),work_code nvarchar(80),interval_km"
            + " int,interval_months int,interval_source nvarchar(1000))");
    batches(
        dir.resolve("referenced_intervals.csv"),
        rows -> {
          try (PreparedStatement s = c.prepareStatement("INSERT INTO #ac_i VALUES(?,?,?,?,?)")) {
            for (var r : rows) {
              strings(s, 1, r, "variant_code", "work_code");
              num(s, 3, r, "interval_km");
              num(s, 4, r, "interval_months");
              strings(s, 5, r, "interval_source");
              s.addBatch();
            }
            s.executeBatch();
          }
          execute(
              c,
              "INSERT INTO"
                  + " dbo.vehicle_work_rule(variant_id,work_id,interval_km,interval_months,interval_source)"
                  + " SELECT v.id,w.id,s.interval_km,s.interval_months,s.interval_source"
                  + " FROM #ac_i s JOIN dbo.vehicle_variant v ON v.code=s.variant_code JOIN"
                  + " dbo.work_definition w ON w.code=s.work_code WHERE NOT EXISTS(SELECT 1 FROM"
                  + " dbo.vehicle_work_rule t WHERE t.variant_id=v.id AND t.work_id=w.id)");
          execute(
              c,
              "UPDATE t SET"
                  + " t.interval_km=s.interval_km,t.interval_months=s.interval_months,t.interval_source=s.interval_source"
                  + " FROM dbo.vehicle_work_rule t JOIN dbo.vehicle_variant v ON v.id=t.variant_id"
                  + " JOIN dbo.work_definition w ON w.id=t.work_id JOIN #ac_i s ON"
                  + " s.variant_code=v.code AND s.work_code=w.code WHERE t.interval_km IS NULL AND"
                  + " t.interval_months IS NULL AND (t.interval_source"
                  + " IS NULL OR t.interval_source LIKE N'AC-SCHEDULE-%')");
          execute(c, "DELETE FROM #ac_i");
          c.commit();
        });
    execute(c, "DROP TABLE #ac_i");
  }

  private static void diagnostics(Connection c, Path dir) throws Exception {
    disableKnownLegacyDiagnostics(c);
    execute(
        c,
        "CREATE TABLE #ac_d(code nvarchar(80),work_code nvarchar(80),phrase nvarchar(160),weight"
            + " int,active bit)");
    batches(
        dir.resolve("diagnostic_rules.csv"),
        rows -> {
          try (PreparedStatement s = c.prepareStatement("INSERT INTO #ac_d VALUES(?,?,?,?,?)")) {
            for (var r : rows) {
              strings(s, 1, r, "code", "work_code", "phrase");
              num(s, 4, r, "weight");
              s.setBoolean(5, "1".equals(r.get("active")));
              s.addBatch();
            }
            s.executeBatch();
          }
          execute(
              c,
              "IF EXISTS(SELECT 1 FROM #ac_d s JOIN dbo.diagnostic_rule t ON t.code=s.code JOIN"
                  + " dbo.work_definition w ON w.id=t.candidate_id WHERE s.work_code<>w.code OR"
                  + " s.phrase<>t.phrase OR s.weight<>t.weight) THROW 51000,'Izmijenjeno"
                  + " dijagnosticko pravilo istoga koda; potreban review.',1");
          execute(
              c,
              "INSERT INTO dbo.diagnostic_rule(code,candidate_id,phrase,weight,active) SELECT"
                  + " s.code,w.id,s.phrase,s.weight,s.active FROM #ac_d s JOIN dbo.work_definition w"
                  + " ON w.code=s.work_code WHERE w.category=N'REPAIR' AND NOT EXISTS(SELECT 1 FROM"
                  + " dbo.diagnostic_rule t WHERE t.code=s.code)");
          execute(c, "DELETE FROM #ac_d");
          c.commit();
        });
    execute(c, "DROP TABLE #ac_d");
  }

  private static void disableKnownLegacyDiagnostics(Connection c) throws SQLException {
    String[][] known = {
      {"ac-warm", "AC_COMPRESSOR", "slabo hladi", "4"},
      {"ac-noise", "AC_COMPRESSOR", "zvizdi", "2"},
      {"fan-idle", "RADIATOR_FAN", "u leru", "3"},
      {"fan-heat", "RADIATOR_FAN", "pregrijava", "5"},
      {"ac-fluid", "AC_SERVICE", "slabo hladi", "2"},
      {"bat-start", "BATTERY", "tesko pali", "2"},
      {"bat-slow", "BATTERY", "sporo vergla", "5"},
      {"glow-cold", "GLOW_PLUGS", "hladan", "3"},
      {"glow-start", "GLOW_PLUGS", "tesko pali", "2"}
    };
    try (PreparedStatement find =
            c.prepareStatement(
                "SELECT w.code,r.phrase,r.weight FROM dbo.diagnostic_rule r JOIN dbo.work_definition"
                    + " w ON w.id=r.candidate_id WHERE r.code=?");
        PreparedStatement disable =
            c.prepareStatement("UPDATE dbo.diagnostic_rule SET active=0 WHERE code=?")) {
      for (String[] row : known) {
        find.setString(1, row[0]);
        try (ResultSet r = find.executeQuery()) {
          if (!r.next()) {
            continue;
          }
          if (!row[1].equals(r.getString(1))
              || !row[2].equals(r.getString(2))
              || Integer.parseInt(row[3]) != r.getInt(3)) {
            throw new SQLException(
                "Promijenjeno staro DEMO pravilo; potreban pregled, ne automatsko prepisivanje.");
          }
        }
        disable.setString(1, row[0]);
        disable.executeUpdate();
      }
    }
  }

  private static void report(Connection c) throws SQLException {
    try (Statement s = c.createStatement();
        ResultSet r =
            s.executeQuery(
                "SELECT (SELECT COUNT_BIG(*) FROM dbo.vehicle_variant),(SELECT COUNT_BIG(*) FROM"
                    + " dbo.work_definition),(SELECT COUNT_BIG(*) FROM dbo.vehicle_work_rule),(SELECT"
                    + " COUNT_BIG(*) FROM dbo.diagnostic_rule)")) {
      r.next();
      System.out.printf(
          "Ukupno u bazi: %d varijanti / %d radova / %d pravila / %d dijagnostickih pravila.%n",
          r.getLong(1), r.getLong(2), r.getLong(3), r.getLong(4));
    }
  }
}
