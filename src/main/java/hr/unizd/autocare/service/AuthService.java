package hr.unizd.autocare.service;

import hr.unizd.autocare.domain.AppUser;
import hr.unizd.autocare.domain.Checks;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.model.Data.Account;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.service.TransactionRunner;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Registracija, prijava i profil korisnika. */
public final class AuthService {

    private final TransactionRunner transactions;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public AuthService(
            TransactionRunner transactions,
            PasswordHasher passwordHasher,
            Clock clock) {

        this.transactions = transactions;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    public Account login(
            String email,
            char[] password) {

        String cleanEmail = Checks.email(email);

        return transactions.read(repositories -> {
            AppUser user =
                    repositories.users()
                            .byEmail(cleanEmail)
                            .orElseThrow(
                                    () -> new AppException(
                                            AppException.Kind.AUTHENTICATION,
                                            "E-mail ili lozinka nisu ispravni."));

            if (!passwordHasher.verify(
                    password,
                    user.getPasswordHash())) {

                throw new AppException(
                        AppException.Kind.AUTHENTICATION,
                        "E-mail ili lozinka nisu ispravni.");
            }

            return Mapping.account(user);
        });
    }

    public long register(
            String name,
            String email,
            char[] password,
            VehicleInput vehicleInput,
            List<ServiceInput> history) {

        String cleanName =
                Checks.text(name, 100, "Ime");

        String cleanEmail =
                Checks.email(email);

        Checks.password(password);

        String passwordHash =
                passwordHasher.hash(password);

        return transactions.write(repositories -> {
            if (repositories.users()
                    .byEmail(cleanEmail)
                    .isPresent()) {

                throw AppException.conflict(
                        "E-mail adresa je vec registrirana.");
            }

            if (vehicleInput.getYear()
                    > LocalDate.now(clock).getYear()) {

                throw AppException.validation(
                        "Godina proizvodnje nije valjana.");
            }

            AppUser user =
                    new AppUser(
                            cleanName,
                            cleanEmail,
                            passwordHash);

            repositories.users().add(user);

            VehicleVariant variant =
                    repositories.catalog()
                            .variant(vehicleInput.getVariantId());

            Vehicle vehicle =
                    new Vehicle(
                            user,
                            variant,
                            vehicleInput.getYear(),
                            vehicleInput.getMileage());

            repositories.vehicles().add(vehicle);
            user.activate(vehicle);

            for (ServiceInput serviceInput : history) {
                if (serviceInput.getMileage()
                        > vehicleInput.getMileage()) {

                    throw AppException.validation(
                            "Pocetna povijest ne moze imati vecu "
                                    + "kilometrazu od trenutne.");
                }

                ServiceRecordService.saveInside(
                        repositories,
                        vehicle,
                        serviceInput,
                        true,
                        clock);
            }

            return user.getId();
        });
    }

    public Account account(long ownerId) {
        return transactions.read(
                repositories -> Mapping.account(
                        repositories.users()
                                .require(ownerId)));
    }

    public void profile(
            long ownerId,
            String name,
            String email,
            char[] currentPassword,
            char[] newPassword) {

        String cleanName =
                Checks.text(name, 100, "Ime");

        String cleanEmail =
                Checks.email(email);

        transactions.write(repositories -> {
            AppUser user =
                    repositories.users().require(ownerId);

            repositories.users()
                    .byEmail(cleanEmail)
                    .ifPresent(other -> {
                        if (!other.getId().equals(ownerId)) {
                            throw AppException.conflict(
                                    "E-mail adresa je zauzeta.");
                        }
                    });

            boolean changesPassword =
                    newPassword != null
                            && newPassword.length > 0;

            if (changesPassword) {
                if (!passwordHasher.verify(
                        currentPassword,
                        user.getPasswordHash())) {

                    throw new AppException(
                            AppException.Kind.AUTHENTICATION,
                            "Trenutna lozinka nije ispravna.");
                }

                Checks.password(newPassword);

                user.changePasswordHash(
                        passwordHasher.hash(newPassword));
            }

            user.changeProfile(
                    cleanName,
                    cleanEmail);

            return null;
        });
    }
}
