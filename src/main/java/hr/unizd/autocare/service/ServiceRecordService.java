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
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
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

/** Poslovna granica za servis, njegove stavke, kilometražu i riješene probleme. */
public final class ServiceRecordService {
  private final EntityManagerFactory entityManagerFactory;

  public ServiceRecordService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public long create(long ownerId, long vehicleId, ServiceInput input) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);

      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }

      ServiceRecord serviceRecord =
          saveInside(
              catalogRepository,
              serviceRecordRepository,
              problemRepository,
              vehicle,
              input,
              false);

      transaction.commit();
      return serviceRecord.getId();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  static ServiceRecord saveInside(
      CatalogRepository catalogRepository,
      ServiceRecordRepository serviceRecordRepository,
      ProblemRepository problemRepository,
      Vehicle vehicle,
      ServiceInput input,
      boolean historical) {
    validate(input, historical);

    if (input.getDate().getYear() < vehicle.getProductionYear()) {
      throw new AppException("Servis ne može biti prije godine proizvodnje.");
    }

    ServiceRecord serviceRecord =
        new ServiceRecord(vehicle, input.getDate(), input.getMileage(), input.getNote());

    for (ItemInput itemInput : input.getItems()) {
      WorkDefinition work = catalogRepository.findWork(itemInput.getWorkId());
      if (work == null) {
        throw new AppException("Odabrani rad nije pronađen.");
      }

      if (catalogRepository.findRule(vehicle.getVariant().getId(), work.getId()) == null) {
        throw new AppException("Odabrani rad nije dostupan za ovo vozilo.");
      }

      serviceRecord.addItem(work, itemInput.getActualPrice());
    }

    serviceRecordRepository.add(serviceRecord);

    if (input.getMileage() > vehicle.getCurrentMileage()) {
      vehicle.updateMileage(input.getMileage());
    }

    resolveSelectedProblems(problemRepository, vehicle, serviceRecord, input.getResolvedProblemIds());
    return serviceRecord;
  }

  private static void resolveSelectedProblems(
      ProblemRepository problemRepository,
      Vehicle vehicle,
      ServiceRecord serviceRecord,
      List<Long> problemIds) {
    for (Long problemId : problemIds) {
      Problem problem = problemRepository.findForOwner(vehicle.getOwner().getId(), problemId);
      if (problem == null) {
        throw new AppException("Problem nije pronađen.");
      }

      problem.resolve(serviceRecord);
    }
  }

  public static void validate(ServiceInput input, boolean historical) {
    if (input == null || input.getDate() == null) {
      throw new AppException("Unesite datum servisa.");
    }

    if (input.getDate().isAfter(LocalDate.now())) {
      throw new AppException("Datum servisa ne može biti u budućnosti.");
    }

    Checks.mileage(input.getMileage());
    Checks.optional(input.getNote(), 2000, "Napomena");

    if (input.getItems().isEmpty()) {
      throw new AppException("Dodajte barem jednu stavku servisa.");
    }

    for (ItemInput itemInput : input.getItems()) {
      Checks.money(itemInput.getActualPrice(), historical);
    }

    if (historical && !input.getResolvedProblemIds().isEmpty()) {
      throw new AppException("Početna povijest ne rješava postojeće probleme.");
    }
  }

  public List<ServiceRow> list(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);

      if (vehicleRepository.findForOwner(ownerId, vehicleId) == null) {
        throw new AppException("Vozilo nije pronađeno.");
      }

      List<ServiceRow> rows = new ArrayList<>();
      for (ServiceRecord serviceRecord : serviceRecordRepository.list(ownerId, vehicleId)) {
        rows.add(Mapping.service(serviceRecord));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public ServiceDetail detail(long ownerId, long serviceId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);

      ServiceRecord serviceRecord = serviceRecordRepository.findForOwner(ownerId, serviceId);
      if (serviceRecord == null) {
        throw new AppException("Servis nije pronađen.");
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
