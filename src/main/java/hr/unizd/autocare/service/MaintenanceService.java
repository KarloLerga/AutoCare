package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.MaintenanceCalculator;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleWorkRule;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.MaintenanceRow;
import hr.unizd.autocare.repository.Repositories;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Izracun iz pravila i servisne povijesti, bez spremanja izvedenih rokova. */
public final class MaintenanceService {

  private final TransactionRunner transactions;
  private final Clock clock;

  public MaintenanceService(TransactionRunner transactions, Clock clock) {
    this.transactions = transactions;
    this.clock = clock;
  }

  public List<MaintenanceRow> list(long ownerId, long vehicleId) {
    return transactions.read(
        repositories -> {
          Vehicle vehicle = repositories.vehicles().requireOwned(ownerId, vehicleId);
          return calculate(repositories, ownerId, vehicle, clock);
        });
  }

  static List<MaintenanceRow> calculate(
      Repositories repositories, long ownerId, Vehicle vehicle, Clock clock) {
    Map<Long, ServiceItem> latestItems = new HashMap<>();

    // historyItems already orders by service date, mileage and ID descending.
    for (ServiceItem item : repositories.services().historyItems(ownerId, vehicle.getId())) {
      latestItems.putIfAbsent(item.getWork().getId(), item);
    }

    List<MaintenanceRow> rows = new ArrayList<>();
    MaintenanceCalculator calculator = new MaintenanceCalculator();
    LocalDate today = LocalDate.now(clock);

    for (VehicleWorkRule rule : repositories.catalog().rules(vehicle.getVariant().getId())) {
      WorkDefinition work = rule.getWork();

      if (work.getCategory() != WorkCategory.MAINTENANCE) {
        continue;
      }

      ServiceItem lastItem = latestItems.get(work.getId());
      LocalDate lastDate = null;
      Integer lastMileage = null;
      LocalDate nextDate = null;
      Integer nextMileage = null;

      if (lastItem != null) {
        lastDate = lastItem.getServiceRecord().getServiceDate();
        lastMileage = lastItem.getServiceRecord().getMileage();

        if (rule.getIntervalMonths() != null) {
          nextDate = lastDate.plusMonths(rule.getIntervalMonths());
        }

        if (rule.getIntervalKm() != null) {
          nextMileage = Math.addExact(lastMileage, rule.getIntervalKm());
        }
      }

      MaintenanceStatus status =
          calculator.calculate(
              rule.getScheduleKind(),
              rule.getIntervalKm(),
              rule.getIntervalMonths(),
              lastDate,
              lastMileage,
              vehicle.getCurrentMileage(),
              today);

      Integer remainingKm = nextMileage == null ? null : nextMileage - vehicle.getCurrentMileage();
      Long remainingDays = nextDate == null ? null : ChronoUnit.DAYS.between(today, nextDate);

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
              rule.getEstimatedPrice(),
              rule.getIntervalSource(),
              rule.getEstimateNote(),
              remainingKm,
              remainingDays));
    }

    return List.copyOf(rows);
  }
}
