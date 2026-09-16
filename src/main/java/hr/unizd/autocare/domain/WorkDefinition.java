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

/** OpÄ‡a definicija odrÅ¾avanja ili popravka. */
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

    private Integer defaultIntervalKm;

    private Integer defaultIntervalMonths;

    @Column(precision = 9, scale = 2)
    private BigDecimal defaultEstimatedPrice;

    @Column(length = 1000)
    private String estimateNote;

    protected WorkDefinition() {
    }

    /**
     * Stari jednostavni konstruktor ostaje koristan za popravke i postojeÄ‡e testove.
     */
    public WorkDefinition(
            String code,
            String name,
            WorkCategory category,
            BigDecimal defaultEstimatedPrice,
            String estimateNote) {

        this(
                code,
                name,
                category,
                null,
                null,
                defaultEstimatedPrice,
                estimateNote);
    }

    public WorkDefinition(
            String code,
            String name,
            WorkCategory category,
            Integer defaultIntervalKm,
            Integer defaultIntervalMonths,
            BigDecimal defaultEstimatedPrice,
            String estimateNote) {

        this.code = Checks.text(code, 80, "Kod");
        this.name = Checks.text(name, 160, "Rad");
        this.category = Objects.requireNonNull(category);

        validateInterval(defaultIntervalKm, defaultIntervalMonths);

        if (category == WorkCategory.REPAIR
                && (defaultIntervalKm != null || defaultIntervalMonths != null)) {
            throw new IllegalArgumentException(
                    "Popravak nema preventivni interval.");
        }

        this.defaultIntervalKm = defaultIntervalKm;
        this.defaultIntervalMonths = defaultIntervalMonths;
        this.defaultEstimatedPrice =
                Checks.money(defaultEstimatedPrice, true);
        this.estimateNote =
                Checks.optional(estimateNote, 1000, "Izvor procjene");
    }

    private void validateInterval(
            Integer intervalKm,
            Integer intervalMonths) {

        if (intervalKm != null && intervalKm <= 0) {
            throw new IllegalArgumentException(
                    "Kilometarski interval mora biti pozitivan.");
        }

        if (intervalMonths != null && intervalMonths <= 0) {
            throw new IllegalArgumentException(
                    "Vremenski interval mora biti pozitivan.");
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

    public Integer getDefaultIntervalKm() {
        return defaultIntervalKm;
    }

    public Integer getDefaultIntervalMonths() {
        return defaultIntervalMonths;
    }

    public BigDecimal getDefaultEstimatedPrice() {
        return defaultEstimatedPrice;
    }

    public String getEstimateNote() {
        return estimateNote;
    }
}
