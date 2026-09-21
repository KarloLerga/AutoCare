package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

public final class JpaVehicleRepository implements VehicleRepository {
  private final EntityManager entityManager;

  public JpaVehicleRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Vehicle findForOwner(long ownerId, long vehicleId) {
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

  @Override
  public List<Vehicle> findAllForOwner(long ownerId) {
    return entityManager
        .createQuery(
            "select vehicle from Vehicle vehicle "
                + "where vehicle.owner.id=:ownerId order by vehicle.id",
            Vehicle.class)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  @Override
  public void add(Vehicle vehicle) {
    entityManager.persist(vehicle);
  }

  @Override
  public void delete(Vehicle vehicle) {
    entityManager.remove(vehicle);
  }
}
