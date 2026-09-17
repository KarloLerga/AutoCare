package hr.unizd.autocare.domain;

import hr.unizd.autocare.strategy.CombinedMaintenanceStrategy;
import hr.unizd.autocare.strategy.MaintenanceStrategy;
import hr.unizd.autocare.strategy.MileageMaintenanceStrategy;
import hr.unizd.autocare.strategy.TimeMaintenanceStrategy;
import java.time.LocalDate;

/** Kontekst strategije: odabire izračun prema konkretnom intervalu zapisanom u pravilu. */
public final class MaintenanceCalculator {
  private final MaintenanceStrategy mileageStrategy = new MileageMaintenanceStrategy();
  private final MaintenanceStrategy timeStrategy = new TimeMaintenanceStrategy();
  private final MaintenanceStrategy combinedStrategy = new CombinedMaintenanceStrategy();

  public MaintenanceStatus calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    if (intervalKm != null && intervalMonths != null) {
      return combinedStrategy.calculate(
          intervalKm, intervalMonths, lastDate, lastMileage, currentMileage, today);
    }
    if (intervalKm != null) {
      return mileageStrategy.calculate(
          intervalKm, null, lastDate, lastMileage, currentMileage, today);
    }
    if (intervalMonths != null) {
      return timeStrategy.calculate(
          null, intervalMonths, lastDate, lastMileage, currentMileage, today);
    }
    throw new IllegalArgumentException("Pravilo nema interval.");
  }
}
