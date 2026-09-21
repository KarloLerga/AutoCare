package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.model.Data.VehicleRow;
import hr.unizd.autocare.repository.CatalogRepository;
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
      UserRepository userRepository = new UserRepository(entityManager);
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
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
      UserRepository userRepository = new UserRepository(entityManager);
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
      UserRepository userRepository = new UserRepository(entityManager);
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
      CatalogRepository catalogRepository = new CatalogRepository(entityManager);
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
      Vehicle vehicle = new VehicleRepository(entityManager).findForOwner(ownerId, vehicleId);
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
      UserRepository userRepository = new UserRepository(entityManager);
      VehicleRepository vehicleRepository = new VehicleRepository(entityManager);
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

}
