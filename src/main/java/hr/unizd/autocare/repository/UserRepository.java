package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.AppUser;

/** Dohvat i spremanje korisnika. */
public interface UserRepository {
  AppUser findByEmail(String email);

  AppUser findById(long id);

  void add(AppUser user);
}
