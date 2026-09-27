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
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String email;
  private String password;

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


  public Integer getId() {
    return id;
  }


  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public Vehicle getActiveVehicle() {
    return activeVehicle;
  }
}
