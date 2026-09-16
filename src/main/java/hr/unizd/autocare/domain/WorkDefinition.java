package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Opis rada i eventualna oznacena zadana procjena. */
@Entity
@Table(name="work_definition")
public class WorkDefinition {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=80)
    private String code;
    @Column(nullable=false, length=160)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private WorkCategory category;
    @Column(name="default_estimated_price", precision=9, scale=2)
    private BigDecimal defaultEstimatedPrice;
    @Column(name="estimate_note", length=1000)
    private String estimateNote;
    protected WorkDefinition() {
    }
    public WorkDefinition(String code, String name, WorkCategory category, BigDecimal price, String estimateNote) {
        this.code=Checks.text(code, 80, "Kod");
        this.name=Checks.text(name, 160, "Rad");
        this.category=Objects.requireNonNull(category);
        this.defaultEstimatedPrice=Checks.money(price, true);
        this.estimateNote=Checks.optional(estimateNote, 1000, "Izvor procjene");
        if(price!=null && this.estimateNote==null)throw new IllegalArgumentException("Procjena zahtijeva opis izvora ili DEMO oznaku.");
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
