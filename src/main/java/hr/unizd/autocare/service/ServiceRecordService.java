package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ServiceItem;
import hr.unizd.autocare.domain.ServiceRecord;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.ItemInput;
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

/**
 * Provodi spremanje i čitanje servisne povijesti vozila.
 *
 * <p>Spremanje servisa je transakcijski use-case koji može obuhvatiti ServiceRecord, njegove
 * stavke, ažuriranje kilometraže vozila i povezivanje otvorenih problema sa servisom koji ih je
 * riješio.
 */
public class ServiceRecordService {
  /** JPA tvornica iz koje se otvara EntityManager za svaki servisni use-case. */
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis servisne povijesti.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public ServiceRecordService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Sprema novi servis i sve povezane promjene u jednoj transakciji.
   *
   * <p>Metoda validira ulaz, provjerava da vozilo pripada korisniku, pretvara odabrane katalog
   * radove u ServiceItem stavke sa stvarnim cijenama, sprema servis, po potrebi povećava trenutačnu
   * kilometražu vozila te odabrane probleme povezuje s tim servisom.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator servisiranog vozila
   * @param input podaci uneseni kroz editor servisa
   * @throws IllegalArgumentException ako vozilo, rad, problem ili uneseni podaci nisu valjani
   */
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

      ServiceRecord serviceRecord = new ServiceRecord(vehicle, input.getDate(), input.getMileage(), input.getNote());

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

  /**
   * Povezuje odabrane otvorene probleme s upravo spremljenim servisom.
   *
   * <p>Za svaki ID provjerava da problem pripada istom korisniku i vozilu prije poziva domenske
   * metode {@code Problem.resolve}.
   *
   * @param problemRepository repository za dohvat problema
   * @param vehicle vozilo na kojem je servis izveden
   * @param serviceRecord servis koji rješava probleme
   * @param problemIds identifikatori problema označenih u editoru
   */
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

  /**
   * Provjerava osnovnu konzistentnost podataka novog servisa prije početka persistence operacija.
   *
   * <p>Provjerava datum, zabranu budućeg datuma i postojanje barem jedne servisne stavke. Ostala
   * pravila provode konstruktori ulaznih i domenskih objekata.
   *
   * @param input podaci servisa
   * @throws IllegalArgumentException ako podaci nisu valjani
   */
  private static void validate(ServiceInput input) {
    if (input == null || input.getDate() == null) {
      throw new IllegalArgumentException("Unesite datum servisa.");
    }
    if (input.getDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Datum servisa ne može biti u budućnosti.");
    }
    if (input.getItems().isEmpty()) {
      throw new IllegalArgumentException("Dodajte barem jednu stavku servisa.");
    }
  }

  /**
   * Dohvaća servisnu povijest vozila i pretvara zapise u retke spremne za prikaz.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @return servisni zapisi pripremljeni za ServicesView
   */
  public List<ServiceRow> list(int ownerId, int vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      List<ServiceRow> rows = new ArrayList<>();
      for (ServiceRecord serviceRecord : serviceRecordRepository.list(ownerId, vehicleId)) {
        rows.add(serviceRow(serviceRecord));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća puni detalj jednog servisa koji pripada korisniku.
   *
   * @param ownerId identifikator vlasnika
   * @param serviceId identifikator servisa
   * @return zaglavlje servisa, njegove stavke i problemi riješeni tim servisom
   * @throws IllegalArgumentException ako servis nije pronađen za tog korisnika
   */
  public ServiceDetail detail(int ownerId, int serviceId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      ServiceRecordRepository serviceRecordRepository = new ServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new ProblemRepository(entityManager);

      ServiceRecord serviceRecord = serviceRecordRepository.findForOwner(ownerId, serviceId);
      if (serviceRecord == null) {
        throw new IllegalArgumentException("Servis nije pronađen.");
      }

      List<ServiceItem> items = new ArrayList<>();
      for (ServiceItem serviceItem : serviceRecord.getItems()) {
        items.add(serviceItem);
      }

      return new ServiceDetail(
          serviceRow(serviceRecord),
          items,
          problemRepository.resolvedDescriptions(ownerId, serviceId));
    } finally {
      entityManager.close();
    }
  }

  /**
   * Pretvara persistentni ServiceRecord u jednostavan prikazni ServiceRow.
   *
   * <p>Sažima nazive radova i računa ukupni stvarni trošak preko domenskog zapisa.
   *
   * @param serviceRecord servisni zapis iz baze
   * @return redak spreman za prikaz
   */
  private static ServiceRow serviceRow(ServiceRecord serviceRecord) {
    String names = "";
    for (ServiceItem serviceItem : serviceRecord.getItems()) {
      if (!names.isEmpty()) {
        names += ", ";
      }
      names += serviceItem.getWork().getName();
    }

    return new ServiceRow(
        serviceRecord.getId(),
        serviceRecord.getServiceDate(),
        serviceRecord.getMileage(),
        names,
        serviceRecord.total(),
        serviceRecord.getNote());
  }
}
