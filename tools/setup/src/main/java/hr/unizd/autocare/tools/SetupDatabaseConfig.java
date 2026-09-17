package hr.unizd.autocare.tools;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Setup-only JPA bootstrap za validate/update i izolirane testne baze. */
final class SetupDatabaseConfig {

  private SetupDatabaseConfig() {}

  static EntityManagerFactory open(String schemaAction, boolean test) {
    if (!Set.of("none", "validate", "update").contains(schemaAction)) {
      throw new IllegalArgumentException("Dopusteni su none, validate ili eksplicitni update.");
    }

    SetupSqlSettings settings = SetupSqlSettings.environment(test);

    if (schemaAction.equals("update")) {
      settings.requireSchemaConsent(System.getenv());
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", settings.url());

    // Hibernate uses the JDBC properties supplied here; credentials remain outside the artifact.
    Map<String, String> environment = System.getenv();
    String prefix = test ? "AUTOCARE_TEST_" : "AUTOCARE_DB_";
    properties.put("jakarta.persistence.jdbc.user", environment.get(prefix + "USER"));
    properties.put("jakarta.persistence.jdbc.password", environment.get(prefix + "PASSWORD"));
    properties.put(
        "jakarta.persistence.jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
    properties.put("hibernate.default_schema", "dbo");
    properties.put("hibernate.use_nationalized_character_data", "true");
    properties.put("hibernate.hbm2ddl.auto", schemaAction);
    properties.put("hibernate.hbm2ddl.halt_on_error", "true");
    properties.put("hibernate.connection.pool_size", "2");

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
