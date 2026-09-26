package hr.unizd.autocare.app;

import hr.unizd.autocare.domain.Vehicle;

/** Singleton koji čuva trenutno prijavljenog korisnika i aktivno vozilo. */
public class Session {
  private static Session instance;

  private int ownerId;
  private Vehicle activeVehicle;

  private Session() {}

  /** Vraća jedinu sesiju aplikacije. */
  public static Session getInstance() {
    if (instance == null) {
      instance = new Session();
    }
    return instance;
  }

  /** Bilježi prijavljenog korisnika i uklanja prethodno aktivno vozilo. */
  public void login(int ownerId) {
    this.ownerId = ownerId;
    activeVehicle = null;
  }

  /** Briše korisnika i aktivno vozilo iz trenutačne sesije. */
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

  /** Postavlja vozilo koje se koristi kao kontekst glavnih ekrana. */
  public void setActiveVehicle(Vehicle activeVehicle) {
    this.activeVehicle = activeVehicle;
  }
}
