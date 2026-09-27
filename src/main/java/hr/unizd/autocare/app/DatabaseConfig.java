package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizira konfiguraciju potrebnu za otvaranje JPA persistence sloja prema Azure SQL bazi.
 *
 * <p>Klasa sastavlja SQL Server JDBC URL, dodaje korisničke podatke u JPA properties i vraća
 * {@link EntityManagerFactory}. Time Service i Repository klase ne moraju poznavati detalje
 * povezivanja s bazom.
 */
public class DatabaseConfig {
  private static final String HOST = "auto-care.database.windows.net";
  private static final String PORT = "1433";
  private static final String DATABASE = "free-sql-db-0650603";
  private static final String USERNAME = "karlolerga";
  private static final String PASSWORD = "autocare_123";

  /**
   * Sprječava stvaranje instance jer se konfiguracija koristi isključivo kroz statičku metodu
   * {@link #open()}.
   */
  private DatabaseConfig() {}

  /**
   * Otvara zajednički {@link EntityManagerFactory} za AutoCare persistence unit.
   *
   * <p>Metoda sastavlja JDBC URL za Azure SQL, postavlja korisničko ime i lozinku te pokreće JPA
   * bootstrap preko {@link Persistence}.
   *
   * @return otvoreni EntityManagerFactory koji Service sloj koristi tijekom rada aplikacije
   * @throws jakarta.persistence.PersistenceException ako se persistence unit ne može inicijalizirati
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
