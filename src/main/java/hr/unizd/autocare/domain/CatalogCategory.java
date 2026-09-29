package hr.unizd.autocare.domain;

/**
 * Korisničke grupe standardnih radova koje omogućuju pregled i filtriranje informativnog kataloga.
 */
public enum CatalogCategory {
  /** Redovni potrošni radovi i periodično održavanje. */
  REGULAR_MAINTENANCE("Redovno održavanje"),
  /** Veći planirani servisni zahvati. */
  MAJOR_SERVICE("Veći servisi"),
  /** Radovi vezani uz kočioni sustav. */
  BRAKES("Kočnice"),
  /** Radovi vezani uz gume i kotače. */
  TYRES_WHEELS("Gume i kotači"),
  /** Radovi vezani uz motor. */
  ENGINE("Motor"),
  /** Radovi vezani uz mjenjač i spojku. */
  TRANSMISSION("Mjenjač i spojka"),
  /** Radovi vezani uz ovjes i upravljanje. */
  SUSPENSION_STEERING("Ovjes i upravljanje"),
  /** Radovi vezani uz klimatizaciju i hlađenje. */
  CLIMATE_COOLING("Klima i hlađenje"),
  /** Radovi vezani uz električni sustav vozila. */
  ELECTRICAL("Elektrika"),
  /** Radovi vezani uz ispušni sustav i emisije. */
  EXHAUST_EMISSIONS("Ispuh i emisije"),
  /** Radovi vezani uz karoseriju i stakla. */
  BODY_GLASS("Karoserija i stakla"),
  /** Dijagnostički postupci i očitanja. */
  DIAGNOSTICS("Dijagnostika");

  /** Tekst koji se prikazuje korisniku umjesto tehničkog naziva enum vrijednosti. */
  private final String displayName;

  /**
   * Povezuje enum vrijednost s lokaliziranim nazivom koji prikazuje korisničko sučelje.
   *
   * @param displayName naziv kategorije prikazan korisniku
   */
  CatalogCategory(String displayName) {
    this.displayName = displayName;
  }

  /** Dohvaća lokalizirani naziv kategorije koji se prikazuje korisniku.
   *
   * @return naziv kategorije namijenjen prikazu u sučelju
   */
  public String getDisplayName() {
    return displayName;
  }

  /** Vraća naziv kategorije za JComboBox i ostale tekstualne prikaze.
   *
   * @return korisnički naziv kategorije
   */
  @Override
  public String toString() {
    return displayName;
  }
}
