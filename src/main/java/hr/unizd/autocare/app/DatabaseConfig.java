package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Otvara EntityManagerFactory za bazu podataka. */
public final class DatabaseConfig {
  private static final String HOST = "auto-care.database.windows.net";
  private static final String PORT = "1433";
  private static final String DATABASE = "free-sql-db-0650603";
  private static final String USERNAME = "karlolerga";
  private static final String PASSWORD = "autocare_123";

  private DatabaseConfig() {}

  public static EntityManagerFactory open() {
    String jdbcUrl =
        "jdbc:sqlserver://"
            + HOST
            + ":"
            + PORT
            + ";databaseName="
            + DATABASE
            + ";encrypt=true;trustServerCertificate=false;";

    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", USERNAME);
    properties.put("jakarta.persistence.jdbc.password", PASSWORD);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
