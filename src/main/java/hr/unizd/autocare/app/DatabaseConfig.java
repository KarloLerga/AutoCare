package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Konfigurira i otvara JPA vezu aplikacije prema Azure SQL bazi. */
public class DatabaseConfig {
  private static final String HOST = "auto-care.database.windows.net";
  private static final String PORT = "1433";
  private static final String DATABASE = "free-sql-db-0650603";
  private static final String USERNAME = "karlolerga";
  private static final String PASSWORD = "autocare_123";

  private DatabaseConfig() {}

  /**
   * Stvara tvornicu JPA entity managera za persistence unit aplikacije.
   *
   * @return otvorena tvornica koju aplikacija zatvara pri gašenju
   * @throws jakarta.persistence.PersistenceException ako se persistence unit ne može pokrenuti
   */
  public static EntityManagerFactory open() {
    String jdbcUrl = "jdbc:sqlserver://" + HOST + ":" + PORT + ";databaseName=" + DATABASE
        + ";encrypt=true;trustServerCertificate=false;";

    Map<String, Object> properties = new HashMap<>();
    properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    properties.put("jakarta.persistence.jdbc.user", USERNAME);
    properties.put("jakarta.persistence.jdbc.password", PASSWORD);

    return Persistence.createEntityManagerFactory("autocare", properties);
  }
}
