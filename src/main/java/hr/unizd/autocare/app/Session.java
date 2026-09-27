package hr.unizd.autocare.app;

import hr.unizd.autocare.domain.Vehicle;

/**
 * Singleton koji čuva zajednički korisnički kontekst tijekom rada aplikacije.
 *
 * <p>Session pamti identifikator prijavljenog korisnika i vozilo koje je trenutačno aktivno.
 * Controlleri koriste istu instancu kako se korisnički kontekst ne bi ručno prosljeđivao između
 * svih ekrana.
 */
public class Session {
  private static Session instance;

  private int ownerId;
  private Vehicle activeVehicle;

  /** Privatni konstruktor osigurava da se Session koristi kao Singleton. */
  private Session() {}

  /**
   * Vraća jedinu instancu aplikacijske sesije.
   *
   * @return zajednička Session instanca
   */
  public static Session getInstance() {
    if (instance == null) {
      instance = new Session();
    }
    return instance;
  }

  /**
   * Otvara korisnički kontekst nakon uspješne prijave.
   *
   * <p>Postavlja identifikator prijavljenog korisnika i čisti prethodno aktivno vozilo kako bi ga
   * aplikacija ponovno učitala iz podataka tog korisnika.
   *
   * @param ownerId identifikator prijavljenog korisnika
   */
  public void login(int ownerId) {
    this.ownerId = ownerId;
    activeVehicle = null;
  }

  /**
   * Briše podatke prijavljene sesije pri odjavi korisnika.
   *
   * <p>Nakon odjave nema aktivnog korisnika ni aktivnog vozila.
   */
  public void logout() {
    ownerId = 0;
    activeVehicle = null;
  }

  /** Vraća identifikator prijavljenog korisnika. */
  public int getOwnerId() {
    return ownerId;
  }

  /** Vraća trenutačno aktivno vozilo, ako je odabrano. */
  public Vehicle getActiveVehicle() {
    return activeVehicle;
  }

  /**
   * Postavlja vozilo koje predstavlja trenutačni kontekst aplikacije.
   *
   * @param activeVehicle trenutno aktivno vozilo ili {@code null} ako korisnik nema aktivno vozilo
   */
  public void setActiveVehicle(Vehicle activeVehicle) {
    this.activeVehicle = activeVehicle;
  }
}
