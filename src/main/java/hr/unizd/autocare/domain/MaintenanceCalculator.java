package hr.unizd.autocare.domain;

import hr.unizd.autocare.strategy.CombinedMaintenanceStrategy;
import hr.unizd.autocare.strategy.MaintenanceStrategy;
import hr.unizd.autocare.strategy.MileageMaintenanceStrategy;
import hr.unizd.autocare.strategy.TimeMaintenanceStrategy;
import java.time.LocalDate;

/** Odabire strategiju prema vrsti servisnog intervala. */
public class MaintenanceCalculator {
  private final MaintenanceStrategy mileageStrategy = new MileageMaintenanceStrategy();
  private final MaintenanceStrategy timeStrategy = new TimeMaintenanceStrategy();
  private final MaintenanceStrategy combinedStrategy = new CombinedMaintenanceStrategy();

  /**
   * Izračunava udio preostalog intervala prema kilometraži, vremenu ili oboma.
   *
   * @param intervalKm interval održavanja u kilometrima, ako postoji
   * @param intervalMonths interval održavanja u mjesecima, ako postoji
   * @param lastDate datum posljednjeg evidentiranog rada
   * @param lastMileage kilometraža posljednjeg evidentiranog rada
   * @param currentMileage trenutačna kilometraža vozila
   * @param today datum na koji se računa preostali interval
   * @return omjer preostalog i ukupnog intervala; manja vrijednost znači bliži rok
   */
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    if (intervalKm != null && intervalMonths != null) {
      return combinedStrategy.calculate(intervalKm, intervalMonths, lastDate, lastMileage, currentMileage, today);
    }
    if (intervalKm != null) {
      return mileageStrategy.calculate(intervalKm, null, lastDate, lastMileage, currentMileage, today);
    }
    return timeStrategy.calculate(null, intervalMonths, lastDate, lastMileage, currentMileage, today);
  }
}
