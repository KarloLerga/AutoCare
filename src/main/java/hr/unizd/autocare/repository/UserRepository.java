package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Repository za persistence operacije nad korisničkim računima.
 *
 * <p>Klasa radi s EntityManagerom koji joj predaje Service, pa ne otvara vlastite transakcije.
 */
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
   * Traži korisnika prema e-mail adresi.
   *
   * @param email e-mail adresa
   * @return pronađeni korisnik ili {@code null} ako račun ne postoji
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
