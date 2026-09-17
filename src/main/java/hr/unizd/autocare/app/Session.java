package hr.unizd.autocare.app;

import hr.unizd.autocare.model.Data.VehicleRow;

/** Stanje prijavljenog desktop klijenta; koristi se samo na EDT-u. */
public final class Session {
  private long ownerId;
  private VehicleRow activeVehicle;

  public void login(long ownerId) {
    this.ownerId = ownerId;
    activeVehicle = null;
  }

  public void logout() {
    ownerId = 0;
    activeVehicle = null;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public VehicleRow getActiveVehicle() {
    return activeVehicle;
  }

  public void setActiveVehicle(VehicleRow activeVehicle) {
    this.activeVehicle = activeVehicle;
  }
}
