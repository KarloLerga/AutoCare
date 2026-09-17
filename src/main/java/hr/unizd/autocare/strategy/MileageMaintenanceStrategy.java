package hr.unizd.autocare.strategy;

import hr.unizd.autocare.domain.MaintenanceStatus;
import java.time.LocalDate;

/** Status prema preostaloj kilometraži. */
public final class MileageMaintenanceStrategy implements MaintenanceStrategy {
  @Override
  public MaintenanceStatus calculate(
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
    return status(remaining);
  }

  static MaintenanceStatus status(int remaining) {
    if (remaining <= 0) {
      return MaintenanceStatus.DUE;
    }
    if (remaining <= 3000) {
      return MaintenanceStatus.SOON;
    }
    return MaintenanceStatus.OK;
  }
}
