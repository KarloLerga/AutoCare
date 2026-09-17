package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Objects;

/** Pravilo za tocnu varijantu vozila i jedan zahvat. */
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
  private String intervalSource;
  private String estimateNote;

  protected VehicleWorkRule() {}

  public VehicleWorkRule(
      VehicleVariant variant,
      WorkDefinition work,
      Integer intervalKm,
      Integer intervalMonths,
      BigDecimal estimatedPrice,
      String intervalSource,
      String estimateNote) {
    this.variant = Objects.requireNonNull(variant);
    this.work = Objects.requireNonNull(work);

    if (intervalKm != null && (intervalKm < 1 || intervalKm > 1_000_000)) {
      throw new IllegalArgumentException("Nevaljan kilometarski interval.");
    }
    if (intervalMonths != null && (intervalMonths < 1 || intervalMonths > 1200)) {
      throw new IllegalArgumentException("Nevaljan vremenski interval.");
    }

    boolean hasInterval = intervalKm != null || intervalMonths != null;
    if (work.getCategory() == WorkCategory.REPAIR && hasInterval) {
      throw new IllegalArgumentException("Popravak nema preventivni interval.");
    }

    this.intervalKm = intervalKm;
    this.intervalMonths = intervalMonths;
    this.estimatedPrice = Checks.money(estimatedPrice, true);
    this.intervalSource = Checks.optional(intervalSource, 1000, "Izvor intervala");
    this.estimateNote = Checks.optional(estimateNote, 1000, "Izvor cijene");

    if (hasInterval && this.intervalSource == null) {
      throw new IllegalArgumentException("Interval zahtijeva izvor ili DEMO oznaku.");
    }
    if (estimatedPrice != null && this.estimateNote == null) {
      throw new IllegalArgumentException("Cijena zahtijeva izvor ili DEMO oznaku.");
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

  public String getIntervalSource() {
    return intervalSource;
  }

  public String getEstimateNote() {
    return estimateNote;
  }
}
