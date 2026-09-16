package hr.unizd.autocare.domain;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/** Cisti kalendarski izracun, bez baze i GUI-a; oba kriterija povezuje OR. */
public final class MaintenanceCalculator {
    public MaintenanceStatus calculate(ScheduleKind kind, Integer km, Integer months, LocalDate date, Integer mileage, int current, LocalDate today) {
        if(kind==ScheduleKind.CONDITION_BASED)return MaintenanceStatus.CONDITION_BASED;
        if(kind==ScheduleKind.VEHICLE_INDICATOR)return MaintenanceStatus.VEHICLE_INDICATOR;
        return calculate(km, months, date, mileage, current, today);
    }
    public MaintenanceStatus calculate(Integer intervalKm, Integer intervalMonths, LocalDate lastDate, Integer lastMileage, int currentMileage, LocalDate today) {
        if(intervalKm==null && intervalMonths==null)return MaintenanceStatus.UNKNOWN_INTERVAL;
        if((intervalKm!=null && intervalKm<=0)||(intervalMonths!=null && intervalMonths<=0))throw new IllegalArgumentException("Interval mora biti pozitivan.");
        if((intervalKm!=null && lastMileage==null)||(intervalMonths!=null && lastDate==null))return MaintenanceStatus.UNKNOWN_HISTORY;
        long remainingKm=intervalKm==null?Long.MAX_VALUE:(long)lastMileage+intervalKm-currentMileage;
        LocalDate next=intervalMonths==null?null:lastDate.plusMonths(intervalMonths);
        long remainingDays=next==null?Long.MAX_VALUE:ChronoUnit.DAYS.between(today, next);
        if(remainingKm<=0 || remainingDays<=0)return MaintenanceStatus.DUE;
        long kmThreshold=intervalKm==null?0:Math.min(3000, (intervalKm+4L)/5L);
        long dayThreshold=next==null?0:Math.min(30, (ChronoUnit.DAYS.between(lastDate, next)+4)/5);
        if(remainingKm<=kmThreshold || remainingDays<=dayThreshold)return MaintenanceStatus.SOON;
        return MaintenanceStatus.OK;
    }
}
