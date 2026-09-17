package hr.unizd.autocare;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Iskljucivo testni bootstrap. Nije dio normalne aplikacije ni setup modula. */
final class SqlTestDatabase {
  private SqlTestDatabase() {}

  static EntityManagerFactory open() {
    Map<String, String> environment = System.getenv();
    String testHost = required(environment, "AUTOCARE_TEST_HOST").strip();
    String testName = required(environment, "AUTOCARE_TEST_NAME").strip();
    String testUser = required(environment, "AUTOCARE_TEST_USER");
    String testPassword = required(environment, "AUTOCARE_TEST_PASSWORD");
    String testPort = environment.getOrDefault("AUTOCARE_TEST_PORT", "1433");
    String runtimeHost = required(environment, "AUTOCARE_DB_HOST").strip();
    String runtimeName = required(environment, "AUTOCARE_DB_NAME").strip();

    if (!testName.toLowerCase(Locale.ROOT).endsWith("_test")) {
      throw new IllegalArgumentException("Integracijski test treba zasebnu bazu s nastavkom _test.");
    }
    if (testHost.equalsIgnoreCase(runtimeHost) && testName.equalsIgnoreCase(runtimeName)) {
      throw new IllegalArgumentException("Test ne smije koristiti aplikacijsku bazu.");
    }
    if (!testName.equals(required(environment, "AUTOCARE_TEST_SCHEMA_TARGET").strip())) {
      throw new IllegalArgumentException("AUTOCARE_TEST_SCHEMA_TARGET mora potvrditi tocnu testnu bazu.");
    }

    String jdbcUrl =
        "jdbc:sqlserver://"
            + testHost
            + ":"
            + testPort
            + ";databaseName="
            + testName
            + ";encrypt=true;trustServerCertificate=false;loginTimeout=60;applicationName=AutoCareTest;";
    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", testUser);
    properties.put("jakarta.persistence.jdbc.password", testPassword);
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("hibernate.hbm2ddl.halt_on_error", "true");

    EntityManagerFactory factory = Persistence.createEntityManagerFactory("autocare", properties);
    try {
      EntityManager entityManager = factory.createEntityManager();
      try {
        String actualName =
            (String) entityManager.createNativeQuery("select DB_NAME()").getSingleResult();
        if (!testName.equalsIgnoreCase(actualName)) {
          throw new IllegalStateException("Otvorena je neocekivana baza.");
        }
      } finally {
        entityManager.close();
      }
      return factory;
    } catch (RuntimeException | Error failure) {
      try {
        factory.close();
      } catch (RuntimeException cleanupFailure) {
        failure.addSuppressed(cleanupFailure);
      }
      throw failure;
    }
  }

  private static String required(Map<String, String> environment, String key) {
    String value = environment.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Nedostaje " + key + ". SQL test nije izvrsen.");
    }
    return value;
  }
}
