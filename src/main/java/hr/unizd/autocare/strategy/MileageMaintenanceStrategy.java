package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/** Računanje intervala koji ovisi o kilometraži. */
public final class MileageMaintenanceStrategy implements MaintenanceStrategy {
  @Override
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    int remainingKm = lastMileage + intervalKm - currentMileage;
    return (double) remainingKm / intervalKm;
  }
}
