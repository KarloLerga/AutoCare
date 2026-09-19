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
import java.util.Objects;

/** Servis i njegove stavke čine jednu cjelinu za spremanje. */
@Entity
public class ServiceRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Vehicle vehicle;

  private LocalDate serviceDate;
  private int mileage;
  private String note;

  @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL)
  private List<ServiceItem> items = new ArrayList<>();

  protected ServiceRecord() {}

  public ServiceRecord(Vehicle vehicle, LocalDate serviceDate, int mileage, String note) {
    this.vehicle = Objects.requireNonNull(vehicle);
    this.serviceDate = Objects.requireNonNull(serviceDate);
    this.mileage = Checks.mileage(mileage);
    this.note = Checks.optional(note, 2000, "Napomena");
  }

  public void addItem(WorkDefinition work, BigDecimal actualPrice) {
    Objects.requireNonNull(work);
    for (ServiceItem serviceItem : items) {
      if (Objects.equals(serviceItem.getWork().getCode(), work.getCode())) {
        throw new IllegalArgumentException("Rad je već dodan u servis.");
      }
    }
    items.add(new ServiceItem(this, work, actualPrice));
  }

  public CostSummary total() {
    List<BigDecimal> prices = new ArrayList<>();
    for (ServiceItem serviceItem : items) {
      prices.add(serviceItem.getActualPrice());
    }
    return CostSummary.of(prices);
  }

  public Long getId() {
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
