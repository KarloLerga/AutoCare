package hr.unizd.autocare.domain;

/**
 * Korisničke grupe standardnih radova koje omogućuju pregled i filtriranje informativnog kataloga.
 */
public enum CatalogCategory {
  REGULAR_MAINTENANCE("Redovno održavanje"),
  MAJOR_SERVICE("Veći servisi"),
  BRAKES("Kočnice"),
  TYRES_WHEELS("Gume i kotači"),
  ENGINE("Motor"),
  TRANSMISSION("Mjenjač i spojka"),
  SUSPENSION_STEERING("Ovjes i upravljanje"),
  CLIMATE_COOLING("Klima i hlađenje"),
  ELECTRICAL("Elektrika"),
  EXHAUST_EMISSIONS("Ispuh i emisije"),
  BODY_GLASS("Karoserija i stakla"),
  DIAGNOSTICS("Dijagnostika");

  private final String displayName;

  /**
   * @param displayName naziv kategorije prikazan korisniku
   */
  CatalogCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
