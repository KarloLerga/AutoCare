package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Kataloška varijanta vozila koju aplikacija samo čita iz baze. */
@Entity
public class VehicleVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String make;
  private String model;
  private String generation;
  private String engineLabel;
  private String fuelType;
  private Integer powerHp;
  private String transmission;
  private int yearFrom;
  private Integer yearTo;

  @Enumerated(EnumType.STRING)
  private VehiclePriceClass priceClass;

  protected VehicleVariant() {}

  public boolean covers(int year) {
    if (year < yearFrom) {
      return false;
    }
    if (yearTo != null && year > yearTo) {
      return false;
    }
    return true;
  }

  public Long getId() {
    return id;
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

  @Override
  public String toString() {
    return generation + " / " + engineLabel + " / " + fuelType;
  }
}
