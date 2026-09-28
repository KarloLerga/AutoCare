package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Persistentni domenski objekt korisničkog računa.
 *
 * <p>Korisnik može posjedovati više vozila, a aktivno vozilo predstavlja trenutačno odabrani
 * kontekst aplikacije i nije isto što i samo vlasništvo nad vozilom.
 */
@Entity
public class AppUser {
  /** Baza generira jedinstveni identifikator računa koji koriste veze i dohvat korisnika. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /** Ime prikazno povezano s korisničkim računom. */
  private String name;

  /** Normalizirana e-mail adresa kojom se korisnik prijavljuje. */
  private String email;

  /** Lozinka spremljena uz račun; autentikaciju provodi {@code AuthService}. */
  private String password;

  /**
   * Trenutačno odabrano vozilo korisnika, koje služi kao zadani kontekst u aplikaciji.
   *
   * <p>{@code ManyToOne} mapira vezu preko ključa vozila na strani računa; ovo polje bilježi
   * odabir, dok se vlasništvo nad vozilom provjerava u Service sloju.
   */
  @ManyToOne
  private Vehicle activeVehicle;

  /** Konstruktor bez argumenata potreban JPA provideru pri učitavanju entiteta. */
  protected AppUser() {}

  /**
   * Stvara novi korisnički račun iz već validiranih podataka.
   *
   * @param name ime korisnika
   * @param email normalizirana e-mail adresa
   * @param password lozinka korisnika
   * @throws IllegalArgumentException ako neki podatak nije valjan
   */
  public AppUser(String name, String email, String password) {
    this.name = Checks.text(name, 100, "Ime");
    this.email = Checks.email(email);
    this.password = Checks.password(password);
  }

  /**
   * Postavlja vozilo kao aktivno vozilo korisnika.
   *
   * <p>Service prije poziva provjerava da odabrano vozilo stvarno pripada tom korisniku.
   *
   * @param vehicle vozilo koje postaje aktivno
   * @throws IllegalArgumentException ako vozilo nije zadano
   */
  public void activate(Vehicle vehicle) {
    if (vehicle == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }

    activeVehicle = vehicle;
  }


  /** @return identifikator koji je dodijelila baza */
  public Integer getId() {
    return id;
  }


  /** @return e-mail adresa korištena za prijavu */
  public String getEmail() {
    return email;
  }

  /** @return spremljena vrijednost lozinke korisnika */
  public String getPassword() {
    return password;
  }

  /** @return odabrano aktivno vozilo ili {@code null} ako nije postavljeno */
  public Vehicle getActiveVehicle() {
    return activeVehicle;
  }
}
