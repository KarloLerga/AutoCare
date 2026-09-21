package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

/** Problemi koje vlasnik bilježi bez automatske dijagnostike. */
public final class ProblemService {
  private final EntityManagerFactory entityManagerFactory;

  public ProblemService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<Problem> list(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      if (vehicleRepository.findForOwner(ownerId, vehicleId) == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      return new ProblemRepository(entityManager).list(ownerId, vehicleId);
    } finally {
      entityManager.close();
    }
  }

  public void create(long ownerId, long vehicleId, String description, ProblemCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new VehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      Problem problem = new Problem(vehicle, description, category, LocalDateTime.now());
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
