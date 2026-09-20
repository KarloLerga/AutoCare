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
    if (intervalKm == null || lastMileage == null) {
      throw new IllegalArgumentException("Kilometarski interval zahtijeva zadnju kilometražu.");
    }
    int remaining = lastMileage + intervalKm - currentMileage;
    return (double) remaining / intervalKm;
  }
}
