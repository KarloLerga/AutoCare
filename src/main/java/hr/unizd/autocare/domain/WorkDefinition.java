package hr.unizd.autocare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.Objects;

/** Vrsta zahvata i eventualna informativna zadana procjena. */
@Entity
public class WorkDefinition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 80)
  private String code;

  @Column(nullable = false, length = 160)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private WorkCategory category;

  @Column(precision = 9, scale = 2)
  private BigDecimal defaultEstimatedPrice;

  @Column(length = 1000)
  private String estimateNote;

  protected WorkDefinition() {}

  public WorkDefinition(
      String code,
      String name,
      WorkCategory category,
      BigDecimal defaultEstimatedPrice,
      String estimateNote) {
    this.code = Checks.text(code, 80, "Kod");
    this.name = Checks.text(name, 160, "Rad");
    this.category = Objects.requireNonNull(category);
    this.defaultEstimatedPrice = Checks.money(defaultEstimatedPrice, true);
    this.estimateNote = Checks.optional(estimateNote, 1000, "Izvor procjene");

    if (defaultEstimatedPrice != null && this.estimateNote == null) {
      throw new IllegalArgumentException("Procjena zahtijeva opis izvora ili DEMO oznaku.");
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

  public BigDecimal getDefaultEstimatedPrice() {
    return defaultEstimatedPrice;
  }

  public String getEstimateNote() {
    return estimateNote;
  }
}
