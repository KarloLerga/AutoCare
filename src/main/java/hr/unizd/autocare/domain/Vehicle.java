package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;

/** Konkretno vozilo korisnika; trenutna kilometraza ne smije se smanjiti. */
@Entity
@Table(indexes = @Index(name = "idx_vehicle_owner", columnList = "owner_id"))
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private AppUser owner;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private VehicleVariant variant;

  @Column(nullable = false)
  private int productionYear;

  @Column(nullable = false)
  private int currentMileage;

  protected Vehicle() {}

  public Vehicle(AppUser owner, VehicleVariant variant, int productionYear, int currentMileage) {
    this.owner = Objects.requireNonNull(owner);
    changeIdentity(variant, productionYear);
    this.currentMileage = Checks.mileage(currentMileage);
  }

  public void changeIdentity(VehicleVariant variant, int productionYear) {
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
      throw new IllegalArgumentException("Trenutna kilometraza ne moze se smanjiti.");
    }

    currentMileage = mileage;
  }

  public Long getId() {
    return id;
  }

  public long getVersion() {
    return version;
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
