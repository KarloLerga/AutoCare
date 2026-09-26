package hr.unizd.autocare.observer;

/** Vrste promjena o kojima Observeri trebaju biti obaviješteni. */
public enum AppEvent {
  /** Podaci ili popis vozila promijenili su se. */
  VEHICLE_CHANGED,
  /** Korisnik je odabrao drugo aktivno vozilo. */
  ACTIVE_VEHICLE_CHANGED,
  /** Spremljen je novi servisni zapis. */
  SERVICE_SAVED
}
