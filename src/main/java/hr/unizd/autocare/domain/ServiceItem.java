package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Kompozicijski dio servisa; procjena nije stvarna cijena. */
@Entity
@Table(name="service_item", uniqueConstraints=@UniqueConstraint(name="uk_service_work", columnNames= {
    "service_record_id", "work_id"
}))
public class ServiceItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="service_record_id", nullable=false)
    private ServiceRecord serviceRecord;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="work_id", nullable=false)
    private WorkDefinition work;
    @Column(name="actual_price", precision=9, scale=2)
    private BigDecimal actualPrice;
    protected ServiceItem() {
    }
    ServiceItem(ServiceRecord record, WorkDefinition work, BigDecimal price) {
        serviceRecord=Objects.requireNonNull(record);
        this.work=Objects.requireNonNull(work);
        actualPrice=Checks.money(price, true);
    }
    public Long getId() {
        return id;
    }
    public ServiceRecord getServiceRecord() {
        return serviceRecord;
    }
    public WorkDefinition getWork() {
        return work;
    }
    public BigDecimal getActualPrice() {
        return actualPrice;
    }
}
