package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Repository za dohvat i spremanje persistentnih korisničkih računa.
 *
 * <p>Klasa radi s EntityManagerom koji joj predaje Service, pa ne otvara vlastite EntityManagere
 * ni transakcije. JPQL dohvat po e-mailu vraća najviše jedan račun, a dohvat po ID-u koristi JPA
 * {@code find}.
 */
public class UserRepository {
  /** EntityManager kojim Service izvodi dohvat i spremanje računa u trenutačnoj operaciji. */
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
   * Traži najviše jednog korisnika prema e-mail adresi.
   *
   * <p>Upit ograničava broj rezultata na jedan; prazna lista se pretvara u {@code null}.
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
   * Dohvaća korisnika prema primarnom ključu kroz JPA {@code find}.
   *
   * @param id primarni ključ korisnika
   * @return korisnik ili {@code null} ako nije pronađen
   */
  public AppUser findById(int id) {
    return entityManager.find(AppUser.class, id);
  }

  /**
   * Predaje novi korisnički račun persistence kontekstu trenutačnog EntityManagera.
   *
   * <p>Repository ne pokreće transakciju; nju otvara Service koji koordinira registraciju.
   *
   * @param user račun koji treba spremiti
   */
  public void add(AppUser user) {
    entityManager.persist(user);
  }
}
