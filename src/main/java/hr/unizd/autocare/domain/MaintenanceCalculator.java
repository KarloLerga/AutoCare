package hr.unizd.autocare.domain;

import hr.unizd.autocare.strategy.CombinedMaintenanceStrategy;
import hr.unizd.autocare.strategy.MaintenanceStrategy;
import hr.unizd.autocare.strategy.MileageMaintenanceStrategy;
import hr.unizd.autocare.strategy.TimeMaintenanceStrategy;
import java.time.LocalDate;

/**
 * Domenski kalkulator koji odabire odgovarajuću Strategy implementaciju za servisni interval.
 *
 * <p>Kalkulator ne dohvaća podatke iz baze i ne poznaje Swing. Dobiva sve potrebne vrijednosti
 * kroz parametre i odlučuje treba li računati prema kilometraži, vremenu ili kombinaciji oba
 * kriterija.
 */
public class MaintenanceCalculator {
  /** Računa preostali omjer kada se interval prati samo prema kilometraži. */
  private final MaintenanceStrategy mileageStrategy = new MileageMaintenanceStrategy();

  /** Računa preostali omjer kada se interval prati samo prema vremenu. */
  private final MaintenanceStrategy timeStrategy = new TimeMaintenanceStrategy();

  /** Uspoređuje kilometarski i vremenski interval te odabire onaj koji prije dospijeva. */
  private final MaintenanceStrategy combinedStrategy = new CombinedMaintenanceStrategy();

  /**
   * Računa relativni dio servisnog intervala koji je još preostao.
   *
   * @param intervalKm kilometarski interval ili {@code null} ako se ne koristi
   * @param intervalMonths vremenski interval u mjesecima ili {@code null} ako se ne koristi
   * @param lastDate datum posljednje izvedbe rada
   * @param lastMileage kilometraža posljednje izvedbe rada
   * @param currentMileage trenutačna kilometraža vozila
   * @param today datum na koji se račun radi
   * @return relativni preostali interval; vrijednost može pasti ispod nule kada je interval prošao
   */
  public double calculate(
      Integer intervalKm,
      Integer intervalMonths,
      LocalDate lastDate,
      Integer lastMileage,
      int currentMileage,
      LocalDate today) {
    if (intervalKm != null && intervalMonths != null) {
      return combinedStrategy.calculate(intervalKm, intervalMonths, lastDate, lastMileage, currentMileage, today);
    }
    if (intervalKm != null) {
      return mileageStrategy.calculate(intervalKm, null, lastDate, lastMileage, currentMileage, today);
    }
    return timeStrategy.calculate(null, intervalMonths, lastDate, lastMileage, currentMileage, today);
  }
}
