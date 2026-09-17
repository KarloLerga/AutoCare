package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.ProblemEstimate;
import hr.unizd.autocare.model.Data.ProblemRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Ručni unos problema i procjena odabranog konkretnog popravka. */
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
      for (Problem problem :
          new JpaProblemRepository(entityManager).list(ownerId, vehicleId)) {
        rows.add(Mapping.problem(problem));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public ProblemEstimate estimate(long ownerId, long vehicleId, long repairWorkId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      VehicleWorkRule rule =
          new JpaCatalogRepository(entityManager)
              .findRule(vehicle.getVariant().getId(), repairWorkId);
      requireRepair(rule);
      return new ProblemEstimate(
          rule.getWork().getId(), rule.getWork().getName(), rule.getEstimatedPrice());
    } finally {
      entityManager.close();
    }
  }

  public long create(long ownerId, long vehicleId, String description, long repairWorkId) {
    String cleanDescription = Checks.text(description, 2000, "Opis problema");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      VehicleWorkRule rule =
          catalogRepository.findRule(vehicle.getVariant().getId(), repairWorkId);
      requireRepair(rule);
      Problem problem =
          new Problem(
              vehicle,
              cleanDescription,
              LocalDateTime.now(),
              rule.getWork(),
              rule.getEstimatedPrice());
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

  private static void requireRepair(VehicleWorkRule rule) {
    if (rule == null || rule.getWork().getCategory() != WorkCategory.REPAIR) {
      throw new AppException("Odabrani popravak nije dostupan za ovo vozilo.");
    }
  }
}
