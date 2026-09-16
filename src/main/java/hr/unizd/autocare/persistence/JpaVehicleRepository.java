package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.repository.VehicleRepository;
import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaVehicleRepository implements VehicleRepository {
  private final EntityManager em;

  public JpaVehicleRepository(EntityManager em) {
    this.em = em;
  }

  public Vehicle requireOwned(long owner, long id) {
    return em.createQuery(
            "select v from Vehicle v join fetch v.variant where v.id=:id and v.owner.id=:owner",
            Vehicle.class)
        .setParameter("id", id)
        .setParameter("owner", owner)
        .getResultStream()
        .findFirst()
        .orElseThrow(
            () -> new AppException(AppException.Kind.NOT_FOUND, "Vozilo nije pronadjeno."));
  }

  public List<Vehicle> list(long owner) {
    return em.createQuery(
            "select v from Vehicle v join fetch v.variant where v.owner.id=:owner order by v.id",
            Vehicle.class)
        .setParameter("owner", owner)
        .getResultList();
  }

  public void add(Vehicle v) {
    em.persist(v);
  }

  public void delete(Vehicle v) {
    em.remove(v);
  }

  public boolean hasHistory(long vehicle) {
    long services =
        em.createQuery("select count(s) from ServiceRecord s where s.vehicle.id=:v", Long.class)
            .setParameter("v", vehicle)
            .getSingleResult();
    if (services > 0) {
      return true;
    }
    return em.createQuery("select count(p) from Problem p where p.vehicle.id=:v", Long.class)
            .setParameter("v", vehicle)
            .getSingleResult()
        > 0;
  }
}
