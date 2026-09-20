package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Otvara EntityManagerFactory za Azure SQL bazu. */
public final class DatabaseConfig {
  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    Map<String, Object> properties = new HashMap<>();
    String jdbcUrl =
        "jdbc:sqlserver://YOUR_AZURE_SQL_HOST:1433;"
            + "databaseName=YOUR_DATABASE_NAME;"
            + "user=YOUR_SQL_USERNAME;"
            + "password=YOUR_SQL_PASSWORD;"
            + "encrypt=true;trustServerCertificate=false;";

    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
