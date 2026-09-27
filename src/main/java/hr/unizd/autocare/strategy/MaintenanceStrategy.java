package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/** Strategy sučelje za različite načine računanja preostalog servisnog intervala. */
public interface MaintenanceStrategy {
  /**
   * Računa relativni dio servisnog intervala koji je preostao prema pravilima konkretne strategije.
   *
   * @param intervalKm kilometarski interval, ako se koristi
   * @param intervalMonths vremenski interval u mjesecima, ako se koristi
   * @param lastDate datum prethodne izvedbe rada
   * @param lastMileage kilometraža prethodne izvedbe rada
   * @param currentMileage trenutačna kilometraža vozila
   * @param today datum izračuna
   * @return relativni preostali interval; vrijednost može biti negativna nakon dospijeća
   */
  double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today);
}
