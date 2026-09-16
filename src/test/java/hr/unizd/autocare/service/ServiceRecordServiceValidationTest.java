package hr.unizd.autocare.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ServiceInput;
import hr.unizd.autocare.repository.Repositories;
import hr.unizd.autocare.repository.ServiceRecordRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** Unos i kanonizacija zahtjeva; ne zamjenjuje transakcijski SQL integracijski test. */
class ServiceRecordServiceValidationTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC);
  private static final String KEY = "abcdef01-2345-4678-9012-abcdefabcdef";

  @Test
  void regularServiceAcceptsActualCents() {
    assertDoesNotThrow(
        () -> ServiceRecordService.validate(input(KEY, new BigDecimal("53.47")), false, CLOCK));
  }

  @Test
  void historicalPriceCanRemainUnknown() {
    assertDoesNotThrow(() -> ServiceRecordService.validate(input(KEY, null), true, CLOCK));
    assertThrows(
        IllegalArgumentException.class,
        () -> ServiceRecordService.validate(input(KEY, null), false, CLOCK));
  }

  @Test
  void actualPriceIsNotRoundedFromExtraDecimals() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ServiceRecordService.validate(input(KEY, new BigDecimal("53.471")), false, CLOCK));
  }

  @Test
  void validUppercaseUuidIsAccepted() {
    assertDoesNotThrow(
        () ->
            ServiceRecordService.validate(
                input(KEY.toUpperCase(Locale.ROOT), BigDecimal.ZERO), false, CLOCK));
  }

  @Test
  void invalidMissingAndShortUuidHaveValidationErrors() {
    for (String invalid :
        new String[] {null, "", "1-1-1-1-1", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}) {
      AppException error =
          assertThrows(
              AppException.class,
              () -> ServiceRecordService.validate(input(invalid, BigDecimal.ZERO), false, CLOCK));
      assertEquals(AppException.Kind.VALIDATION, error.getKind());
    }
  }

  @Test
  void duplicatedWorkIsRejected() {
    ServiceInput duplicated =
        new ServiceInput(
            KEY,
            LocalDate.now(CLOCK),
            100_000,
            null,
            List.of(new ItemInput(1, BigDecimal.ONE), new ItemInput(1, BigDecimal.TEN)),
            List.of());
    assertThrows(AppException.class, () -> ServiceRecordService.validate(duplicated, false, CLOCK));
  }

  @Test
  void futureDateIsRejected() {
    ServiceInput future =
        new ServiceInput(
            KEY,
            LocalDate.now(CLOCK).plusDays(1),
            100_000,
            null,
            List.of(new ItemInput(1, BigDecimal.ONE)),
            List.of());
    assertThrows(AppException.class, () -> ServiceRecordService.validate(future, false, CLOCK));
  }

  @Test
  void initialHistoryCannotResolveExistingProblems() {
    ServiceInput history =
        new ServiceInput(
            KEY, LocalDate.now(CLOCK), 100_000, null, List.of(new ItemInput(1, null)), List.of(1L));
    assertThrows(AppException.class, () -> ServiceRecordService.validate(history, true, CLOCK));
  }

  @Test
  void findSavedUsesCanonicalKeyBeforeTheRepositoryQuery() {
    AtomicReference<String> queriedKey = new AtomicReference<>();
    ServiceRecordRepository repository =
        (ServiceRecordRepository)
            Proxy.newProxyInstance(
                ServiceRecordRepository.class.getClassLoader(),
                new Class<?>[] {ServiceRecordRepository.class},
                (object, method, arguments) -> {
                  if (!method.getName().equals("byRequest")) {
                    throw new AssertionError("Unexpected method " + method.getName());
                  }
                  queriedKey.set((String) arguments[1]);
                  return Optional.empty();
                });

    TransactionRunner runner =
        new TransactionRunner() {
          @Override
          public <T> T read(Function<Repositories, T> action) {
            return action.apply(new Repositories(null, null, repository, null, null));
          }

          @Override
          public <T> T write(Function<Repositories, T> action) {
            throw new AssertionError("findSaved must not write");
          }
        };

    assertNull(new ServiceRecordService(runner, CLOCK).findSaved(1L, KEY.toUpperCase(Locale.ROOT)));
    assertEquals(KEY, queriedKey.get());
  }

  private ServiceInput input(String key, BigDecimal price) {
    return new ServiceInput(
        key, LocalDate.now(CLOCK), 100_000, "test", List.of(new ItemInput(1L, price)), List.of());
  }
}
