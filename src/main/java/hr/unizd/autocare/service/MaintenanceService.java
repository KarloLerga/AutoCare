package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Servisna povijest je izvor istine za praćene intervale održavanja. */
public final class MaintenanceService {
  private final EntityManagerFactory entityManagerFactory;

  public MaintenanceService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<MaintenanceRow> list(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      return calculate(
          new JpaCatalogRepository(entityManager),
          new JpaServiceRecordRepository(entityManager),
          ownerId,
          vehicle);
    } finally {
      entityManager.close();
    }
  }

  static List<MaintenanceRow> calculate(
      CatalogRepository catalogRepository,
      ServiceRecordRepository serviceRecordRepository,
      long ownerId,
      Vehicle vehicle) {
    Map<Long, ServiceItem> latestItems =
        latestItems(serviceRecordRepository, ownerId, vehicle.getId());
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.now();
    List<MaintenanceRow> rows = new ArrayList<>();

    for (WorkDefinition work : catalogRepository.works(WorkCategory.MAINTENANCE)) {
      ServiceItem last = latestItems.get(work.getId());
      if (last == null) {
        continue;
      }

      LocalDate lastDate = last.getServiceRecord().getServiceDate();
      Integer lastMileage = last.getServiceRecord().getMileage();
      LocalDate nextDate = nextDate(lastDate, work.getIntervalMonths());
      Integer nextMileage = nextMileage(lastMileage, work.getIntervalKm());
      MaintenanceStatus status =
          calculator.calculate(
              work.getIntervalKm(),
              work.getIntervalMonths(),
              lastDate,
              lastMileage,
              vehicle.getCurrentMileage(),
              today);

      rows.add(
          new MaintenanceRow(
              work.getName(),
              lastDate,
              lastMileage,
              nextDate,
              nextMileage,
              status));
    }
    return rows;
  }

  private static Map<Long, ServiceItem> latestItems(
      ServiceRecordRepository serviceRecordRepository, long ownerId, long vehicleId) {
    Map<Long, ServiceItem> latest = new HashMap<>();
    for (ServiceItem item : serviceRecordRepository.historyItems(ownerId, vehicleId)) {
      if (!latest.containsKey(item.getWork().getId())) {
        latest.put(item.getWork().getId(), item);
      }
    }
    return latest;
  }

  private static LocalDate nextDate(LocalDate lastDate, Integer months) {
    if (months == null) {
      return null;
    }
    return lastDate.plusMonths(months);
  }

  private static Integer nextMileage(Integer lastMileage, Integer intervalKm) {
    if (intervalKm == null) {
      return null;
    }
    return lastMileage + intervalKm;
  }
}
