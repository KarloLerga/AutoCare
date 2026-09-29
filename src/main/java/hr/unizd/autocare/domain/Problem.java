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
  /** Baza generira identifikator zapisa problema. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Vozilo na kojem je problem zabilježen.
   *
   * <p>Svaki problem pripada jednom vozilu, dok vozilo može imati više problema. Veza
   * {@code ManyToOne} sprema strani ključ vozila u retku problema, pa se povijest problema može
   * dohvatiti za konkretno vozilo i provjeriti kroz njegova vlasnika.
   */
  @ManyToOne
  private Vehicle vehicle;

  /** Opis simptoma koji je korisnik unio; sam opis ne predstavlja automatsku dijagnozu. */
  private String description;

  /** Kategorija koju je korisnik odabrao za lakše razvrstavanje; {@code STRING} sprema naziv enum vrijednosti. */
  @Enumerated(EnumType.STRING)
  private ProblemCategory category;

  /** Datum na koji je problem evidentiran. */
  private LocalDate createdAt;

  /**
   * Servisni zapis kojim je problem označen riješenim.
   *
   * <p>{@code ManyToOne} pohranjuje strani ključ servisa u retku problema; više problema može
   * biti povezano s istim servisom, a {@code null} znači da problem još nije zatvoren.
   */
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

  /** Dohvaća bazni identifikator kojim se problem povezuje s prikazom i servisom rješenja.
   *
   * @return identifikator problema dodijeljen u bazi
   */
  public Integer getId() {
    return id;
  }

  /** Dohvaća opis simptoma koji je korisnik evidentirao.
   *
   * @return korisnički uneseni opis problema
   */
  public String getDescription() {
    return description;
  }

  /** Dohvaća korisnikovu kategoriju za razvrstavanje problema.
   *
   * @return odabrana kategorija problema
   */
  public ProblemCategory getCategory() {
    return category;
  }

  /** Dohvaća datum kada je problem zabilježen.
   *
   * @return datum evidentiranja problema
   */
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
