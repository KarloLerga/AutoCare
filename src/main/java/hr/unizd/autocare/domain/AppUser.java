package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.util.Objects;

/** Korisnicki racun i njegovo aktivno vozilo. */
@Entity
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @ManyToOne(fetch = FetchType.LAZY)
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

  public long getVersion() {
    return version;
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
