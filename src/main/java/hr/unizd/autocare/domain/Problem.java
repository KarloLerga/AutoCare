package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Opis simptoma i snimka analize; rjesenje se povezuje sa stvarnim servisom. */
@Entity
@Table(indexes = @Index(name = "idx_problem_vehicle_status", columnList = "vehicle_id,status"))
public class Problem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(nullable = false)
  private long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Vehicle vehicle;

  @Column(nullable = false, unique = true, length = 36)
  private String requestKey;

  @Column(nullable = false, length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProblemStatus status;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  private WorkDefinition suggestedRepair;

  @Column(precision = 5, scale = 2)
  private BigDecimal matchPercent;

  @Column(precision = 9, scale = 2)
  private BigDecimal estimatedCost;

  @Column(length = 1000)
  private String estimateNote;

  @ManyToOne(fetch = FetchType.LAZY)
  private ServiceRecord resolvedByService;

  protected Problem() {}

  public Problem(
      Vehicle vehicle,
      String requestKey,
      String description,
      LocalDateTime createdAt,
      WorkDefinition suggestedRepair,
      BigDecimal matchPercent,
      BigDecimal estimatedCost,
      String estimateNote) {
    this.vehicle = Objects.requireNonNull(vehicle);
    this.requestKey = UUID.fromString(requestKey).toString();
    this.description = Checks.text(description, 2000, "Opis simptoma");
    this.createdAt = Objects.requireNonNull(createdAt);
    status = ProblemStatus.OPEN;

    if (suggestedRepair != null && suggestedRepair.getCategory() != WorkCategory.REPAIR) {
      throw new IllegalArgumentException("Kandidat mora biti popravak.");
    }

    boolean inconsistentResult = (suggestedRepair == null) != (matchPercent == null);
    boolean invalidPercent =
        matchPercent != null
            && (matchPercent.signum() < 0 || matchPercent.compareTo(new BigDecimal("100")) > 0);

    if (inconsistentResult || invalidPercent) {
      throw new IllegalArgumentException("Nevaljana snimka analize.");
    }

    this.suggestedRepair = suggestedRepair;
    this.matchPercent = matchPercent;
    this.estimatedCost = Checks.money(estimatedCost, true);
    this.estimateNote = Checks.optional(estimateNote, 1000, "Izvor procjene");
  }

  public void resolve(ServiceRecord serviceRecord) {
    Objects.requireNonNull(serviceRecord);

    if (status != ProblemStatus.OPEN) {
      throw new IllegalArgumentException("Problem je vec rijesen.");
    }

    Vehicle serviceVehicle = serviceRecord.getVehicle();
    boolean sameObject = vehicle == serviceVehicle;
    boolean sameId = vehicle.getId() != null && vehicle.getId().equals(serviceVehicle.getId());

    if (!sameObject && !sameId) {
      throw new IllegalArgumentException("Servis pripada drugom vozilu.");
    }

    resolvedByService = serviceRecord;
    status = ProblemStatus.RESOLVED;
  }

  public Long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public String getDescription() {
    return description;
  }

  public ProblemStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public WorkDefinition getSuggestedRepair() {
    return suggestedRepair;
  }

  public BigDecimal getMatchPercent() {
    return matchPercent;
  }

  public BigDecimal getEstimatedCost() {
    return estimatedCost;
  }

  public String getEstimateNote() {
    return estimateNote;
  }

  public ServiceRecord getResolvedByService() {
    return resolvedByService;
  }
}
