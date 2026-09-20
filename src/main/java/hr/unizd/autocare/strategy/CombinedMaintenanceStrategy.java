package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/** Kod kombiniranog intervala uzima se kriterij koji dolazi prije. */
public final class CombinedMaintenanceStrategy implements MaintenanceStrategy {
  private final MileageMaintenanceStrategy mileageStrategy = new MileageMaintenanceStrategy();
  private final TimeMaintenanceStrategy timeStrategy = new TimeMaintenanceStrategy();

  @Override
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    double mileageRemaining =
        mileageStrategy.calculate(intervalKm, null, lastDate, lastMileage, currentMileage, today);
    double timeRemaining =
        timeStrategy.calculate(null, intervalMonths, lastDate, lastMileage, currentMileage, today);
    return Math.min(mileageRemaining, timeRemaining);
  }
}
