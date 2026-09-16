package hr.unizd.autocare.repository;

import hr.unizd.autocare.domain.Problem;
import hr.unizd.autocare.domain.ProblemStatus;
import java.util.List;
import java.util.Optional;

/** Transakcijski repository; sam ne otvara niti zatvara EntityManager. */
public interface ProblemRepository {
  void add(Problem problem);

  Problem requireOwned(long owner, long id);

  List<Problem> list(long owner, long vehicle, ProblemStatus status);

  Optional<Problem> byRequest(long owner, String key);

  List<String> resolvedDescriptions(long owner, long service);

  long openCount(long owner, long vehicle);

  void deleteForVehicle(long vehicle);
}
