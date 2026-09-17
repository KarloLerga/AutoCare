package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
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
import java.util.List;

/** Registracija, prijava i profil korisnika. */
public final class AuthService {
  private final EntityManagerFactory entityManagerFactory;

  public AuthService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public Account login(String email, String password) {
    String cleanEmail = Checks.email(email);
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      UserRepository userRepository = new JpaUserRepository(entityManager);
      AppUser user = userRepository.findByEmail(cleanEmail);

      if (user == null
          || user.getPassword() == null
          || !user.getPassword().equals(password)) {
        throw new AppException("E-mail ili lozinka nisu ispravni.");
      }

      return Mapping.account(user);
    } finally {
      entityManager.close();
    }
  }

  public long register(
      String name,
      String email,
      String password,
      VehicleInput vehicleInput,
      List<ServiceInput> history) {
    String cleanName = Checks.text(name, 100, "Ime");
    String cleanEmail = Checks.email(email);
    String cleanPassword = Checks.password(password);

    if (vehicleInput == null
        || vehicleInput.getYear() < 1886
        || vehicleInput.getYear() > LocalDate.now().getYear()) {
      throw new AppException("Godina proizvodnje nije valjana.");
    }

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      UserRepository userRepository = new JpaUserRepository(entityManager);
      VehicleRepository vehicleRepository = new JpaVehicleRepository(entityManager);
      CatalogRepository catalogRepository = new JpaCatalogRepository(entityManager);
      ServiceRecordRepository serviceRecordRepository =
          new JpaServiceRecordRepository(entityManager);
      ProblemRepository problemRepository = new JpaProblemRepository(entityManager);

      if (userRepository.findByEmail(cleanEmail) != null) {
        throw new AppException("E-mail adresa je vec registrirana.");
      }

      AppUser user = new AppUser(cleanName, cleanEmail, cleanPassword);
      userRepository.add(user);

      VehicleVariant variant = catalogRepository.findVariant(vehicleInput.getVariantId());

      if (variant == null) {
        throw new AppException("Odaberite postojeću varijantu vozila.");
      }

      Vehicle vehicle =
          new Vehicle(user, variant, vehicleInput.getYear(), vehicleInput.getMileage());
      vehicleRepository.add(vehicle);
      user.activate(vehicle);

      for (ServiceInput serviceInput : history) {
        if (serviceInput.getMileage() > vehicleInput.getMileage()) {
          throw new AppException("Pocetna povijest ne moze imati vecu kilometrazu od trenutne.");
        }

        ServiceRecordService.saveInside(
            catalogRepository,
            serviceRecordRepository,
            problemRepository,
            vehicle,
            serviceInput,
            true);
      }

      transaction.commit();
      return user.getId();
    } catch (RuntimeException exception) {
      if (transaction.isActive()) {
        transaction.rollback();
      }

      throw exception;
    } finally {
      entityManager.close();
    }
  }

  public Account account(long ownerId) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      UserRepository userRepository = new JpaUserRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);

      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }

      return Mapping.account(user);
    } finally {
      entityManager.close();
    }
  }

  public void profile(long ownerId, String name, String email) {
    String cleanName = Checks.text(name, 100, "Ime");
    String cleanEmail = Checks.email(email);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      UserRepository userRepository = new JpaUserRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);

      if (user == null) {
        throw new AppException("Korisnik nije pronadjen.");
      }

      AppUser otherUser = userRepository.findByEmail(cleanEmail);

      if (otherUser != null && !otherUser.getId().equals(ownerId)) {
        throw new AppException("E-mail adresa je zauzeta.");
      }

      user.changeProfile(cleanName, cleanEmail);
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
