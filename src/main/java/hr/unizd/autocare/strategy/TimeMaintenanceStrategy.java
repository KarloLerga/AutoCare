package hr.unizd.autocare.strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Strategy implementacija koja stanje održavanja računa prema proteklom vremenu.
 *
 * <p>Izračun koristi broj dana između zadnjeg i sljedećeg servisnog datuma te broj preostalih dana;
 * rezultat je omjer, nula označava dospijeće, a negativna vrijednost da je datum prošao.
 */
public class TimeMaintenanceStrategy implements MaintenanceStrategy {
  /**
   * Uspoređuje datum posljednje izvedbe, vremenski interval i datum izračuna.
   *
   * @param intervalKm kilometarski interval koji ova strategija ne koristi
   * @param intervalMonths vremenski interval u mjesecima
   * @param lastDate datum posljednje izvedbe rada
   * @param lastMileage kilometraža posljednje izvedbe koja se ovdje ne koristi
   * @param currentMileage trenutačna kilometraža koja se ovdje ne koristi
   * @param today datum na koji se izračun radi
   * @return relativni dio vremenskog intervala koji je preostao
   */
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
