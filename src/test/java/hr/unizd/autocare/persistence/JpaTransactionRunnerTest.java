package hr.unizd.autocare.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.unizd.autocare.service.AppException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Lifecycle testovi za jednostavni runner; svi repositoryji dijele isti EntityManager. */
class JpaTransactionRunnerTest {

  @Test
  void oneWriteSharesOneEntityManagerAcrossAllRepositories() {
    Lifecycle lifecycle = new Lifecycle();
    int result =
        new JpaTransactionRunner(lifecycle.factory)
            .write(
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
        List.of("create", "transaction", "begin", "action", "commit", "close"),
        lifecycle.calls);
  }

  @Test
  void readUsesTheSameEntityManagerWithoutOpeningATransaction() {
    Lifecycle lifecycle = new Lifecycle();
    String result =
        new JpaTransactionRunner(lifecycle.factory)
            .read(
                repositories -> {
                  lifecycle.calls.add("action");
                  return "read";
                });
    assertEquals("read", result);
    assertEquals(List.of("create", "action", "close"), lifecycle.calls);
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
                          lifecycle.calls.add("action");
                          throw original;
                        }));
    assertSame(original, actual);
    assertEquals(
        List.of("create", "transaction", "begin", "action", "isActive", "rollback", "close"),
        lifecycle.calls);
  }

  @Test
  void commitFailureIsPropagatedWithoutCommitClassification() {
    Lifecycle lifecycle = new Lifecycle();
    PersistenceException original = new PersistenceException("commit failed");
    lifecycle.commitFailure = original;
    PersistenceException actual =
        assertThrows(
            PersistenceException.class,
            () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 1));
    assertSame(original, actual);
    assertEquals(
        List.of("create", "transaction", "begin", "commit", "isActive", "rollback", "close"),
        lifecycle.calls);
  }

  @Test
  void closeFailureIsPropagatedAsTheActualCloseFailure() {
    Lifecycle lifecycle = new Lifecycle();
    PersistenceException original = new PersistenceException("close failed");
    lifecycle.closeFailure = original;
    PersistenceException actual =
        assertThrows(
            PersistenceException.class,
            () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 5));
    assertSame(original, actual);
    assertFalse(lifecycle.calls.contains("rollback"));
  }

  @Test
  void beginFailureDoesNotCommit() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.beginFailure = new PersistenceException("begin failed");
    PersistenceException error =
        assertThrows(
            PersistenceException.class,
            () -> new JpaTransactionRunner(lifecycle.factory).write(repositories -> 1));
    assertSame(lifecycle.beginFailure, error);
    assertFalse(lifecycle.calls.contains("commit"));
    assertEquals(
        List.of("create", "transaction", "begin", "isActive", "close"), lifecycle.calls);
  }

  private static Object entityManagerOf(Object repository) {
    try {
      Field field = repository.getClass().getDeclaredField("em");
      field.setAccessible(true);
      return field.get(repository);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError("Repository treba imati EntityManager polje.", exception);
    }
  }

  private static final class Lifecycle {
    private final List<String> calls = new ArrayList<>();
    private boolean active;
    private RuntimeException beginFailure;
    private RuntimeException commitFailure;
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
                  return active;
                case "rollback":
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
