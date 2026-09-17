package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaUserRepository implements UserRepository {
  private final EntityManager entityManager;

  public JpaUserRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
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

  @Override
  public AppUser findById(long id) {
    return entityManager.find(AppUser.class, id);
  }

  @Override
  public void add(AppUser user) {
    entityManager.persist(user);
  }
}
