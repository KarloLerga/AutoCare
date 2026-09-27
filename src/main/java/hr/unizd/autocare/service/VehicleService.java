package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.repository.CatalogRepository;
import hr.unizd.autocare.repository.UserRepository;
import hr.unizd.autocare.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/** Provodi use-caseove vezane uz korisnikova vozila i aktivno vozilo. */
public class VehicleService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis vozila.
   *
   * @param entityManagerFactory zajednička JPA tvornica
   */
  public VehicleService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Dohvaća sva vozila koja pripadaju korisniku.
   *
   * @param ownerId identifikator vlasnika
   * @return vozila vlasnika
   */
  public List<Vehicle> list(int ownerId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return new VehicleRepository(entityManager).findAllForOwner(ownerId);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dohvaća trenutačno aktivno vozilo korisnika.
   *
   * <p>Metoda vraća {@code null} ako korisnik još nema postavljeno aktivno vozilo.
   *
   * @param ownerId identifikator vlasnika
   * @return aktivno vozilo ili {@code null} ako nije odabrano
   * @throws IllegalArgumentException ako korisnik ne postoji
   */
  public Vehicle active(int ownerId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      UserRepository userRepository = new UserRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);
      if (user == null) {
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }
      return user.getActiveVehicle();
    } finally {
      entityManager.close();
    }
  }

  /**
   * Dodaje novo vozilo korisniku na temelju odabrane kataloške varijante.
   *
   * <p>Provjerava postojanje korisnika i varijante, valjanost godine za odabranu varijantu te
   * kilometražu. Novo vozilo sprema se u transakciji.
   *
   * @param ownerId identifikator vlasnika
   * @param variantId identifikator kataloške varijante
   * @param year godina proizvodnje konkretnog vozila
   * @param mileage trenutačna kilometraža
   * @throws IllegalArgumentException ako korisnik ili varijanta ne postoje ili podaci nisu valjani
   */
  public void add(int ownerId, int variantId, int year, int mileage) {
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

      VehicleVariant variant = catalogRepository.findVariant(variantId);
      if (variant == null) {
        throw new IllegalArgumentException("Odaberite postojeću varijantu vozila.");
      }

      Vehicle vehicle = new Vehicle(user, variant, year, mileage);
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

  /**
   * Mijenja kilometražu vozila koje pripada zadanom korisniku.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @param mileage nova kilometraža
   * @throws IllegalArgumentException ako vozilo nije pronađeno ili kilometraža nije dopuštena
   */
  public void updateMileage(int ownerId, int vehicleId, int mileage) {
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

  /**
   * Postavlja jedno od korisnikovih vozila kao aktivno vozilo.
   *
   * @param ownerId identifikator vlasnika
   * @param vehicleId identifikator vozila
   * @throws IllegalArgumentException ako vozilo ne pripada korisniku
   */
  public void activate(int ownerId, int vehicleId) {
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
