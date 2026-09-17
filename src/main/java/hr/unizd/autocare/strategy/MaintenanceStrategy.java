package hr.unizd.autocare.strategy;

import hr.unizd.autocare.domain.MaintenanceStatus;
import java.time.LocalDate;

/** Strategija za izračun statusa jednog konkretnog intervala održavanja. */
public interface MaintenanceStrategy {
  MaintenanceStatus calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today);
}
