package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Simptom i snimka najboljeg prijedloga; rjesenje je servis. */
@Entity
@Table(name="problem", indexes=@Index(name="idx_problem_vehicle_status", columnList="vehicle_id,status"))
public class Problem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Version @Column(nullable=false)
    private long version;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="vehicle_id", nullable=false)
    private Vehicle vehicle;
    @Column(name="request_key", nullable=false, unique=true, length=36)
    private String requestKey;
    @Column(nullable=false, length=2000)
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private ProblemStatus status;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="suggested_repair_id")
    private WorkDefinition suggestedRepair;
    @Column(name="match_percent", precision=5, scale=2)
    private BigDecimal matchPercent;
    @Column(name="estimated_cost", precision=9, scale=2)
    private BigDecimal estimatedCost;
    @Column(name="estimate_note", length=1000)
    private String estimateNote;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="resolved_by_service_id")
    private ServiceRecord resolvedByService;
    protected Problem() {
    }
    public Problem(Vehicle vehicle, String key, String description, LocalDateTime created, WorkDefinition suggestion, BigDecimal score, BigDecimal price, String estimateNote) {
        this.vehicle=Objects.requireNonNull(vehicle);
        requestKey=UUID.fromString(key).toString();
        this.description=Checks.text(description, 2000, "Opis simptoma");
        createdAt=Objects.requireNonNull(created);
        status=ProblemStatus.OPEN;
        if(suggestion!=null && suggestion.getCategory()!=WorkCategory.REPAIR)throw new IllegalArgumentException("Kandidat mora biti popravak.");
        if((suggestion==null)!=(score==null) || (score!=null && (score.signum()<0 || score.compareTo(new BigDecimal("100"))>0)))throw new IllegalArgumentException("Nevaljana snimka analize.");
        suggestedRepair=suggestion;
        matchPercent=score;
        estimatedCost=Checks.money(price, true);
        this.estimateNote=Checks.optional(estimateNote, 1000, "Izvor procjene");
    }
    public void resolve(ServiceRecord record) {
        if(status!=ProblemStatus.OPEN)throw new IllegalArgumentException("Problem je vec rijesen.");
        if(vehicle!=record.getVehicle() && (vehicle.getId()==null || !vehicle.getId().equals(record.getVehicle().getId())))throw new IllegalArgumentException("Servis pripada drugom vozilu.");
        resolvedByService=record;
        status=ProblemStatus.RESOLVED;
    }
    public Long getId() {
        return id;
    }
    public long getVersion() {
        return version;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public String getRequestKey() {
        return requestKey;
    }
    public String getDescription() {
        return description;
    }
    public ProblemStatus getStatus() {
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public WorkDefinition getSuggestedRepair() {
        return suggestedRepair;
    }
    public BigDecimal getMatchPercent() {
        return matchPercent;
    }
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
    public String getEstimateNote() {
        return estimateNote;
    }
    public ServiceRecord getResolvedByService() {
        return resolvedByService;
    }
}
