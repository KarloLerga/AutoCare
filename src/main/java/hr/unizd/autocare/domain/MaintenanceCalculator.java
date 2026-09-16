package hr.unizd.autocare.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Racuna status odrzavanja prema kilometrima i/ili vremenu. */
public final class MaintenanceCalculator {

    public MaintenanceStatus calculate(
            Integer intervalKm,
            Integer intervalMonths,
            LocalDate lastDate,
            Integer lastMileage,
            int currentMileage,
            LocalDate today) {

        if (intervalKm == null && intervalMonths == null) {
            return MaintenanceStatus.NO_DATA;
        }

        if (intervalKm != null && lastMileage == null) {
            return MaintenanceStatus.NO_DATA;
        }

        if (intervalMonths != null && lastDate == null) {
            return MaintenanceStatus.NO_DATA;
        }

        Integer remainingKm = null;
        Long remainingDays = null;

        if (intervalKm != null) {
            int nextMileage = lastMileage + intervalKm;
            remainingKm = nextMileage - currentMileage;
        }

        if (intervalMonths != null) {
            LocalDate nextDate = lastDate.plusMonths(intervalMonths);
            remainingDays = ChronoUnit.DAYS.between(today, nextDate);
        }

        if ((remainingKm != null && remainingKm <= 0)
                || (remainingDays != null && remainingDays <= 0)) {
            return MaintenanceStatus.DUE;
        }

        if ((remainingKm != null && remainingKm <= 3000)
                || (remainingDays != null && remainingDays <= 30)) {
            return MaintenanceStatus.SOON;
        }

        return MaintenanceStatus.OK;
    }
}
