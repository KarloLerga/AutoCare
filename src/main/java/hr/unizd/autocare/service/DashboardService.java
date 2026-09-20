package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
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

  public Dashboard get(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      JpaCatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      JpaServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      JpaProblemRepository problemRepository = new JpaProblemRepository(entityManager);

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
