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

  /** @return identifikator kataloške varijante */
  public Integer getId() {
    return id;
  }

  /** @return marka vozila */
  public String getMake() {
    return make;
  }

  /** @return model vozila */
  public String getModel() {
    return model;
  }

  /** @return generacijska oznaka */
  public String getGeneration() {
    return generation;
  }

  /** @return oznaka motora */
  public String getEngineLabel() {
    return engineLabel;
  }

  /** @return vrsta goriva */
  public String getFuelType() {
    return fuelType;
  }

  /** @return snaga u konjskim snagama ili {@code null} ako nije poznata */
  public Integer getPowerHp() {
    return powerHp;
  }

  /** @return vrsta mjenjača */
  public String getTransmission() {
    return transmission;
  }

  /** @return prva godina raspona proizvodnje */
  public int getYearFrom() {
    return yearFrom;
  }

  /** @return zadnja godina raspona ili {@code null} za otvoreni raspon */
  public Integer getYearTo() {
    return yearTo;
  }

  /** @return sažeti naziv varijante za prikaz u izborima i drugim tekstualnim kontekstima */
  @Override
  public String toString() {
    return generation + " / " + engineLabel + " / " + fuelType;
  }
}
