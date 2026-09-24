package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.CatalogCategory;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Jednostavni podaci koji nisu zasebni domenski entiteti. */
public final class Data {
  private Data() {}

  public static final class VehicleRow {
    private final int id;
    private final VehicleVariant variant;
    private final int year;
    private final int mileage;
    private final boolean active;

    public VehicleRow(
        int id, VehicleVariant variant, int year, int mileage, boolean active) {
      this.id = id;
      this.variant = variant;
      this.year = year;
      this.mileage = mileage;
      this.active = active;
    }

    public int getId() {
      return id;
    }

    public VehicleVariant getVariant() {
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

  /** Podaci uneseni pri dodavanju vozila. */
  public static final class VehicleInput {
    private final int variantId;
    private final int year;
    private final int mileage;

    public VehicleInput(int variantId, int year, int mileage) {
      this.variantId = variantId;
      this.year = year;
      this.mileage = mileage;
    }

    public int getVariantId() {
      return variantId;
    }

    public int getYear() {
      return year;
    }

    public int getMileage() {
      return mileage;
    }
  }

  public static final class ItemInput {
    private final int workId;
    private final BigDecimal actualPrice;

    public ItemInput(int workId, BigDecimal actualPrice) {
      this.workId = workId;
      this.actualPrice = actualPrice;
    }

    public int getWorkId() {
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
    private final List<Integer> resolvedProblemIds;

    public ServiceInput(
        LocalDate date,
        int mileage,
        String note,
        List<ItemInput> items,
        List<Integer> resolvedProblemIds) {
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

    public List<Integer> getResolvedProblemIds() {
      return resolvedProblemIds;
    }
  }

  public static final class ServiceRow {
    private final int id;
    private final LocalDate date;
    private final int mileage;
    private final String names;
    private final BigDecimal total;
    private final String note;

    public ServiceRow(
        int id, LocalDate date, int mileage, String names, BigDecimal total, String note) {
      this.id = id;
      this.date = date;
      this.mileage = mileage;
      this.names = names;
      this.total = total;
      this.note = note;
    }

    public int getId() {
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

    public BigDecimal getTotal() {
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

  public static final class MaintenanceRow {
    private final String name;
    private final LocalDate lastDate;
    private final Integer lastMileage;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final Integer remainingKm;
    private final Long remainingDays;
    private final double remainingRatio;

    public MaintenanceRow(
        String name,
        LocalDate lastDate,
        Integer lastMileage,
        LocalDate nextDate,
        Integer nextMileage,
        Integer remainingKm,
        Long remainingDays,
        double remainingRatio) {
      this.name = name;
      this.lastDate = lastDate;
      this.lastMileage = lastMileage;
      this.nextDate = nextDate;
      this.nextMileage = nextMileage;
      this.remainingKm = remainingKm;
      this.remainingDays = remainingDays;
      this.remainingRatio = remainingRatio;
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

    public Integer getRemainingKm() {
      return remainingKm;
    }

    public Long getRemainingDays() {
      return remainingDays;
    }

    public double getRemainingRatio() {
      return remainingRatio;
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
    private final BigDecimal total;
    private final long openProblems;
    private final MaintenanceRow nextMaintenance;

    public Dashboard(
        VehicleRow vehicle,
        BigDecimal total,
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

    public BigDecimal getTotal() {
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
