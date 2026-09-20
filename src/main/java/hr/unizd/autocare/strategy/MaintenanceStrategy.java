package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/** Način računanja koliko je servisnog intervala još preostalo. */
public interface MaintenanceStrategy {
  double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today);
}
