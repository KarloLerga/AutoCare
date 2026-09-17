package hr.unizd.autocare.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Osnovne provjere korisnickog unosa. */
public final class Checks {
  private Checks() {}

  public static String text(String value, int max, String label) {
    if (value == null || value.strip().isEmpty()) {
      throw new IllegalArgumentException(label + " je obavezan.");
    }

    String cleanValue = value.strip();

    if (cleanValue.length() > max) {
      throw new IllegalArgumentException(label + " je predugacak.");
    }

    return cleanValue;
  }

  public static String optional(String value, int max, String label) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return text(value, max, label);
  }

  public static String email(String value) {
    String email = text(value, 254, "E-mail").toLowerCase(Locale.ROOT);
    int at = email.indexOf('@');
    int dot = email.lastIndexOf('.');

    if (at <= 0 || dot <= at + 1 || dot >= email.length() - 1 || email.contains(" ")) {
      throw new IllegalArgumentException("Unesite valjanu e-mail adresu.");
    }

    return email;
  }

  public static int mileage(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Kilometraza ne moze biti negativna.");
    }

    return value;
  }

  public static BigDecimal money(BigDecimal value, boolean nullable) {
    if (value == null) {
      if (nullable) {
        return null;
      }

      throw new IllegalArgumentException("Unesite stvarno placenu cijenu.");
    }

    if (value.signum() < 0) {
      throw new IllegalArgumentException("Cijena ne moze biti negativna.");
    }

    return value.setScale(2, RoundingMode.HALF_UP);
  }

  public static String password(String value) {
    if (value == null || value.length() < 6) {
      throw new IllegalArgumentException("Lozinka treba imati najmanje 6 znakova.");
    }

    return value;
  }
}
