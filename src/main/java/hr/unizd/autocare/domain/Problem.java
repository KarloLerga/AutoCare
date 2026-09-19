package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

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
    if (vehicle == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("Datum bilješke je obavezan.");
    }

    this.vehicle = vehicle;
    this.description = Checks.text(description, 2000, "Bilješka");
    if (category == null) {
      this.category = ProblemCategory.OTHER;
    } else {
      this.category = category;
    }
    this.createdAt = createdAt;
    status = ProblemStatus.OPEN;
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
    if (serviceRecord == null) {
      throw new IllegalArgumentException("Servis je obavezan.");
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
}
