package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Vehicle;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Repository za JPA pristup konkretnim korisničkim vozilima.
 *
 * <p>Upiti ovdje primjenjuju identifikator vlasnika kada dohvaćaju korisnikove podatke; poslovni
 * tok i transakciju koordinira pozivajući Service.
 */
public class VehicleRepository {
  /** EntityManager trenutačne Service operacije kroz koji se izvode upiti i persist pozivi. */
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
   * Dohvaća najviše jedno vozilo samo ako pripada zadanom korisniku.
   *
   * <p>Ova metoda centralizira provjeru vlasništva i sprječava da viši slojevi slučajno rade s
   * vozilom drugog korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return vozilo ili {@code null} ako zapis ne postoji ili vozilo ne pripada korisniku
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
   * Dohvaća sva vozila jednog korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @return vozila korisnika sortirana po identifikatoru; prazna lista ako nema vozila
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
   * Predaje novo vozilo persistence kontekstu trenutačnog EntityManagera.
   *
   * <p>Transakciju otvara Service jer dodavanje vozila može uključivati i promjenu aktivnog vozila.
   *
   * @param vehicle vozilo koje treba spremiti
   */
  public void add(Vehicle vehicle) {
    entityManager.persist(vehicle);
  }
}
