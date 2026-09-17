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

/** Provjere osnovne logike koje ne trebaju bazu. */
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
    throw new AssertionError("Ocekivana validacijska iznimka.");
  }

  public static void maintenance() {
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate lastDate = LocalDate.of(2025, 9, 15);
    LocalDate today = LocalDate.of(2026, 9, 15);

    equal(MaintenanceStatus.NO_DATA, calculator.calculate(null, null, null, null, 1000, today));
    equal(MaintenanceStatus.NO_DATA, calculator.calculate(10000, 12, null, null, 1000, today));
    equal(MaintenanceStatus.DUE, calculator.calculate(10000, 12, lastDate, 1000, 1000, today));
    equal(MaintenanceStatus.DUE, calculator.calculate(10000, 12, today, 1000, 11000, today));
    equal(MaintenanceStatus.SOON, calculator.calculate(10000, 12, today, 1000, 8000, today));
    equal(MaintenanceStatus.OK, calculator.calculate(10000, 12, today, 1000, 1000, today));
    equal(
        MaintenanceStatus.DUE,
        calculator.calculate(
            null,
            1,
            LocalDate.of(2024, 1, 31),
            null,
            0,
            LocalDate.of(2024, 2, 29)));
    equal(MaintenanceStatus.SOON, calculator.calculate(null, 1, today, null, 0, today));
    equal(
        MaintenanceStatus.SOON,
        calculator.calculate(null, 1, today, null, 0, LocalDate.of(2026, 10, 10)));
    equal(MaintenanceStatus.NO_DATA, calculator.calculate(null, 1, null, 1000, 1000, today));
    equal(MaintenanceStatus.OK, calculator.calculate(10000, null, null, 1000, 1000, today));
    equal(MaintenanceStatus.DUE, calculator.calculate(0, null, today, 0, 0, today));
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

    fails(
        new Runnable() {
          @Override
          public void run() {
            Ui.parseMoney("", false);
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            Ui.parseMoney("1.200,50", false);
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            Ui.parseMoney("-2", false);
          }
        });
  }

  public static void validation() {
    equal("karlo@example.com", Checks.email(" Karlo@Example.com "));
    equal(3000001, Checks.mileage(3000001));
    equal(LocalDate.of(2026, 9, 15), Ui.parseDate("15.09.2026."));

    fails(
        new Runnable() {
          @Override
          public void run() {
            Checks.email("bad");
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            Checks.mileage(-1);
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            Checks.password("short".toCharArray());
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            Ui.parseDate("31.02.2026.");
          }
        });

    Checks.password("simple".toCharArray());
    checks++;
  }

  public static void diagnostics() {
    DiagnosticStrategy strategy = new KeywordDiagnosticStrategy();
    List<RuleData> rules =
        List.of(
            new RuleData(1, "A", "slabo hladi", 3, null, null),
            new RuleData(1, "A", "zvizdi", 1, null, null),
            new RuleData(2, "B", "hladi", 1, null, null));

    List<DiagnosticResult> results = strategy.analyze("SLABO HLADI", rules);
    equal(2, results.size());
    equal(2L, results.get(0).getCandidateId());
    equal(new BigDecimal("75.00"), results.get(1).getScore());
    equal(
        new BigDecimal("75.00"),
        strategy.analyze("slabo hladi slabo hladi", rules).get(1).getScore());
    equal(0, strategy.analyze("rashladi", rules).size());
    equal(
        new BigDecimal("100.00"),
        strategy.analyze("slabo hladi i zvi\u017edi", rules).get(0).getScore());
    equal(0, strategy.analyze("", rules).size());
    equal("cudno d", KeywordDiagnosticStrategy.normalize("\u010cudno \u0110"));
  }

  public static void entities() {
    AppUser user = new AppUser("Test", "test@example.com", "not-a-real-hash");
    VehicleVariant variant =
        new VehicleVariant("test", "DEMO", "A", "G", "D", 2010, 2026, "Diesel");
    Vehicle vehicle = new Vehicle(user, variant, 2017, 1000);

    user.activate(vehicle);
    equal(vehicle, user.getActiveVehicle());

    vehicle.updateMileage(2000);
    equal(2000, vehicle.getCurrentMileage());

    fails(
        new Runnable() {
          @Override
          public void run() {
            vehicle.updateMileage(1999);
          }
        });

    fails(
        new Runnable() {
          @Override
          public void run() {
            vehicle.changeIdentity(variant, 2009);
          }
        });

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

    equal(10000, fallback.getDefaultIntervalKm());
    equal(12, fallback.getDefaultIntervalMonths());
    equal(new BigDecimal("250.00"), fallback.getDefaultEstimatedPrice());

    ServiceRecord record = new ServiceRecord(vehicle, LocalDate.now(), 1000, null);
    record.addItem(work, null);
    equal(1L, record.total().getUnknownCount());

    fails(
        new Runnable() {
          @Override
          public void run() {
            record.addItem(work, BigDecimal.ZERO);
          }
        });

    VehicleWorkRule rule = new VehicleWorkRule(variant, work, 10000, 12, null, null, null);
    equal(10000, rule.getIntervalKm());
    equal(12, rule.getIntervalMonths());

    fails(
        new Runnable() {
          @Override
          public void run() {
            new WorkDefinition(
                "X", "X", WorkCategory.REPAIR, 1000, null, BigDecimal.ONE, "DEMO");
          }
        });
  }

  public static void passwords() {
    PasswordHasher passwordHasher = new PasswordHasher();
    char[] password = "duga testna lozinka".toCharArray();

    String firstHash = passwordHasher.hash(password);
    String secondHash = passwordHasher.hash(password);

    equal(false, firstHash.equals(secondHash));
    equal(true, passwordHasher.verify(password, firstHash));
    equal(false, passwordHasher.verify("wrong-password".toCharArray(), firstHash));
    equal(false, passwordHasher.verify(password, "invalid"));
    equal(false, passwordHasher.verify(password, "pbkdf2-sha256$1$bad$bad"));

    Arrays.fill(password, '\0');
  }

  public static void main(String[] arguments) throws Exception {
    maintenance();
    money();
    validation();
    diagnostics();
    entities();
    passwords();
    System.out.println("Core checks passed: " + checks);
  }
}
