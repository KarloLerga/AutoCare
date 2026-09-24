package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Problem;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje problema vozila. */
public final class ProblemRepository {
  private final EntityManager entityManager;

  public ProblemRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void add(Problem problem) {
    entityManager.persist(problem);
  }

  public Problem findForOwner(int ownerId, int problemId) {
    List<Problem> problems =
        entityManager
            .createQuery(
                "select problem from Problem problem where problem.id=:problemId "
                    + "and problem.vehicle.owner.id=:ownerId",
                Problem.class)
            .setParameter("problemId", problemId)
            .setParameter("ownerId", ownerId)
            .setMaxResults(1)
            .getResultList();

    if (problems.isEmpty()) {
      return null;
    }
    return problems.get(0);
  }

  public List<Problem> list(int ownerId, int vehicleId) {
    return entityManager
        .createQuery(
            "select problem from Problem problem "
                + "where problem.vehicle.id=:vehicleId "
                + "and problem.vehicle.owner.id=:ownerId "
                + "order by problem.createdAt desc, problem.id desc",
            Problem.class)
        .setParameter("vehicleId", vehicleId)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public List<String> resolvedDescriptions(int ownerId, int serviceId) {
    return entityManager
        .createQuery(
            "select problem.description from Problem problem "
                + "where problem.resolvedByService.id=:serviceId "
                + "and problem.vehicle.owner.id=:ownerId order by problem.id",
            String.class)
        .setParameter("serviceId", serviceId)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public long openCount(int ownerId, int vehicleId) {
    return entityManager
        .createQuery(
            "select count(problem) from Problem problem "
                + "where problem.vehicle.owner.id=:ownerId "
                + "and problem.vehicle.id=:vehicleId "
                + "and problem.resolvedByService is null",
            Long.class)
        .setParameter("ownerId", ownerId)
        .setParameter("vehicleId", vehicleId)
        .getSingleResult();
  }
}
