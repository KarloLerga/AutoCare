package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

/** Podaci za četiri kartice dashboarda aktivnog vozila. */
public final class DashboardService {
  private final EntityManagerFactory entityManagerFactory;

  public DashboardService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public Dashboard get(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      CatalogRepository catalogRepository = new CatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new ServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new ProblemRepository(entityManager);

      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      List<MaintenanceRow> maintenance =
          MaintenanceService.calculate(
              catalogRepository,
              serviceRecordRepository,
              ownerId,
              vehicle);
      MaintenanceRow next = null;
      for (MaintenanceRow row : maintenance) {
        if (next == null || comesBefore(row, next)) {
          next = row;
        }
      }
      return new Dashboard(
          Mapping.vehicle(vehicle, vehicleId),
          serviceRecordRepository.total(ownerId, vehicleId),
          problemRepository.openCount(ownerId, vehicleId),
          next);
    } finally {
      entityManager.close();
    }
  }

  private static boolean comesBefore(MaintenanceRow first, MaintenanceRow second) {
    if (first.getRemainingRatio() != second.getRemainingRatio()) {
      return first.getRemainingRatio() < second.getRemainingRatio();
    }
    return first.getName().compareTo(second.getName()) < 0;
  }
}
