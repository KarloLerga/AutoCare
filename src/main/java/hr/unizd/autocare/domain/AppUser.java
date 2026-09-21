package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Korisnički račun i njegovo aktivno vozilo. */
@Entity
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String email;
  private String password;

  @ManyToOne
  private Vehicle activeVehicle;

  protected AppUser() {}

  public AppUser(String name, String email, String password) {
    this.name = Checks.text(name, 100, "Ime");
    this.email = Checks.email(email);
    this.password = Checks.password(password);
  }

  public void activate(Vehicle vehicle) {
    if (vehicle == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }

    activeVehicle = vehicle;
  }

  public void changeProfile(String name, String email) {
    this.name = Checks.text(name, 100, "Ime");
    this.email = Checks.email(email);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
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
