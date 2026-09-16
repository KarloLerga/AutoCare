package hr.unizd.autocare.persistence;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.repository.UserRepository;
import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import java.util.Optional;

/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaUserRepository implements UserRepository {
  private final EntityManager em;

  public JpaUserRepository(EntityManager em) {
    this.em = em;
  }

  public Optional<AppUser> byEmail(String email) {
    return em.createQuery("select u from AppUser u where u.email=:email", AppUser.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst();
  }

  public AppUser require(long id) {
    AppUser u = em.find(AppUser.class, id);
    if (u == null) {
      throw new AppException(AppException.Kind.NOT_FOUND, "Korisnik nije pronadjen.");
    }
    return u;
  }

  public void add(AppUser u) {
    em.persist(u);
  }
}
