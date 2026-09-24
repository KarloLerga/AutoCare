package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Vehicle;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje korisnikovih vozila. */
public final class VehicleRepository {
  private final EntityManager entityManager;

  public VehicleRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Vehicle findForOwner(int ownerId, int vehicleId) {
    List<Vehicle> vehicles =
        entityManager
            .createQuery(
                "select vehicle from Vehicle vehicle "
                    + "where vehicle.id=:vehicleId and vehicle.owner.id=:ownerId",
                Vehicle.class)
            .setParameter("vehicleId", vehicleId)
            .setParameter("ownerId", ownerId)
            .setMaxResults(1)
            .getResultList();

    if (vehicles.isEmpty()) {
      return null;
    }
    return vehicles.get(0);
  }

  public List<Vehicle> findAllForOwner(int ownerId) {
    return entityManager
        .createQuery(
            "select vehicle from Vehicle vehicle "
                + "where vehicle.owner.id=:ownerId order by vehicle.id",
            Vehicle.class)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public void add(Vehicle vehicle) {
    entityManager.persist(vehicle);
  }
}
