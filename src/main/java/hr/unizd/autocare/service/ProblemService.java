package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;

/** Problemi koje vlasnik bilježi bez automatske dijagnostike. */
public class ProblemService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis za evidentiranje problema vozila.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public ProblemService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Vraća probleme odabranog vozila korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return problemi vozila
   */
  public List<Problem> list(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new ProblemRepository(entityManager).list(ownerId, vehicleId);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Sprema novi korisnički opis problema za vozilo u vlasništvu korisnika.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @param description opis problema
   * @param category kategorija problema
   * @throws IllegalArgumentException ako vozilo ne pripada korisniku ili opis nije valjan
   */
  public void create(int ownerId, int vehicleId, String description, ProblemCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new VehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      Problem problem = new Problem(vehicle, description, category, LocalDate.now());
      new ProblemRepository(entityManager).add(problem);
      transaction.commit();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }
}
