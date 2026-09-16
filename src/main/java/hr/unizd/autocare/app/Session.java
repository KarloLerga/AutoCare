package hr.unizd.autocare.app;

import hr.unizd.autocare.model.Data.VehicleRow;

/** Stanje prijavljenog desktop klijenta; koristi se samo na EDT-u. */
public final class Session {
  private long owner;
  private VehicleRow active;

  public void login(long owner) {
    this.owner = owner;
    active = null;
  }

  public void logout() {
    owner = 0;
    active = null;
  }

  public long owner() {
    return owner;
  }

  public VehicleRow active() {
    return active;
  }

  public void setActive(VehicleRow active) {
    this.active = active;
  }
}
