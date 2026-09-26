package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Problem;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje problema vozila. */
public class ProblemRepository {
  private final EntityManager entityManager;

  /**
   * Stvara repozitorij za EntityManager trenutačne operacije.
   *
   * @param entityManager EntityManager kojim upravlja pozivajući servis
   */
  public ProblemRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Predaje novi problem persistence kontekstu.
   *
   * @param problem problem koji treba spremiti
   */
  public void add(Problem problem) {
    entityManager.persist(problem);
  }

  /**
   * Vraća problem samo ako pripada navedenom korisniku; inače vraća {@code null}.
   *
   * @param ownerId primarni ključ vlasnika
   * @param problemId primarni ključ problema
   * @return problem vlasnika ili {@code null}
   */
  public Problem findForOwner(int ownerId, int problemId) {
    List<Problem> problems = entityManager
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

  /**
   * Vraća probleme vozila poredane od najnovijeg prema najstarijem.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return evidentirani problemi
   */
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

  /**
   * Vraća opise problema koje je riješio zadani servis.
   *
   * @param ownerId primarni ključ vlasnika
   * @param serviceId primarni ključ servisa
   * @return opisi povezanih riješenih problema
   */
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

  /**
   * Broji neriješene probleme vozila.
   *
   * @param ownerId primarni ključ vlasnika
   * @param vehicleId primarni ključ vozila
   * @return broj neriješenih problema
   */
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
