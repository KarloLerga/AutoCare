package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Persistentni domenski objekt konkretnog vozila koje pripada korisniku.
 *
 * <p>Vozilo je povezano s kataloškom VehicleVariant, ali dodatno čuva podatke specifične za
 * korisnikov primjerak: godinu proizvodnje i trenutačnu kilometražu.
 */
@Entity
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  private AppUser owner;

  @ManyToOne
  private VehicleVariant variant;

  private int productionYear;
  private int currentMileage;

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected Vehicle() {}

  /**
   * Stvara novo korisničko vozilo.
   *
   * @param owner vlasnik vozila
   * @param variant odabrana kataloška varijanta
   * @param productionYear godina proizvodnje konkretnog vozila
   * @param currentMileage trenutačna kilometraža
   * @throws IllegalArgumentException ako su podaci nevaljani ili godina nije pokrivena varijantom
   */
  public Vehicle(AppUser owner, VehicleVariant variant, int productionYear, int currentMileage) {
    if (owner == null) {
      throw new IllegalArgumentException("Korisnik je obavezan.");
    }
    if (variant == null) {
      throw new IllegalArgumentException("Varijanta vozila je obavezna.");
    }
    if (!variant.covers(productionYear)) {
      throw new IllegalArgumentException("Godina nije u rasponu varijante.");
    }

    this.owner = owner;
    this.variant = variant;
    this.productionYear = productionYear;
    this.currentMileage = Checks.mileage(currentMileage);
  }

  /**
   * Ažurira trenutačnu kilometražu vozila uz domensku provjeru vrijednosti.
   *
   * @param mileage nova kilometraža
   * @throws IllegalArgumentException ako je kilometraža negativna ili manja od postojeće
   */
  public void updateMileage(int mileage) {
    Checks.mileage(mileage);
    if (mileage < currentMileage) {
      throw new IllegalArgumentException("Trenutna kilometraža ne može se smanjiti.");
    }
    currentMileage = mileage;
  }

  public Integer getId() {
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
