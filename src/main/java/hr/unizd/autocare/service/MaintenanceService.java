package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Racuna odrzavanje iz servisne povijesti i pravila za vozilo. */
public final class MaintenanceService {
  private final EntityManagerFactory entityManagerFactory;

  public MaintenanceService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<MaintenanceRow> list(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      return calculate(catalogRepository, serviceRecordRepository, ownerId, vehicle);
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
        findLatestItems(serviceRecordRepository, ownerId, vehicle.getId());
    Map<Long, VehicleWorkRule> rules = findRules(catalogRepository, vehicle.getVariant().getId());
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.now();
    List<MaintenanceRow> rows = new ArrayList<>();

    for (WorkDefinition work : catalogRepository.works(WorkCategory.MAINTENANCE)) {
      VehicleWorkRule rule = rules.get(work.getId());
      Integer intervalKm;
      Integer intervalMonths;
      BigDecimal estimatedPrice;
      String intervalSource;
      String estimateNote;

      if (rule != null) {
        intervalKm = rule.getIntervalKm();
        intervalMonths = rule.getIntervalMonths();
        estimatedPrice = rule.getEstimatedPrice();
        intervalSource = rule.getIntervalSource();
        estimateNote = rule.getEstimateNote();
      } else {
        intervalKm = work.getDefaultIntervalKm();
        intervalMonths = work.getDefaultIntervalMonths();
        estimatedPrice = work.getDefaultEstimatedPrice();
        intervalSource = null;
        estimateNote = work.getEstimateNote();
        if (intervalKm == null && intervalMonths == null) {
          continue;
        }
      }

      ServiceItem lastServiceItem = latestItems.get(work.getId());
      LocalDate lastDate = null;
      Integer lastMileage = null;
      if (lastServiceItem != null) {
        lastDate = lastServiceItem.getServiceRecord().getServiceDate();
        lastMileage = lastServiceItem.getServiceRecord().getMileage();
      }

      LocalDate nextDate = nextDate(lastDate, intervalMonths);
      Integer nextMileage = nextMileage(lastMileage, intervalKm);
      MaintenanceStatus status =
          calculator.calculate(
              intervalKm,
              intervalMonths,
              lastDate,
              lastMileage,
              vehicle.getCurrentMileage(),
              today);
      Integer remainingKm = null;
      if (nextMileage != null) {
        remainingKm = nextMileage - vehicle.getCurrentMileage();
      }
      Long remainingDays = null;
      if (nextDate != null) {
        remainingDays = ChronoUnit.DAYS.between(today, nextDate);
      }

      rows.add(
          new MaintenanceRow(
              work.getId(),
              work.getCode(),
              work.getName(),
              lastDate,
              lastMileage,
              nextDate,
              nextMileage,
              status,
              estimatedPrice,
              intervalSource,
              estimateNote,
              remainingKm,
              remainingDays));
    }
    return rows;
  }

  private static Map<Long, VehicleWorkRule> findRules(
      CatalogRepository catalogRepository, long variantId) {
    Map<Long, VehicleWorkRule> rules = new HashMap<>();
    for (VehicleWorkRule rule : catalogRepository.rules(variantId)) {
      rules.put(rule.getWork().getId(), rule);
    }
    return rules;
  }

  private static Map<Long, ServiceItem> findLatestItems(
      ServiceRecordRepository serviceRecordRepository, long ownerId, long vehicleId) {
    Map<Long, ServiceItem> latestItems = new HashMap<>();
    for (ServiceItem serviceItem :
        serviceRecordRepository.historyItems(ownerId, vehicleId)) {
      long workId = serviceItem.getWork().getId();
      if (!latestItems.containsKey(workId)) {
        latestItems.put(workId, serviceItem);
      }
    }
    return latestItems;
  }

  private static LocalDate nextDate(LocalDate lastDate, Integer intervalMonths) {
    if (lastDate == null || intervalMonths == null) {
      return null;
    }
    return lastDate.plusMonths(intervalMonths);
  }

  private static Integer nextMileage(Integer lastMileage, Integer intervalKm) {
    if (lastMileage == null || intervalKm == null) {
      return null;
    }
    return lastMileage + intervalKm;
  }
}
