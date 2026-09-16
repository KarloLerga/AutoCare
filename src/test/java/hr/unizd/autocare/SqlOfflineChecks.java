package hr.unizd.autocare;

import hr.unizd.autocare.app.SqlSettings;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ScheduleKind;
import hr.unizd.autocare.view.components.EstimateFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** JDK-only provjere konfiguracije i granica; ne otvara SQL/JPA vezu. */
public final class SqlOfflineChecks {

  private static int checks;

  private SqlOfflineChecks() {}

  private static void check(boolean condition) {
    checks++;

    if (!condition) {
      throw new AssertionError("Offline check " + checks);
    }
  }

  private static void rejects(Runnable action) {
    checks++;

    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      return;
    }

    throw new AssertionError("Expected input rejection");
  }

  public static void run() {
    Map<String, String> environment =
        new HashMap<>(
            Map.of(
                "AUTOCARE_DB_HOST", "unit.database.windows.net",
                "AUTOCARE_DB_PORT", "1433",
                "AUTOCARE_DB_NAME", "unit_db",
                "AUTOCARE_DB_USER", "test",
                "AUTOCARE_DB_PASSWORD", "fictional-test-value"));

    SqlSettings settings = SqlSettings.from(environment);
    check(settings.getJdbcUrl().startsWith("jdbc:sqlserver://"));
    check(settings.getJdbcUrl().contains("encrypt=true"));
    check(settings.getJdbcUrl().contains("trustServerCertificate=false"));
    check(!settings.getJdbcUrl().contains("fictional-test-value"));
    check(!settings.toString().contains("fictional-test-value"));
    check(settings.getDatabase().equals("unit_db"));

    environment.put("AUTOCARE_DB_NAME", "master");
    rejects(() -> SqlSettings.from(environment));
    environment.put("AUTOCARE_DB_NAME", "x;encrypt=false");
    rejects(() -> SqlSettings.from(environment));
    environment.put("AUTOCARE_DB_NAME", "");
    rejects(() -> SqlSettings.from(environment));
    environment.put("AUTOCARE_DB_NAME", "unit_db");
    environment.put("AUTOCARE_DB_PORT", "0");
    rejects(() -> SqlSettings.from(environment));
    environment.put("AUTOCARE_DB_PORT", "1433");

    check(EstimateFormat.rounded(new BigDecimal("284")).equals(new BigDecimal("280")));
    check(EstimateFormat.rounded(new BigDecimal("285")).equals(new BigDecimal("290")));
    check(EstimateFormat.display(null).equals("Nema procjene"));
    rejects(() -> EstimateFormat.rounded(new BigDecimal("-1")));
    check(Checks.money(new BigDecimal("53.47"), false).equals(new BigDecimal("53.47")));

    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.of(2026, 9, 16);
    check(
        calculator.calculate(ScheduleKind.CONDITION_BASED, null, null, null, null, 90000, today)
            == MaintenanceStatus.NO_DATA);
    check(
        calculator.calculate(ScheduleKind.VEHICLE_INDICATOR, null, null, null, null, 90000, today)
            == MaintenanceStatus.NO_DATA);
    check(
        calculator.calculate(ScheduleKind.UNKNOWN, null, null, null, null, 90000, today)
            == MaintenanceStatus.NO_DATA);

    System.out.println("Additional offline checks passed: " + checks);
  }

  public static void main(String[] args) {
    run();
  }
}
