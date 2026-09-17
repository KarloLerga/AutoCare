package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

/** Konkretno vozilo korisnika; trenutna kilometraža ne smije se smanjiti. */
@Entity
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private AppUser owner;

  @ManyToOne
  private VehicleVariant variant;

  private int productionYear;
  private int currentMileage;

  protected Vehicle() {}

  public Vehicle(AppUser owner, VehicleVariant variant, int productionYear, int currentMileage) {
    this.owner = Objects.requireNonNull(owner);
    setIdentity(variant, productionYear);
    this.currentMileage = Checks.mileage(currentMileage);
  }

  private void setIdentity(VehicleVariant variant, int productionYear) {
    Objects.requireNonNull(variant);
    if (!variant.covers(productionYear)) {
      throw new IllegalArgumentException("Godina nije u rasponu varijante.");
    }
    this.variant = variant;
    this.productionYear = productionYear;
  }

  public void updateMileage(int mileage) {
    Checks.mileage(mileage);
    if (mileage < currentMileage) {
      throw new IllegalArgumentException("Trenutna kilometraža ne može se smanjiti.");
    }
    currentMileage = mileage;
  }

  public Long getId() {
    return id;
  }

  public AppUser getOwner() {
    return owner;
  }

  public VehicleVariant getVariant() {
    return variant;
  }

  public int getProductionYear() {
    return productionYear;
  }

  public int getCurrentMileage() {
    return currentMileage;
  }
}
