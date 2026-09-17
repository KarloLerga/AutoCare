package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Objects;

/** Jedino mjesto konkretne primjenjivosti, cijene i intervala za par varijanta/rad. */
@Entity
public class VehicleWorkRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private VehicleVariant variant;

  @ManyToOne
  private WorkDefinition work;

  private Integer intervalKm;
  private Integer intervalMonths;
  private BigDecimal estimatedPrice;

  protected VehicleWorkRule() {}

  public VehicleWorkRule(
      VehicleVariant variant,
      WorkDefinition work,
      Integer intervalKm,
      Integer intervalMonths,
      BigDecimal estimatedPrice) {
    this.variant = Objects.requireNonNull(variant);
    this.work = Objects.requireNonNull(work);
    validateInterval(intervalKm, intervalMonths);
    if (work.getCategory() == WorkCategory.REPAIR
        && (intervalKm != null || intervalMonths != null)) {
      throw new IllegalArgumentException("Popravak nema preventivni interval.");
    }
    if (work.getCategory() == WorkCategory.MAINTENANCE
        && intervalKm == null
        && intervalMonths == null) {
      throw new IllegalArgumentException("Održavanje mora imati kilometarski ili vremenski interval.");
    }
    if (estimatedPrice == null || estimatedPrice.signum() <= 0) {
      throw new IllegalArgumentException("Procijenjena cijena mora biti pozitivna.");
    }
    this.intervalKm = intervalKm;
    this.intervalMonths = intervalMonths;
    this.estimatedPrice = Checks.money(estimatedPrice, false);
  }

  private static void validateInterval(Integer intervalKm, Integer intervalMonths) {
    if (intervalKm != null && intervalKm <= 0) {
      throw new IllegalArgumentException("Kilometarski interval mora biti pozitivan.");
    }
    if (intervalMonths != null && intervalMonths <= 0) {
      throw new IllegalArgumentException("Vremenski interval mora biti pozitivan.");
    }
  }

  public Long getId() {
    return id;
  }

  public VehicleVariant getVariant() {
    return variant;
  }

  public WorkDefinition getWork() {
    return work;
  }

  public Integer getIntervalKm() {
    return intervalKm;
  }

  public Integer getIntervalMonths() {
    return intervalMonths;
  }

  public BigDecimal getEstimatedPrice() {
    return estimatedPrice;
  }
}
