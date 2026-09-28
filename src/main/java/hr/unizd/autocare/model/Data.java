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
    /** Identifikator kataloške definicije koju je korisnik odabrao. */
    private final int workId;

    /** Stvarni iznos koji je korisnik platio za odabrani rad. */
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

    /** @return identifikator odabranog kataloškog rada */
    public int getWorkId() {
      return workId;
    }

    /** @return stvarna cijena unesena za stavku */
    public BigDecimal getActualPrice() {
      return actualPrice;
    }
  }

  /**
   * Cjelovit skup vrijednosti koje ServiceEditorDialog predaje ServiceRecordServiceu pri spremanju
   * novog servisa.
   */
  public static final class ServiceInput {
    /** Datum servisa koji će biti spremljen u servisnoj povijesti. */
    private final LocalDate date;

    /** Kilometraža vozila pri servisu. */
    private final int mileage;

    /** Neobavezna napomena uz servis. */
    private final String note;

    /** Radovi i stvarne cijene koje treba pretvoriti u ServiceItem entitete. */
    private final List<ItemInput> items;

    /** Identifikatori otvorenih problema koje korisnik želi povezati s ovim servisom. */
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

    /** @return datum servisa */
    public LocalDate getDate() {
      return date;
    }

    /** @return kilometraža pri servisu */
    public int getMileage() {
      return mileage;
    }

    /** @return napomena ili {@code null} ako nije unesena */
    public String getNote() {
      return note;
    }

    /** @return stavke servisa koje je unio korisnik */
    public List<ItemInput> getItems() {
      return items;
    }

    /** @return identifikatori problema koje ovaj servis treba označiti riješenima */
    public List<Integer> getResolvedProblemIds() {
      return resolvedProblemIds;
    }
  }

  /**
   * Prikazni model jednog retka servisne povijesti s već pripremljenim nazivima radova i ukupnim
   * stvarnim troškom.
   */
  public static final class ServiceRow {
    /** Identifikator servisa koji koriste prikaz i otvaranje njegovih detalja. */
    private final int id;

    /** Datum servisnog zapisa. */
    private final LocalDate date;

    /** Kilometraža zapisana za servis. */
    private final int mileage;

    /** Sažeti tekst naziva svih radova u servisu. */
    private final String names;

    /** Zbroj stvarno plaćenih cijena servisnih stavki. */
    private final BigDecimal total;

    /** Napomena uz servis ili {@code null} ako nije unesena. */
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

    /** @return identifikator servisnog zapisa */
    public int getId() {
      return id;
    }

    /** @return datum servisa */
    public LocalDate getDate() {
      return date;
    }

    /** @return kilometraža pri servisu */
    public int getMileage() {
      return mileage;
    }

    /** @return sažeti popis naziva izvedenih radova */
    public String getNames() {
      return names;
    }

    /** @return stvarni ukupni trošak servisa */
    public BigDecimal getTotal() {
      return total;
    }

    /** @return napomena servisa ili {@code null} ako je nema */
    public String getNote() {
      return note;
    }
  }

  /** Puni prikaz jednog servisa: zaglavlje, servisne stavke i problemi riješeni servisom. */
  public static final class ServiceDetail {
    /** Sažetak servisnog zapisa prikazan kao zaglavlje detalja. */
    private final ServiceRow header;

    /** Pojedinačni radovi i stvarne cijene spremljene u servisu. */
    private final List<ServiceItem> items;

    /** Opisi problema koji su označeni riješenima ovim servisom. */
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

    /** @return sažetak servisa koji čini zaglavlje detaljnog prikaza */
    public ServiceRow getHeader() {
      return header;
    }

    /** @return kopija popisa servisnih stavki za detaljni prikaz */
    public List<ServiceItem> getItems() {
      return items;
    }

    /** @return opisi problema riješenih servisom */
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
    /** Naziv kataloškog rada čiji se interval prati. */
    private final String name;

    /** Datum posljednjeg evidentiranog rada ili {@code null} ako nema vremenske povijesti. */
    private final LocalDate lastDate;

    /** Kilometraža posljednje izvedbe ili {@code null} ako nema kilometarske povijesti. */
    private final Integer lastMileage;

    /** Izračunati sljedeći ciljani datum, ako rad ima vremenski interval. */
    private final LocalDate nextDate;

    /** Izračunata sljedeća ciljana kilometraža, ako rad ima kilometarski interval. */
    private final Integer nextMileage;

    /** Nenegativan broj preostalih kilometara ili {@code null} ako kriterij nije primjenjiv. */
    private final Integer remainingKm;

    /** Nenegativan broj preostalih dana ili {@code null} ako kriterij nije primjenjiv. */
    private final Long remainingDays;

    /** Relativni preostali interval koji služi za određivanje hitnosti rada. */
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

    /** @return naziv rada */
    public String getName() {
      return name;
    }

    /** @return datum posljednje izvedbe ili {@code null} */
    public LocalDate getLastDate() {
      return lastDate;
    }

    /** @return kilometraža posljednje izvedbe ili {@code null} */
    public Integer getLastMileage() {
      return lastMileage;
    }

    /** @return sljedeći ciljani datum ili {@code null} */
    public LocalDate getNextDate() {
      return nextDate;
    }

    /** @return sljedeća ciljana kilometraža ili {@code null} */
    public Integer getNextMileage() {
      return nextMileage;
    }

    /** @return preostali kilometri ili {@code null} */
    public Integer getRemainingKm() {
      return remainingKm;
    }

    /** @return preostali kalendarski dani ili {@code null} */
    public Long getRemainingDays() {
      return remainingDays;
    }

    /** @return relativni dio intervala koji je ostao; negativna vrijednost znači da je prošao */
    public double getRemainingRatio() {
      return remainingRatio;
    }
  }

  /** Sažetak podataka koje DashboardView prikazuje za aktivno vozilo. */
  public static final class Dashboard {
    /** Aktivno vozilo na koje se odnose ostale vrijednosti sažetka. */
    private final Vehicle vehicle;

    /** Zbroj stvarno evidentiranih servisnih cijena tog vozila. */
    private final BigDecimal total;

    /** Broj problema vozila koji još nisu povezani s riješivim servisom. */
    private final long openProblems;

    /** Najbliži izračunati rad održavanja ili {@code null} ako nema takvog rada. */
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

    /** @return vozilo sažetka */
    public Vehicle getVehicle() {
      return vehicle;
    }

    /** @return ukupni stvarno evidentirani servisni trošak */
    public BigDecimal getTotal() {
      return total;
    }

    /** @return broj otvorenih problema */
    public long getOpenProblems() {
      return openProblems;
    }

    /** @return najbliži rad održavanja ili {@code null} ako nije izračunat */
    public MaintenanceRow getNextMaintenance() {
      return nextMaintenance;
    }
  }
}
