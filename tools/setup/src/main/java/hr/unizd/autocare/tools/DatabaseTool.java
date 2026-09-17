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
    try (EntityManagerFactory entityManagerFactory =
        SetupDatabaseConfig.open(update ? "update" : "none", false)) {
      switch (action) {
        case "schema-update" ->
            System.out.println(
                "Hibernate update dovrsen. Provjerite DB log/strukturu; sljedeca pokretanja koriste"
                    + " hbm2ddl=none.");
        case "db-check" -> check(entityManagerFactory);
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
              entityManagerFactory,
              entityManager -> {
                if (demo) {
                  DevelopmentSeed.demo(entityManager);
                } else {
                  DevelopmentSeed.core(entityManager);
                }
              });
          System.out.println("Seed je dovrsen. Postojeci zapisi nisu duplicirani.");
        }
        case "import-catalog" -> {
          if (args.length < 2) {
            throw new IllegalArgumentException("Navedite normalized.csv.");
          }
          importCatalog(entityManagerFactory, Path.of(args[1]));
        }
        case "import-rules" -> {
          if (args.length < 2 || !Arrays.asList(args).contains("--confirm-reviewed-data")) {
            throw new IllegalArgumentException(
                "Navedite reviewed_rules.csv i --confirm-reviewed-data.");
          }
          importRules(
              entityManagerFactory, Path.of(args[1]), Arrays.asList(args).contains("--replace-existing"));
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
                 value(r, "transmission")));
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

  private static void importRules(
      EntityManagerFactory entityManagerFactory, Path path, boolean replace) {
    try {
      List<Map<String, String>> rows = csv(path);
      transaction(
          entityManagerFactory,
          entityManager -> {
            Set<String> seen = new HashSet<>();
            for (Map<String, String> row : rows) {
              if (!"APPROVED".equals(value(row, "review_status"))) {
                throw new IllegalArgumentException(
                    "Uvoz odbijen: svaki red mora biti rucno pregledan i APPROVED.");
              }
              String variantCode = value(row, "variant_code"), workCode = value(row, "work_code");
              if (!seen.add(variantCode + "/" + workCode)) {
                throw new IllegalArgumentException("Duplicirani par varijanta/rad.");
              }
              VehicleVariant vehicleVariant =
                  entityManager
                      .createQuery(
                          "select vehicleVariant from VehicleVariant vehicleVariant "
                              + "where vehicleVariant.code=:code",
                          VehicleVariant.class)
                      .setParameter("code", variantCode)
                      .getSingleResult();
              WorkDefinition workDefinition =
                  entityManager
                      .createQuery(
                          "select workDefinition from WorkDefinition workDefinition "
                              + "where workDefinition.code=:code",
                          WorkDefinition.class)
                      .setParameter("code", workCode)
                      .getSingleResult();
              Integer intervalKm = integer(row, "interval_km"),
                  intervalMonths = integer(row, "interval_months");
              String priceText = value(row, "estimated_price");
              BigDecimal price = priceText == null ? null : new BigDecimal(priceText);
              String intervalSource = value(row, "interval_source"),
                  estimateNote = value(row, "estimate_note");
              List<VehicleWorkRule> existingRules =
                  entityManager
                      .createQuery(
                          "select vehicleWorkRule from VehicleWorkRule vehicleWorkRule "
                              + "where vehicleWorkRule.variant.id=:variantId "
                              + "and vehicleWorkRule.work.id=:workId",
                          VehicleWorkRule.class)
                      .setParameter("variantId", vehicleVariant.getId())
                      .setParameter("workId", workDefinition.getId())
                      .setMaxResults(1)
                      .getResultList();
              VehicleWorkRule existingRule =
                  existingRules.isEmpty() ? null : existingRules.get(0);
              if (existingRule == null) {
                entityManager.persist(
                    new VehicleWorkRule(
                        vehicleVariant,
                        workDefinition,
                        intervalKm,
                        intervalMonths,
                        price,
                        intervalSource,
                        estimateNote));
              } else if (replace) {
                VehicleWorkRule replacement =
                    new VehicleWorkRule(
                        vehicleVariant,
                        workDefinition,
                        intervalKm,
                        intervalMonths,
                        price,
                        intervalSource,
                        estimateNote);
                entityManager.createQuery(
                        "update VehicleWorkRule vehicleWorkRule set "
                            + "vehicleWorkRule.intervalKm=:intervalKm, "
                            + "vehicleWorkRule.intervalMonths=:intervalMonths, "
                            + "vehicleWorkRule.estimatedPrice=:estimatedPrice, "
                            + "vehicleWorkRule.intervalSource=:intervalSource, "
                            + "vehicleWorkRule.estimateNote=:estimateNote "
                            + "where vehicleWorkRule.id=:ruleId")
                    .setParameter("intervalKm", replacement.getIntervalKm())
                    .setParameter("intervalMonths", replacement.getIntervalMonths())
                    .setParameter("estimatedPrice", replacement.getEstimatedPrice())
                    .setParameter("intervalSource", replacement.getIntervalSource())
                    .setParameter("estimateNote", replacement.getEstimateNote())
                    .setParameter("ruleId", existingRule.getId())
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
