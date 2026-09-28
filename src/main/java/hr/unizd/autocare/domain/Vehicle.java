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
  /** Identifikator konkretnog vozila koji dodjeljuje baza. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Korisnik koji posjeduje ovo vozilo.
   *
   * <p>Svako vozilo ima jednog vlasnika, a jedan korisnik može imati više vozila.
   * {@code ManyToOne} smješta strani ključ vlasnika u retku vozila; servisni upiti koriste tu vezu
   * za ograničavanje podataka na prijavljenog korisnika.
   */
  @ManyToOne
  private AppUser owner;

  /**
   * Kataloška varijanta kojoj konkretno vozilo pripada.
   *
   * <p>Svako vozilo referencira jednu varijantu, a jedna varijanta može opisivati više vozila.
   * Strani ključ varijante nalazi se na strani vozila; varijanta opisuje tehničku konfiguraciju,
   * a ne vlasnikov pojedinačni primjerak.
   */
  @ManyToOne
  private VehicleVariant variant;

  /** Godina proizvodnje konkretnog primjerka, provjerena prema rasponu njegove varijante. */
  private int productionYear;

  /** Zadnja poznata kilometraža koja se koristi u izračunima održavanja. */
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

  /** @return identifikator vozila dodijeljen u bazi */
  public Integer getId() {
    return id;
  }

  /** @return korisnik kojem vozilo pripada */
  public AppUser getOwner() {
    return owner;
  }

  /** @return kataloška varijanta ovog vozila */
  public VehicleVariant getVariant() {
    return variant;
  }

  /** @return godina proizvodnje konkretnog vozila */
  public int getProductionYear() {
    return productionYear;
  }

  /** @return trenutačno evidentirana kilometraža u kilometrima */
  public int getCurrentMileage() {
    return currentMileage;
  }
}
