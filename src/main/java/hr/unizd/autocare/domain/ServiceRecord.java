package hr.unizd.autocare.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Servis i njegove stavke cine jednu cjelinu za spremanje. */
@Entity
public class ServiceRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Vehicle vehicle;

  @Column(nullable = false)
  private LocalDate serviceDate;

  @Column(nullable = false)
  private int mileage;

  @Column(length = 2000)
  private String note;

  @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
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

    for (ServiceItem item : items) {
      if (Objects.equals(item.getWork().getCode(), work.getCode())) {
        throw new IllegalArgumentException("Rad je vec dodan u servis.");
      }
    }

    items.add(new ServiceItem(this, work, actualPrice));
  }

  public CostSummary total() {
    List<BigDecimal> prices = new ArrayList<>();

    for (ServiceItem item : items) {
      prices.add(item.getActualPrice());
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
    return Collections.unmodifiableList(items);
  }
}
