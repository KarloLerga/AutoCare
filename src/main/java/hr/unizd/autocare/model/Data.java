package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.CostSummary;
import hr.unizd.autocare.domain.MaintenanceStatus;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.WorkCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Nepromjenjivi ulazi i rezultati. Niti jedan tip ne nosi JPA entitet ili Swing komponentu. */
public final class Data {
  private Data() {}

  /** Account - jednostavan prijenos podataka preko granice slojeva. */
  public static final class Account {
    private final long id;
    private final long version;
    private final String name;
    private final String email;
    private final Long activeVehicleId;

    public Account(long id, long version, String name, String email, Long activeVehicleId) {
      this.id = id;
      this.version = version;
      this.name = name;
      this.email = email;
      this.activeVehicleId = activeVehicleId;
    }

    public long getId() {
      return id;
    }

    public long getVersion() {
      return version;
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

  /** Credentials - jednostavan prijenos podataka preko granice slojeva. */
  public static final class Credentials {
    private final long id;
    private final long version;
    private final String hash;

    public Credentials(long id, long version, String hash) {
      this.id = id;
      this.version = version;
      this.hash = hash;
    }

    public long getId() {
      return id;
    }

    public long getVersion() {
      return version;
    }

    public String getHash() {
      return hash;
    }
  }

  /** VariantRow - jednostavan prijenos podataka preko granice slojeva. */
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
    private final String imagePath;

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
        Integer to,
        String imagePath) {
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
      this.imagePath = imagePath;
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

    public String getImagePath() {
      return imagePath;
    }
  }

  /** VehicleRow - jednostavan prijenos podataka preko granice slojeva. */
  public static final class VehicleRow {
    private final long id;
    private final long version;
    private final VariantRow variant;
    private final int year;
    private final int mileage;
    private final boolean active;

    public VehicleRow(
        long id, long version, VariantRow variant, int year, int mileage, boolean active) {
      this.id = id;
      this.version = version;
      this.variant = variant;
      this.year = year;
      this.mileage = mileage;
      this.active = active;
    }

    public long getId() {
      return id;
    }

    public long getVersion() {
      return version;
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

  /** VehicleInput - jednostavan prijenos podataka preko granice slojeva. */
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

  /** WorkRow - jednostavan prijenos podataka preko granice slojeva. */
  public static final class WorkRow {
    private final long id;
    private final String code;
    private final String name;
    private final WorkCategory category;
    private final BigDecimal price;
    private final String priceNote;

    public WorkRow(
        long id,
        String code,
        String name,
        WorkCategory category,
        BigDecimal price,
        String priceNote) {
      this.code = code;
      this.id = id;
      this.name = name;
      this.category = category;
      this.price = price;
      this.priceNote = priceNote;
    }

    public String getCode() {
      return code;
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

    public BigDecimal getPrice() {
      return price;
    }

    public String getPriceNote() {
      return priceNote;
    }
  }

  /** ItemInput - jednostavan prijenos podataka preko granice slojeva. */
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

  /** ServiceInput - jednostavan prijenos podataka preko granice slojeva. */
  public static final class ServiceInput {
    private final String requestKey;
    private final LocalDate date;
    private final int mileage;
    private final String note;
    private final List<ItemInput> items;
    private final List<Long> resolvedProblemIds;

    public ServiceInput(
        String requestKey,
        LocalDate date,
        int mileage,
        String note,
        List<ItemInput> items,
        List<Long> resolvedProblemIds) {
      this.requestKey = requestKey;
      this.date = date;
      this.mileage = mileage;
      this.note = note;
      this.items = List.copyOf(items);
      this.resolvedProblemIds = List.copyOf(resolvedProblemIds);
    }

    public String getRequestKey() {
      return requestKey;
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

  /** ServiceRow - jednostavan prijenos podataka preko granice slojeva. */
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

  /** ItemRow - jednostavan prijenos podataka preko granice slojeva. */
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

  /** ServiceDetail - jednostavan prijenos podataka preko granice slojeva. */
  public static final class ServiceDetail {
    private final ServiceRow header;
    private final List<ItemRow> items;
    private final List<String> resolvedProblems;

    public ServiceDetail(ServiceRow header, List<ItemRow> items, List<String> resolvedProblems) {
      this.header = header;
      this.items = List.copyOf(items);
      this.resolvedProblems = List.copyOf(resolvedProblems);
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

  /** ProblemRow - jednostavan prijenos podataka preko granice slojeva. */
  public static final class ProblemRow {
    private final long id;
    private final long version;
    private final String description;
    private final ProblemStatus status;
    private final LocalDateTime createdAt;
    private final String suggestion;
    private final BigDecimal score;
    private final BigDecimal price;
    private final String priceNote;
    private final Long resolvedServiceId;

    public ProblemRow(
        long id,
        long version,
        String description,
        ProblemStatus status,
        LocalDateTime createdAt,
        String suggestion,
        BigDecimal score,
        BigDecimal price,
        String priceNote,
        Long resolvedServiceId) {
      this.id = id;
      this.version = version;
      this.description = description;
      this.status = status;
      this.createdAt = createdAt;
      this.suggestion = suggestion;
      this.score = score;
      this.price = price;
      this.priceNote = priceNote;
      this.resolvedServiceId = resolvedServiceId;
    }

    public long getId() {
      return id;
    }

    public long getVersion() {
      return version;
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

    public String getSuggestion() {
      return suggestion;
    }

    public BigDecimal getScore() {
      return score;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public String getPriceNote() {
      return priceNote;
    }

    public Long getResolvedServiceId() {
      return resolvedServiceId;
    }
  }

  /** RuleData - jednostavan prijenos podataka preko granice slojeva. */
  public static final class RuleData {
    private final long candidateId;
    private final String candidateName;
    private final String phrase;
    private final int weight;
    private final BigDecimal price;
    private final String priceNote;

    public RuleData(
        long candidateId,
        String candidateName,
        String phrase,
        int weight,
        BigDecimal price,
        String priceNote) {
      this.candidateId = candidateId;
      this.candidateName = candidateName;
      this.phrase = phrase;
      this.weight = weight;
      this.price = price;
      this.priceNote = priceNote;
    }

    public long getCandidateId() {
      return candidateId;
    }

    public String getCandidateName() {
      return candidateName;
    }

    public String getPhrase() {
      return phrase;
    }

    public int getWeight() {
      return weight;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public String getPriceNote() {
      return priceNote;
    }
  }

  /** DiagnosticResult - jednostavan prijenos podataka preko granice slojeva. */
  public static final class DiagnosticResult {
    private final long candidateId;
    private final String candidateName;
    private final BigDecimal score;
    private final int matchedWeight;
    private final BigDecimal price;
    private final String priceNote;

    public DiagnosticResult(
        long candidateId,
        String candidateName,
        BigDecimal score,
        int matchedWeight,
        BigDecimal price,
        String priceNote) {
      this.candidateId = candidateId;
      this.candidateName = candidateName;
      this.score = score;
      this.matchedWeight = matchedWeight;
      this.price = price;
      this.priceNote = priceNote;
    }

    public long getCandidateId() {
      return candidateId;
    }

    public String getCandidateName() {
      return candidateName;
    }

    public BigDecimal getScore() {
      return score;
    }

    public int getMatchedWeight() {
      return matchedWeight;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public String getPriceNote() {
      return priceNote;
    }
  }

  /** Analysis - jednostavan prijenos podataka preko granice slojeva. */
  public static final class Analysis {
    private final String description;
    private final List<DiagnosticResult> results;

    public Analysis(String description, List<DiagnosticResult> results) {
      this.description = description;
      this.results = List.copyOf(results);
    }

    public String getDescription() {
      return description;
    }

    public List<DiagnosticResult> getResults() {
      return results;
    }
  }

  /** MaintenanceRow - jednostavan prijenos podataka preko granice slojeva. */
  public static final class MaintenanceRow {
    private final long workId;
    private final String workCode;
    private final String name;
    private final LocalDate lastDate;
    private final Integer lastMileage;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final MaintenanceStatus status;
    private final Integer remainingKm;
    private final Long remainingDays;
    private final BigDecimal price;
    private final String intervalSource;
    private final String priceNote;

    public MaintenanceRow(
        long workId,
        String workCode,
        String name,
        LocalDate lastDate,
        Integer lastMileage,
        LocalDate nextDate,
        Integer nextMileage,
        MaintenanceStatus status,
        BigDecimal price,
        String intervalSource,
        String priceNote,
        Integer remainingKm,
        Long remainingDays) {
      this.workId = workId;
      this.workCode = workCode;
      this.name = name;
      this.lastDate = lastDate;
      this.lastMileage = lastMileage;
      this.nextDate = nextDate;
      this.nextMileage = nextMileage;
      this.status = status;
      this.price = price;
      this.intervalSource = intervalSource;
      this.priceNote = priceNote;
      this.remainingKm = remainingKm;
      this.remainingDays = remainingDays;
    }

    public String getWorkCode() {
      return workCode;
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

    public Integer getRemainingKm() {
      return remainingKm;
    }

    public Long getRemainingDays() {
      return remainingDays;
    }

    public MaintenanceStatus getStatus() {
      return status;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public String getIntervalSource() {
      return intervalSource;
    }

    public String getPriceNote() {
      return priceNote;
    }
  }

  /** Dashboard - jednostavan prijenos podataka preko granice slojeva. */
  public static final class Dashboard {
    private final VehicleRow vehicle;
    private final CostSummary total;
    private final long openProblems;
    private final int due;
    private final int soon;
    private final int unknown;
    private final int covered;

    public Dashboard(
        VehicleRow vehicle,
        CostSummary total,
        long openProblems,
        int due,
        int soon,
        int unknown,
        int covered) {
      this.vehicle = vehicle;
      this.total = total;
      this.openProblems = openProblems;
      this.due = due;
      this.soon = soon;
      this.unknown = unknown;
      this.covered = covered;
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

    public int getDue() {
      return due;
    }

    public int getSoon() {
      return soon;
    }

    public int getUnknown() {
      return unknown;
    }

    public int getCovered() {
      return covered;
    }
  }
}
