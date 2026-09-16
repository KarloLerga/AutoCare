package hr.unizd.autocare.view.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Informativne procjene. Nikada ne koristiti za stvarno placene iznose. */
public final class EstimateFormat {
  private EstimateFormat() {}

  public static BigDecimal rounded(BigDecimal amount) {
    if (amount == null) {
      return null;
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("Negativna procjena.");
    }
    return amount.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN);
  }

  public static String display(BigDecimal amount) {
    if (amount == null) {
      return "Nema procjene";
    }
    NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("hr-HR"));
    return "\u2248 " + format.format(rounded(amount)) + " \u20ac";
  }
}
