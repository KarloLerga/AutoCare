package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;

/** Korisnikova bilješka o onome što primjećuje na vozilu. */
@Entity
public class Problem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Vehicle vehicle;

  private String description;

  @Enumerated(EnumType.STRING)
  private ProblemCategory category;

  @Enumerated(EnumType.STRING)
  private ProblemStatus status;

  private LocalDateTime createdAt;

  @ManyToOne
  private ServiceRecord resolvedByService;

  protected Problem() {}

  public Problem(
      Vehicle vehicle,
      String description,
      ProblemCategory category,
      LocalDateTime createdAt) {
    this.vehicle = Objects.requireNonNull(vehicle);
    this.description = Checks.text(description, 2000, "Bilješka");
    this.category = category == null ? ProblemCategory.OTHER : category;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.status = ProblemStatus.OPEN;
  }

  public void close() {
    if (status != ProblemStatus.OPEN) {
      throw new IllegalArgumentException("Bilješka je već zatvorena.");
    }
    status = ProblemStatus.RESOLVED;
  }

  public void resolve(ServiceRecord serviceRecord) {
    if (status != ProblemStatus.OPEN) {
      throw new IllegalArgumentException("Bilješka je već zatvorena.");
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

  public ProblemCategory getCategory() {
    return category;
  }

  public ProblemStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public ServiceRecord getResolvedByService() {
    return resolvedByService;
  }
}
