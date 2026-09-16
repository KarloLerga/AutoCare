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
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RaÄuna odrÅ¾avanje iz servisne povijesti i pravila za vozilo. */
public final class MaintenanceService {

    private final TransactionRunner transactions;
    private final Clock clock;

    public MaintenanceService(
            TransactionRunner transactions,
            Clock clock) {

        this.transactions = transactions;
        this.clock = clock;
    }

    public List<MaintenanceRow> list(
            long ownerId,
            long vehicleId) {

        return transactions.read(repositories -> {
            Vehicle vehicle =
                    repositories.vehicles()
                            .requireOwned(ownerId, vehicleId);

            return calculate(
                    repositories,
                    ownerId,
                    vehicle,
                    clock);
        });
    }

    static List<MaintenanceRow> calculate(
            Repositories repositories,
            long ownerId,
            Vehicle vehicle,
            Clock clock) {

        Map<Long, ServiceItem> latestItems =
                findLatestItems(
                        repositories,
                        ownerId,
                        vehicle.getId());

        Map<Long, VehicleWorkRule> rules =
                findRules(
                        repositories,
                        vehicle.getVariant().getId());

        MaintenanceCalculator calculator =
                new MaintenanceCalculator();

        LocalDate today = LocalDate.now(clock);
        List<MaintenanceRow> rows = new ArrayList<>();

        for (WorkDefinition work
                : repositories.catalog()
                        .works(WorkCategory.MAINTENANCE)) {

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

            ServiceItem lastItem = latestItems.get(work.getId());

            LocalDate lastDate = null;
            Integer lastMileage = null;

            if (lastItem != null) {
                lastDate =
                        lastItem.getServiceRecord()
                                .getServiceDate();

                lastMileage =
                        lastItem.getServiceRecord()
                                .getMileage();
            }

            LocalDate nextDate =
                    nextDate(lastDate, intervalMonths);

            Integer nextMileage =
                    nextMileage(lastMileage, intervalKm);

            MaintenanceStatus status =
                    calculator.calculate(
                            intervalKm,
                            intervalMonths,
                            lastDate,
                            lastMileage,
                            vehicle.getCurrentMileage(),
                            today);

            Integer remainingKm =
                    nextMileage == null
                            ? null
                            : nextMileage - vehicle.getCurrentMileage();

            Long remainingDays =
                    nextDate == null
                            ? null
                            : ChronoUnit.DAYS.between(today, nextDate);

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
            Repositories repositories,
            long variantId) {

        Map<Long, VehicleWorkRule> rules = new HashMap<>();

        for (VehicleWorkRule rule
                : repositories.catalog().rules(variantId)) {

            rules.put(rule.getWork().getId(), rule);
        }

        return rules;
    }

    private static Map<Long, ServiceItem> findLatestItems(
            Repositories repositories,
            long ownerId,
            long vehicleId) {

        Map<Long, ServiceItem> latestItems = new HashMap<>();

        for (ServiceItem item
                : repositories.services()
                        .historyItems(ownerId, vehicleId)) {

            latestItems.putIfAbsent(
                    item.getWork().getId(),
                    item);
        }

        return latestItems;
    }

    private static LocalDate nextDate(
            LocalDate lastDate,
            Integer intervalMonths) {

        if (lastDate == null || intervalMonths == null) {
            return null;
        }

        return lastDate.plusMonths(intervalMonths);
    }

    private static Integer nextMileage(
            Integer lastMileage,
            Integer intervalKm) {

        if (lastMileage == null || intervalKm == null) {
            return null;
        }

        return lastMileage + intervalKm;
    }
}
