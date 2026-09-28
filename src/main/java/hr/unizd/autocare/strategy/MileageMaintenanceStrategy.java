package hr.unizd.autocare.strategy;

import java.time.LocalDate;

/**
 * Strategy implementacija koja stanje održavanja računa isključivo prema prijeđenoj kilometraži.
 *
 * <p>Rezultat je bezdimenzijski omjer preostalih kilometara prema punom intervalu: nula označava
 * dospijeće, a negativna vrijednost da je interval prijeđen.
 */
public class MileageMaintenanceStrategy implements MaintenanceStrategy {
  /**
   * Uspoređuje kilometražu posljednje izvedbe, kilometarski interval i trenutačnu kilometražu.
   *
   * @param intervalKm kilometarski interval
   * @param intervalMonths vremenski interval koji ova strategija ne koristi
   * @param lastDate datum posljednje izvedbe koji ova strategija ne koristi
   * @param lastMileage kilometraža posljednje izvedbe rada
   * @param currentMileage trenutačna kilometraža vozila
   * @param today datum izračuna koji ova strategija ne koristi
   * @return relativni dio kilometarskog intervala koji je preostao
   */
  @Override
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    int remainingKm = lastMileage + intervalKm - currentMileage;
    return (double) remainingKm / intervalKm;
  }
}
