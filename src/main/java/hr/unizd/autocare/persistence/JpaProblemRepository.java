package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.repository.ProblemRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA upiti za ručno unesene probleme. */
public final class JpaProblemRepository implements ProblemRepository {
  private final EntityManager entityManager;

  public JpaProblemRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void add(Problem problem) {
    entityManager.persist(problem);
  }

  @Override
  public Problem findForOwner(long ownerId, long problemId) {
    List<Problem> problems =
        entityManager
            .createQuery(
                "select problem from Problem problem "
                    + "left join fetch problem.resolvedByService where problem.id=:problemId "
                    + "and problem.vehicle.owner.id=:ownerId",
                Problem.class)
            .setParameter("problemId", problemId)
            .setParameter("ownerId", ownerId)
            .setMaxResults(1)
            .getResultList();
    return problems.isEmpty() ? null : problems.get(0);
  }

  @Override
  public List<Problem> list(long ownerId, long vehicleId) {
    return entityManager
        .createQuery(
            "select problem from Problem problem "
                + "where problem.vehicle.id=:vehicleId "
                + "and problem.vehicle.owner.id=:ownerId "
                + "order by case when problem.status=:openStatus then 0 else 1 end, "
                + "problem.createdAt desc,problem.id desc",
            Problem.class)
        .setParameter("vehicleId", vehicleId)
        .setParameter("ownerId", ownerId)
        .setParameter("openStatus", ProblemStatus.OPEN)
        .getResultList();
  }

  @Override
  public List<String> resolvedDescriptions(long ownerId, long serviceId) {
    return entityManager
        .createQuery(
            "select problem.description from Problem problem where "
                + "problem.resolvedByService.id=:serviceId and problem.vehicle.owner.id=:ownerId "
                + "order by problem.id",
            String.class)
        .setParameter("serviceId", serviceId)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  @Override
  public long openCount(long ownerId, long vehicleId) {
    return entityManager
        .createQuery(
            "select count(problem) from Problem problem where problem.vehicle.owner.id=:ownerId "
                + "and problem.vehicle.id=:vehicleId and problem.status=:status",
            Long.class)
        .setParameter("ownerId", ownerId)
        .setParameter("vehicleId", vehicleId)
        .setParameter("status", ProblemStatus.OPEN)
        .getSingleResult();
  }

  @Override
  public void deleteForVehicle(long vehicleId) {
    entityManager
        .createQuery("delete from Problem problem where problem.vehicle.id=:vehicleId")
        .setParameter("vehicleId", vehicleId)
        .executeUpdate();
  }
}
