package hr.unizd.autocare.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ServiceInput;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Provjera ulaza servisne povijesti bez request-key/idempotency mehanizma. */
class ServiceRecordServiceValidationTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void regularServiceAcceptsActualCents() {
    assertDoesNotThrow(() -> ServiceRecordService.validate(input(new BigDecimal("53.47")), false, CLOCK));
  }

  @Test
  void historicalPriceCanRemainUnknown() {
    assertDoesNotThrow(() -> ServiceRecordService.validate(input(null), true, CLOCK));
    assertThrows(
        IllegalArgumentException.class,
        () -> ServiceRecordService.validate(input(null), false, CLOCK));
  }

  @Test
  void actualPriceIsNotRoundedFromExtraDecimals() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ServiceRecordService.validate(input(new BigDecimal("53.471")), false, CLOCK));
  }

  @Test
  void duplicatedWorkIsRejected() {
    ServiceInput duplicated =
        new ServiceInput(
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
            LocalDate.now(CLOCK),
            100_000,
            null,
            List.of(new ItemInput(1, null)),
            List.of(1L));
    assertThrows(AppException.class, () -> ServiceRecordService.validate(history, true, CLOCK));
  }

  private ServiceInput input(BigDecimal price) {
    return new ServiceInput(
        LocalDate.now(CLOCK),
        100_000,
        "test",
        List.of(new ItemInput(1L, price)),
        List.of());
  }
}
