package hr.unizd.autocare.strategy;

import hr.unizd.autocare.domain.MaintenanceStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Status prema preostalom vremenu. */
public final class TimeMaintenanceStrategy implements MaintenanceStrategy {
  @Override
  public MaintenanceStatus calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    if (intervalMonths == null || lastDate == null) {
      throw new IllegalArgumentException("Vremenski interval zahtijeva datum zadnjeg servisa.");
    }
    long remaining = ChronoUnit.DAYS.between(today, lastDate.plusMonths(intervalMonths));
    if (remaining <= 0) {
      return MaintenanceStatus.DUE;
    }
    if (remaining <= 30) {
      return MaintenanceStatus.SOON;
    }
    return MaintenanceStatus.OK;
  }
}
