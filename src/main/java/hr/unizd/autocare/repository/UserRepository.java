package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje korisnika. */
public class UserRepository {
  private final EntityManager entityManager;

  /**
   * Stvara repozitorij za EntityManager trenutačne operacije.
   *
   * @param entityManager EntityManager kojim upravlja pozivajući servis
   */
  public UserRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Vraća korisnika s adresom e-pošte ili {@code null} ako ne postoji.
   *
   * @param email adresa korisničkog računa
   * @return pronađeni korisnik ili {@code null}
   */
  public AppUser findByEmail(String email) {
    List<AppUser> users = entityManager
            .createQuery("select user from AppUser user where user.email=:email", AppUser.class)
            .setParameter("email", email)
            .setMaxResults(1)
            .getResultList();

    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  /**
   * Dohvaća korisnika prema primarnom ključu.
   *
   * @param id primarni ključ korisnika
   * @return korisnik ili {@code null} ako nije pronađen
   */
  public AppUser findById(int id) {
    return entityManager.find(AppUser.class, id);
  }

  /**
   * Predaje novi korisnički račun persistence kontekstu.
   *
   * @param user račun koji treba spremiti
   */
  public void add(AppUser user) {
    entityManager.persist(user);
  }
}
