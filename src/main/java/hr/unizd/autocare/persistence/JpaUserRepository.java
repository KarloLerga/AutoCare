package hr.unizd.autocare.persistence;
import jakarta.persistence.*;
import hr.unizd.autocare.domain.*;
import hr.unizd.autocare.repository.*;
import hr.unizd.autocare.service.AppException;
import java.util.*;
/** JPA upiti koriste vezane parametre i postojeci EntityManager. */
public final class JpaUserRepository implements UserRepository {
    private final EntityManager em;
    public JpaUserRepository(EntityManager em) {
        this.em=em;
    }
    public Optional<User> byEmail(String email) {
        return em.createQuery("select u from User u where u.email=:email", User.class).setParameter("email", email).getResultStream().findFirst();
    }
    public User require(long id) {
        User u=em.find(User.class, id);
        if(u==null)throw new AppException(AppException.Kind.NOT_FOUND, "Korisnik nije pronadjen.");
        return u;
    }
    public User lock(long id) {
        User u=em.find(User.class, id, LockModeType.PESSIMISTIC_WRITE);
        if(u==null)throw new AppException(AppException.Kind.AUTHENTICATION, "Ponovno se prijavite.");
        return u;
    }
    public void add(User u) {
        em.persist(u);
    }
}
