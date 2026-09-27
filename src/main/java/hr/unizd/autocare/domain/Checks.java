package hr.unizd.autocare.domain;

import java.math.BigDecimal;

/**
 * Zajednička domenska pravila za validaciju i normalizaciju korisničkog unosa.
 *
 * <p>Metode vraćaju očišćenu vrijednost kada je unos valjan ili bacaju
 * {@link IllegalArgumentException} s porukom prikladnom za korisničko sučelje.
 */
public class Checks {
  /** Sprječava stvaranje instance utility klase. */
  private Checks() {}

  /**
   * Provjerava obavezni tekst, uklanja rubne razmake i ograničava najveću duljinu.
   *
   * @param value uneseni tekst
   * @param max najveći dopušteni broj znakova
   * @param label naziv polja koji se koristi u poruci o pogrešci
   * @return očišćeni tekst
   * @throws IllegalArgumentException ako je vrijednost prazna ili preduga
   */
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

  /**
   * Validira neobavezni tekst istim pravilima kao obavezni tekst kada je vrijednost unesena.
   *
   * @param value uneseni tekst
   * @param max najveći dopušteni broj znakova
   * @param label naziv polja
   * @return očišćeni tekst ili {@code null} za prazan unos
   * @throws IllegalArgumentException ako uneseni neprazni tekst prelazi dopuštenu duljinu
   */
  public static String optional(String value, int max, String label) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return text(value, max, label);
  }

  /**
   * Normalizira e-mail u mala slova i provjerava osnovni oblik adrese.
   *
   * @param value unesena e-mail adresa
   * @return normalizirana e-mail adresa
   * @throws IllegalArgumentException ako adresa nije u prihvatljivom obliku
   */
  public static String email(String value) {
    String email = text(value, 254, "E-mail").toLowerCase();
    int at = email.indexOf('@');
    int dot = email.lastIndexOf('.');

    if (at <= 0 || dot <= at + 1 || dot >= email.length() - 1 || email.contains(" ")) {
      throw new IllegalArgumentException("Unesite valjanu e-mail adresu.");
    }

    return email;
  }

  /**
   * Provjerava da kilometraža nije negativna.
   *
   * @param value kilometraža
   * @return ista vrijednost kada je valjana
   * @throws IllegalArgumentException ako je kilometraža negativna
   */
  public static int mileage(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Kilometraža ne može biti negativna.");
    }

    return value;
  }

  /**
   * Provjerava stvarno plaćenu cijenu servisne stavke.
   *
   * @param value uneseni iznos
   * @return isti iznos kada je valjan
   * @throws IllegalArgumentException ako iznos nije zadan ili je negativan
   */
  public static BigDecimal money(BigDecimal value) {
    if (value == null) {
      throw new IllegalArgumentException("Unesite stvarno plaćenu cijenu.");
    }
    if (value.signum() < 0) {
      throw new IllegalArgumentException("Cijena ne može biti negativna.");
    }
    return value;
  }

  /**
   * Provjerava minimalnu dopuštenu duljinu lozinke.
   *
   * @param value unesena lozinka
   * @return ista lozinka kada zadovoljava pravilo
   * @throws IllegalArgumentException ako je lozinka prekratka ili nije zadana
   */
  public static String password(String value) {
    if (value == null || value.length() < 6) {
      throw new IllegalArgumentException("Lozinka treba imati najmanje 6 znakova.");
    }

    return value;
  }
}
