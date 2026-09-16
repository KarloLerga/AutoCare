package hr.unizd.autocare.repository;
import hr.unizd.autocare.domain.*;
import java.util.*;
/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface VehicleRepository {
    Vehicle requireOwned(long owner, long id);
    List<Vehicle> list(long owner);
    void add(Vehicle vehicle);
    void delete(Vehicle vehicle);
    boolean hasHistory(long vehicleId);
}
