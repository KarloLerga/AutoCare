package hr.unizd.autocare.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.unizd.autocare.model.Data.ItemInput;
import hr.unizd.autocare.model.Data.ServiceInput;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Provjera ulaza servisne povijesti bez idempotency mehanizma. */
class ServiceRecordServiceValidationTest {
  @Test
  void regularServiceAcceptsActualCents() {
    assertDoesNotThrow(
        () -> ServiceRecordService.validate(input(new BigDecimal("53.47")), false));
  }

  @Test
  void historicalPriceCanRemainUnknown() {
    assertDoesNotThrow(() -> ServiceRecordService.validate(input(null), true));
    assertThrows(
        IllegalArgumentException.class,
        () -> ServiceRecordService.validate(input(null), false));
  }

  @Test
  void actualPriceIsRoundedToCents() {
    assertDoesNotThrow(
        () -> ServiceRecordService.validate(input(new BigDecimal("53.471")), false));
  }

  @Test
  void duplicatedWorkIsAcceptedByServiceValidation() {
    ServiceInput duplicated =
        new ServiceInput(
            LocalDate.now(),
            100_000,
            null,
            List.of(new ItemInput(1, BigDecimal.ONE), new ItemInput(1, BigDecimal.TEN)),
            List.of());
    assertDoesNotThrow(() -> ServiceRecordService.validate(duplicated, false));
  }

  @Test
  void futureDateIsRejected() {
    ServiceInput future =
        new ServiceInput(
            LocalDate.now().plusDays(1),
            100_000,
            null,
            List.of(new ItemInput(1, BigDecimal.ONE)),
            List.of());
    assertThrows(
        IllegalArgumentException.class, () -> ServiceRecordService.validate(future, false));
  }

  @Test
  void initialHistoryCannotResolveExistingProblems() {
    ServiceInput history =
        new ServiceInput(
            LocalDate.now(),
            100_000,
            null,
            List.of(new ItemInput(1, null)),
            List.of(1L));
    assertThrows(
        IllegalArgumentException.class, () -> ServiceRecordService.validate(history, true));
  }

  private ServiceInput input(BigDecimal price) {
    return new ServiceInput(
        LocalDate.now(),
        100_000,
        "test",
        List.of(new ItemInput(1L, price)),
        List.of());
  }
}
