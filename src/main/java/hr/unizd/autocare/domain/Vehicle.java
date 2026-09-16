package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Konkretno vozilo korisnika; kilometraza nikada ne pada. */
@Entity
@Table(name="vehicle", indexes= {
    @Index(name="idx_vehicle_owner", columnList="owner_id")
})
public class Vehicle {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Version @Column(nullable=false)
    private long version;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="owner_id", nullable=false)
    private User owner;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="variant_id", nullable=false)
    private VehicleVariant variant;
    @Column(name="production_year", nullable=false)
    private int year;
    @Column(name="current_mileage", nullable=false)
    private int currentMileage;
    protected Vehicle() {
    }
    public Vehicle(User owner, VehicleVariant variant, int year, int mileage) {
        this.owner=Objects.requireNonNull(owner);
        changeIdentity(variant, year);
        currentMileage=Checks.mileage(mileage);
    }
    public void changeIdentity(VehicleVariant variant, int year) {
        Objects.requireNonNull(variant);
        if(!variant.covers(year))throw new IllegalArgumentException("Godina nije u rasponu varijante.");
        this.variant=variant;
        this.year=year;
    }
    public void updateMileage(int mileage) {
        Checks.mileage(mileage);
        if(mileage<currentMileage)throw new IllegalArgumentException("Trenutna kilometraza ne moze se smanjiti.");
        currentMileage=mileage;
    }
    public Long getId() {
        return id;
    }
    public long getVersion() {
        return version;
    }
    public User getOwner() {
        return owner;
    }
    public VehicleVariant getVariant() {
        return variant;
    }
    public int getYear() {
        return year;
    }
    public int getCurrentMileage() {
        return currentMileage;
    }
}
