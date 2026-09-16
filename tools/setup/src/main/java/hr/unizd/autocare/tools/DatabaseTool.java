package hr.unizd.autocare.tools;

import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkDefinition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** CLI alat za developera. Nikada se automatski ne izvrsava pri GUI prijavi. */
public final class DatabaseTool {
  private DatabaseTool() {}

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Navedite developersku naredbu, primjerice db-check.");
      return;
    }

    run(args);
  }

  public static void run(String[] args) {
    String action = args[0];
    if (Set.of("db-list", "sql-check", "seed-validate", "seed-all").contains(action)) {
      SqlSeedTool.run(args);
      return;
    }
    if (action.equals("import-intervals")) {
      ReviewedIntervalTool.run(args);
      return;
    }
    if (action.equals("import-images")) {
      ImagePathTool.run(args);
      return;
    }
    boolean update = action.equals("schema-update");
    if (update && !Arrays.asList(args).contains("--confirm-development-schema")) {
      throw new IllegalArgumentException(
          "Dodajte --confirm-development-schema. Ovo ne stvara bazu; AUTOCARE_SCHEMA_TARGET mora"
              + " potvrditi postojece ime.");
    }
    Set<String> commands =
        Set.of(
            "schema-update",
            "db-check",
            "seed-core",
            "seed-demo",
            "import-catalog",
            "import-rules");
    if (!commands.contains(action)) {
      throw new IllegalArgumentException("Naredbe: " + commands);
    }
    try (EntityManagerFactory emf =
        SetupDatabaseConfig.open(update ? "update" : "validate", false)) {
      switch (action) {
        case "schema-update" ->
            System.out.println(
                "Hibernate update dovrsen. Provjerite DB log/strukturu; sljedeca pokretanja koriste"
                    + " validate.");
        case "db-check" -> check(emf);
        case "seed-core", "seed-demo" -> {
          boolean demo = action.equals("seed-demo");
          if (demo) {
            String name = System.getenv("AUTOCARE_DB_NAME");
            if (!Arrays.asList(args).contains("--confirm-demo")
                || name == null
                || (!name.endsWith("_dev") && !name.endsWith("_test"))) {
              throw new IllegalArgumentException(
                  "DEMO seed zahtijeva _dev/_test bazu i --confirm-demo.");
            }
          }
          transaction(
              emf,
              em -> {
                if (demo) {
                  DevelopmentSeed.demo(em);
                } else {
                  DevelopmentSeed.core(em);
                }
              });
          System.out.println("Seed je dovrsen. Postojeci zapisi nisu duplicirani.");
        }
        case "import-catalog" -> {
          if (args.length < 2) {
            throw new IllegalArgumentException("Navedite normalized.csv.");
          }
          importCatalog(emf, Path.of(args[1]));
        }
        case "import-rules" -> {
          if (args.length < 2 || !Arrays.asList(args).contains("--confirm-reviewed-data")) {
            throw new IllegalArgumentException(
                "Navedite reviewed_rules.csv i --confirm-reviewed-data.");
          }
          importRules(emf, Path.of(args[1]), Arrays.asList(args).contains("--replace-existing"));
        }
        default -> throw new IllegalStateException();
      }
    }
  }

  private static void check(EntityManagerFactory emf) {
    try (EntityManager em = emf.createEntityManager()) {
      Object[] info =
          (Object[])
              em.createNativeQuery(
                      "SELECT DB_NAME(), CAST(SERVERPROPERTY('EngineEdition') AS int),"
                          + " SUSER_SNAME()")
                  .getSingleResult();
      System.out.println(
          "SQL Server | baza: " + info[0] + " | engine: " + info[1] + " | korisnik: " + info[2]);
      System.out.println("JDBC TLS: encrypt=true; trustServerCertificate=false");
      System.out.println(
          "Kataloske varijante: "
              + em.createQuery("select count(v) from VehicleVariant v", Long.class)
                  .getSingleResult());
    }
  }

  private static void transaction(
      EntityManagerFactory emf, java.util.function.Consumer<EntityManager> work) {
    try (EntityManager em = emf.createEntityManager()) {
      EntityTransaction tx = em.getTransaction();
      try {
        tx.begin();
        work.accept(em);
        tx.commit();
      } catch (RuntimeException ex) {
        if (tx.isActive()) {
          try {
            tx.rollback();
          } catch (RuntimeException rollback) {
            ex.addSuppressed(rollback);
          }
        }
        throw ex;
      }
    }
  }

  private static List<Map<String, String>> csv(Path path) throws java.io.IOException {
    List<Map<String, String>> rows = new ArrayList<>();
    try (CsvReader reader = new CsvReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
      List<String> header = reader.readRow();
      if (header == null) {
        throw new IllegalArgumentException("Prazna CSV datoteka.");
      }
      if (new HashSet<>(header).size() != header.size()) {
        throw new IllegalArgumentException("Duplicirano ime stupca.");
      }
      List<String> line;
      int n = 1;
      while ((line = reader.readRow()) != null) {
        n++;
        if (line.size() != header.size()) {
          throw new IllegalArgumentException("Pogresan broj polja u retku " + n);
        }
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < header.size(); i++) {
          row.put(header.get(i), line.get(i));
        }
        rows.add(row);
      }
    }
    return rows;
  }

  private static String value(Map<String, String> row, String key) {
    if (!row.containsKey(key)) {
      throw new IllegalArgumentException("Nedostaje stupac " + key);
    }
    String s = row.get(key);
    return s == null || s.isBlank() ? null : s;
  }

  private static Integer integer(Map<String, String> row, String key) {
    String s = value(row, key);
    return s == null ? null : Integer.valueOf(s);
  }

  private static void importCatalog(EntityManagerFactory emf, Path path) {
    try {
      List<Map<String, String>> rows = csv(path);
      List<VehicleVariant> input = new ArrayList<>();
      for (Map<String, String> r : rows) {
        Integer from = integer(r, "year_from");
        if (from == null) {
          throw new IllegalArgumentException("Nedostaje pocetna godina.");
        }
        input.add(
            new VehicleVariant(
                value(r, "code"),
                value(r, "make"),
                value(r, "model"),
                value(r, "generation"),
                value(r, "engine_label"),
                from,
                integer(r, "year_to"),
                value(r, "body_type"),
                value(r, "fuel_type"),
                integer(r, "power_hp"),
                value(r, "transmission"),
                value(r, "image_path")));
      }
      Set<String> known;
      try (EntityManager em = emf.createEntityManager()) {
        known =
            new HashSet<>(
                em.createQuery("select v.code from VehicleVariant v", String.class)
                    .getResultList());
      }
      Set<String> seen = new HashSet<>();
      List<VehicleVariant> fresh = new ArrayList<>();
      for (VehicleVariant v : input) {
        if (!seen.add(v.getCode())) {
          throw new IllegalArgumentException("Duplicirani code u CSV-u: " + v.getCode());
        }
        if (!known.contains(v.getCode())) {
          fresh.add(v);
        }
      }
      for (int start = 0; start < fresh.size(); start += 500) {
        List<VehicleVariant> batch = fresh.subList(start, Math.min(start + 500, fresh.size()));
        transaction(
            emf,
            em -> {
              for (VehicleVariant v : batch) {
                em.persist(v);
              }
            });
        System.out.println("Uvezeno " + Math.min(start + 500, fresh.size()) + " / " + fresh.size());
      }
      System.out.println(
          "Novih varijanti: "
              + fresh.size()
              + "; vec postojecih: "
              + (input.size() - fresh.size()));
    } catch (java.io.IOException ex) {
      throw new IllegalArgumentException("CSV se ne moze procitati.", ex);
    }
  }

  private static void importRules(EntityManagerFactory emf, Path path, boolean replace) {
    try {
      List<Map<String, String>> rows = csv(path);
      transaction(
          emf,
          em -> {
            Set<String> seen = new HashSet<>();
            for (Map<String, String> r : rows) {
              if (!"APPROVED".equals(value(r, "review_status"))) {
                throw new IllegalArgumentException(
                    "Uvoz odbijen: svaki red mora biti rucno pregledan i APPROVED.");
              }
              String variantCode = value(r, "variant_code"), workCode = value(r, "work_code");
              if (!seen.add(variantCode + "/" + workCode)) {
                throw new IllegalArgumentException("Duplicirani par varijanta/rad.");
              }
              VehicleVariant v =
                  em.createQuery(
                          "select v from VehicleVariant v where v.code=:c", VehicleVariant.class)
                      .setParameter("c", variantCode)
                      .getSingleResult();
              WorkDefinition w =
                  em.createQuery(
                          "select w from WorkDefinition w where w.code=:c", WorkDefinition.class)
                      .setParameter("c", workCode)
                      .getSingleResult();
              Integer km = integer(r, "interval_km"), months = integer(r, "interval_months");
              String priceText = value(r, "estimated_price");
              BigDecimal price = priceText == null ? null : new BigDecimal(priceText);
              String source = value(r, "interval_source"), priceNote = value(r, "estimate_note");
              VehicleWorkRule old =
                  em.createQuery(
                          "select r from VehicleWorkRule r where r.variant.id=:v and r.work.id=:w",
                          VehicleWorkRule.class)
                      .setParameter("v", v.getId())
                      .setParameter("w", w.getId())
                      .getResultStream()
                      .findFirst()
                      .orElse(null);
              if (old == null) {
                em.persist(new VehicleWorkRule(v, w, km, months, price, source, priceNote));
              } else if (replace) {
                VehicleWorkRule replacement =
                    new VehicleWorkRule(v, w, km, months, price, source, priceNote);
                em.createQuery(
                        "update VehicleWorkRule r set r.intervalKm=:km, r.intervalMonths=:months,"
                            + " r.estimatedPrice=:price, r.intervalSource=:source,"
                            + " r.estimateNote=:note, r.scheduleKind=:kind where r.id=:id")
                    .setParameter("km", replacement.getIntervalKm())
                    .setParameter("months", replacement.getIntervalMonths())
                    .setParameter("price", replacement.getEstimatedPrice())
                    .setParameter("source", replacement.getIntervalSource())
                    .setParameter("note", replacement.getEstimateNote())
                    .setParameter("kind", replacement.getScheduleKind())
                    .setParameter("id", old.getId())
                    .executeUpdate();
              } else {
                throw new IllegalArgumentException(
                    "Pravilo vec postoji. Za namjernu reviziju dodajte --replace-existing nakon"
                        + " pregleda svih polja.");
              }
            }
          });
      System.out.println("Pregledana pravila uvezena atomarno: " + rows.size());
    } catch (java.io.IOException ex) {
      throw new IllegalArgumentException("CSV se ne moze procitati.", ex);
    }
  }
}
