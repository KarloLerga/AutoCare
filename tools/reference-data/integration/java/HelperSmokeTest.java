import hr.unizd.autocare.view.components.EstimateFormat;
import java.math.BigDecimal;

/** Dependency-free helper smoke test. This does not test the complete AF2 application. */
public class HelperSmokeTest {
  public static void main(String[] args) {
    check(EstimateFormat.rounded(new BigDecimal("285")).compareTo(new BigDecimal("290")) == 0);
    check(EstimateFormat.rounded(new BigDecimal("284")).compareTo(new BigDecimal("280")) == 0);
    check(EstimateFormat.display(null).equals("Nema procjene"));
    check(EstimateFormat.display(new BigDecimal("120.00")).contains("120"));
    check(new BigDecimal("123.45").toPlainString().equals("123.45"));
    System.out.println("PASS: 5 helper assertions. Full Java 25/GUI/JPA testing still required.");
  }

  private static void check(boolean condition) {
    if (!condition) {
      throw new AssertionError();
    }
  }
}
