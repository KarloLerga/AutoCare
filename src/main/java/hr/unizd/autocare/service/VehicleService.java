package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
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
import java.util.ArrayList;
import java.util.List;

/** Upravljanje vozilima; identitet se stvara jednom, a kasnije se mijenja samo kilometraža. */
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
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      Long activeId = null;
      if (user.getActiveVehicle() != null) {
        activeId = user.getActiveVehicle().getId();
      }
      List<VehicleRow> rows = new ArrayList<>();
      for (Vehicle vehicle : vehicleRepository.findAllForOwner(ownerId)) {
        rows.add(Mapping.vehicle(vehicle, activeId));
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
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      if (user.getActiveVehicle() == null) {
        return null;
      }

      Vehicle vehicle = user.getActiveVehicle();
      return Mapping.vehicle(vehicle, vehicle.getId());
    } finally {
      entityManager.close();
    }
  }

  public void add(long ownerId, VehicleInput input) {
    if (input == null) {
      throw new IllegalArgumentException("Vozilo je obavezno.");
    }

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      VehicleVariant variant = catalogRepository.findVariant(input.getVariantId());
      if (variant == null) {
        throw new IllegalArgumentException("Odaberite postojeću varijantu vozila.");
      }
      Vehicle vehicle = new Vehicle(user, variant, input.getYear(), input.getMileage());
      vehicleRepository.add(vehicle);
      if (user.getActiveVehicle() == null) {
        user.activate(vehicle);
      }
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

  public void updateMileage(long ownerId, long vehicleId, int mileage) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      Vehicle vehicle = new JpaVehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      vehicle.updateMileage(mileage);
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
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
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
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      if (vehicles.size() <= 1) {
        throw new IllegalArgumentException("Posljednje vozilo nije moguće obrisati.");
      }
      if (vehicle == null) {
        throw new IllegalArgumentException("Vozilo nije pronađeno.");
      }
      if (user.getActiveVehicle() != null
          && user.getActiveVehicle().getId().equals(vehicleId)) {
        for (Vehicle other : vehicles) {
          if (!other.getId().equals(vehicleId)) {
            user.activate(other);
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
}
