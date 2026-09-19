package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Objects;

/** Informativni raspon cijene jednog zahvata za širu cjenovnu klasu vozila. */
@Entity
public class WorkPriceRange {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private WorkDefinition work;

  @Enumerated(EnumType.STRING)
  private VehiclePriceClass priceClass;

  private BigDecimal minPrice;
  private BigDecimal maxPrice;

  protected WorkPriceRange() {}

  public WorkPriceRange(
      WorkDefinition work,
      VehiclePriceClass priceClass,
      BigDecimal minPrice,
      BigDecimal maxPrice) {
    this.work = Objects.requireNonNull(work);
    this.priceClass = Objects.requireNonNull(priceClass);
    this.minPrice = Checks.money(minPrice, false);
    this.maxPrice = Checks.money(maxPrice, false);
    if (this.maxPrice.compareTo(this.minPrice) < 0) {
      throw new IllegalArgumentException("Najveća cijena ne može biti manja od najmanje.");
    }
  }

  public Long getId() {
    return id;
  }

  public WorkDefinition getWork() {
    return work;
  }

  public VehiclePriceClass getPriceClass() {
    return priceClass;
  }

  public BigDecimal getMinPrice() {
    return minPrice;
  }

  public BigDecimal getMaxPrice() {
    return maxPrice;
  }
}
