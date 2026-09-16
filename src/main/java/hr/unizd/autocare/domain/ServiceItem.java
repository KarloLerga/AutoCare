package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Objects;

/** Jedan izvrseni zahvat; actualPrice je stvarno placeno, a ne procjena. */
@Entity
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_service_work",
            columnNames = {"serviceRecord_id", "work_id"}))
public class ServiceItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private ServiceRecord serviceRecord;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private WorkDefinition work;

  @Column(precision = 9, scale = 2)
  private BigDecimal actualPrice;

  protected ServiceItem() {}

  ServiceItem(ServiceRecord serviceRecord, WorkDefinition work, BigDecimal actualPrice) {
    this.serviceRecord = Objects.requireNonNull(serviceRecord);
    this.work = Objects.requireNonNull(work);
    this.actualPrice = Checks.money(actualPrice, true);
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
