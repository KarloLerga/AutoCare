package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/** Informativni raspon cijene zahvata za cjenovnu klasu vozila. */
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

  public WorkDefinition getWork() {
    return work;
  }

  public BigDecimal getMinPrice() {
    return minPrice;
  }

  public BigDecimal getMaxPrice() {
    return maxPrice;
  }
}
