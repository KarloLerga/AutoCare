package hr.unizd.autocare.domain;

/** Razlikuje standardne radove održavanja od popravaka u katalogu i servisnom unosu. */
public enum WorkCategory {
  /** Planirani radovi koji se prikazuju u katalogu održavanja. */
  MAINTENANCE,
  /** Radovi popravka koji se mogu evidentirati u servisnom zapisu. */
  REPAIR
}
