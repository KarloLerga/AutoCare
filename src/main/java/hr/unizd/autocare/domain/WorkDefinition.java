package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

/** Katalog konkretnih zahvata; cijena i interval pripadaju pravilu varijante. */
@Entity
public class WorkDefinition {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;
  private String name;

  @Enumerated(EnumType.STRING)
  private WorkCategory category;

  protected WorkDefinition() {}

  public WorkDefinition(String code, String name, WorkCategory category) {
    this.code = Checks.text(code, 80, "Kod");
    this.name = Checks.text(name, 160, "Rad");
    this.category = Objects.requireNonNull(category);
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
}
