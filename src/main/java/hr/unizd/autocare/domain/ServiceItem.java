package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Persistentna stavka jednog servisa koja povezuje ServiceRecord sa standardnim WorkDefinitionom.
 *
 * <p>Za razliku od informativnog raspona u katalogu, {@code actualPrice} predstavlja stvarno
 * plaćeni iznos konkretne izvedbe rada.
 */
@Entity
public class ServiceItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  private ServiceRecord serviceRecord;

  @ManyToOne
  private WorkDefinition work;

  private BigDecimal actualPrice;

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected ServiceItem() {}

  /**
   * Stvara stavku koja povezuje servis, izvedeni standardni rad i stvarnu cijenu.
   *
   * @param serviceRecord servis kojem stavka pripada
   * @param work izvedeni standardni rad
   * @param actualPrice stvarno plaćeni iznos
   * @throws IllegalArgumentException ako cijena nije zadana ili je negativna
   */
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
