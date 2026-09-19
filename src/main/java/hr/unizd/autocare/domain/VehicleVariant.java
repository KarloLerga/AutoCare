package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Identitet kataloške varijante vozila, bez podataka o korisnikovim servisima. */
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

  @Enumerated(EnumType.STRING)
  private VehiclePriceClass priceClass;

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
    this.priceClass = VehiclePriceClass.STANDARD;
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
      String transmission) {
    this(code, make, model, generation, engineLabel, yearFrom, yearTo, fuelType);
    this.bodyType = Checks.optional(bodyType, 100, "Karoserija");

    if (powerHp != null && powerHp <= 0) {
      throw new IllegalArgumentException("Snaga mora biti pozitivna.");
    }

    this.powerHp = powerHp;
    this.transmission = Checks.optional(transmission, 120, "Mjenjač");
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

  public VehiclePriceClass getPriceClass() {
    return priceClass;
  }
}
