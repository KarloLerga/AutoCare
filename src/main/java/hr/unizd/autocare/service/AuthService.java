package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.persistence.JpaUserRepository;
import hr.unizd.autocare.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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

      if (user == null || user.getPassword() == null || !user.getPassword().equals(password)) {
        throw new IllegalArgumentException("E-mail ili lozinka nisu ispravni.");
      }

      return Mapping.account(user);
    } finally {
      entityManager.close();
    }
  }

  public long register(String name, String email, String password) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      UserRepository userRepository = new JpaUserRepository(entityManager);
      AppUser user = new AppUser(name, email, password);

      if (userRepository.findByEmail(user.getEmail()) != null) {
        throw new IllegalArgumentException("E-mail adresa je već registrirana.");
      }

      userRepository.add(user);
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
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }

      return Mapping.account(user);
    } finally {
      entityManager.close();
    }
  }

  public void profile(long ownerId, String name, String email) {
    String cleanEmail = Checks.email(email);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      UserRepository userRepository = new JpaUserRepository(entityManager);
      AppUser user = userRepository.findById(ownerId);

      if (user == null) {
        throw new IllegalArgumentException("Korisnik nije pronađen.");
      }

      AppUser otherUser = userRepository.findByEmail(cleanEmail);

      if (otherUser != null && !otherUser.getId().equals(ownerId)) {
        throw new IllegalArgumentException("E-mail adresa je zauzeta.");
      }

      user.changeProfile(name, cleanEmail);
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
