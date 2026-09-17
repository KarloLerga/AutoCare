package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

/** Podaci za dashboard aktivnog vozila. */
public final class DashboardService {
  private final EntityManagerFactory entityManagerFactory;

  public DashboardService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public Dashboard get(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }

      List<MaintenanceRow> maintenance =
          MaintenanceService.calculate(
              catalogRepository, serviceRecordRepository, ownerId, vehicle);
      int due = 0;
      int soon = 0;
      int noData = 0;
      for (MaintenanceRow maintenanceRow : maintenance) {
        if (maintenanceRow.getStatus() == MaintenanceStatus.DUE) {
          due++;
        } else if (maintenanceRow.getStatus() == MaintenanceStatus.SOON) {
          soon++;
        } else if (maintenanceRow.getStatus() == MaintenanceStatus.NO_DATA) {
          noData++;
        }
      }

      return new Dashboard(
          Mapping.vehicle(vehicle, vehicleId),
          serviceRecordRepository.total(ownerId, vehicleId),
          problemRepository.openCount(ownerId, vehicleId),
          due,
          soon,
          noData,
          maintenance.size());
    } finally {
      entityManager.close();
    }
  }
}
