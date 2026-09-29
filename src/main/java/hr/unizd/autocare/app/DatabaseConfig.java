package hr.unizd.autocare.app;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizira konfiguraciju potrebnu za otvaranje JPA persistence sloja prema
 * Azure SQL bazi.
 *
 * <p>Klasa sastavlja SQL Server JDBC URL, dodaje korisničke podatke u JPA properties i vraća
 * {@link EntityManagerFactory}. Time Service i Repository klase ne moraju poznavati detalje
 * povezivanja s bazom. Sama klasa ne otvara pojedinačne EntityManagere niti pokreće upite.
 */
public class DatabaseConfig {
  /** Host SQL Server poslužitelja kojem se persistence sloj povezuje. */
  private static final String HOST = "auto-care.database.windows.net";

  /** TCP priključak SQL Server JDBC veze. */
  private static final String PORT = "1433";

  /** Naziv baze koji se postavlja u JDBC URL. */
  private static final String DATABASE = "free-sql-db-0650603";

  /** Korisničko ime koje JDBC driver predaje poslužitelju pri povezivanju. */
  private static final String USERNAME = "karlolerga";

  /** Lozinka JDBC računa; vrijednost se namjerno ne ponavlja u dokumentaciji. */
  private static final String PASSWORD = "autocare_123";

  /**
   * Sprječava stvaranje instance jer se konfiguracija koristi isključivo kroz statičku metodu
   * {@link #open()}.
   */
  private DatabaseConfig() {
  }

  /**
   * Otvara zajednički {@link EntityManagerFactory} za AutoCare persistence unit.
   *
   * <p>Metoda sastavlja JDBC URL za Azure SQL, postavlja korisničko ime i lozinku kao JPA
   * properties te pokreće bootstrap preko {@link Persistence}. JPA učitava persistence unit
   * naziva {@code autocare}; ne stvara se EntityManager dok Service ne pokrene pojedinu operaciju.
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
