package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/**
 * Persistentna definicija standardnog rada iz kataloga.
 *
 * <p>Rad ima vrstu, korisničku kategoriju, opcionalni kilometarski i vremenski interval te
 * informativni raspon cijene. Raspon cijene nije stvarno plaćeni iznos korisnikova servisa.
 */
@Entity
public class WorkDefinition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;

  @Enumerated(EnumType.STRING)
  private WorkCategory category;

  @Enumerated(EnumType.STRING)
  private CatalogCategory catalogCategory;

  private Integer intervalKm;
  private Integer intervalMonths;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;

  /** Konstruktor bez argumenata potreban JPA provideru. */
  protected WorkDefinition() {}

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public WorkCategory getCategory() {
    return category;
  }

  public CatalogCategory getCatalogCategory() {
    return catalogCategory;
  }

  public Integer getIntervalKm() {
    return intervalKm;
  }

  public Integer getIntervalMonths() {
    return intervalMonths;
  }

  public BigDecimal getMinPrice() {
    return minPrice;
  }

  public BigDecimal getMaxPrice() {
    return maxPrice;
  }

  @Override
  public String toString() {
    return name;
  }
}
