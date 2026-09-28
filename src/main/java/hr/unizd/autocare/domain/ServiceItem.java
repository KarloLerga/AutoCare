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
  /** Identifikator servisne stavke koji dodjeljuje baza. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Servisni zapis kojem ova izvedena stavka pripada.
   *
   * <p>{@code ManyToOne} stavlja strani ključ servisa u tablicu stavki; servis može imati više
   * stavki, dok svaka stavka pripada jednom servisu.
   */
  @ManyToOne
  private ServiceRecord serviceRecord;

  /**
   * Jedna kataloška definicija rada izvedena u ovoj stavci.
   *
   * <p>Svaka stavka referencira jedan rad, a isti kataloški rad može se pojaviti u više servisnih
   * stavki; strani ključ definicije nalazi se na strani stavke.
   */
  @ManyToOne
  private WorkDefinition work;

  /** Iznos koji je korisnik stvarno platio za ovu izvedbu rada. */
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

  /** @return servisni zapis kojem stavka pripada */
  public ServiceRecord getServiceRecord() {
    return serviceRecord;
  }

  /** @return kataloška definicija izvedenog rada */
  public WorkDefinition getWork() {
    return work;
  }

  /** @return stvarno plaćena cijena stavke */
  public BigDecimal getActualPrice() {
    return actualPrice;
  }
}
