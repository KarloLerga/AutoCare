package hr.unizd.autocare.model;

import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sadrži male pomoćne objekte za prijenos i prikaz podataka koji nisu zasebni persistentni
 * domenski entiteti.
 *
 * <p>Tipovi ove klase razdvajaju GUI podatke od JPA entiteta i sprječavaju prosljeđivanje velikog
 * broja nepovezanih vrijednosti kroz Controller i Service potpise.
 */
public class Data {
  /** Sprječava stvaranje instance spremnika pomoćnih tipova. */
  private Data() {}

  /** Jedna stavka unesena u formi novog servisa: odabrani rad i stvarno plaćena cijena. */
  public static final class ItemInput {
    private final int workId;
    private final BigDecimal actualPrice;

    /**
     * Stvara podatke jedne servisne stavke.
     *
     * @param workId identifikator odabranog standardnog rada
     * @param actualPrice stvarno plaćena cijena te servisne stavke
     */
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

  /**
   * Cjelovit skup vrijednosti koje ServiceEditorDialog predaje ServiceRecordServiceu pri spremanju
   * novog servisa.
   */
  public static final class ServiceInput {
    private final LocalDate date;
    private final int mileage;
    private final String note;
    private final List<ItemInput> items;
    private final List<Integer> resolvedProblemIds;

    /**
     * Stvara ulaz servisnog use-casea i kopira predane kolekcije.
     *
     * @param date datum servisa
     * @param mileage kilometraža pri servisu
     * @param note opcionalna napomena
     * @param items unesene servisne stavke
     * @param resolvedProblemIds problemi označeni kao riješeni ovim servisom
     */
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

  /**
   * Prikazni model jednog retka servisne povijesti s već pripremljenim nazivima radova i ukupnim
   * stvarnim troškom.
   */
  public static final class ServiceRow {
    private final int id;
    private final LocalDate date;
    private final int mileage;
    private final String names;
    private final BigDecimal total;
    private final String note;

    /**
     * Stvara redak spreman za prikaz servisne povijesti.
     *
     * @param id identifikator servisa
     * @param date datum servisa
     * @param mileage kilometraža na servisu
     * @param names sažeti nazivi radova
     * @param total ukupni stvarni trošak
     * @param note napomena uz servis
     */
    public ServiceRow(int id, LocalDate date, int mileage, String names, BigDecimal total, String note) {
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

  /** Puni prikaz jednog servisa: zaglavlje, servisne stavke i problemi riješeni servisom. */
  public static final class ServiceDetail {
    private final ServiceRow header;
    private final List<ServiceItem> items;
    private final List<String> resolvedProblems;

    /**
     * Stvara prikazni model detalja spremljenog servisa.
     *
     * @param header sažetak servisa
     * @param items stavke spremljene u servisu
     * @param resolvedProblems opisi problema riješenih tim servisom
     */
    public ServiceDetail(ServiceRow header, List<ServiceItem> items, List<String> resolvedProblems) {
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

  /**
   * Prikazni rezultat izračuna jednog praćenog rada održavanja.
   *
   * <p>Sadrži podatke posljednje izvedbe, izračun sljedećeg dospijeća i preostali interval u
   * kilometrima, danima i relativnom omjeru.
   */
  public static final class MaintenanceRow {
    private final String name;
    private final LocalDate lastDate;
    private final Integer lastMileage;
    private final LocalDate nextDate;
    private final Integer nextMileage;
    private final Integer remainingKm;
    private final Long remainingDays;
    private final double remainingRatio;

    /**
     * Stvara rezultat izračunatog intervala održavanja.
     *
     * @param name naziv standardnog rada
     * @param lastDate datum posljednje izvedbe
     * @param lastMileage kilometraža posljednje izvedbe
     * @param nextDate sljedeći ciljani datum ili {@code null}
     * @param nextMileage sljedeća ciljana kilometraža ili {@code null}
     * @param remainingKm preostali kilometri ili {@code null}
     * @param remainingDays preostali kalendarski dani ili {@code null}
     * @param remainingRatio relativni dio intervala koji je ostao
     */
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

  /** Sažetak podataka koje DashboardView prikazuje za aktivno vozilo. */
  public static final class Dashboard {
    private final Vehicle vehicle;
    private final BigDecimal total;
    private final long openProblems;
    private final MaintenanceRow nextMaintenance;

    /**
     * Stvara sažetak podataka za aktivno vozilo.
     *
     * @param vehicle aktivno vozilo
     * @param total ukupni evidentirani servisni trošak
     * @param openProblems broj otvorenih problema
     * @param nextMaintenance najbliže sljedeće održavanje, ako postoji
     */
    public Dashboard(Vehicle vehicle, BigDecimal total, long openProblems, MaintenanceRow nextMaintenance) {
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
