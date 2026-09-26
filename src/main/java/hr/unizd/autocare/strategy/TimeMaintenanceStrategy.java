package hr.unizd.autocare.strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Računanje intervala koji ovisi o vremenu. */
public class TimeMaintenanceStrategy implements MaintenanceStrategy {
  @Override
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    LocalDate nextDate = lastDate.plusMonths(intervalMonths);
    long intervalDays = ChronoUnit.DAYS.between(lastDate, nextDate);
    long remainingDays = ChronoUnit.DAYS.between(today, nextDate);
    return (double) remainingDays / intervalDays;
  }
}
