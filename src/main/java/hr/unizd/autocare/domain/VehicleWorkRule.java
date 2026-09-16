package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Potpuni interval primjenjiv na tocnu katalosku varijantu. */
@Entity
@Table(name="vehicle_work_rule", uniqueConstraints=@UniqueConstraint(name="uk_variant_work", columnNames= {
    "variant_id", "work_id"
}))
public class VehicleWorkRule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="variant_id", nullable=false)
    private VehicleVariant variant;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="work_id", nullable=false)
    private WorkDefinition work;
    @Column(name="interval_km")
    private Integer intervalKm;
    @Enumerated(EnumType.STRING) @Column(name="schedule_kind", nullable=false, length=24)
    private ScheduleKind scheduleKind=ScheduleKind.UNKNOWN;
    @Column(name="interval_months")
    private Integer intervalMonths;
    @Column(name="estimated_price", precision=9, scale=2)
    private BigDecimal estimatedPrice;
    @Column(name="interval_source", length=1000)
    private String intervalSource;
    @Column(name="estimate_note", length=1000)
    private String estimateNote;
    protected VehicleWorkRule() {
    }
    public VehicleWorkRule(VehicleVariant variant, WorkDefinition work, Integer km, Integer months, BigDecimal price, String intervalSource, String estimateNote) {
        this.variant=Objects.requireNonNull(variant);
        this.work=Objects.requireNonNull(work);
        if((km!=null && (km<1 || km>1_000_000)) || (months!=null && (months<1 || months>1200))) throw new IllegalArgumentException("Nevaljan interval.");
        if(work.getCategory()==WorkCategory.REPAIR && (km!=null || months!=null))throw new IllegalArgumentException("Popravak nema preventivni interval.");
        this.scheduleKind=km!=null||months!=null?ScheduleKind.FIXED:(work.getCategory()==WorkCategory.REPAIR?ScheduleKind.CONDITION_BASED:ScheduleKind.UNKNOWN);
        this.intervalKm=km;
        this.intervalMonths=months;
        this.estimatedPrice=Checks.money(price, true);
        this.intervalSource=Checks.optional(intervalSource, 1000, "Izvor intervala");
        this.estimateNote=Checks.optional(estimateNote, 1000, "Izvor cijene");
        if((km!=null || months!=null) && this.intervalSource==null)throw new IllegalArgumentException("Interval zahtijeva izvor ili DEMO oznaku.");
        if(price!=null && this.estimateNote==null)throw new IllegalArgumentException("Cijena zahtijeva izvor ili DEMO oznaku.");
    }
    /** Izricita revizija referentnih podataka; koristi je samo developerski importer. */
    public void revise(Integer km, Integer months, BigDecimal price, String source, String priceNote) {
        VehicleWorkRule value=new VehicleWorkRule(variant, work, km, months, price, source, priceNote);
        scheduleKind=value.scheduleKind;
        intervalKm=value.intervalKm;
        intervalMonths=value.intervalMonths;
        estimatedPrice=value.estimatedPrice;
        intervalSource=value.intervalSource;
        estimateNote=value.estimateNote;
    }
    public Long getId() {
        return id;
    }
    public VehicleVariant getVariant() {
        return variant;
    }
    public WorkDefinition getWork() {
        return work;
    }
    public ScheduleKind getScheduleKind() {
        if(scheduleKind==null || (scheduleKind==ScheduleKind.UNKNOWN && (intervalKm!=null || intervalMonths!=null)))
            return intervalKm!=null||intervalMonths!=null?ScheduleKind.FIXED:ScheduleKind.UNKNOWN;
        return scheduleKind;
    }
    /** Promjena nacina pracenja ne smije prikriti vec zadane numericne intervale. */
    public void defineScheduleKind(ScheduleKind kind) {
        java.util.Objects.requireNonNull(kind);
        if(kind==ScheduleKind.FIXED && intervalKm==null && intervalMonths==null)throw new IllegalArgumentException("Fiksni plan treba interval.");
        if(kind!=ScheduleKind.FIXED && (intervalKm!=null || intervalMonths!=null))throw new IllegalArgumentException("Nefiksni plan nema brojcani interval.");
        if(work.getCategory()==WorkCategory.REPAIR && kind==ScheduleKind.FIXED)throw new IllegalArgumentException("Popravak nema preventivni rok.");
        scheduleKind=kind;
    }
    public Integer getIntervalKm() {
        return intervalKm;
    }
    public Integer getIntervalMonths() {
        return intervalMonths;
    }
    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }
    public String getIntervalSource() {
        return intervalSource;
    }
    public String getEstimateNote() {
        return estimateNote;
    }
}
