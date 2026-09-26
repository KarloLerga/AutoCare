package hr.unizd.autocare.domain;

import java.math.BigDecimal;

/** Osnovne provjere korisničkog unosa. */
public class Checks {
  private Checks() {}

  public static String text(String value, int max, String label) {
    if (value == null || value.strip().isEmpty()) {
      throw new IllegalArgumentException(label + " je obavezan.");
    }

    String cleanValue = value.strip();

    if (cleanValue.length() > max) {
      throw new IllegalArgumentException(label + " je predugačak.");
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
    String email = text(value, 254, "E-mail").toLowerCase();
    int at = email.indexOf('@');
    int dot = email.lastIndexOf('.');

    if (at <= 0 || dot <= at + 1 || dot >= email.length() - 1 || email.contains(" ")) {
      throw new IllegalArgumentException("Unesite valjanu e-mail adresu.");
    }

    return email;
  }

  public static int mileage(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Kilometraža ne može biti negativna.");
    }

    return value;
  }

  public static BigDecimal money(BigDecimal value) {
    if (value == null) {
      throw new IllegalArgumentException("Unesite stvarno plaćenu cijenu.");
    }
    if (value.signum() < 0) {
      throw new IllegalArgumentException("Cijena ne može biti negativna.");
    }
    return value;
  }

  public static String password(String value) {
    if (value == null || value.length() < 6) {
      throw new IllegalArgumentException("Lozinka treba imati najmanje 6 znakova.");
    }

    return value;
  }
}
