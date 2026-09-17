package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.WorkCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Nepromjenjivi ulazi i rezultati između poslovnog sloja i Swinga. */
public final class Data {
  private Data() {}

  public static final class Account {
    private final long id;
    private final String name;
    private final String email;
    private final Long activeVehicleId;

    public Account(long id, String name, String email, Long activeVehicleId) {
      this.id = id;
      this.name = name;
      this.email = email;
      this.activeVehicleId = activeVehicleId;
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getEmail() {
      return email;
    }

    public Long getActiveVehicleId() {
      return activeVehicleId;
    }
  }

  public static final class VariantRow {
    private final long id;
    private final String code;
    private final String make;
    private final String model;
    private final String generation;
    private final String engine;
    private final String fuel;
    private final String transmission;
    private final Integer powerHp;
    private final int from;
    private final Integer to;

    public VariantRow(
        long id,
        String code,
        String make,
        String model,
        String generation,
        String engine,
        String fuel,
        String transmission,
        Integer powerHp,
        int from,
        Integer to) {
      this.id = id;
      this.code = code;
      this.make = make;
      this.model = model;
      this.generation = generation;
      this.engine = engine;
      this.fuel = fuel;
      this.transmission = transmission;
      this.powerHp = powerHp;
      this.from = from;
      this.to = to;
    }

    public long getId() {
      return id;
    }

    public String getCode() {
      return code;
    }

    public String getMake() {
      return make;
    }

    public String getModel() {
      return model;
    }

    public String getGeneration() {
      return generation;
    }

    public String getEngine() {
      return engine;
    }

    public String getFuel() {
      return fuel;
    }

    public String getTransmission() {
      return transmission;
    }

    public Integer getPowerHp() {
      return powerHp;
    }

    public int getFrom() {
      return from;
    }

    public Integer getTo() {
      return to;
    }

    @Override
    public String toString() {
      return generation + " / " + engine + " / " + fuel;
    }
  }

  public static final class VehicleRow {
    private final long id;
    private final VariantRow variant;
    private final int year;
    private final int mileage;
    private final boolean active;

    public VehicleRow(long id, VariantRow variant, int year, int mileage, boolean active) {
      this.id = id;
      this.variant = variant;
      this.year = year;
      this.mileage = mileage;
      this.active = active;
    }

    public long getId() {
      return id;
    }

    public VariantRow getVariant() {
      return variant;
    }

    public int getYear() {
      return year;
    }

    public int getMileage() {
      return mileage;
    }

    public boolean getActive() {
      return active;
    }
  }

  /** Ulaz za stvaranje vozila; identitet se nakon stvaranja više ne mijenja. */
  public static final class VehicleInput {
    private final long variantId;
    private final int year;
    private final int mileage;

    public VehicleInput(long variantId, int year, int mileage) {
      this.variantId = variantId;
      this.year = year;
      this.mileage = mileage;
    }

    public long getVariantId() {
      return variantId;
    }

    public int getYear() {
      return year;
    }

    public int getMileage() {
      return mileage;
    }
  }

  public static final class WorkRow {
    private final long id;
    private final String code;
    private final String name;
    private final WorkCategory category;

    public WorkRow(long id, String code, String name, WorkCategory category) {
      this.id = id;
      this.code = code;
      this.name = name;
      this.category = category;
    }

    public long getId() {
      return id;
    }

    public String getCode() {
      return code;
    }

    public String getName() {
      return name;
    }

    public WorkCategory getCategory() {
      return category;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static final class ItemInput {
    private final long workId;
    private final BigDecimal actualPrice;

    public ItemInput(long workId, BigDecimal actualPrice) {
      this.workId = workId;
      this.actualPrice = actualPrice;
    }

    public long getWorkId() {
      return workId;
    }

    public BigDecimal getActualPrice() {
      return actualPrice;
    }
  }

  public static final class ServiceInput {
    private final LocalDate date;
    private final int mileage;
    private final String note;
    private final List<ItemInput> items;
    private final List<Long> resolvedProblemIds;

    public ServiceInput(
        LocalDate date,
        int mileage,
        String note,
        List<ItemInput> items,
        List<Long> resolvedProblemIds) {
      this.date = date;
      this.mileage = mileage;
      this.note = note;
      this.items = new ArrayList<>(items);
      this.resolvedProblemIds = new ArrayList<>(resolvedProblemIds);
    }

    public LocalDate getDate() {
      return date;
    }

    public int getMileage() {
      return mileage;
    }

    public String getNote() {
      return note;
    }

    public List<ItemInput> getItems() {
      return items;
    }

    public List<Long> getResolvedProblemIds() {
      return resolvedProblemIds;
    }
  }

  public static final class ServiceRow {
    private final long id;
    private final LocalDate date;
    private final int mileage;
    private final String names;
    private final CostSummary total;
    private final String note;

    public ServiceRow(
        long id, LocalDate date, int mileage, String names, CostSummary total, String note) {
      this.id = id;
      this.date = date;
      this.mileage = mileage;
      this.names = names;
      this.total = total;
      this.note = note;
    }

    public long getId() {
      return id;
    }

    public LocalDate getDate() {
      return date;
    }

    public int getMileage() {
      return mileage;
    }

    public String getNames() {
      return names;
    }

    public CostSummary getTotal() {
      return total;
    }

    public String getNote() {
      return note;
    }
  }

  public static final class ItemRow {
    private final String name;
    private final WorkCategory category;
    private final BigDecimal actualPrice;

    public ItemRow(String name, WorkCategory category, BigDecimal actualPrice) {
      this.name = name;
      this.category = category;
      this.actualPrice = actualPrice;
    }

    public String getName() {
      return name;
    }

    public WorkCategory getCategory() {
      return category;
    }

    public BigDecimal getActualPrice() {
      return actualPrice;
    }
  }

  public static final class ServiceDetail {
    private final ServiceRow header;
    private final List<ItemRow> items;
    private final List<String> resolvedProblems;

    public ServiceDetail(ServiceRow header, List<ItemRow> items, List<String> resolvedProblems) {
      this.header = header;
      this.items = new ArrayList<>(items);
      this.resolvedProblems = new ArrayList<>(resolvedProblems);
    }

    public ServiceRow getHeader() {
      return header;
    }

    public List<ItemRow> getItems() {
      return items;
    }

    public List<String> getResolvedProblems() {
      return resolvedProblems;
    }
  }

  public static final class ProblemRow {
    private final long id;
    private final String description;
    private final ProblemStatus status;
    private final LocalDateTime createdAt;
    private final String suggestedRepair;
    private final BigDecimal estimatedCost;
    private final Long resolvedServiceId;

    public ProblemRow(
        long id,
        String description,
        ProblemStatus status,
        LocalDateTime createdAt,
        String suggestedRepair,
        BigDecimal estimatedCost,
        Long resolvedServiceId) {
      this.id = id;
      this.description = description;
      this.status = status;
      this.createdAt = createdAt;
      this.suggestedRepair = suggestedRepair;
      this.estimatedCost = estimatedCost;
      this.resolvedServiceId = resolvedServiceId;
    }

    public long getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }

    public ProblemStatus getStatus() {
      return status;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public String getSuggestedRepair() {
      return suggestedRepair;
    }

    public BigDecimal getEstimatedCost() {
      return estimatedCost;
    }

    public Long getResolvedServiceId() {
      return resolvedServiceId;
    }
  }

  public static final class ProblemEstimate {
    private final long workId;
    private final String workName;
    private final BigDecimal estimatedCost;

    public ProblemEstimate(long workId, String workName, BigDecimal estimatedCost) {
      this.workId = workId;
      this.workName = workName;
      this.estimatedCost = estimatedCost;
    }

    public long getWorkId() {
      return workId;
    }

    public String getWorkName() {
      return workName;
    }

    public BigDecimal getEstimatedCost() {
      return estimatedCost;
    }
  }

  public static final class MaintenanceRow {
    private final long workId;
    private final String workCode;
    private final String name;
    private final LocalDate lastDate;
    private final Integer lastMileage;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final MaintenanceStatus status;

    public MaintenanceRow(
        long workId,
        String workCode,
        String name,
        LocalDate lastDate,
        Integer lastMileage,
        LocalDate nextDate,
        Integer nextMileage,
        MaintenanceStatus status) {
      this.workId = workId;
      this.workCode = workCode;
      this.name = name;
      this.lastDate = lastDate;
      this.lastMileage = lastMileage;
      this.nextDate = nextDate;
      this.nextMileage = nextMileage;
      this.status = status;
    }

    public long getWorkId() {
      return workId;
    }

    public String getWorkCode() {
      return workCode;
    }

    public String getName() {
      return name;
    }

    public LocalDate getLastDate() {
      return lastDate;
    }

    public Integer getLastMileage() {
      return lastMileage;
    }

    public LocalDate getNextDate() {
      return nextDate;
    }

    public Integer getNextMileage() {
      return nextMileage;
    }

    public MaintenanceStatus getStatus() {
      return status;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static final class MaintenanceEstimate {
    private final long workId;
    private final String workCode;
    private final String workName;
    private final BigDecimal estimatedPrice;
    private final Integer intervalKm;
    private final Integer intervalMonths;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final MaintenanceStatus status;

    public MaintenanceEstimate(
        long workId,
        String workCode,
        String workName,
        BigDecimal estimatedPrice,
        Integer intervalKm,
        Integer intervalMonths,
        LocalDate nextDate,
        Integer nextMileage,
        MaintenanceStatus status) {
      this.workId = workId;
      this.workCode = workCode;
      this.workName = workName;
      this.estimatedPrice = estimatedPrice;
      this.intervalKm = intervalKm;
      this.intervalMonths = intervalMonths;
      this.nextDate = nextDate;
      this.nextMileage = nextMileage;
      this.status = status;
    }

    public long getWorkId() {
      return workId;
    }

    public String getWorkCode() {
      return workCode;
    }

    public String getWorkName() {
      return workName;
    }

    public BigDecimal getEstimatedPrice() {
      return estimatedPrice;
    }

    public Integer getIntervalKm() {
      return intervalKm;
    }

    public Integer getIntervalMonths() {
      return intervalMonths;
    }

    public LocalDate getNextDate() {
      return nextDate;
    }

    public Integer getNextMileage() {
      return nextMileage;
    }

    public MaintenanceStatus getStatus() {
      return status;
    }
  }

  public static final class Dashboard {
    private final VehicleRow vehicle;
    private final CostSummary total;
    private final long openProblems;
    private final MaintenanceRow nextMaintenance;

    public Dashboard(
        VehicleRow vehicle,
        CostSummary total,
        long openProblems,
        MaintenanceRow nextMaintenance) {
      this.vehicle = vehicle;
      this.total = total;
      this.openProblems = openProblems;
      this.nextMaintenance = nextMaintenance;
    }

    public VehicleRow getVehicle() {
      return vehicle;
    }

    public CostSummary getTotal() {
      return total;
    }

    public long getOpenProblems() {
      return openProblems;
    }

    public MaintenanceRow getNextMaintenance() {
      return nextMaintenance;
    }
  }
}
