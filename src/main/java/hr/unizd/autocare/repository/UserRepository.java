package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA dohvat i spremanje korisnika. */
public final class UserRepository {
  private final EntityManager entityManager;

  public UserRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public AppUser findByEmail(String email) {
    List<AppUser> users =
        entityManager
            .createQuery("select user from AppUser user where user.email=:email", AppUser.class)
            .setParameter("email", email)
            .setMaxResults(1)
            .getResultList();

    if (users.isEmpty()) {
      return null;
    }
    return users.get(0);
  }

  public AppUser findById(long id) {
    return entityManager.find(AppUser.class, id);
  }

  public void add(AppUser user) {
    entityManager.persist(user);
  }
}
