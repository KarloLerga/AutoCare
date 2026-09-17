package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.model.Data.Dashboard;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
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
      hr.unizd.autocare.domain.Vehicle vehicle =
          vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      List<MaintenanceRow> maintenance =
          MaintenanceService.calculate(
              new JpaCatalogRepository(entityManager),
              new JpaServiceRecordRepository(entityManager),
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
          new JpaServiceRecordRepository(entityManager).total(ownerId, vehicleId),
          new JpaProblemRepository(entityManager).openCount(ownerId, vehicleId),
          next);
    } finally {
      entityManager.close();
    }
  }

  private static boolean comesBefore(MaintenanceRow first, MaintenanceRow second) {
    int firstRank = statusRank(first);
    int secondRank = statusRank(second);
    if (firstRank != secondRank) {
      return firstRank < secondRank;
    }
    LocalDate firstDate = first.getNextDate();
    LocalDate secondDate = second.getNextDate();
    if (firstDate == null && secondDate != null) {
      return false;
    }
    if (firstDate != null && (secondDate == null || firstDate.isBefore(secondDate))) {
      return true;
    }
    if (firstDate != null && secondDate != null && firstDate.isAfter(secondDate)) {
      return false;
    }
    Integer firstMileage = first.getNextMileage();
    Integer secondMileage = second.getNextMileage();
    if (firstMileage != null && (secondMileage == null || firstMileage < secondMileage)) {
      return true;
    }
    if (firstMileage != null && secondMileage != null && firstMileage > secondMileage) {
      return false;
    }
    return first.getName().compareTo(second.getName()) < 0;
  }

  private static int statusRank(MaintenanceRow row) {
    if (row.getStatus() == MaintenanceStatus.DUE) {
      return 0;
    }
    if (row.getStatus() == MaintenanceStatus.SOON) {
      return 1;
    }
    return 2;
  }
}
