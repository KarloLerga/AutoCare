package hr.unizd.autocare.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistentni domenski zapis jednog stvarno evidentiranog servisa vozila.
 *
 * <p>ServiceRecord čuva datum, kilometražu, napomenu i kolekciju stvarnih ServiceItem stavki.
 * Zbroj stvarnih cijena stavki predstavlja stvarni trošak tog servisa.
 */
@Entity
public class ServiceRecord {
  /** Identifikator servisnog zapisa koji dodjeljuje baza. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Vozilo kojem je servis obavljen.
   *
   * <p>{@code ManyToOne} pohranjuje strani ključ vozila u retku servisa; jedno vozilo zato može
   * imati više zapisa u servisnoj povijesti.
   */
  @ManyToOne
  private Vehicle vehicle;

  /** Datum stvarnog obavljanja servisa. */
  private LocalDate serviceDate;

  /** Kilometraža vozila zabilježena pri ovom servisu. */
  private int mileage;

  /** Neobavezna napomena koju je korisnik unio uz servis. */
  private String note;

  /**
   * Stavke ovog servisa čine njegov sadržaj i stvarni trošak.
   *
   * <p>{@code mappedBy = "serviceRecord"} označava da je vlasnička strana veze polje
   * {@code ServiceItem.serviceRecord}, gdje se nalazi strani ključ. {@code CascadeType.PERSIST}
   * omogućuje da spremanje novog servisa spremi i njegove nove stavke.
   */
  @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.PERSIST)
  private List<ServiceItem> items = new ArrayList<>();

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected ServiceRecord() {}

  /**
   * Stvara novi servisni zapis prije dodavanja njegovih stavki.
   *
   * @param vehicle servisirano vozilo
   * @param serviceDate datum servisa
   * @param mileage kilometraža vozila pri servisu
   * @param note opcionalna napomena
   * @throws IllegalArgumentException ako vozilo ili datum nisu zadani ili kilometraža nije valjana
   */
  public ServiceRecord(Vehicle vehicle, LocalDate serviceDate, int mileage, String note) {
    if (vehicle == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }
    if (serviceDate == null) {
      throw new IllegalArgumentException("Datum servisa je obavezan.");
    }

    this.vehicle = vehicle;
    this.serviceDate = serviceDate;
    this.mileage = Checks.mileage(mileage);
    this.note = Checks.optional(note, 2000, "Napomena");
  }

  /**
   * Dodaje jednu stvarno izvedenu servisnu stavku ovom servisu.
   *
   * <p>Nova stavka povezuje servis sa standardnim radom i sprema stvarno plaćenu cijenu.
   *
   * @param work izvedeni standardni rad
   * @param actualPrice stvarno plaćeni iznos za taj rad
   * @throws IllegalArgumentException ako rad nije zadan ili cijena nije valjana
   */
  public void addItem(WorkDefinition work, BigDecimal actualPrice) {
    if (work == null) {
      throw new IllegalArgumentException("Rad je obavezan.");
    }

    items.add(new ServiceItem(this, work, actualPrice));
  }

  /**
   * Računa ukupni stvarni trošak servisa zbrajanjem cijena svih stavki.
   *
   * @return ukupni stvarni trošak servisa
   */
  public BigDecimal total() {
    BigDecimal total = BigDecimal.ZERO;
    for (ServiceItem serviceItem : items) {
      total = total.add(serviceItem.getActualPrice());
    }
    return total;
  }

  /** @return identifikator servisnog zapisa dodijeljen u bazi */
  public Integer getId() {
    return id;
  }

  /** @return servisirano vozilo */
  public Vehicle getVehicle() {
    return vehicle;
  }

  /** @return datum obavljanja servisa */
  public LocalDate getServiceDate() {
    return serviceDate;
  }

  /** @return kilometraža vozila pri servisu */
  public int getMileage() {
    return mileage;
  }

  /** @return napomena servisa ili {@code null} ako nije unesena */
  public String getNote() {
    return note;
  }

  /**
   * Vraća kopiju stavki kako se agregat ne bi mijenjao izvan {@link #addItem(WorkDefinition,
   * BigDecimal)}.
   *
   * @return nova lista servisnih stavki
   */
  public List<ServiceItem> getItems() {
    return new ArrayList<>(items);
  }
}
