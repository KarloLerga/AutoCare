package hr.unizd.autocare.repository;
import hr.unizd.autocare.domain.*;
import java.util.*;
/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface UserRepository {
    Optional<User> byEmail(String email);
    User require(long id);
    User lock(long id);
    void add(User user);
}
