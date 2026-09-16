package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Objects;

/** Pravilo za tocnu varijantu vozila i jedan zahvat. */
@Entity
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_variant_work",
            columnNames = {"variant_id", "work_id"}))
public class VehicleWorkRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private VehicleVariant variant;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private WorkDefinition work;

  private Integer intervalKm;

  /*
   * Legacy persistence metadata kept for compatibility with existing seed data.
   * Maintenance calculation uses only the nullable kilometre/month intervals.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private ScheduleKind scheduleKind = ScheduleKind.UNKNOWN;

  private Integer intervalMonths;

  @Column(precision = 9, scale = 2)
  private BigDecimal estimatedPrice;

  @Column(length = 1000)
  private String intervalSource;

  @Column(length = 1000)
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

    if (hasInterval) {
      scheduleKind = ScheduleKind.FIXED;
    } else if (work.getCategory() == WorkCategory.REPAIR) {
      scheduleKind = ScheduleKind.CONDITION_BASED;
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

  public ScheduleKind getScheduleKind() {
    return scheduleKind;
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
