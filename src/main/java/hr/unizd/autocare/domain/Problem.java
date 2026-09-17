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

/** Ručno uneseni problem vozila i konkretna informativna procjena popravka. */
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

  private BigDecimal estimatedCost;

  @ManyToOne
  private ServiceRecord resolvedByService;

  protected Problem() {}

  public Problem(
      Vehicle vehicle,
      String description,
      LocalDateTime createdAt,
      WorkDefinition suggestedRepair,
      BigDecimal estimatedCost) {
    this.vehicle = Objects.requireNonNull(vehicle);
    this.description = Checks.text(description, 2000, "Opis problema");
    this.createdAt = Objects.requireNonNull(createdAt);
    this.status = ProblemStatus.OPEN;
    if (suggestedRepair != null && suggestedRepair.getCategory() != WorkCategory.REPAIR) {
      throw new IllegalArgumentException("Odabrani rad mora biti popravak.");
    }
    if (suggestedRepair == null && estimatedCost != null) {
      throw new IllegalArgumentException("Procjena pripada odabranom popravku.");
    }
    this.suggestedRepair = suggestedRepair;
    this.estimatedCost = Checks.money(estimatedCost, true);
  }

  public void resolve(ServiceRecord serviceRecord) {
    if (status != ProblemStatus.OPEN) {
      throw new IllegalArgumentException("Problem je već riješen.");
    }
    if (!vehicle.getId().equals(serviceRecord.getVehicle().getId())) {
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

  public BigDecimal getEstimatedCost() {
    return estimatedCost;
  }

  public ServiceRecord getResolvedByService() {
    return resolvedByService;
  }
}
