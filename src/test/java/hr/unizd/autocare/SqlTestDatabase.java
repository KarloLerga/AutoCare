package hr.unizd.autocare;

import hr.unizd.autocare.app.SqlSettings;
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
    Map<String, String> testValues = new HashMap<>();

    for (String suffix : new String[] {"HOST", "NAME", "USER", "PASSWORD"}) {
      testValues.put("AUTOCARE_DB_" + suffix, required(environment, "AUTOCARE_TEST_" + suffix));
    }

    testValues.put("AUTOCARE_DB_PORT", environment.getOrDefault("AUTOCARE_TEST_PORT", "1433"));
    SqlSettings settings = SqlSettings.from(testValues);

    String testHost = testValues.get("AUTOCARE_DB_HOST").strip();
    String testName = settings.getDatabase();
    String runtimeHost = required(environment, "AUTOCARE_DB_HOST").strip();
    String runtimeName = required(environment, "AUTOCARE_DB_NAME").strip();

    if (!testName.toLowerCase(Locale.ROOT).endsWith("_test")) {
      throw new IllegalArgumentException(
          "Integracijski test treba zasebnu bazu s nastavkom _test.");
    }

    if (testHost.equalsIgnoreCase(runtimeHost) && testName.equalsIgnoreCase(runtimeName)) {
      throw new IllegalArgumentException("Test ne smije koristiti aplikacijsku bazu.");
    }

    if (!testName.equals(required(environment, "AUTOCARE_TEST_SCHEMA_TARGET").strip())) {
      throw new IllegalArgumentException(
          "AUTOCARE_TEST_SCHEMA_TARGET mora potvrditi tocnu testnu bazu.");
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", settings.getJdbcUrl());
    properties.put("jakarta.persistence.jdbc.user", settings.getUsername());
    properties.put("jakarta.persistence.jdbc.password", settings.getPassword());
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("hibernate.hbm2ddl.halt_on_error", "true");
    properties.put("hibernate.hikari.maximumPoolSize", "1");
    properties.put("hibernate.hikari.minimumIdle", "0");
    properties.put("hibernate.hikari.keepaliveTime", "0");

    EntityManagerFactory factory = Persistence.createEntityManagerFactory("autocare", properties);

    try (EntityManager entityManager = factory.createEntityManager()) {
      String actualName =
          (String) entityManager.createNativeQuery("select DB_NAME()").getSingleResult();

      if (!testName.equalsIgnoreCase(actualName)) {
        throw new IllegalStateException("Otvorena je neocekivana baza.");
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
