package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Persistentna kataloška definicija jedne varijante vozila.
 *
 * <p>Varijanta opisuje marku, model, generaciju, motor, gorivo, snagu, mjenjač i raspon godina
 * u kojima se ta varijanta nudila.
 */
@Entity
public class VehicleVariant {
  /** Identifikator kataloške varijante koji dodjeljuje baza. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /** Marka vozila kako je evidentirana u katalogu. */
  private String make;

  /** Model vozila unutar navedene marke. */
  private String model;

  /** Generacijska oznaka kojom se razlikuju izvedbe istog modela. */
  private String generation;

  /** Oznaka motora koja se prikazuje pri odabiru konkretne varijante. */
  private String engineLabel;

  /** Vrsta goriva kataloške varijante. */
  private String fuelType;

  /** Snaga motora u konjskim snagama ili {@code null} ako nije evidentirana. */
  private Integer powerHp;

  /** Vrsta mjenjača kataloške varijante. */
  private String transmission;

  /** Prva godina u kojoj je ova varijanta ponuđena. */
  private int yearFrom;

  /** Zadnja godina ponude ili {@code null} ako raspon nema navedenu završnu godinu. */
  private Integer yearTo;


  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected VehicleVariant() {}

  /**
   * Provjerava nalazi li se godina unutar raspona proizvodnje ove varijante.
   *
   * @param year godina koju treba provjeriti
   * @return {@code true} ako varijanta pokriva zadanu godinu
   */
  public boolean covers(int year) {
    if (year < yearFrom) {
      return false;
    }
    if (yearTo != null && year > yearTo) {
      return false;
    }
    return true;
  }

  /** Dohvaća bazni identifikator kojim vozila referenciraju ovu katalošku varijantu.
   *
   * @return identifikator kataloške varijante
   */
  public Integer getId() {
    return id;
  }

  /** Dohvaća marku navedenu u katalogu varijanti.
   *
   * @return marka vozila
   */
  public String getMake() {
    return make;
  }

  /** Dohvaća model unutar kataloške marke.
   *
   * @return model vozila
   */
  public String getModel() {
    return model;
  }

  /** Dohvaća oznaku generacije koja razlikuje izvedbe modela.
   *
   * @return generacijska oznaka
   */
  public String getGeneration() {
    return generation;
  }

  /** Dohvaća katalošku oznaku motora.
   *
   * @return oznaka motora
   */
  public String getEngineLabel() {
    return engineLabel;
  }

  /** Dohvaća vrstu goriva navedenu za ovu izvedbu.
   *
   * @return vrsta goriva
   */
  public String getFuelType() {
    return fuelType;
  }

  /** Dohvaća katalošku snagu motora kada je ona poznata.
   *
   * @return snaga u konjskim snagama ili {@code null} ako nije poznata
   */
  public Integer getPowerHp() {
    return powerHp;
  }

  /** Dohvaća vrstu mjenjača navedenu u katalogu.
   *
   * @return vrsta mjenjača
   */
  public String getTransmission() {
    return transmission;
  }

  /** Dohvaća prvu godinu pokrivenu ovom varijantom.
   *
   * @return prva godina raspona proizvodnje
   */
  public int getYearFrom() {
    return yearFrom;
  }

  /** Dohvaća zadnju godinu pokrivenu ovom varijantom, ako je raspon zatvoren.
   *
   * @return zadnja godina raspona ili {@code null} za otvoreni raspon
   */
  public Integer getYearTo() {
    return yearTo;
  }

  /** Sastavlja kratak opis varijante za JComboBox i druge tekstualne prikaze.
   *
   * @return sažeti naziv varijante za prikaz u izborima i drugim tekstualnim kontekstima
   */
  @Override
  public String toString() {
    return generation + " / " + engineLabel + " / " + fuelType;
  }
}
