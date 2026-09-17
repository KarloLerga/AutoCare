package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.persistence.JpaCatalogRepository;
import hr.unizd.autocare.persistence.JpaProblemRepository;
import hr.unizd.autocare.persistence.JpaServiceRecordRepository;
import hr.unizd.autocare.persistence.JpaUserRepository;
import hr.unizd.autocare.persistence.JpaVehicleRepository;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.ProblemRepository;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import hr.unizd.autocare.repository.UserRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Upravljanje korisnikovim vozilima i aktivnim vozilom. */
public final class VehicleService {
  private final EntityManagerFactory entityManagerFactory;

  public VehicleService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public List<VehicleRow> list(long ownerId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      Long activeVehicleId =
          user.getActiveVehicle() == null ? null : user.getActiveVehicle().getId();
      List<VehicleRow> rows = new ArrayList<>();
      for (Vehicle vehicle : vehicleRepository.findAllForOwner(ownerId)) {
        rows.add(Mapping.vehicle(vehicle, activeVehicleId));
      }
      return rows;
    } finally {
      entityManager.close();
    }
  }

  public VehicleRow active(long ownerId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      if (user.getActiveVehicle() == null) {
        throw new AppException("Korisnik nema aktivno vozilo.");
      }
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, user.getActiveVehicle().getId());
      if (vehicle == null) {
        throw new AppException("Aktivno vozilo nije pronadjeno.");
      }
      return Mapping.vehicle(vehicle, vehicle.getId());
    } finally {
      entityManager.close();
    }
  }

  public long add(long ownerId, VehicleInput input) {
    validate(input);
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      VehicleVariant variant = catalogRepository.findVariant(input.getVariantId());
      if (variant == null) {
        throw new AppException("Odaberite postojecu varijantu vozila.");
      }
      Vehicle vehicle = new Vehicle(user, variant, input.getYear(), input.getMileage());
      vehicleRepository.add(vehicle);
      transaction.commit();
      return vehicle.getId();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public void update(long ownerId, long vehicleId, VehicleInput input) {
    validate(input);
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }

      boolean identityChanged =
          !vehicle.getVariant().getId().equals(input.getVariantId())
              || vehicle.getProductionYear() != input.getYear();
      if (identityChanged && vehicleRepository.hasHistory(vehicleId)) {
        throw new AppException("Vozilu koje vec ima povijest nije moguce promijeniti model.");
      }
      if (identityChanged) {
        VehicleVariant variant = catalogRepository.findVariant(input.getVariantId());
        if (variant == null) {
          throw new AppException("Odaberite postojecu varijantu vozila.");
        }
        vehicle.changeIdentity(variant, input.getYear());
      }
      vehicle.updateMileage(input.getMileage());
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

  public boolean identityEditable(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      return !vehicleRepository.hasHistory(vehicleId);
    } finally {
      entityManager.close();
    }
  }

  public void activate(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      user.activate(vehicle);
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

  public void delete(long ownerId, long vehicleId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      List<Vehicle> vehicles = vehicleRepository.findAllForOwner(ownerId);
      Vehicle vehicle = vehicleRepository.findForOwner(ownerId, vehicleId);
      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }
      if (vehicles.size() <= 1) {
        throw new AppException("Posljednje vozilo nije moguce obrisati.");
      }
      if (vehicle == null) {
        throw new AppException("Vozilo nije pronadjeno.");
      }
      if (user.getActiveVehicle() != null
          && user.getActiveVehicle().getId().equals(vehicleId)) {
        for (Vehicle otherVehicle : vehicles) {
          if (!otherVehicle.getId().equals(vehicleId)) {
            user.activate(otherVehicle);
            break;
          }
        }
      }
      problemRepository.deleteForVehicle(vehicleId);
      serviceRecordRepository.deleteForVehicle(vehicleId);
      vehicleRepository.delete(vehicle);
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

  private void validate(VehicleInput input) {
    if (input == null
        || input.getYear() < 1886
        || input.getYear() > LocalDate.now().getYear()) {
      throw new AppException("Nevaljana godina proizvodnje.");
    }
    Checks.mileage(input.getMileage());
  }
}
