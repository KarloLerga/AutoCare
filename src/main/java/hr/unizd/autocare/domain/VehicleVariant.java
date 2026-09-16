package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Neizmjenjivi katalog identiteta varijante, bez troskova servisa. */
@Entity
@Table(name="vehicle_variant", indexes= {
    @Index(name="idx_variant_picker", columnList="make,model,year_from")
})
public class VehicleVariant {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=80)
    private String code;
    @Column(nullable=false, length=100)
    private String make;
    @Column(nullable=false, length=150)
    private String model;
    @Column(nullable=false, length=200)
    private String generation;
    @Column(name="engine_label", nullable=false, length=240)
    private String engineLabel;
    @Column(name="body_type", length=100)
    private String bodyType;
    @Column(name="fuel_type", length=80)
    private String fuelType;
    @Column(name="power_hp")
    private Integer powerHp;
    @Column(length=120)
    private String transmission;
    @Column(name="year_from", nullable=false)
    private int yearFrom;
    @Column(name="year_to")
    private Integer yearTo;
    @Column(name="image_path", length=255)
    private String imagePath;
    protected VehicleVariant() {
    }
    public VehicleVariant(String code, String make, String model, String generation, String engineLabel, int from, Integer to, String fuel) {
        this.code=Checks.text(code, 80, "Kod");
        this.make=Checks.text(make, 100, "Marka");
        this.model=Checks.text(model, 150, "Model");
        this.generation=Checks.text(generation, 200, "Generacija");
        this.engineLabel=Checks.text(engineLabel, 240, "Motor");
        if(from<1886 || from>2100 || (to!=null && to<from)) throw new IllegalArgumentException("Nevaljan raspon godina.");
        yearFrom=from;
        yearTo=to;
        fuelType=Checks.optional(fuel, 80, "Gorivo");
    }
    /** Constructor for the reviewed offline catalogue import; optional fields remain nullable. */
    public VehicleVariant(String code, String make, String model, String generation, String engineLabel, int from, Integer to, String body, String fuel, Integer power, String transmission, String imagePath) {
        this(code, make, model, generation, engineLabel, from, to, fuel);
        this.bodyType=Checks.optional(body, 100, "Karoserija");
        if(power!=null&&power<=0)throw new IllegalArgumentException("Snaga mora biti pozitivna.");
        powerHp=power;
        this.transmission=Checks.optional(transmission, 120, "Mjenjac");
        if(imagePath!=null&&(!imagePath.startsWith("/images/")||imagePath.contains("..")))throw new IllegalArgumentException("Slika mora biti lokalni /images/ resurs.");
        this.imagePath=imagePath;
    }
    public boolean covers(int year) {
        return year>=yearFrom && (yearTo==null || year<=yearTo);
    }
    public Long getId() {
        return id;
    }
    public String getCode() {
        return code;
    }
    public String getMake() {
        return make;
    }
    public String getModel() {
        return model;
    }
    public String getGeneration() {
        return generation;
    }
    public String getEngineLabel() {
        return engineLabel;
    }
    public String getBodyType() {
        return bodyType;
    }
    public String getFuelType() {
        return fuelType;
    }
    public Integer getPowerHp() {
        return powerHp;
    }
    public String getTransmission() {
        return transmission;
    }
    public int getYearFrom() {
        return yearFrom;
    }
    public Integer getYearTo() {
        return yearTo;
    }
    public String getImagePath() {
        return imagePath;
    }
}
