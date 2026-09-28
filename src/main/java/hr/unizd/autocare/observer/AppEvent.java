package hr.unizd.autocare.observer;

/** Događaji koji mogu zahtijevati osvježavanje zajedničkog stanja ili prikaza aplikacije. */
public enum AppEvent {
  /** Promijenjeni su podaci ili popis vozila; objavljuje se nakon uspješne promjene vozila. */
  VEHICLE_CHANGED,
  /** Promijenjeno je trenutačno vozilo koje određuje kontekst prikazanih podataka. */
  ACTIVE_VEHICLE_CHANGED,
  /** Novi servis je spremljen pa prikazi servisne povijesti i održavanja trebaju osvježavanje. */
  SERVICE_SAVED
}
