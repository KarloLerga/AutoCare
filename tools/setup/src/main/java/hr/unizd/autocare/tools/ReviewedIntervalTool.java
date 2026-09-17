package hr.unizd.autocare.tools;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Small reviewed schedule batches. Changes interval fields only, never existing estimates or actual
 * payments.
 */
public final class ReviewedIntervalTool {
  private ReviewedIntervalTool() {}

  public static void run(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException(
          "import-intervals reviewed.csv [--apply] [--replace-interval-only]");
    }
    List<Map<String, String>> rows = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    try {
      SeedFiles.read(
          Path.of(args[1]),
          r -> {
            String v = SeedFiles.text(r, "variant_code", 80, true),
                w = SeedFiles.text(r, "work_code", 80, true);
            if (!seen.add(v + "/" + w) || rows.size() >= 500) {
              throw new IllegalArgumentException(
                  "Duplicirani par ili vise od 500 pregledanih redaka.");
            }
            if (!"APPROVED".equals(r.get("review_status"))
                || !"YES".equals(r.get("whole_variant_scope_confirmed"))) {
              throw new IllegalArgumentException(
                  "Potrebni su APPROVED i whole_variant_scope_confirmed=YES. Nije dovoljan samo"
                      + " jedan VIN unutar sire kataloske varijante.");
            }
            String reviewer = SeedFiles.text(r, "reviewer", 100, true),
                source = SeedFiles.text(r, "interval_source", 700, true);
            LocalDate on = LocalDate.parse(SeedFiles.text(r, "reviewed_on", 10, true));
            if (on.isAfter(LocalDate.now())) {
              throw new IllegalArgumentException("Datum pregleda je u buducnosti.");
            }
            Integer km = SeedFiles.integer(r, "interval_km", 1, 1000000),
                mo = SeedFiles.integer(r, "interval_months", 1, 1200);
            if (km == null && mo == null) {
              throw new IllegalArgumentException("Nema intervala.");
            }
            Map<String, String> copy = new HashMap<>(r);
            copy.put(
                "interval_source", "REVIEWED_SCOPE | " + reviewer + " | " + on + " | " + source);
            rows.add(copy);
          });
      System.out.println("Pregledanih intervala: " + rows.size());
      if (!Arrays.asList(args).contains("--apply")) {
        return;
      }
      SetupSqlSettings cfg = SetupSqlSettings.environment(false);
      if (!cfg.database().equals(System.getenv("AUTOCARE_SEED_TARGET"))) {
        throw new IllegalArgumentException("Potvrdite AUTOCARE_SEED_TARGET.");
      }
      boolean replace = Arrays.asList(args).contains("--replace-interval-only");
      try (Connection c = cfg.connect(false)) {
        c.setAutoCommit(false);
        try {
          for (Map<String, String> r : rows) {
            long vid, wid;
            Integer km = SeedFiles.integer(r, "interval_km", 1, 1000000),
                mo = SeedFiles.integer(r, "interval_months", 1, 1200);
            try (PreparedStatement s =
                c.prepareStatement(
                    "SELECT vehicleVariant.id,workDefinition.id,workDefinition.category "
                        + "FROM dbo.vehicle_variant vehicleVariant CROSS JOIN "
                        + "dbo.work_definition workDefinition "
                        + "WHERE vehicleVariant.code=? AND workDefinition.code=?")) {
              s.setString(1, r.get("variant_code"));
              s.setString(2, r.get("work_code"));
              try (ResultSet q = s.executeQuery()) {
                if (!q.next() || !"MAINTENANCE".equals(q.getString(3))) {
                  throw new IllegalArgumentException("Nepoznat par ili rad nije odrzavanje.");
                }
                vid = q.getLong(1);
                wid = q.getLong(2);
              }
            }
            Long id = null;
            Integer oldKm = null, oldMo = null;
            try (PreparedStatement s =
                c.prepareStatement(
                    "SELECT id,interval_km,interval_months FROM dbo.vehicle_work_rule"
                        + " WITH(UPDLOCK,HOLDLOCK) WHERE variant_id=? AND work_id=?")) {
              s.setLong(1, vid);
              s.setLong(2, wid);
              try (ResultSet q = s.executeQuery()) {
                if (q.next()) {
                  id = q.getLong(1);
                  oldKm = (Integer) q.getObject(2);
                  oldMo = (Integer) q.getObject(3);
                }
              }
            }
            if (id != null
                && !replace
                && (oldKm != null || oldMo != null)) {
              if (Objects.equals(km, oldKm) && Objects.equals(mo, oldMo)) {
                continue;
              }
              throw new IllegalArgumentException(
                  "Postojeci plan se razlikuje. Pregledajte ga; --replace-interval-only nikad ne"
                      + " mijenja cijenu.");
            }
            String sql =
                id == null
                    ? "INSERT INTO"
                          + " dbo.vehicle_work_rule(interval_km,interval_months,interval_source,variant_id,work_id)"
                          + " VALUES(?,?,?,?,?)"
                    : "UPDATE dbo.vehicle_work_rule SET"
                        + " interval_km=?,interval_months=?,interval_source=?"
                        + " WHERE variant_id=? AND work_id=?";
            try (PreparedStatement s = c.prepareStatement(sql)) {
              s.setObject(1, km, Types.INTEGER);
              s.setObject(2, mo, Types.INTEGER);
              s.setNString(3, r.get("interval_source"));
              s.setLong(4, vid);
              s.setLong(5, wid);
              s.executeUpdate();
            }
          }
          c.commit();
          System.out.println("Intervali spremljeni. Cijene nisu promijenjene.");
        } catch (Exception ex) {
          try {
            c.rollback();
          } catch (SQLException rb) {
            ex.addSuppressed(rb);
          }
          throw ex;
        }
      }
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Uvoz intervala nije potvrden. Kod izgubljenog commita prvo provjerite stvarno stanje;"
              + " CSV se moze ponoviti.",
          ex);
    }
  }
}
