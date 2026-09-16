package hr.unizd.autocare.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Lifecycle jedinicni testovi bez mreze i bez dodatne mocking biblioteke. JDK proxy je iskljucivo
 * testni instrument pravih JPA sucelja, ne dio runtimea. Ne provjerava SQL Server ni Hibernate
 * mapping: za to je potreban SqlServerIT.
 */
class JpaTransactionRunnerTest {

  @Test
  void oneWriteSharesOneEntityManagerAcrossAllRepositories() {
    Lifecycle lifecycle = new Lifecycle();
    JpaTransactionRunner runner = new JpaTransactionRunner(lifecycle.factory);

    int result =
        runner.write(
            repositories -> {
              Object[] all = {
                repositories.users(),
                repositories.vehicles(),
                repositories.services(),
                repositories.problems(),
                repositories.catalog()
              };
              for (Object repository : all) {
                assertSame(lifecycle.lastEntityManager, entityManagerOf(repository));
              }
              lifecycle.calls.add("action");
              return 42;
            });

    assertEquals(42, result);
    assertEquals(
        List.of("create", "transaction", "begin", "action", "flush", "commit", "close"),
        lifecycle.calls);
  }

  @Test
  void readHasNoExplicitWriteFlush() {
    Lifecycle lifecycle = new Lifecycle();
    String result = new JpaTransactionRunner(lifecycle.factory).read(repositories -> "read");
    assertEquals("read", result);
    assertEquals(List.of("create", "transaction", "begin", "commit", "close"), lifecycle.calls);
  }

  @Test
  void eachOperationGetsANewEntityManager() {
    Lifecycle lifecycle = new Lifecycle();
    JpaTransactionRunner runner = new JpaTransactionRunner(lifecycle.factory);
    runner.read(repositories -> null);
    EntityManager first = lifecycle.lastEntityManager;
    runner.read(repositories -> null);
    assertFalse(first == lifecycle.lastEntityManager);
    assertEquals(2, lifecycle.calls.stream().filter("create"::equals).count());
  }

  @Test
  void applicationFailureRollsBackAndKeepsTheSameException() {
    Lifecycle lifecycle = new Lifecycle();
    AppException original = AppException.validation("bad input");
    AppException actual =
        assertThrows(
            AppException.class,
            () ->
                new JpaTransactionRunner(lifecycle.factory)
                    .write(
                        repositories -> {
                          throw original;
                        }));
    assertSame(original, actual);
    assertEquals(
        List.of("create", "transaction", "begin", "isActive", "rollback", "close"),
        lifecycle.calls);
  }

  @Test
  void isActiveFailureDoesNotMaskTheOriginalProblem() {
    Lifecycle lifecycle = new Lifecycle();
    AppException original = AppException.validation("original");
    lifecycle.activeFailure = new PersistenceException("isActive failed");
    AppException actual =
        assertThrows(
            AppException.class,
            () ->
                new JpaTransactionRunner(lifecycle.factory)
                    .write(
                        repositories -> {
                          throw original;
                        }));
    assertSame(original, actual);
    assertSame(lifecycle.activeFailure, actual.getSuppressed()[0]);
    assertFalse(lifecycle.calls.contains("rollback"));
    assertTrue(lifecycle.calls.contains("close"));
  }

  @Test
  void rollbackFailureIsSuppressedNotSubstituted() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.rollbackFailure = new PersistenceException("rollback failed");
    AppException original = AppException.validation("original");
    AppException actual =
        assertThrows(
            AppException.class,
            () ->
                new JpaTransactionRunner(lifecycle.factory)
                    .write(
                        repositories -> {
                          throw original;
                        }));
    assertSame(original, actual);
    assertSame(lifecycle.rollbackFailure, actual.getSuppressed()[0]);
  }

  @Test
  void cleanupDoesNotSelfSuppressTheSameThrowable() {
    Lifecycle lifecycle = new Lifecycle();
    AppException original = AppException.validation("same instance");
    lifecycle.activeFailure = original;
    AppException actual =
        assertThrows(
            AppException.class,
            () ->
                new JpaTransactionRunner(lifecycle.factory)
                    .write(
                        repositories -> {
                          throw original;
                        }));
    assertSame(original, actual);
    assertEquals(0, actual.getSuppressed().length);
  }

  @Test
  void successfulCommitRemainsSuccessfulWhenCloseFails() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.closeFailure = new PersistenceException("close failed after commit");
    int result = new JpaTransactionRunner(lifecycle.factory).write(repositories -> 5);
    assertEquals(5, result);
    assertTrue(lifecycle.calls.contains("commit"));
    assertFalse(lifecycle.calls.contains("rollback"));
  }

  @Test
  void beginFailureClosesWithoutCommit() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.beginFailure = new PersistenceException("begin failed");
    AppException error =
        assertThrows(
            AppException.class,
            () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 1));
    assertEquals(AppException.Kind.DATABASE, error.getKind());
    assertFalse(lifecycle.calls.contains("commit"));
    assertTrue(lifecycle.calls.contains("close"));
  }

  @Test
  void flushFailureIsNotReportedAsAnAttemptedCommit() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.flushFailure = new PersistenceException("flush failed");
    AppException error =
        assertThrows(
            AppException.class,
            () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 1));
    assertEquals(AppException.Kind.DATABASE, error.getKind());
    assertTrue(lifecycle.calls.contains("rollback"));
    assertFalse(lifecycle.calls.contains("commit"));
  }

  @Test
  void genericCommitFailureRemainsUnknownEvenAfterRollback() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.commitFailure = new RollbackException("commit failed without a conclusive cause");
    AppException error = commitError(lifecycle);
    assertEquals(AppException.Kind.COMMIT_UNKNOWN, error.getKind());
    assertTrue(lifecycle.calls.contains("rollback"));
  }

  @Test
  void transportFailureWrappedByRollbackExceptionRemainsUnknown() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.commitFailure =
        new RollbackException("lost acknowledgement", new SQLException("lost", "08006"));
    assertEquals(AppException.Kind.COMMIT_UNKNOWN, commitError(lifecycle).getKind());
  }

  @Test
  void knownOptimisticConflictHasConflictClassification() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.commitFailure =
        new RollbackException("conflict", new OptimisticLockException("stale"));
    assertEquals(AppException.Kind.CONFLICT, commitError(lifecycle).getKind());
  }

  @Test
  void knownIntegrityViolationHasConflictClassification() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.commitFailure =
        new RollbackException("constraint", new SQLException("duplicate", "23000"));
    assertEquals(AppException.Kind.CONFLICT, commitError(lifecycle).getKind());
  }

  @Test
  void transportEvidenceWinsOverConstraintEvidenceInSqlChain() {
    Lifecycle lifecycle = new Lifecycle();
    SQLException constraint = new SQLException("duplicate", "23000");
    constraint.setNextException(new SQLException("connection broken", "08S01"));
    lifecycle.commitFailure = new RollbackException("mixed", constraint);
    assertEquals(AppException.Kind.COMMIT_UNKNOWN, commitError(lifecycle).getKind());
  }

  @Test
  void jvmErrorIsNotTranslatedAndStillAttemptsCleanup() {
    Lifecycle lifecycle = new Lifecycle();
    AssertionError original = new AssertionError("test failure");
    AssertionError actual =
        assertThrows(
            AssertionError.class,
            () ->
                new JpaTransactionRunner(lifecycle.factory)
                    .write(
                        repositories -> {
                          throw original;
                        }));
    assertSame(original, actual);
    assertTrue(lifecycle.calls.contains("rollback"));
    assertTrue(lifecycle.calls.contains("close"));
  }

  private AppException commitError(Lifecycle lifecycle) {
    return assertThrows(
        AppException.class,
        () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 1));
  }

  private static Object entityManagerOf(Object repository) {
    try {
      Field field = repository.getClass().getDeclaredField("em");
      field.setAccessible(true);
      return field.get(repository);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(
          "Review test when repository internals are intentionally renamed", exception);
    }
  }

  /** Minimalni snimac redoslijeda JPA poziva; svaki neocekivani poziv pada. */
  private static final class Lifecycle {
    private final List<String> calls = new ArrayList<>();
    private boolean active;
    private RuntimeException beginFailure;
    private RuntimeException flushFailure;
    private RuntimeException commitFailure;
    private RuntimeException activeFailure;
    private RuntimeException rollbackFailure;
    private RuntimeException closeFailure;
    private EntityManager lastEntityManager;
    private final EntityTransaction transaction =
        proxy(
            EntityTransaction.class,
            (object, method, args) -> {
              String name = method.getName();
              calls.add(name);
              switch (name) {
                case "begin":
                  raise(beginFailure);
                  active = true;
                  return null;
                case "commit":
                  raise(commitFailure);
                  active = false;
                  return null;
                case "isActive":
                  raise(activeFailure);
                  return active;
                case "rollback":
                  raise(rollbackFailure);
                  active = false;
                  return null;
                default:
                  throw new AssertionError("Unexpected transaction method: " + name);
              }
            });
    private final EntityManagerFactory factory =
        proxy(
            EntityManagerFactory.class,
            (object, method, args) -> {
              if (!method.getName().equals("createEntityManager")) {
                throw new AssertionError("Unexpected factory method: " + method.getName());
              }
              calls.add("create");
              lastEntityManager =
                  proxy(
                      EntityManager.class,
                      (manager, operation, parameters) -> {
                        switch (operation.getName()) {
                          case "getTransaction":
                            calls.add("transaction");
                            return transaction;
                          case "flush":
                            calls.add("flush");
                            raise(flushFailure);
                            return null;
                          case "close":
                            calls.add("close");
                            raise(closeFailure);
                            return null;
                          default:
                            throw new AssertionError(
                                "Unexpected EntityManager method: " + operation.getName());
                        }
                      });
              return lastEntityManager;
            });

    private static void raise(RuntimeException failure) {
      if (failure != null) {
        throw failure;
      }
    }
  }

  private static <T> T proxy(Class<T> api, InvocationHandler handler) {
    return api.cast(Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[] {api}, handler));
  }
}
