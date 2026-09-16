package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;
import java.util.Optional;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface UserRepository {
  Optional<AppUser> byEmail(String email);

  AppUser require(long id);

  AppUser lock(long id);

  void add(AppUser user);
}
