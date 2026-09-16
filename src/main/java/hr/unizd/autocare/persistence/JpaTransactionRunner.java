package hr.unizd.autocare.persistence;

import hr.unizd.autocare.repository.Repositories;
import hr.unizd.autocare.service.AppException;
import hr.unizd.autocare.service.TransactionRunner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tehnicki lifecycle jedne Service operacije, ne repository i ne novi poslovni sloj. Service daje
 * sadrzaj transakcije; ova klasa provodi begin/flush/commit/rollback/close.
 */
public final class JpaTransactionRunner implements TransactionRunner {

  private static final Logger LOG = Logger.getLogger(JpaTransactionRunner.class.getName());

  private final EntityManagerFactory factory;

  public JpaTransactionRunner(EntityManagerFactory factory) {
    this.factory = Objects.requireNonNull(factory);
  }

  @Override
  public <T> T read(Function<Repositories, T> action) {
    return run(action, false);
  }

  @Override
  public <T> T write(Function<Repositories, T> action) {
    return run(action, true);
  }

  private <T> T run(Function<Repositories, T> action, boolean write) {
    Objects.requireNonNull(action);
    EntityManager entityManager = null;
    EntityTransaction transaction = null;
    boolean commitStarted = false;
    boolean committed = false;

    try {
      entityManager = factory.createEntityManager();
      transaction = entityManager.getTransaction();
      transaction.begin();

      // Svih pet dobiva ISTI EntityManager, ne po jedan za svaku tablicu.
      Repositories repositories =
          new Repositories(
              new JpaUserRepository(entityManager),
              new JpaVehicleRepository(entityManager),
              new JpaServiceRecordRepository(entityManager),
              new JpaProblemRepository(entityManager),
              new JpaCatalogRepository(entityManager));

      T result = action.apply(repositories);

      if (write) {
        // Flush salje SQL, ali nije potvrda transakcije.
        entityManager.flush();
      }

      commitStarted = true;
      transaction.commit();
      committed = true;
      return result;
    } catch (RuntimeException failure) {
      rollback(transaction, failure);
      throw translate(failure, write && commitStarted);
    } catch (Error failure) {
      // Pokusaj oslobadjanja resursa; ozbiljna JVM greska se ne pretvara u GUI uspjeh.
      rollback(transaction, failure);
      throw failure;
    } finally {
      close(entityManager, committed);
    }
  }

  private static void rollback(EntityTransaction transaction, Throwable original) {
    if (transaction == null) {
      return;
    }

    try {
      // I isActive() moze baciti iznimku: i on mora biti unutar try bloka.
      if (transaction.isActive()) {
        transaction.rollback();
      }
    } catch (RuntimeException cleanupFailure) {
      if (cleanupFailure != original) {
        original.addSuppressed(cleanupFailure);
      }
    }
  }

  private static void close(EntityManager entityManager, boolean committed) {
    if (entityManager == null) {
      return;
    }

    try {
      entityManager.close();
    } catch (RuntimeException failure) {
      String message =
          committed
              ? "Commit je uspio; zatvaranje EntityManagera nije uspjelo."
              : "Zatvaranje EntityManagera nije uspjelo.";
      LOG.log(Level.WARNING, message, failure);
    }
  }

  private static AppException translate(RuntimeException failure, boolean commitStarted) {
    if (commitStarted) {
      // Poznat konflikt mozemo imenovati samo bez komunikacijske greske u uzroku.
      if (!hasConnectionFailure(failure) && isConflict(failure)) {
        return conflict(failure);
      }

      // Sam RollbackException ne dokazuje da server nije commitirao.
      // Ni naknadni uspjeh lokalnog rollback() nije takav dokaz.
      return new AppException(
          AppException.Kind.COMMIT_UNKNOWN,
          "Ishod spremanja nije potvrdjen. Provjerite je li zapis spremljen prije ponavljanja.",
          failure);
    }

    if (failure instanceof AppException applicationFailure) {
      return applicationFailure;
    }

    if (failure instanceof IllegalArgumentException) {
      return new AppException(AppException.Kind.VALIDATION, failure.getMessage(), failure);
    }

    if (isConflict(failure)) {
      return conflict(failure);
    }

    return new AppException(
        AppException.Kind.DATABASE,
        "Baza nije dostupna ili operacija nije uspjela. Provjerite vezu i zapisnik.",
        failure);
  }

  private static AppException conflict(Throwable failure) {
    return new AppException(
        AppException.Kind.CONFLICT,
        "Podaci su promijenjeni, zauzeti ili zapis vec postoji. Osvjezite prikaz.",
        failure);
  }

  private static boolean hasConnectionFailure(Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof SQLException sql && hasSqlState(sql, "08")) {
        return true;
      }
      if (cause instanceof java.io.IOException) {
        return true;
      }
    }
    return false;
  }

  private static boolean isConflict(Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof OptimisticLockException
          || cause instanceof PessimisticLockException
          || cause instanceof LockTimeoutException) {
        return true;
      }
      if (cause instanceof SQLException sql && hasSqlState(sql, "23")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasSqlState(SQLException failure, String prefix) {
    for (SQLException sql = failure; sql != null; sql = sql.getNextException()) {
      if (sql.getSQLState() != null && sql.getSQLState().startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
