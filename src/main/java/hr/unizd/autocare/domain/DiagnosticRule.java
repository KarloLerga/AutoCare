package hr.unizd.autocare.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

/** Podatkovno pravilo za lokalnu analizu simptoma. */
@Entity
public class DiagnosticRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;

  @ManyToOne
  private WorkDefinition candidate;

  private String phrase;
  private int weight;
  private boolean active;

  protected DiagnosticRule() {}

  public DiagnosticRule(String code, WorkDefinition candidate, String phrase, int weight) {
    this.code = Checks.text(code, 80, "Kod");
    this.candidate = Objects.requireNonNull(candidate);
    this.phrase = Checks.text(phrase, 160, "Fraza");
    if (candidate.getCategory() != WorkCategory.REPAIR) {
      throw new IllegalArgumentException("Kandidat mora biti popravak.");
    }
    if (weight < 1 || weight > 100) {
      throw new IllegalArgumentException("Tezina mora biti 1 - 100.");
    }
    this.weight = weight;
    active = true;
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public WorkDefinition getCandidate() {
    return candidate;
  }

  public String getPhrase() {
    return phrase;
  }

  public int getWeight() {
    return weight;
  }

  public boolean getActive() {
    return active;
  }
}
