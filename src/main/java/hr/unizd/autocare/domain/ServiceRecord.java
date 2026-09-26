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

/** Servis i njegove stavke čine jednu cjelinu za spremanje. */
@Entity
/** Servisni zapis vozila, uključujući izvršene radove i napomenu. */
public class ServiceRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  private Vehicle vehicle;

  private LocalDate serviceDate;
  private int mileage;
  private String note;

  @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.PERSIST)
  private List<ServiceItem> items = new ArrayList<>();

  protected ServiceRecord() {}

  /**
   * Stvara servisni zapis za vozilo.
   *
   * @param vehicle vozilo kojem servis pripada
   * @param serviceDate datum obavljenog servisa
   * @param mileage kilometraža na dan servisa
   * @param note neobavezna napomena
   * @throws IllegalArgumentException ako vozilo, datum ili kilometraža nisu valjani
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

  /** Dodaje izvršeni rad s iznosom koji je korisnik stvarno platio. */
  public void addItem(WorkDefinition work, BigDecimal actualPrice) {
    if (work == null) {
      throw new IllegalArgumentException("Rad je obavezan.");
    }

    items.add(new ServiceItem(this, work, actualPrice));
  }

  /** Vraća zbroj stvarnih cijena svih stavki servisa. */
  public BigDecimal total() {
    BigDecimal total = BigDecimal.ZERO;
    for (ServiceItem serviceItem : items) {
      total = total.add(serviceItem.getActualPrice());
    }
    return total;
  }

  public Integer getId() {
    return id;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public LocalDate getServiceDate() {
    return serviceDate;
  }

  public int getMileage() {
    return mileage;
  }

  public String getNote() {
    return note;
  }

  public List<ServiceItem> getItems() {
    return items;
  }
}
