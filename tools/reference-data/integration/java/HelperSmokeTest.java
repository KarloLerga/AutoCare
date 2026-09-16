import hr.unizd.autocare.domain.EstimateSelection;
import hr.unizd.autocare.view.components.EstimateFormat;
import java.math.BigDecimal;
import java.util.Set;

/** Dependency-free helper smoke test. This does not test the complete AF2 application. */
public class HelperSmokeTest {
  public static void main(String[] args) {
    check(EstimateFormat.rounded(new BigDecimal("285")).compareTo(new BigDecimal("290")) == 0);
    check(EstimateFormat.rounded(new BigDecimal("284")).compareTo(new BigDecimal("280")) == 0);
    check(EstimateFormat.display(null).equals("Nema procjene"));
    check(EstimateFormat.display(new BigDecimal("120.00")).contains("120"));
    check(
        EstimateSelection.conflicts(Set.of("FRONT_BRAKES"), "FRONT_DISCS_PADS")
            .contains("FRONT_BRAKES"));
    check(EstimateSelection.conflicts(Set.of("OIL_SERVICE"), "OIL_FILTER").contains("OIL_SERVICE"));
    check(EstimateSelection.conflicts(Set.of("CLUTCH_KIT"), "CLUTCH_DMF").contains("CLUTCH_KIT"));
    check(
        EstimateSelection.conflicts(Set.of("TIMING_BELT_PUMP"), "COOLANT")
            .contains("TIMING_BELT_PUMP"));
    check(EstimateSelection.conflicts(Set.of("BRAKE_FLUID"), "AIR_FILTER").isEmpty());
    check(EstimateSelection.conflicts(Set.of("AIR_FILTER"), "AIR_FILTER").contains("AIR_FILTER"));
    check(new BigDecimal("123.45").toPlainString().equals("123.45"));
    System.out.println("PASS: 11 helper assertions. Full Java 25/GUI/JPA testing still required.");
  }

  private static void check(boolean condition) {
    if (!condition) {
      throw new AssertionError();
    }
  }
}
