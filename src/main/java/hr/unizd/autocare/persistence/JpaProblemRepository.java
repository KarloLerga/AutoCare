package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaProblemRepository implements ProblemRepository {
  private final EntityManager em;

  public JpaProblemRepository(EntityManager em) {
    this.em = em;
  }

  public void add(Problem p) {
    em.persist(p);
  }

  public Problem requireOwned(long owner, long id) {
    return em.createQuery(
            "select p from Problem p where p.id=:id and p.vehicle.owner.id=:o", Problem.class)
        .setParameter("id", id)
        .setParameter("o", owner)
        .getResultStream()
        .findFirst()
        .orElseThrow(
            () -> new AppException(AppException.Kind.NOT_FOUND, "Problem nije pronadjen."));
  }

  public List<Problem> list(long owner, long vehicle, ProblemStatus status) {
    return em.createQuery(
            "select p from Problem p left join fetch p.suggestedRepair left join fetch"
                + " p.resolvedByService where p.vehicle.id=:v and p.vehicle.owner.id=:o and"
                + " p.status=:s order by p.createdAt desc,p.id desc",
            Problem.class)
        .setParameter("v", vehicle)
        .setParameter("o", owner)
        .setParameter("s", status)
        .getResultList();
  }

  public Optional<Problem> byRequest(long owner, String key) {
    return em.createQuery(
            "select p from Problem p where p.requestKey=:k and p.vehicle.owner.id=:o",
            Problem.class)
        .setParameter("k", key)
        .setParameter("o", owner)
        .getResultStream()
        .findFirst();
  }

  public List<String> resolvedDescriptions(long owner, long service) {
    return em.createQuery(
            "select p.description from Problem p where p.resolvedByService.id=:s and"
                + " p.vehicle.owner.id=:o order by p.id",
            String.class)
        .setParameter("s", service)
        .setParameter("o", owner)
        .getResultList();
  }

  public long openCount(long owner, long vehicle) {
    return em.createQuery(
            "select count(p) from Problem p where p.vehicle.owner.id=:o and p.vehicle.id=:v and"
                + " p.status=:s",
            Long.class)
        .setParameter("o", owner)
        .setParameter("v", vehicle)
        .setParameter("s", ProblemStatus.OPEN)
        .getSingleResult();
  }

  public void deleteForVehicle(long vehicle) {
    em.createQuery("delete from Problem p where p.vehicle.id=:v")
        .setParameter("v", vehicle)
        .executeUpdate();
  }
}
