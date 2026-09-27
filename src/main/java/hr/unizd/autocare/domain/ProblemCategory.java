package hr.unizd.autocare.domain;

/** Gruba korisnička kategorija evidentiranog problema bez pokušaja automatske dijagnoze kvara. */
public enum ProblemCategory {
  ENGINE("Motor"),
  BRAKES("Kočnice"),
  SUSPENSION("Ovjes"),
  CLIMATE("Klima"),
  ELECTRICAL("Elektrika"),
  OTHER("Ostalo");

  private final String displayName;

  /** @param displayName naziv kategorije prikazan korisniku */
  ProblemCategory(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
