package hr.unizd.autocare.strategy;

import hr.unizd.autocare.domain.MaintenanceStatus;
import java.time.LocalDate;

/** Kombinirani interval dospijeva čim je dospio bilo kilometarski ili vremenski kriterij. */
public final class CombinedMaintenanceStrategy implements MaintenanceStrategy {
  private final MileageMaintenanceStrategy mileage = new MileageMaintenanceStrategy();
  private final TimeMaintenanceStrategy time = new TimeMaintenanceStrategy();

  @Override
  public MaintenanceStatus calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    MaintenanceStatus mileageStatus =
        mileage.calculate(intervalKm, null, lastDate, lastMileage, currentMileage, today);
    MaintenanceStatus timeStatus =
        time.calculate(null, intervalMonths, lastDate, lastMileage, currentMileage, today);
    if (mileageStatus == MaintenanceStatus.DUE || timeStatus == MaintenanceStatus.DUE) {
      return MaintenanceStatus.DUE;
    }
    if (mileageStatus == MaintenanceStatus.SOON || timeStatus == MaintenanceStatus.SOON) {
      return MaintenanceStatus.SOON;
    }
    return MaintenanceStatus.OK;
  }
}
