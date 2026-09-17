package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface UserRepository {
  AppUser findByEmail(String email);

  AppUser findById(long id);

  void add(AppUser user);
}
