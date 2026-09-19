package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Vehicle;
import java.util.List;

/** Dohvat i spremanje korisnikovih vozila. */
public interface VehicleRepository {
  Vehicle findForOwner(long ownerId, long vehicleId);

  List<Vehicle> findAllForOwner(long ownerId);

  void add(Vehicle vehicle);

  void delete(Vehicle vehicle);
}
