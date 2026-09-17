package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Problem vozila i rezultat analize spremljen u trenutku prijave. */
@Entity
public class Problem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Vehicle vehicle;

  private String description;

  @Enumerated(EnumType.STRING)
  private ProblemStatus status;

  private LocalDateTime createdAt;

  @ManyToOne
  private WorkDefinition suggestedRepair;

  private BigDecimal matchPercent;
  private BigDecimal estimatedCost;
  private String estimateNote;

  @ManyToOne
  private ServiceRecord resolvedByService;

  protected Problem() {}

  public Problem(
      Vehicle vehicle,
      String description,
      LocalDateTime createdAt,
      WorkDefinition suggestedRepair,
      BigDecimal matchPercent,
      BigDecimal estimatedCost,
      String estimateNote) {
    this.vehicle = Objects.requireNonNull(vehicle);
    this.description = Checks.text(description, 2000, "Opis simptoma");
    this.createdAt = Objects.requireNonNull(createdAt);
    this.status = ProblemStatus.OPEN;

    if (suggestedRepair != null && suggestedRepair.getCategory() != WorkCategory.REPAIR) {
      throw new IllegalArgumentException("Predlozeni rad mora biti popravak.");
    }

    this.suggestedRepair = suggestedRepair;
    this.matchPercent = matchPercent;
    this.estimatedCost = Checks.money(estimatedCost, true);
    this.estimateNote = Checks.optional(estimateNote, 1000, "Izvor procjene");
  }

  public void resolve(ServiceRecord serviceRecord) {
    if (status != ProblemStatus.OPEN) {
      throw new IllegalArgumentException("Problem je vec rijesen.");
    }

    Vehicle serviceVehicle = serviceRecord.getVehicle();
    if (!vehicle.getId().equals(serviceVehicle.getId())) {
      throw new IllegalArgumentException("Servis pripada drugom vozilu.");
    }

    resolvedByService = serviceRecord;
    status = ProblemStatus.RESOLVED;
  }

  public Long getId() {
    return id;
  }

  public Vehicle getVehicle() {
    return vehicle;
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
