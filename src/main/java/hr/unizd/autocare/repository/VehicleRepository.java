package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Vehicle;
import java.util.List;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface VehicleRepository {
  Vehicle findForOwner(long ownerId, long vehicleId);

  List<Vehicle> findAllForOwner(long ownerId);

  void add(Vehicle vehicle);

  void delete(Vehicle vehicle);
}
