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
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Spremanje i čitanje servisne povijesti. */
public final class ServiceRecordService {
  private final EntityManagerFactory entityManagerFactory;

  public ServiceRecordService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public void create(int ownerId, int vehicleId, ServiceInput input) {
    validate(input);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      CatalogRepository catalogRepository = new CatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new ProblemRepository(entityManager);

      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      if (input.getDate().getYear() < vehicle.getProductionYear()) {
        throw new IllegalArgumentException("Servis ne može biti prije godine proizvodnje.");
      }

      ServiceRecord serviceRecord =
          new ServiceRecord(vehicle, input.getDate(), input.getMileage(), input.getNote());

      for (ItemInput itemInput : input.getItems()) {
        WorkDefinition work = catalogRepository.findWork(itemInput.getWorkId());
        if (work == null) {
          throw new IllegalArgumentException("Odabrani rad nije pronađen.");
        }
        serviceRecord.addItem(work, itemInput.getActualPrice());
      }

      serviceRecordRepository.add(serviceRecord);

      if (input.getMileage() > vehicle.getCurrentMileage()) {
        vehicle.updateMileage(input.getMileage());
      }

      resolveSelectedProblems(problemRepository, vehicle, serviceRecord, input.getResolvedProblemIds());
      transaction.commit();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  private static void resolveSelectedProblems(
      ProblemRepository problemRepository,
      Vehicle vehicle,
      ServiceRecord serviceRecord,
      List<Integer> problemIds) {
    for (Integer problemId : problemIds) {
      Problem problem = problemRepository.findForOwner(vehicle.getOwner().getId(), problemId);
      if (problem == null) {
        throw new IllegalArgumentException("Problem nije pronađen.");
      }
      problem.resolve(serviceRecord);
    }
  }

  private static void validate(ServiceInput input) {
    if (input == null || input.getDate() == null) {
      throw new IllegalArgumentException("Unesite datum servisa.");
    }
    if (input.getDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Datum servisa ne može biti u budućnosti.");
    }

    Checks.mileage(input.getMileage());
    Checks.optional(input.getNote(), 2000, "Napomena");

    if (input.getItems().isEmpty()) {
      throw new IllegalArgumentException("Dodajte barem jednu stavku servisa.");
    }

    for (ItemInput itemInput : input.getItems()) {
      Checks.money(itemInput.getActualPrice());
    }
  }

  public List<ServiceRow> list(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      List<ServiceRow> rows = new ArrayList<>();
      for (ServiceRecord serviceRecord : serviceRecordRepository.list(ownerId, vehicleId)) {
        rows.add(Mapping.service(serviceRecord));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public ServiceDetail detail(int ownerId, int serviceId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new ProblemRepository(entityManager);

      ServiceRecord serviceRecord = serviceRecordRepository.findForOwner(ownerId, serviceId);
      if (serviceRecord == null) {
        throw new IllegalArgumentException("Servis nije pronađen.");
      }

      List<ItemRow> items = new ArrayList<>();
      for (ServiceItem serviceItem : serviceRecord.getItems()) {
        WorkDefinition work = serviceItem.getWork();
        items.add(new ItemRow(work.getName(), serviceItem.getActualPrice()));
      }

      return new ServiceDetail(
          Mapping.service(serviceRecord),
          items,
          problemRepository.resolvedDescriptions(ownerId, serviceId));
    } finally {
      entityManager.close();
    }
  }
}
