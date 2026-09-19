package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
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

/** Bilješke vlasnika vozila bez automatske dijagnostike ili pogađanja kvara. */
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
        throw new AppException("Vozilo nije pronađeno.");
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

  public long create(
      long ownerId,
      long vehicleId,
      String description,
      ProblemCategory category) {
    String cleanDescription = Checks.text(description, 2000, "Bilješka");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      Problem problem = new Problem(vehicle, cleanDescription, category, LocalDateTime.now());
      new JpaProblemRepository(entityManager).add(problem);
      transaction.commit();
      return problem.getId();
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
        throw new AppException("Bilješka nije pronađena.");
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
