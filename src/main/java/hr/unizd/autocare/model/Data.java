package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Jednostavni pomoćni podaci koji nisu zasebni domenski entiteti. */
public final class Data {
  private Data() {}

  /** Jedna stavka unesena u formi novog servisa. */
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

  /** Podaci forme za spremanje novog servisa. */
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

  /** Redak servisne povijesti s pripremljenim nazivima radova i ukupnim troškom. */
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

  /** Detalj servisa koji sadrži redak servisa, njegove stavke i riješene probleme. */
  public static final class ServiceDetail {
    private final ServiceRow header;
    private final List<ServiceItem> items;
    private final List<String> resolvedProblems;

    public ServiceDetail(
        ServiceRow header, List<ServiceItem> items, List<String> resolvedProblems) {
      this.header = header;
      this.items = new ArrayList<>(items);
      this.resolvedProblems = new ArrayList<>(resolvedProblems);
    }

    public ServiceRow getHeader() {
      return header;
    }

    public List<ServiceItem> getItems() {
      return items;
    }

    public List<String> getResolvedProblems() {
      return resolvedProblems;
    }
  }

  /** Izračunati podaci jednog praćenog održavanja. */
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

  /** Podaci koje Dashboard prikazuje za aktivno vozilo. */
  public static final class Dashboard {
    private final Vehicle vehicle;
    private final BigDecimal total;
    private final long openProblems;
    private final MaintenanceRow nextMaintenance;

    public Dashboard(
        Vehicle vehicle,
        BigDecimal total,
        long openProblems,
        MaintenanceRow nextMaintenance) {
      this.vehicle = vehicle;
      this.total = total;
      this.openProblems = openProblems;
      this.nextMaintenance = nextMaintenance;
    }

    public Vehicle getVehicle() {
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
