package hr.unizd.autocare.domain;

/** Gruba kategorija korisnikove bilješke, bez pokušaja dijagnostike kvara. */
public enum ProblemCategory {
  ENGINE("Motor"),
  BRAKES("Kočnice"),
  SUSPENSION("Ovjes"),
  CLIMATE("Klima"),
  ELECTRICAL("Elektrika"),
  OTHER("Ostalo");

  private final String displayName;

  ProblemCategory(String displayName) {
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
