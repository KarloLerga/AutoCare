package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

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
   * Trenutačno odabrano vozilo korisnika koje određuje kontekst Dashboarda, servisa, održavanja i
   * problema.
   *
   * <p>Ova veza nije isto što i vlasništvo. Vlasništvo je definirano preko {@link Vehicle#getOwner()},
   * gdje jedan korisnik može imati više vozila. Ovdje svaki korisnik može imati najviše jedno
   * aktivno vozilo, a isto konkretno vozilo ne smije biti aktivno za više različitih korisnika.
   *
   * <p>Strani ključ nalazi se u stupcu {@code APP_USER.active_vehicle_id}. Vrijednost može biti
   * {@code null} dok korisnik nema aktivno vozilo. Baza jedinstvenost ne-null vrijednosti osigurava
   * filtered unique indexom, a Service dodatno provjerava da vozilo pripada korisniku.
   */
  @OneToOne
  @JoinColumn(name = "active_vehicle_id")
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
