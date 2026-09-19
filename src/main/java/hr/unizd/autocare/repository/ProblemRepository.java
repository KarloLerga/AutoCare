package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Problem;
import java.util.List;

/** Dohvat i spremanje bilješki vozila. */
public interface ProblemRepository {
  void add(Problem problem);

  Problem findForOwner(long ownerId, long problemId);

  List<Problem> list(long ownerId, long vehicleId);

  List<String> resolvedDescriptions(long ownerId, long serviceId);

  long openCount(long ownerId, long vehicleId);

  void deleteForVehicle(long vehicleId);
}
