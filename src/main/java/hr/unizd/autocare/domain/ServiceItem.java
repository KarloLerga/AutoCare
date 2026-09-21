package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/** Jedan izvršeni zahvat i stvarno plaćena cijena. */
@Entity
public class ServiceItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private ServiceRecord serviceRecord;

  @ManyToOne
  private WorkDefinition work;

  private BigDecimal actualPrice;

  protected ServiceItem() {}

  ServiceItem(ServiceRecord serviceRecord, WorkDefinition work, BigDecimal actualPrice) {
    this.serviceRecord = serviceRecord;
    this.work = work;
    this.actualPrice = Checks.money(actualPrice);
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
