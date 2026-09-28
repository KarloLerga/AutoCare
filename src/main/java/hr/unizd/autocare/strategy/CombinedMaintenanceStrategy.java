package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/**
 * Strategy implementacija za radove koji imaju i kilometarski i vremenski servisni interval.
 *
 * <p>Računa oba kriterija i vraća onaj koji ranije dospijeva, odnosno manji preostali omjer.
 */
public class CombinedMaintenanceStrategy implements MaintenanceStrategy {
  /** Izračunava preostali omjer kilometarskog intervala. */
  private final MileageMaintenanceStrategy mileageStrategy = new MileageMaintenanceStrategy();

  /** Izračunava preostali omjer vremenskog intervala. */
  private final TimeMaintenanceStrategy timeStrategy = new TimeMaintenanceStrategy();

  /**
   * Računa kilometarski i vremenski kriterij te vraća stroži od ta dva rezultata.
   *
   * @param intervalKm kilometarski interval
   * @param intervalMonths vremenski interval u mjesecima
   * @param lastDate datum posljednje izvedbe rada
   * @param lastMileage kilometraža posljednje izvedbe rada
   * @param currentMileage trenutačna kilometraža vozila
   * @param today datum na koji se izračun radi
   * @return manji preostali omjer kilometarskog i vremenskog intervala
   */
  @Override
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    double mileageRemaining = mileageStrategy.calculate(intervalKm, null, lastDate, lastMileage, currentMileage, today);
    double timeRemaining = timeStrategy.calculate(null, intervalMonths, lastDate, lastMileage, currentMileage, today);
    return Math.min(mileageRemaining, timeRemaining);
  }
}
