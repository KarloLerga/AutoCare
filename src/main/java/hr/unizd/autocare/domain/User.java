package hr.unizd.autocare.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
/** Racun i trajno odabrano vlastito vozilo. */
@Entity
@Table(name="app_user")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Version @Column(nullable=false)
    private long version;
    @Column(nullable=false, length=100)
    private String name;
    @Column(nullable=false, unique=true, length=254)
    private String email;
    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="active_vehicle_id")
    private Vehicle activeVehicle;
    protected User() {
    }
    public User(String name, String email, String hash) {
        this.name=Checks.text(name, 100, "Ime");
        this.email=Checks.email(email);
        this.passwordHash=Objects.requireNonNull(hash);
    }
    public void activate(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);
        if(vehicle.getOwner()!=this && (id==null || !id.equals(vehicle.getOwner().getId()))) throw new IllegalArgumentException("Vozilo nije vase.");
        activeVehicle=vehicle;
    }
    public void changeProfile(String name, String email) {
        this.name=Checks.text(name, 100, "Ime");
        this.email=Checks.email(email);
    }
    public void changePasswordHash(String hash) {
        passwordHash=Objects.requireNonNull(hash);
    }
    public Long getId() {
        return id;
    }
    public long getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public Vehicle getActiveVehicle() {
        return activeVehicle;
    }
}
