package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Agregat servisa. Stavke nastaju i spremaju se zajedno. */
@Entity
@Table(name="service_record", indexes=@Index(name="idx_service_vehicle_date", columnList="vehicle_id,service_date"))
public class ServiceRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="vehicle_id", nullable=false)
    private Vehicle vehicle;
    @Column(name="request_key", nullable=false, unique=true, length=36)
    private String requestKey;
    @Column(name="service_date", nullable=false)
    private LocalDate date;
    @Column(nullable=false)
    private int mileage;
    @Column(length=2000)
    private String note;
    @OneToMany(mappedBy="serviceRecord", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("id ASC")
    private List<ServiceItem> items;
    protected ServiceRecord() {
    }
    public ServiceRecord(Vehicle vehicle, String key, LocalDate date, int mileage, String note) {
        this.vehicle=Objects.requireNonNull(vehicle);
        requestKey=UUID.fromString(key).toString();
        this.date=Objects.requireNonNull(date);
        this.mileage=Checks.mileage(mileage);
        this.note=Checks.optional(note, 2000, "Napomena");
        items=new ArrayList<>();
    }
    public void addItem(WorkDefinition work, BigDecimal actualPrice) {
        Objects.requireNonNull(work);
        for(ServiceItem i:items)if(Objects.equals(i.getWork().getCode(), work.getCode()))throw new IllegalArgumentException("Rad je vec dodan u servis.");
        items.add(new ServiceItem(this, work, actualPrice));
    }
    public CostSummary total() {
        List<BigDecimal> prices=new ArrayList<>();
        for(ServiceItem item:items)prices.add(item.getActualPrice());
        return CostSummary.of(prices);
    }
    public Long getId() {
        return id;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public String getRequestKey() {
        return requestKey;
    }
    public LocalDate getDate() {
        return date;
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
