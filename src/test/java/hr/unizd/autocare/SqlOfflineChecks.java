package hr.unizd.autocare;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.view.components.Ui;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Male provjere koje ne otvaraju SQL/JPA vezu. */
public final class SqlOfflineChecks {
  private static int checks;

  private SqlOfflineChecks() {}

  private static void check(boolean condition) {
    checks++;
    if (!condition) {
      throw new AssertionError("Offline check " + checks);
    }
  }

  public static void run() {
    check(Ui.roundedEstimate(new BigDecimal("284")).equals(new BigDecimal("280")));
    check(Ui.roundedEstimate(new BigDecimal("285")).equals(new BigDecimal("290")));
    check(Ui.estimate(null).equals("Nema procjene"));
    check(Checks.money(new BigDecimal("53.47"), false).equals(new BigDecimal("53.47")));

    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.of(2026, 9, 16);

    check(
        calculator.calculate(10000, null, today, 90000, 99000, today)
            == MaintenanceStatus.SOON);
    check(
        calculator.calculate(null, 12, today, null, 90000, today)
            == MaintenanceStatus.OK);

    System.out.println("Additional offline checks passed: " + checks);
  }

  public static void main(String[] arguments) {
    run();
  }
}
