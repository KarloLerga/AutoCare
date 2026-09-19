package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ProblemCategory;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.WorkCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Jednostavni podaci koje razmjenjuju slojevi aplikacije. */
public final class Data {
  private Data() {}

  public static final class Account {
    private final long id;
    private final String name;
    private final String email;

    public Account(long id, String name, String email) {
      this.id = id;
      this.name = name;
      this.email = email;
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
  }

  public static final class VariantRow {
    private final long id;
    private final String make;
    private final String model;
    private final String generation;
    private final String engine;
    private final String fuel;
    private final String transmission;
    private final Integer powerHp;

    public VariantRow(
        long id,
        String make,
        String model,
        String generation,
        String engine,
        String fuel,
        String transmission,
        Integer powerHp) {
      this.id = id;
      this.make = make;
      this.model = model;
      this.generation = generation;
      this.engine = engine;
      this.fuel = fuel;
      this.transmission = transmission;
      this.powerHp = powerHp;
    }

    public long getId() {
      return id;
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
    private final String name;
    private final WorkCategory category;

    public WorkRow(long id, String name, WorkCategory category) {
      this.id = id;
      this.name = name;
      this.category = category;
    }

    public long getId() {
      return id;
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
    private final BigDecimal actualPrice;

    public ItemRow(String name, BigDecimal actualPrice) {
      this.name = name;
      this.actualPrice = actualPrice;
    }

    public String getName() {
      return name;
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
    private final ProblemCategory category;
    private final ProblemStatus status;
    private final LocalDateTime createdAt;

    public ProblemRow(
        long id,
        String description,
        ProblemCategory category,
        ProblemStatus status,
        LocalDateTime createdAt) {
      this.id = id;
      this.description = description;
      this.category = category;
      this.status = status;
      this.createdAt = createdAt;
    }

    public long getId() {
      return id;
    }

    public String getDescription() {
      return description;
    }

    public ProblemCategory getCategory() {
      return category;
    }

    public ProblemStatus getStatus() {
      return status;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }
  }

  public static final class MaintenanceRow {
    private final long workId;
    private final String name;
    private final LocalDate lastDate;
    private final Integer lastMileage;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final MaintenanceStatus status;

    public MaintenanceRow(
        long workId,
        String name,
        LocalDate lastDate,
        Integer lastMileage,
        LocalDate nextDate,
        Integer nextMileage,
        MaintenanceStatus status) {
      this.workId = workId;
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
  }

  public static final class CatalogRow {
    private final String name;
    private final CatalogCategory catalogCategory;
    private final WorkCategory workCategory;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Integer intervalKm;
    private final Integer intervalMonths;

    public CatalogRow(
        String name,
        CatalogCategory catalogCategory,
        WorkCategory workCategory,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer intervalKm,
        Integer intervalMonths) {
      this.name = name;
      this.catalogCategory = catalogCategory;
      this.workCategory = workCategory;
      this.minPrice = minPrice;
      this.maxPrice = maxPrice;
      this.intervalKm = intervalKm;
      this.intervalMonths = intervalMonths;
    }

    public String getName() {
      return name;
    }

    public CatalogCategory getCatalogCategory() {
      return catalogCategory;
    }

    public WorkCategory getWorkCategory() {
      return workCategory;
    }

    public BigDecimal getMinPrice() {
      return minPrice;
    }

    public BigDecimal getMaxPrice() {
      return maxPrice;
    }

    public Integer getIntervalKm() {
      return intervalKm;
    }

    public Integer getIntervalMonths() {
      return intervalMonths;
    }
  }

  public static final class Dashboard {
    private final VehicleRow vehicle;
    private final CostSummary total;
    private final long activeNotes;
    private final MaintenanceRow nextMaintenance;

    public Dashboard(
        VehicleRow vehicle,
        CostSummary total,
        long activeNotes,
        MaintenanceRow nextMaintenance) {
      this.vehicle = vehicle;
      this.total = total;
      this.activeNotes = activeNotes;
      this.nextMaintenance = nextMaintenance;
    }

    public VehicleRow getVehicle() {
      return vehicle;
    }

    public CostSummary getTotal() {
      return total;
    }

    public long getActiveNotes() {
      return activeNotes;
    }

    public MaintenanceRow getNextMaintenance() {
      return nextMaintenance;
    }
  }
}
