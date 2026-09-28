package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Problem;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Repository za JPA pristup evidentiranim problemima i njihovim vezama sa servisima.
 *
 * <p>Upiti ograničavaju dohvat vlasnikom vozila kako bi se podaci čitali samo u korisnikovu
 * kontekstu. Transakcijsku koordinaciju obavlja Service.
 */
public class ProblemRepository {
  /** EntityManager koji pozivajući Service koristi za upite i spremanje problema. */
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
   * Dodaje novi problem u persistence context trenutačnog EntityManagera.
   *
   * <p>Spremanje u bazu potvrđuje transakcija koju pokreće Service.
   *
   * @param problem novi problem koji se dodaje u persistence context
   */
  public void add(Problem problem) {
    entityManager.persist(problem);
  }

  /**
   * Dohvaća problem samo ako pripada vozilu zadanog korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @param problemId identifikator problema
   * @return problem ili {@code null} ako nije pronađen u korisničkom kontekstu
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
   * Dohvaća probleme vozila od najnovije evidentiranog prema starijima.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return evidentirani problemi u redoslijedu upita; prazna lista ako ih nema
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
   * Dohvaća samo tekstualne opise problema koji su riješeni određenim servisom.
   *
   * @param ownerId identifikator vlasnika
   * @param serviceId identifikator servisa
   * @return opisi sortirani po identifikatoru problema; prazna lista ako ih servis nije riješio
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
   * Broji probleme vozila koji još nemaju povezan servis rješenja.
   *
   * <p>Agregatni JPQL upit vraća jednu skalaru vrijednost, uključujući nulu kada nema otvorenih
   * problema.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return broj otvorenih problema
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
