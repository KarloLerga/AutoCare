package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ItemRow;
import hr.unizd.autocare.model.Data.ServiceDetail;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.ServiceRow;
import hr.unizd.autocare.repository.Repositories;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

  /** Poslovna granica za servis, njegove stavke, kilometrazu i rijesene probleme. */
public final class ServiceRecordService {

  private final TransactionRunner transactions;
  private final Clock clock;

  public ServiceRecordService(TransactionRunner transactions, Clock clock) {
    this.transactions = Objects.requireNonNull(transactions);
    this.clock = Objects.requireNonNull(clock);
  }

  /** Sprema cijeli servis u jednoj transakciji; osvjezavanje GUI-ja zasebno je citanje. */
  public long create(long owner, long vehicle, ServiceInput input) {
    return transactions.write(
        repositories -> {
          repositories.users().lock(owner);
          validate(input, false, clock);
          Vehicle ownedVehicle = repositories.vehicles().requireOwned(owner, vehicle);
          ServiceRecord saved = saveInside(repositories, ownedVehicle, input, false, clock);
          return saved.getId();
        });
  }

  /**
   * Koristi VEC OTVORENU Service transakciju. Poziva ga i registracija za pocetnu povijest. Ne
   * otvara novi EntityManager i ne radi zaseban commit.
   */
  static ServiceRecord saveInside(
      Repositories repositories,
      Vehicle vehicle,
      ServiceInput input,
      boolean historical,
      Clock clock) {
    validate(input, historical, clock);

    if (input.getDate().getYear() < vehicle.getProductionYear()) {
      throw AppException.validation("Servis ne moze biti prije godine proizvodnje.");
    }

    ServiceRecord serviceRecord =
        new ServiceRecord(
            vehicle,
            input.getDate(),
            input.getMileage(),
            input.getNote());

    for (ItemInput item : input.getItems()) {
      WorkDefinition work = repositories.catalog().work(item.getWorkId());

      if (work.getCode().startsWith("OTHER_")
          && (input.getNote() == null || input.getNote().isBlank())) {
        throw AppException.validation("Za drugi rad upisite stvarni opis zahvata u napomenu.");
      }

      serviceRecord.addItem(work, item.getActualPrice());
    }

    repositories.services().add(serviceRecord);

    if (input.getMileage() > vehicle.getCurrentMileage()) {
      vehicle.updateMileage(input.getMileage());
    }

    resolveSelectedProblems(repositories, vehicle, serviceRecord, input.getResolvedProblemIds());
    return serviceRecord;
  }

  private static void resolveSelectedProblems(
      Repositories repositories,
      Vehicle vehicle,
      ServiceRecord serviceRecord,
      List<Long> problemIds) {
    Set<Long> selected = new HashSet<>();

    for (Long problemId : problemIds) {
      if (!selected.add(problemId)) {
        throw AppException.validation("Problem je odabran vise puta.");
      }

      Problem problem = repositories.problems().requireOwned(vehicle.getOwner().getId(), problemId);

      if (!Objects.equals(problem.getVehicle().getId(), vehicle.getId())) {
        throw AppException.validation("Problem pripada drugom vozilu.");
      }

      // Promjena managed domenskog objekta dio je iste transakcije kao i servis.
      problem.resolve(serviceRecord);
    }
  }

  /**
   * Provjerava unos; vlasnistvo i aktualno stanje dodatno provjerava use-case.
   *
   * @param input snapshot forme
   * @param historical dopusta nepoznatu cijenu samo u pocetnoj povijesti
   * @param clock izvor danasnjeg datuma
   */
  public static void validate(ServiceInput input, boolean historical, Clock clock) {
    if (input == null || input.getDate() == null || input.getDate().isAfter(LocalDate.now(clock))) {
      throw AppException.validation("Unesite datum koji nije u buducnosti.");
    }

    Checks.mileage(input.getMileage());
    Checks.optional(input.getNote(), 2000, "Napomena");
    if (input.getItems().isEmpty() || input.getItems().size() > 100) {
      throw AppException.validation("Servis treba imati 1 - 100 stavki.");
    }

    Set<Long> selectedWorks = new HashSet<>();

    for (ItemInput item : input.getItems()) {
      if (!selectedWorks.add(item.getWorkId())) {
        throw AppException.validation("Isti rad nije moguce dodati dvaput.");
      }
      Checks.money(item.getActualPrice(), historical);
    }

    if (historical && !input.getResolvedProblemIds().isEmpty()) {
      throw AppException.validation("Pocetna povijest ne rjesava postojece probleme.");
    }
  }

  public List<ServiceRow> page(long owner, long vehicle, int offset) {
    if (offset < 0) {
      throw AppException.validation("Nevaljana stranica.");
    }

    return transactions.read(
        repositories -> {
          repositories.vehicles().requireOwned(owner, vehicle);
          List<ServiceRow> rows = new ArrayList<>();

          for (ServiceRecord serviceRecord :
              repositories.services().page(owner, vehicle, offset, 50)) {
            rows.add(Mapping.service(serviceRecord));
          }
          return List.copyOf(rows);
        });
  }

  public ServiceDetail detail(long owner, long service) {
    return transactions.read(
        repositories -> {
          ServiceRecord serviceRecord = repositories.services().requireOwned(owner, service);
          List<ItemRow> items = new ArrayList<>();

          for (ServiceItem item : serviceRecord.getItems()) {
            WorkDefinition work = item.getWork();
            items.add(new ItemRow(work.getName(), work.getCategory(), item.getActualPrice()));
          }

          return new ServiceDetail(
              Mapping.service(serviceRecord),
              items,
              repositories.problems().resolvedDescriptions(owner, service));
        });
  }

}
