package hr.unizd.autocare.domain;

/** Gruba kategorija korisnikovog problema, bez pokušaja dijagnostike kvara. */
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

  @Override
  public String toString() {
    return displayName;
  }
}
