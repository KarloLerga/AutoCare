package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Problemi koje vlasnik zapisuje bez automatske dijagnostike ili pogađanja kvara. */
public final class ProblemService {
  private final EntityManagerFactory entityManagerFactory;

  public ProblemService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<ProblemRow> list(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      if (vehicleRepository.findForOwner(ownerId, vehicleId) == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      List<ProblemRow> rows = new ArrayList<>();
      for (Problem problem : new JpaProblemRepository(entityManager).list(ownerId, vehicleId)) {
        rows.add(Mapping.problem(problem));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public void create(
      long ownerId,
      long vehicleId,
      String description,
      ProblemCategory category) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      Problem problem = new Problem(vehicle, description, category, LocalDateTime.now());
      new JpaProblemRepository(entityManager).add(problem);
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

  public void close(long ownerId, long problemId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Problem problem = new JpaProblemRepository(entityManager).findForOwner(ownerId, problemId);
      if (problem == null) {
      throw new IllegalArgumentException("Problem nije pronađen.");
      }
      problem.close();
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
