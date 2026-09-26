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

  public ProblemService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<Problem> list(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new ProblemRepository(entityManager).list(ownerId, vehicleId);
    } finally {
      entityManager.close();
    }
  }

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
