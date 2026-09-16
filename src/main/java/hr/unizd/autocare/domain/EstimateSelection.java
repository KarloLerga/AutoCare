package hr.unizd.autocare.domain;

import java.util.HashSet;
import java.util.Set;

/** Zastita procjene od zbrajanja istog ukljucenog rada dvaput. */
public final class EstimateSelection {
  private EstimateSelection() {}

  private static final Set<Set<String>> OVERLAPS =
      Set.of(
          Set.of("OIL_SERVICE", "ENGINE_OIL"),
          Set.of("OIL_SERVICE", "OIL_FILTER"),
          Set.of("FRONT_BRAKES", "FRONT_DISCS_PADS"),
          Set.of("REAR_BRAKES", "REAR_DISCS_PADS"),
          Set.of("CLUTCH_KIT", "CLUTCH_DMF"),
          Set.of("TIMING_BELT_PUMP", "WATER_PUMP"),
          Set.of("TIMING_BELT_PUMP", "COOLANT"),
          Set.of("WATER_PUMP", "COOLANT"),
          Set.of("AC_R134A", "AC_R1234YF"),
          Set.of("AC_COMPRESSOR", "AC_R134A"),
          Set.of("AC_COMPRESSOR", "AC_R1234YF"),
          Set.of("DPF_CLEAN", "DPF_REPLACEMENT"));

  public static Set<String> conflicts(Set<String> selectedCodes, String candidateCode) {
    if (selectedCodes == null || candidateCode == null || candidateCode.isBlank()) {
      throw new IllegalArgumentException("Nedostaje odabir rada.");
    }
    Set<String> result = new HashSet<>();
    if (selectedCodes.contains(candidateCode)) {
      result.add(candidateCode);
    }
    for (Set<String> pair : OVERLAPS) {
      if (!pair.contains(candidateCode)) {
        continue;
      }
      for (String code : pair) {
        if (!code.equals(candidateCode) && selectedCodes.contains(code)) {
          result.add(code);
        }
      }
    }
    return Set.copyOf(result);
  }
}
