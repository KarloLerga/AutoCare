package hr.unizd.autocare.domain;

/** Gruba korisnička kategorija evidentiranog problema bez pokušaja automatske dijagnoze kvara. */
public enum ProblemCategory {
  /** Kvar ili simptom koji se odnosi na motor. */
  ENGINE("Motor"),
  /** Kvar ili simptom koji se odnosi na kočnice. */
  BRAKES("Kočnice"),
  /** Kvar ili simptom koji se odnosi na ovjes. */
  SUSPENSION("Ovjes"),
  /** Kvar ili simptom koji se odnosi na klimatizaciju. */
  CLIMATE("Klima"),
  /** Kvar ili simptom koji se odnosi na elektriku vozila. */
  ELECTRICAL("Elektrika"),
  /** Kategorija za problem koji ne pripada ostalim ponuđenim skupinama. */
  OTHER("Ostalo");

  /** Korisnički naziv kategorije koji se prikazuje u sučelju. */
  private final String displayName;

  /**
   * Povezuje enum vrijednost s nazivom kategorije prikazanim u sučelju.
   *
   * @param displayName naziv kategorije prikazan korisniku
   */
  ProblemCategory(String displayName) {
    this.displayName = displayName;
  }

  /** @return naziv kategorije prikladan za prikaz */
  @Override
  public String toString() {
    return displayName;
  }
}
