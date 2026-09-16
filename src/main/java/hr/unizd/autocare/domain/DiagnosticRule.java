package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Podatkovno pravilo za lokalnu Strategy analizu. */
@Entity
@Table(name="diagnostic_rule")
public class DiagnosticRule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=80)
    private String code;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="candidate_id", nullable=false)
    private WorkDefinition candidate;
    @Column(nullable=false, length=160)
    private String phrase;
    @Column(nullable=false)
    private int weight;
    @Column(nullable=false)
    private boolean active;
    protected DiagnosticRule() {
    }
    public DiagnosticRule(String code, WorkDefinition candidate, String phrase, int weight) {
        this.code=Checks.text(code, 80, "Kod");
        this.candidate=Objects.requireNonNull(candidate);
        if(candidate.getCategory()!=WorkCategory.REPAIR)throw new IllegalArgumentException("Kandidat mora biti popravak.");
        this.phrase=Checks.text(phrase, 160, "Fraza");
        if(weight<1 || weight>100)throw new IllegalArgumentException("Tezina mora biti 1 - 100.");
        this.weight=weight;
        active=true;
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
