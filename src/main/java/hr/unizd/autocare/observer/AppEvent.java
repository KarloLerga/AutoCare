package hr.unizd.autocare.observer;

/** Događaji koji mogu zahtijevati osvježavanje zajedničkog stanja ili prikaza aplikacije. */
public enum AppEvent {
  /** Promijenjen je popis ili podatak vozila. */
  VEHICLE_CHANGED,
  /** Promijenjeno je vozilo koje predstavlja aktivni kontekst. */
  ACTIVE_VEHICLE_CHANGED,
  /** Spremljen je novi servis i ovisni prikazi trebaju osvježavanje. */
  SERVICE_SAVED
}
