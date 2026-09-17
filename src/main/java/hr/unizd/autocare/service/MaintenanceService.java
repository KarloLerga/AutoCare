package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.model.Data.MaintenanceEstimate;
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

/** Tracked održavanje računa se samo za konkretno pravilo koje postoji u povijesti. */
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
        throw new AppException("Vozilo nije pronađeno.");
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

  public MaintenanceEstimate estimate(long ownerId, long vehicleId, long workId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      VehicleWorkRule rule =
          catalogRepository.findRule(vehicle.getVariant().getId(), workId);
      if (rule == null || rule.getWork().getCategory() != WorkCategory.MAINTENANCE) {
        throw new AppException("Odabrano održavanje nije dostupno za ovo vozilo.");
      }
      ServiceItem last = latestItems(
              new JpaServiceRecordRepository(entityManager), ownerId, vehicleId)
          .get(workId);
      if (last == null) {
        throw new AppException("Za odabrano održavanje nema zapisa u servisnoj povijesti.");
      }
      return estimate(rule, last, vehicle, LocalDate.now());
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

    for (VehicleWorkRule rule : catalogRepository.rules(vehicle.getVariant().getId())) {
      if (rule.getWork().getCategory() != WorkCategory.MAINTENANCE) {
        continue;
      }
      ServiceItem last = latestItems.get(rule.getWork().getId());
      if (last == null) {
        continue;
      }
      ServiceRecordValues values = ServiceRecordValues.of(last);
      LocalDate nextDate = nextDate(values.date, rule.getIntervalMonths());
      Integer nextMileage = nextMileage(values.mileage, rule.getIntervalKm());
      MaintenanceStatus status =
          calculator.calculate(
              rule.getIntervalKm(),
              rule.getIntervalMonths(),
              values.date,
              values.mileage,
              vehicle.getCurrentMileage(),
              today);
      rows.add(
          new MaintenanceRow(
              rule.getWork().getId(),
              rule.getWork().getCode(),
              rule.getWork().getName(),
              values.date,
              values.mileage,
              nextDate,
              nextMileage,
              status));
    }
    return rows;
  }

  private static MaintenanceEstimate estimate(
      VehicleWorkRule rule, ServiceItem last, Vehicle vehicle, LocalDate today) {
    ServiceRecordValues values = ServiceRecordValues.of(last);
    LocalDate nextDate = nextDate(values.date, rule.getIntervalMonths());
    Integer nextMileage = nextMileage(values.mileage, rule.getIntervalKm());
    MaintenanceStatus status =
        new MaintenanceCalculator()
            .calculate(
                rule.getIntervalKm(),
                rule.getIntervalMonths(),
                values.date,
                values.mileage,
                vehicle.getCurrentMileage(),
                today);
    return new MaintenanceEstimate(
        rule.getWork().getId(),
        rule.getWork().getCode(),
        rule.getWork().getName(),
        rule.getEstimatedPrice(),
        rule.getIntervalKm(),
        rule.getIntervalMonths(),
        nextDate,
        nextMileage,
        status);
  }

  private static Map<Long, ServiceItem> latestItems(
      ServiceRecordRepository serviceRecordRepository, long ownerId, long vehicleId) {
    Map<Long, ServiceItem> latest = new HashMap<>();
    for (ServiceItem item : serviceRecordRepository.historyItems(ownerId, vehicleId)) {
      latest.putIfAbsent(item.getWork().getId(), item);
    }
    return latest;
  }

  private static LocalDate nextDate(LocalDate lastDate, Integer months) {
    return months == null ? null : lastDate.plusMonths(months);
  }

  private static Integer nextMileage(Integer lastMileage, Integer intervalKm) {
    return intervalKm == null ? null : lastMileage + intervalKm;
  }

  private static final class ServiceRecordValues {
    private final LocalDate date;
    private final Integer mileage;

    private ServiceRecordValues(LocalDate date, Integer mileage) {
      this.date = date;
      this.mileage = mileage;
    }

    static ServiceRecordValues of(ServiceItem item) {
      return new ServiceRecordValues(
          item.getServiceRecord().getServiceDate(), item.getServiceRecord().getMileage());
    }
  }
}
