package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

/** Standardni zahvat iz kataloga te, za održavanje, njegov servisni interval. */
@Entity
public class WorkDefinition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;
  private String name;

  @Enumerated(EnumType.STRING)
  private WorkCategory category;

  @Enumerated(EnumType.STRING)
  private CatalogCategory catalogCategory;

  private Integer intervalKm;
  private Integer intervalMonths;

  protected WorkDefinition() {}

  public WorkDefinition(
      String code,
      String name,
      WorkCategory category,
      CatalogCategory catalogCategory,
      Integer intervalKm,
      Integer intervalMonths) {
    this.code = Checks.text(code, 80, "Kod");
    this.name = Checks.text(name, 160, "Rad");
    this.category = Objects.requireNonNull(category);
    this.catalogCategory = Objects.requireNonNull(catalogCategory);
    validateInterval(category, intervalKm, intervalMonths);
    this.intervalKm = intervalKm;
    this.intervalMonths = intervalMonths;
  }

  private static void validateInterval(
      WorkCategory category, Integer intervalKm, Integer intervalMonths) {
    if (intervalKm != null && intervalKm <= 0) {
      throw new IllegalArgumentException("Kilometarski interval mora biti pozitivan.");
    }
    if (intervalMonths != null && intervalMonths <= 0) {
      throw new IllegalArgumentException("Vremenski interval mora biti pozitivan.");
    }
    if (category == WorkCategory.MAINTENANCE && intervalKm == null && intervalMonths == null) {
      throw new IllegalArgumentException("Održavanje mora imati kilometarski ili vremenski interval.");
    }
    if (category == WorkCategory.REPAIR && (intervalKm != null || intervalMonths != null)) {
      throw new IllegalArgumentException("Popravak nema preventivni interval.");
    }
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
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
}
