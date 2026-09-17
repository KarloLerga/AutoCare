package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Identitet kataloske varijante vozila, bez podataka o korisnikovim servisima. */
@Entity
public class VehicleVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;
  private String make;
  private String model;
  private String generation;
  private String engineLabel;
  private String bodyType;
  private String fuelType;
  private Integer powerHp;
  private String transmission;
  private int yearFrom;
  private Integer yearTo;
  private String imagePath;

  protected VehicleVariant() {}

  public VehicleVariant(
      String code,
      String make,
      String model,
      String generation,
      String engineLabel,
      int yearFrom,
      Integer yearTo,
      String fuelType) {
    this.code = Checks.text(code, 80, "Kod");
    this.make = Checks.text(make, 100, "Marka");
    this.model = Checks.text(model, 150, "Model");
    this.generation = Checks.text(generation, 200, "Generacija");
    this.engineLabel = Checks.text(engineLabel, 240, "Motor");
    if (yearFrom < 1886 || yearFrom > 2100 || (yearTo != null && yearTo < yearFrom)) {
      throw new IllegalArgumentException("Nevaljan raspon godina.");
    }
    this.yearFrom = yearFrom;
    this.yearTo = yearTo;
    this.fuelType = Checks.optional(fuelType, 80, "Gorivo");
  }

  public VehicleVariant(
      String code,
      String make,
      String model,
      String generation,
      String engineLabel,
      int yearFrom,
      Integer yearTo,
      String bodyType,
      String fuelType,
      Integer powerHp,
      String transmission,
      String imagePath) {
    this(code, make, model, generation, engineLabel, yearFrom, yearTo, fuelType);
    this.bodyType = Checks.optional(bodyType, 100, "Karoserija");
    if (powerHp != null && powerHp <= 0) {
      throw new IllegalArgumentException("Snaga mora biti pozitivna.");
    }
    if (imagePath != null && (!imagePath.startsWith("/images/") || imagePath.contains(".."))) {
      throw new IllegalArgumentException("Slika mora biti lokalni /images/ resurs.");
    }
    this.powerHp = powerHp;
    this.transmission = Checks.optional(transmission, 120, "Mjenjac");
    this.imagePath = imagePath;
  }

  public boolean covers(int year) {
    return year >= yearFrom && (yearTo == null || year <= yearTo);
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public String getGeneration() {
    return generation;
  }

  public String getEngineLabel() {
    return engineLabel;
  }

  public String getBodyType() {
    return bodyType;
  }

  public String getFuelType() {
    return fuelType;
  }

  public Integer getPowerHp() {
    return powerHp;
  }

  public String getTransmission() {
    return transmission;
  }

  public int getYearFrom() {
    return yearFrom;
  }

  public Integer getYearTo() {
    return yearTo;
  }

  public String getImagePath() {
    return imagePath;
  }
}
