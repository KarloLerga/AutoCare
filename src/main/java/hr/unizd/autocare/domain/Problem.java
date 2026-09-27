package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * Persistentni domenski objekt korisnički evidentiranog problema vozila.
 *
 * <p>Problem nema zaseban ručno postavljen status. Otvoren je dok nije povezan sa
 * ServiceRecordom kroz koji je riješen.
 */
@Entity
public class Problem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  private Vehicle vehicle;

  private String description;

  @Enumerated(EnumType.STRING)
  private ProblemCategory category;

  private LocalDate createdAt;

  @ManyToOne
  private ServiceRecord resolvedByService;

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected Problem() {}

  /**
   * Stvara novi otvoreni problem vozila.
   *
   * @param vehicle vozilo na koje se problem odnosi
   * @param description korisnički opis problema
   * @param category gruba kategorija problema
   * @param createdAt datum evidentiranja
   * @throws IllegalArgumentException ako vozilo, opis ili datum nisu valjani
   */
  public Problem(Vehicle vehicle, String description, ProblemCategory category, LocalDate createdAt) {
    if (vehicle == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("Datum problema je obavezan.");
    }

    this.vehicle = vehicle;
    this.description = Checks.text(description, 2000, "Opis problema");
    if (category == null) {
      this.category = ProblemCategory.OTHER;
    } else {
      this.category = category;
    }
    this.createdAt = createdAt;
  }

  /**
   * Označava problem riješenim povezivanjem sa servisom koji ga je riješio.
   *
   * @param serviceRecord servis istog vozila kojim je problem riješen
   * @throws IllegalArgumentException ako je problem već riješen, servis nije zadan ili pripada drugom vozilu
   */
  public void resolve(ServiceRecord serviceRecord) {
    if (resolvedByService != null) {
      throw new IllegalArgumentException("Problem je već zatvoren.");
    }
    if (serviceRecord == null) {
      throw new IllegalArgumentException("Servis je obavezan.");
    }
    if (!vehicle.getId().equals(serviceRecord.getVehicle().getId())) {
      throw new IllegalArgumentException("Servis pripada drugom vozilu.");
    }

    resolvedByService = serviceRecord;
  }

  public Integer getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public ProblemCategory getCategory() {
    return category;
  }

  public LocalDate getCreatedAt() {
    return createdAt;
  }

  /**
   * Provjerava postoji li servis povezan kao rješenje problema.
   *
   * @return {@code true} ako je problem riješen konkretnim servisom
   */
  public boolean isResolved() {
    return resolvedByService != null;
  }
}
