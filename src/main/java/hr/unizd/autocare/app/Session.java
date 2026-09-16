package hr.unizd.autocare.app;

import hr.unizd.autocare.model.Data.VehicleRow;

/** Stanje prijavljenog desktop klijenta; koristi se samo na EDT-u. */
public final class Session {
  private long owner, epoch;
  private VehicleRow active;
  private boolean writing;

  public void login(long owner) {
    this.owner = owner;
    active = null;
    epoch++;
  }

  public void logout() {
    owner = 0;
    active = null;
    epoch++;
  }

  public long owner() {
    return owner;
  }

  public long epoch() {
    return epoch;
  }

  public VehicleRow active() {
    return active;
  }

  public void setActive(VehicleRow value) {
    boolean changed = active == null || active.getId() != value.getId();
    active = value;
    if (changed) {
      epoch++;
    }
  }

  public boolean isWriting() {
    return writing;
  }

  public void setWriting(boolean writing) {
    this.writing = writing;
  }
}
