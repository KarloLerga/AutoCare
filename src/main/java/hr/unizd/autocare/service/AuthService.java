package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Provodi poslovne postupke prijave i registracije korisnika.
 *
 * <p>Service otvara EntityManager za pojedinu operaciju, koristi UserRepository i primjenjuje
 * validacijska pravila prije nego rezultat vrati Controlleru.
 */
public class AuthService {
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Stvara servis za autentikaciju.
   *
   * @param entityManagerFactory zajednička JPA tvornica iz koje se otvaraju EntityManageri
   */
  public AuthService(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Provjerava vjerodajnice i vraća identifikator pronađenog korisnika.
   *
   * @param email unesena e-mail adresa
   * @param password unesena lozinka
   * @return identifikator prijavljenog korisnika
   * @throws IllegalArgumentException ako unos nije valjan ili korisnik/lozinka ne odgovaraju
   */
  public int login(String email, String password) {
    String cleanEmail = Checks.email(email);
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      UserRepository userRepository = new UserRepository(entityManager);
      AppUser user = userRepository.findByEmail(cleanEmail);

      if (user == null || user.getPassword() == null || !user.getPassword().equals(password)) {
        throw new IllegalArgumentException("E-mail ili lozinka nisu ispravni.");
      }

      return user.getId();
    } finally {
      entityManager.close();
    }
  }

  /**
   * Validira podatke novog računa i sprema korisnika u jednoj transakciji.
   *
   * @param name ime korisnika
   * @param email e-mail adresa koja mora biti jedinstvena
   * @param password lozinka koja mora zadovoljiti osnovno pravilo duljine
   * @return identifikator novostvorenog korisnika
   * @throws IllegalArgumentException ako su podaci nevaljani ili e-mail već postoji
   */
  public int register(String name, String email, String password) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      transaction.begin();

      UserRepository userRepository = new UserRepository(entityManager);
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
}
