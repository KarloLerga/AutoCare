package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Vehicle;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje korisnikovih vozila. */
public class VehicleRepository {
  private final EntityManager entityManager;

  /**
   * Stvara repozitorij za EntityManager trenutačne operacije.
   *
   * @param entityManager EntityManager kojim upravlja pozivajući servis
   */
  public VehicleRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Vraća vozilo samo ako pripada navedenom korisniku; inače vraća {@code null}.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return vozilo vlasnika ili {@code null}
   */
  public Vehicle findForOwner(int ownerId, int vehicleId) {
    List<Vehicle> vehicles = entityManager
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

  /**
   * Vraća vozila korisnika poredana prema identifikatoru.
   *
   * @param ownerId primarni ključ vlasnika
   * @return vozila korisnika
   */
  public List<Vehicle> findAllForOwner(int ownerId) {
    return entityManager
        .createQuery(
            "select vehicle from Vehicle vehicle "
                + "where vehicle.owner.id=:ownerId order by vehicle.id",
            Vehicle.class)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  /**
   * Predaje novo vozilo persistence kontekstu.
   *
   * @param vehicle vozilo koje treba spremiti
   */
  public void add(Vehicle vehicle) {
    entityManager.persist(vehicle);
  }
}
