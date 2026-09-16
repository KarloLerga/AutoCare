package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.Credentials;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Hashiranje izvan transakcijskog locka; onboarding se sprema tek na kraju. */
public final class AuthService {
  private final TransactionRunner tx;
  private final PasswordHasher hasher;
  private final Clock clock;

  public AuthService(TransactionRunner tx, PasswordHasher hasher, Clock clock) {
    this.tx = tx;
    this.hasher = hasher;
    this.clock = clock;
  }

  public Account login(String email, char[] password) {
    String canonical = Checks.email(email);
    Credentials c =
        tx.read(
            r ->
                r.users()
                    .byEmail(canonical)
                    .map(u -> new Credentials(u.getId(), u.getVersion(), u.getPasswordHash()))
                    .orElse(null));
    if (c == null || !hasher.verify(password, c.getHash())) {
      throw new AppException(AppException.Kind.AUTHENTICATION, "E-mail ili lozinka nisu ispravni.");
    }
    return tx.read(
        r -> {
          AppUser u = r.users().require(c.getId());
          if (!u.getPasswordHash().equals(c.getHash())) {
            throw AppException.conflict("Prijavite se ponovno.");
          }
          return Mapping.account(u);
        });
  }

  /**
   * Registrira racun zajedno s obveznim prvim vozilom i opcionalnom povijescu. Svi persistentni
   * objekti pripadaju jednoj transakciji; hashiranje joj prethodi.
   *
   * @param name prikazno ime
   * @param email e-mail koji se kanonizira i mora biti jedinstven
   * @param password password buffer; pozivatelj ga brise nakon rada
   * @param vehicle prvo vozilo
   * @param history poznati stari servisi, moze biti prazno
   * @return ID novog korisnika
   */
  public long register(
      String name,
      String email,
      char[] password,
      VehicleInput vehicle,
      List<ServiceInput> history) {
    String canonical = Checks.email(email);
    Checks.text(name, 100, "Ime");
    Checks.password(password);
    if (history.size() > 100) {
      throw AppException.validation("Najvise 100 pocetnih servisa.");
    }
    String hash = hasher.hash(password);
    return tx.write(
        r -> {
          if (r.users().byEmail(canonical).isPresent()) {
            throw AppException.conflict("E-mail adresa vec je registrirana.");
          }
          AppUser u = new AppUser(name, canonical, hash);
          r.users().add(u);
          VehicleVariant variant = r.catalog().variant(vehicle.getVariantId());
          if (vehicle.getYear() > LocalDate.now(clock).getYear()) {
            throw AppException.validation("Godina proizvodnje nije valjana.");
          }
          Vehicle v = new Vehicle(u, variant, vehicle.getYear(), vehicle.getMileage());
          r.vehicles().add(v);
          u.activate(v);
          for (ServiceInput item : history) {
            if (item.getMileage() > vehicle.getMileage()) {
              throw AppException.validation(
                  "Pocetna povijest ne moze imati vecu kilometrazu od trenutne.");
            }
            ServiceRecordService.saveInside(r, v, item, true, clock);
          }
          return u.getId();
        });
  }

  public Account account(long owner) {
    return tx.read(r -> Mapping.account(r.users().require(owner)));
  }

  public void profile(
      long owner, long expected, String name, String email, char[] current, char[] next) {
    String canonical = Checks.email(email);
    Account old = account(owner);
    boolean sensitive = !old.getEmail().equals(canonical) || (next != null && next.length > 0);
    Credentials credentials =
        tx.read(
            r -> {
              AppUser u = r.users().require(owner);
              return new Credentials(u.getId(), u.getVersion(), u.getPasswordHash());
            });
    if (sensitive && !hasher.verify(current, credentials.getHash())) {
      throw new AppException(AppException.Kind.AUTHENTICATION, "Trenutna lozinka nije ispravna.");
    }
    String hash = next != null && next.length > 0 ? hasher.hash(next) : null;
    tx.write(
        r -> {
          AppUser u = r.users().lock(owner);
          Mapping.version(u.getVersion(), expected);
          if (!u.getPasswordHash().equals(credentials.getHash())) {
            throw AppException.conflict("Lozinka je promijenjena u drugoj sesiji.");
          }
          r.users()
              .byEmail(canonical)
              .ifPresent(
                  other -> {
                    if (!other.getId().equals(owner)) {
                      throw AppException.conflict("E-mail je zauzet.");
                    }
                  });
          u.changeProfile(name, canonical);
          if (hash != null) {
            u.changePasswordHash(hash);
          }
          return null;
        });
  }
}
