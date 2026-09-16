package hr.unizd.autocare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import hr.unizd.autocare.domain.Vehicle;
import hr.unizd.autocare.domain.VehicleVariant;
import hr.unizd.autocare.domain.WorkCategory;
import hr.unizd.autocare.domain.WorkDefinition;
import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.model.Data.VehicleInput;
import hr.unizd.autocare.persistence.JpaTransactionRunner;
import hr.unizd.autocare.service.AppException;
import hr.unizd.autocare.service.AuthService;
import hr.unizd.autocare.service.PasswordHasher;
import hr.unizd.autocare.service.ServiceRecordService;
import hr.unizd.autocare.service.TransactionRunner;
import hr.unizd.autocare.service.VehicleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * Stvarna provjera transakcija na izoliranom SQL Serveru; pokrece se profilom sqlserver-it. Ne
 * uvozi development seed. Sve fixture vrijednosti izmisljene su samo za ovaj test. Zavrsni nazivi
 * entiteta/JPQL-a prate cleanup (AppUser, serviceDate, productionYear).
 */
class SqlServerIT {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void serviceSuccessTwoProblemsOwnershipAndHistoricalMileage() {
    try (EntityManagerFactory factory = SqlTestDatabase.open();
        Fixture fixture = new Fixture(factory)) {
      long owner = fixture.register();
      long vehicle = fixture.vehicles.active(owner).getId();
      long firstProblem = fixture.addProblem(vehicle, "first");
      long secondProblem = fixture.addProblem(vehicle, "second");
      long unselectedProblem = fixture.addProblem(vehicle, "unselected");
      ServiceInput input = fixture.service(120_000, List.of(firstProblem, secondProblem));

      long saved = fixture.services.create(owner, vehicle, input);
      fixture.assertProblem(firstProblem, ProblemStatus.RESOLVED, saved);
      fixture.assertProblem(secondProblem, ProblemStatus.RESOLVED, saved);
      fixture.assertProblem(unselectedProblem, ProblemStatus.OPEN, null);
      assertEquals(120_000, fixture.vehicles.active(owner).getMileage());
      assertEquals(
          new BigDecimal("63.47"),
          fixture.services.detail(owner, saved).getHeader().getTotal().getKnownTotal());

      // Svaki poziv je novi studentski zapis; nema request-key idempotency sloja.
      ServiceInput second = fixture.service(120_000, List.of());
      assertNotEquals(saved, fixture.services.create(owner, vehicle, second));
      assertEquals(2, fixture.services.list(owner, vehicle).size());

      ServiceInput changed = fixture.service(120_001, List.of(firstProblem, secondProblem));
      assertThrows(AppException.class, () -> fixture.services.create(owner, vehicle, changed));
      assertThrows(AppException.class, () -> fixture.vehicles.delete(owner, vehicle));

      long otherOwner = fixture.register();
      assertThrows(AppException.class, () -> fixture.services.detail(otherOwner, saved));
      assertThrows(
          AppException.class,
          () ->
              fixture.services.create(
                  otherOwner,
                  vehicle,
                  fixture.service(130_000, List.of())));

      ServiceInput older =
          new ServiceInput(
              LocalDate.of(2020, 3, 1),
              80_000,
              "Old invoice",
              List.of(new ItemInput(fixture.oilWork, new BigDecimal("50.00"))),
              List.of());
      fixture.services.create(owner, vehicle, older);
      assertEquals(120_000, fixture.vehicles.active(owner).getMileage());
    }
  }

  @Test
  void failureAfterOneProblemChangesRollsBackServiceItemsMileageAndProblem() {
    try (EntityManagerFactory factory = SqlTestDatabase.open();
        Fixture fixture = new Fixture(factory)) {
      long owner = fixture.register();
      long vehicle = fixture.vehicles.active(owner).getId();
      long firstProblem = fixture.addProblem(vehicle, "must remain open");
      ServiceInput bad = fixture.service(120_000, List.of(firstProblem, Long.MAX_VALUE));

      // Prvi Problem vec je promijenjen kad dohvat drugog namjerno ne uspije.
      assertThrows(AppException.class, () -> fixture.services.create(owner, vehicle, bad));
      assertEquals(100_000, fixture.vehicles.active(owner).getMileage());
      assertEquals(0, fixture.services.list(owner, vehicle).size());
      fixture.assertProblem(firstProblem, ProblemStatus.OPEN, null);
      fixture.inTransaction(
          entityManager -> {
            assertEquals(
                0L,
                entityManager
                    .createQuery(
                        "select count(i) from ServiceItem i where"
                            + " i.serviceRecord.vehicle.id=:vehicle",
                        Long.class)
                    .setParameter("vehicle", vehicle)
                    .getSingleResult());
            return null;
          });
    }
  }

  @Test
  void failedLaterOnboardingHistoryRollsBackTheEntireRegistration() {
    try (EntityManagerFactory factory = SqlTestDatabase.open();
        Fixture fixture = new Fixture(factory)) {
      String email = fixture.newEmail();
      ServiceInput good =
          new ServiceInput(
              LocalDate.of(2020, 1, 1),
              80_000,
              "Known history",
              List.of(new ItemInput(fixture.oilWork, null)),
              List.of());
      // OTHER_ bez obavezne napomene: provjera aktualnog lokalnog pravila.
      ServiceInput bad =
          new ServiceInput(
              LocalDate.of(2021, 1, 1),
              90_000,
              null,
              List.of(new ItemInput(fixture.otherWork, null)),
              List.of());
      char[] password = "test-only-password-buffer".toCharArray();
      try {
        assertThrows(
            AppException.class,
            () ->
                fixture.auth.register(
                    "Rollback test",
                    email,
                    password,
                    new VehicleInput(fixture.variant, 2017, 100_000),
                    List.of(good, bad)));
      } finally {
        Arrays.fill(password, '\0');
      }

      // Novo citanje iz NOVOG EntityManagera, ne provjera starih Java objekata.
      fixture.inTransaction(
          entityManager -> {
            assertEquals(
                0L,
                entityManager
                    .createQuery("select count(u) from AppUser u where u.email=:email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult());
            assertEquals(
                0L,
                entityManager
                    .createQuery(
                        "select count(v) from Vehicle v where v.variant.id=:variant", Long.class)
                    .setParameter("variant", fixture.variant)
                    .getSingleResult());
            assertEquals(
                0L,
                entityManager
                    .createQuery(
                        "select count(s) from ServiceRecord s where s.vehicle.variant.id=:variant",
                        Long.class)
                    .setParameter("variant", fixture.variant)
                    .getSingleResult());
            return null;
          });
    }
  }

  /** Mali privatni testni skup: jedna varijanta i tri rada, bez velikog CSV-a. */
  private static final class Fixture implements AutoCloseable {
    private final EntityManagerFactory factory;
    private final List<String> emails = new ArrayList<>();
    private final AuthService auth;
    private final VehicleService vehicles;
    private final ServiceRecordService services;
    private final long variant;
    private final long oilWork;
    private final long repairWork;
    private final long otherWork;

    Fixture(EntityManagerFactory factory) {
      this.factory = factory;
      TransactionRunner transactions = new JpaTransactionRunner(factory);
      auth = new AuthService(transactions, new PasswordHasher(), CLOCK);
      vehicles = new VehicleService(transactions, CLOCK);
      services = new ServiceRecordService(transactions, CLOCK);
      String prefix = "review-it-" + UUID.randomUUID();
      long[] ids =
          inTransaction(
              entityManager -> {
                VehicleVariant variantEntity =
                    new VehicleVariant(
                        prefix,
                        "Test",
                        "Synthetic",
                        "Test generation",
                        "Test motor",
                        2010,
                        2026,
                        "Petrol");
                WorkDefinition oil =
                    new WorkDefinition(
                        prefix + "-oil", "Test oil", WorkCategory.MAINTENANCE, null, null);
                WorkDefinition repair =
                    new WorkDefinition(
                        prefix + "-repair", "Test repair", WorkCategory.REPAIR, null, null);
                WorkDefinition other =
                    new WorkDefinition(
                        "OTHER_" + prefix, "Test other", WorkCategory.REPAIR, null, null);
                entityManager.persist(variantEntity);
                entityManager.persist(oil);
                entityManager.persist(repair);
                entityManager.persist(other);
                entityManager.flush();
                return new long[] {
                  variantEntity.getId(), oil.getId(), repair.getId(), other.getId()
                };
              });
      variant = ids[0];
      oilWork = ids[1];
      repairWork = ids[2];
      otherWork = ids[3];
    }

    String newEmail() {
      String email = "review-it-" + UUID.randomUUID() + "@example.com";
      emails.add(email);
      return email;
    }

    long register() {
      char[] password = "test-only-password-buffer".toCharArray();
      try {
        return auth.register(
            "Integration test",
            newEmail(),
            password,
            new VehicleInput(variant, 2017, 100_000),
            List.of());
      } finally {
        Arrays.fill(password, '\0');
      }
    }

    long addProblem(long vehicle, String description) {
      return inTransaction(
          entityManager -> {
            Problem problem =
                new Problem(
                    entityManager.find(Vehicle.class, vehicle),
                    UUID.randomUUID().toString(),
                    description,
                    LocalDateTime.now(CLOCK),
                    null,
                    null,
                    null,
                    null);
            entityManager.persist(problem);
            entityManager.flush();
            return problem.getId();
          });
    }

    ServiceInput service(int mileage, List<Long> problems) {
      return new ServiceInput(
          LocalDate.now(CLOCK),
          mileage,
          "Test invoice",
          List.of(
              new ItemInput(oilWork, new BigDecimal("53.47")),
              new ItemInput(repairWork, new BigDecimal("10.00"))),
          problems);
    }

    void assertProblem(long id, ProblemStatus status, Long service) {
      inTransaction(
          entityManager -> {
            Problem problem = entityManager.find(Problem.class, id);
            assertEquals(status, problem.getStatus());
            if (service == null) {
              assertNull(problem.getResolvedByService());
            } else {
              assertEquals(service, problem.getResolvedByService().getId());
            }
            return null;
          });
    }

    <T> T inTransaction(Function<EntityManager, T> action) {
      try (EntityManager entityManager = factory.createEntityManager()) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
          transaction.begin();
          T result = action.apply(entityManager);
          transaction.commit();
          return result;
        } catch (RuntimeException | Error failure) {
          try {
            if (transaction.isActive()) {
              transaction.rollback();
            }
          } catch (RuntimeException cleanupFailure) {
            if (cleanupFailure != failure) {
              failure.addSuppressed(cleanupFailure);
            }
          }
          throw failure;
        }
      }
    }

    @Override
    public void close() {
      inTransaction(
          entityManager -> {
            if (!emails.isEmpty()) {
              List<Long> owners =
                  entityManager
                      .createQuery(
                          "select u.id from AppUser u where u.email in :emails", Long.class)
                      .setParameter("emails", emails)
                      .getResultList();
              for (Long owner : owners) {
                entityManager
                    .createQuery("update AppUser u set u.activeVehicle=null where u.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
                entityManager
                    .createQuery("delete from Problem p where p.vehicle.owner.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
                entityManager
                    .createQuery(
                        "delete from ServiceItem i where i.serviceRecord.vehicle.owner.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
                entityManager
                    .createQuery("delete from ServiceRecord s where s.vehicle.owner.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
                entityManager
                    .createQuery("delete from Vehicle v where v.owner.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
                entityManager
                    .createQuery("delete from AppUser u where u.id=:owner")
                    .setParameter("owner", owner)
                    .executeUpdate();
              }
            }
            for (Long work : List.of(oilWork, repairWork, otherWork)) {
              entityManager.remove(entityManager.find(WorkDefinition.class, work));
            }
            entityManager.remove(entityManager.find(VehicleVariant.class, variant));
            return null;
          });
    }
  }
}
