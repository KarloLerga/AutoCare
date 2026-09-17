package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

/** Korisnicki racun i njegovo aktivno vozilo. */
@Entity
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String email;
  private String passwordHash;

  @ManyToOne
  private Vehicle activeVehicle;

  protected AppUser() {}

  public AppUser(String name, String email, String passwordHash) {
    this.name = Checks.text(name, 100, "Ime");
    this.email = Checks.email(email);
    this.passwordHash = Objects.requireNonNull(passwordHash);
  }

  public void activate(Vehicle vehicle) {
    Objects.requireNonNull(vehicle);
    boolean sameObject = vehicle.getOwner() == this;
    boolean sameId = id != null && id.equals(vehicle.getOwner().getId());
    if (!sameObject && !sameId) {
      throw new IllegalArgumentException("Vozilo nije vase.");
    }
    activeVehicle = vehicle;
  }

  public void changeProfile(String name, String email) {
    this.name = Checks.text(name, 100, "Ime");
    this.email = Checks.email(email);
  }

  public void changePasswordHash(String passwordHash) {
    this.passwordHash = Objects.requireNonNull(passwordHash);
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

  public String getPasswordHash() {
    return passwordHash;
  }

  public Vehicle getActiveVehicle() {
    return activeVehicle;
  }
}
