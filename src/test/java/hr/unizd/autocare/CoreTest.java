package hr.unizd.autocare;

import org.junit.jupiter.api.Test;

/** Sedam imenovanih skupina testova; bez baze. */
class CoreTest {
  @Test
  void maintenanceRules() {
    CoreChecks.maintenance();
  }

  @Test
  void nullableMoney() {
    CoreChecks.money();
  }

  @Test
  void inputValidation() {
    CoreChecks.validation();
  }

  @Test
  void maintenanceStrategies() {
    CoreChecks.maintenance();
  }

  @Test
  void entityInvariants() {
    CoreChecks.entities();
  }

}
