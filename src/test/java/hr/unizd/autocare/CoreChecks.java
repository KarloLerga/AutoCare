package hr.unizd.autocare;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.view.components.Ui;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Provjere osnovne logike bez baze. */
public final class CoreChecks {
  private static int checks;

  private CoreChecks() {}

  private static void equal(Object expected, Object actual) {
    checks++;
    if (!Objects.equals(expected, actual)) {
      throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }
  }

  private static void fails(Runnable action) {
    checks++;
    try {
      action.run();
    } catch (IllegalArgumentException exception) {
      return;
    }
    throw new AssertionError("Očekivana validacijska iznimka.");
  }

  public static void maintenance() {
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate lastDate = LocalDate.of(2025, 9, 15);
    LocalDate today = LocalDate.of(2026, 9, 15);
    equal(MaintenanceStatus.DUE, calculator.calculate(10000, 12, lastDate, 1000, 1000, today));
    equal(MaintenanceStatus.DUE, calculator.calculate(10000, 12, today, 1000, 11000, today));
    equal(MaintenanceStatus.SOON, calculator.calculate(10000, 12, today, 1000, 8000, today));
    equal(MaintenanceStatus.OK, calculator.calculate(10000, 12, today, 1000, 1000, today));
    equal(MaintenanceStatus.DUE, calculator.calculate(null, 1, lastDate, 1000, 1000, today));
    equal(MaintenanceStatus.SOON, calculator.calculate(null, 1, today, 1000, 1000, today));
    equal(MaintenanceStatus.OK, calculator.calculate(10000, null, today, 1000, 1000, today));
    fails(() -> calculator.calculate(null, null, null, null, 0, today));
    fails(() -> calculator.calculate(10000, null, null, null, 0, today));
  }

  public static void money() {
    equal(
        new BigDecimal("90.00"),
        CostSummary.of(Arrays.asList(new BigDecimal("90.00"), null, BigDecimal.ZERO))
            .getKnownTotal());
    equal(1L, CostSummary.of(Arrays.asList(null, BigDecimal.ZERO)).getUnknownCount());
    equal(0L, CostSummary.of(List.of()).getUnknownCount());
    equal(new BigDecimal("120.50"), Ui.parseMoney("120,50", false));
    equal(new BigDecimal("120.50"), Ui.parseMoney("120.50", false));
    equal(new BigDecimal("0.00"), Ui.parseMoney("0", false));
    equal(new BigDecimal("2.01"), Checks.money(new BigDecimal("2.005"), false));
    equal(null, Ui.parseMoney("", true));
    fails(() -> Ui.parseMoney("", false));
    fails(() -> Ui.parseMoney("1.200,50", false));
    fails(() -> Ui.parseMoney("-2", false));
  }

  public static void validation() {
    equal("karlo@example.com", Checks.email(" Karlo@Example.com "));
    equal(3000001, Checks.mileage(3000001));
    equal(LocalDate.of(2026, 9, 15), Ui.parseDate("15.09.2026."));
    fails(() -> Checks.email("bad"));
    fails(() -> Checks.mileage(-1));
    fails(() -> Checks.password("short"));
    fails(() -> Ui.parseDate("31.02.2026."));
    Checks.password("simple");
    checks++;
  }

  public static void entities() {
    AppUser user = new AppUser("Test", "test@example.com", "not-a-real-password");
    VehicleVariant variant =
        new VehicleVariant("test", "DEMO", "A", "G", "D", 2010, 2026, "Diesel");
    Vehicle vehicle = new Vehicle(user, variant, 2017, 1000);
    user.activate(vehicle);
    equal(vehicle, user.getActiveVehicle());
    vehicle.updateMileage(2000);
    equal(2000, vehicle.getCurrentMileage());
    fails(() -> vehicle.updateMileage(1999));

    WorkDefinition maintenance =
        new WorkDefinition("TEST", "Testni rad", WorkCategory.MAINTENANCE);
    VehicleWorkRule rule =
        new VehicleWorkRule(variant, maintenance, 10000, 12, new BigDecimal("250.00"));
    equal(10000, rule.getIntervalKm());
    equal(12, rule.getIntervalMonths());
    ServiceRecord record = new ServiceRecord(vehicle, LocalDate.now(), 1000, null);
    record.addItem(maintenance, null);
    equal(1L, record.total().getUnknownCount());
    fails(() -> record.addItem(maintenance, BigDecimal.ZERO));
    WorkDefinition repair = new WorkDefinition("REPAIR", "Testni popravak", WorkCategory.REPAIR);
    fails(() -> new VehicleWorkRule(variant, repair, 1000, null, BigDecimal.ONE));
  }

  public static void main(String[] arguments) {
    maintenance();
    money();
    validation();
    entities();
    System.out.println("Core checks passed: " + checks);
  }
}
