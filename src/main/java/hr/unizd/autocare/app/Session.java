package hr.unizd.autocare.app;

import hr.unizd.autocare.model.Data.VehicleRow;

/** Singleton koji čuva trenutno prijavljenog korisnika i aktivno vozilo. */
public final class Session {
  private static Session instance;

  private int ownerId;
  private VehicleRow activeVehicle;

  private Session() {}

  public static Session getInstance() {
    if (instance == null) {
      instance = new Session();
    }
    return instance;
  }

  public void login(int ownerId) {
    this.ownerId = ownerId;
    activeVehicle = null;
  }

  public void logout() {
    ownerId = 0;
    activeVehicle = null;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public VehicleRow getActiveVehicle() {
    return activeVehicle;
  }

  public void setActiveVehicle(VehicleRow activeVehicle) {
    this.activeVehicle = activeVehicle;
  }
}
