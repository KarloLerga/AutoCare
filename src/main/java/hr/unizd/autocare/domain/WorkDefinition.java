package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Standardni zahvat iz kataloga i, za održavanje, njegov servisni interval. */
@Entity
public class WorkDefinition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Enumerated(EnumType.STRING)
  private WorkCategory category;

  @Enumerated(EnumType.STRING)
  private CatalogCategory catalogCategory;

  private Integer intervalKm;
  private Integer intervalMonths;

  protected WorkDefinition() {}

  public Long getId() {
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

  @Override
  public String toString() {
    return name;
  }
}
