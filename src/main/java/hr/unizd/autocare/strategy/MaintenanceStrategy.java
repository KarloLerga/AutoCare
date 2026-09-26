package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/** Način računanja koliko je servisnog intervala još preostalo. */
public interface MaintenanceStrategy {
  /**
   * Računa preostali udio odabranog intervala održavanja.
   *
   * @param intervalKm kilometarski interval, ako se koristi
   * @param intervalMonths vremenski interval u mjesecima, ako se koristi
   * @param lastDate datum prethodnog održavanja
   * @param lastMileage kilometraža prethodnog održavanja
   * @param currentMileage trenutačna kilometraža
   * @param today datum izračuna
   * @return udio intervala koji je preostao; rezultat može biti manji od nule nakon dospijeća
   */
  double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today);
}
