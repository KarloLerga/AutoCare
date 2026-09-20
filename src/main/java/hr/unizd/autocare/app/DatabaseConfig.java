package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Otvara EntityManagerFactory za Azure SQL bazu. */
public final class DatabaseConfig {
  private static final String HOST = "YOUR_AZURE_SQL_HOST";
  private static final String PORT = "1433";
  private static final String DATABASE = "YOUR_DATABASE_NAME";
  private static final String USERNAME = "YOUR_SQL_USERNAME";
  private static final String PASSWORD = "YOUR_SQL_PASSWORD";

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    Map<String, Object> properties = new HashMap<>();
    String jdbcUrl =
        "jdbc:sqlserver://"
            + HOST
            + ":"
            + PORT
            + ";databaseName="
            + DATABASE
            + ";encrypt=true;trustServerCertificate=false;";

    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", USERNAME);
    properties.put("jakarta.persistence.jdbc.password", PASSWORD);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
