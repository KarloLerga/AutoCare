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
import hr.unizd.autocare.model.Data.DiagnosticResult;
import hr.unizd.autocare.model.Data.RuleData;
import hr.unizd.autocare.service.PasswordHasher;
import hr.unizd.autocare.strategy.DiagnosticStrategy;
import hr.unizd.autocare.strategy.KeywordDiagnosticStrategy;
import hr.unizd.autocare.view.components.Ui;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Provjere ciste logike mogu se izvrsiti i bez baze; JUnit ih ukljucuje u Maven test. */
public final class CoreChecks {
  private static int checks;

  private static void eq(Object expected, Object actual) {
    checks++;
    if (!Objects.equals(expected, actual)) {
      throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }
  }

  private static void fails(Runnable action) {
    checks++;
    try {
      action.run();
    } catch (IllegalArgumentException ex) {
      return;
    }
    throw new AssertionError("Ocekivana validacijska iznimka.");
  }

  public static void maintenance() {
    MaintenanceCalculator c = new MaintenanceCalculator();
    LocalDate last = LocalDate.of(2025, 9, 15), today = LocalDate.of(2026, 9, 15);
    eq(MaintenanceStatus.NO_DATA, c.calculate(null, null, null, null, 1000, today));
    eq(MaintenanceStatus.NO_DATA, c.calculate(10000, 12, null, null, 1000, today));
    eq(MaintenanceStatus.DUE, c.calculate(10000, 12, last, 1000, 1000, today));
    eq(MaintenanceStatus.DUE, c.calculate(10000, 12, today, 1000, 11000, today));
    eq(MaintenanceStatus.SOON, c.calculate(10000, 12, today, 1000, 8000, today));
    eq(MaintenanceStatus.OK, c.calculate(10000, 12, today, 1000, 1000, today));
    eq(
        MaintenanceStatus.DUE,
        c.calculate(null, 1, LocalDate.of(2024, 1, 31), null, 0, LocalDate.of(2024, 2, 29)));
    eq(MaintenanceStatus.SOON, c.calculate(null, 1, today, null, 0, today));
    eq(MaintenanceStatus.SOON, c.calculate(null, 1, today, null, 0, LocalDate.of(2026, 10, 10)));
    eq(MaintenanceStatus.NO_DATA, c.calculate(null, 1, null, 1000, 1000, today));
    eq(MaintenanceStatus.OK, c.calculate(10000, null, null, 1000, 1000, today));
    eq(MaintenanceStatus.DUE, c.calculate(0, null, today, 0, 0, today));
  }

  public static void money() {
    eq(
        new BigDecimal("90.00"),
        CostSummary.of(Arrays.asList(new BigDecimal("90.00"), null, BigDecimal.ZERO))
            .getKnownTotal());
    eq(1L, CostSummary.of(Arrays.asList(null, BigDecimal.ZERO)).getUnknownCount());
    eq(0L, CostSummary.of(List.of()).getUnknownCount());
    eq(new BigDecimal("120.50"), Ui.parseMoney("120,50", false));
    eq(new BigDecimal("120.50"), Ui.parseMoney("120.50", false));
    eq(new BigDecimal("0.00"), Ui.parseMoney("0", false));
    eq(null, Ui.parseMoney("", true));
    fails(() -> Ui.parseMoney("", false));
    fails(() -> Ui.parseMoney("1.200,50", false));
    fails(() -> Ui.parseMoney("-2", false));
    fails(() -> Checks.money(new BigDecimal("2.005"), false));
  }

  public static void validation() {
    eq("karlo@example.com", Checks.email(" Karlo@Example.com "));
    fails(() -> Checks.email("bad"));
    fails(() -> Checks.email("a..b@example.com"));
    fails(() -> Checks.mileage(-1));
    fails(() -> Checks.mileage(3000001));
    eq(3000000, Checks.mileage(3000000));
    fails(() -> Checks.password("short".toCharArray()));
    Checks.password("duga-testna-lozinka".toCharArray());
    checks++;
  }

  public static void diagnostics() {
    DiagnosticStrategy s = new KeywordDiagnosticStrategy();
    List<RuleData> rules =
        List.of(
            new RuleData(1, "A", "slabo hladi", 3, null, null),
            new RuleData(1, "A", "zvizdi", 1, null, null),
            new RuleData(2, "B", "hladi", 1, null, null));
    List<DiagnosticResult> results = s.analyze("SLABO HLADI", rules);
    eq(2, results.size());
    eq(2L, results.get(0).getCandidateId());
    eq(new BigDecimal("75.00"), results.get(1).getScore());
    eq(new BigDecimal("75.00"), s.analyze("slabo hladi slabo hladi", rules).get(1).getScore());
    eq(0, s.analyze("rashladi", rules).size());
    eq(new BigDecimal("100.00"), s.analyze("slabo hladi i zvi\u017edi", rules).get(0).getScore());
    eq(0, s.analyze("", rules).size());
    eq("cudno d", KeywordDiagnosticStrategy.normalize("\u010cudno \u0110"));
  }

  public static void entities() {
    AppUser user = new AppUser("Test", "test@example.com", "not-a-real-hash");
    VehicleVariant variant =
        new VehicleVariant("test", "DEMO", "A", "G", "D", 2010, 2026, "Diesel");
    Vehicle v = new Vehicle(user, variant, 2017, 1000);
    user.activate(v);
    eq(v, user.getActiveVehicle());
    v.updateMileage(2000);
    eq(2000, v.getCurrentMileage());
    fails(() -> v.updateMileage(1999));
    fails(() -> v.changeIdentity(variant, 2009));
    WorkDefinition work =
        new WorkDefinition("TEST", "Testni rad", WorkCategory.MAINTENANCE, null, null);
    WorkDefinition fallback =
        new WorkDefinition(
            "FALLBACK",
            "Zadani servis",
            WorkCategory.MAINTENANCE,
            10000,
            12,
            new BigDecimal("250.00"),
            "DEMO");
    eq(10000, fallback.getDefaultIntervalKm());
    eq(12, fallback.getDefaultIntervalMonths());
    eq(new BigDecimal("250.00"), fallback.getDefaultEstimatedPrice());
    ServiceRecord record =
        new ServiceRecord(v, LocalDate.now(), 1000, null);
    record.addItem(work, null);
    eq(1L, record.total().getUnknownCount());
    fails(() -> record.addItem(work, BigDecimal.ZERO));
    fails(() -> new VehicleWorkRule(variant, work, 10000, 12, null, null, null));
    fails(
        () ->
            new WorkDefinition(
                "X", "X", WorkCategory.REPAIR, 1000, null, BigDecimal.ONE, "DEMO"));
  }

  public static void passwords() {
    PasswordHasher p = new PasswordHasher();
    char[] password = "duga testna lozinka".toCharArray();
    String a = p.hash(password), b = p.hash(password);
    eq(false, a.equals(b));
    eq(true, p.verify(password, a));
    eq(false, p.verify("wrong-password".toCharArray(), a));
    eq(false, p.verify(password, "invalid"));
    eq(false, p.verify(password, "pbkdf2-sha256$1$bad$bad"));
    Arrays.fill(password, '\0');
  }

  public static void main(String[] args) throws Exception {
    maintenance();
    money();
    validation();
    diagnostics();
    entities();
    passwords();
    System.out.println("Core checks passed: " + checks);
  }
}
