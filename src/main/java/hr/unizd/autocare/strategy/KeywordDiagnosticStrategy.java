package hr.unizd.autocare.strategy;

import hr.unizd.autocare.model.Data.DiagnosticResult;
import hr.unizd.autocare.model.Data.RuleData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Ponderirana pokrivenost aktivnih pravila kandidata; nije vjerojatnost kvara. */
public final class KeywordDiagnosticStrategy implements DiagnosticStrategy {
  public static String normalize(String value) {
    return Normalizer.normalize(
            value.toLowerCase(Locale.ROOT).replace("\u0111", "d"), Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "")
        .replaceAll("[^a-z0-9]+", " ")
        .trim();
  }

  @Override
  public List<DiagnosticResult> analyze(String description, List<RuleData> rules) {
    String normalizedDescription = " " + normalize(description) + " ";
    Map<Long, List<RuleData>> groups = new LinkedHashMap<>();
    for (RuleData ruleData : rules) {
      List<RuleData> candidateRules = groups.get(ruleData.getCandidateId());
      if (candidateRules == null) {
        candidateRules = new ArrayList<>();
        groups.put(ruleData.getCandidateId(), candidateRules);
      }
      candidateRules.add(ruleData);
    }

    List<DiagnosticResult> results = new ArrayList<>();
    for (List<RuleData> candidateRules : groups.values()) {
      int totalWeight = 0;
      int matchedWeight = 0;
      Set<String> seenPhrases = new HashSet<>();
      for (RuleData ruleData : candidateRules) {
        String phrase = normalize(ruleData.getPhrase());
        if (phrase.isEmpty() || !seenPhrases.add(phrase)) {
          continue;
        }
        totalWeight += ruleData.getWeight();
        if (normalizedDescription.contains(" " + phrase + " ")) {
          matchedWeight += ruleData.getWeight();
        }
      }

      if (matchedWeight > 0 && totalWeight > 0) {
        RuleData firstRule = candidateRules.get(0);
        BigDecimal score =
            BigDecimal.valueOf(matchedWeight)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
        results.add(
            new DiagnosticResult(
                firstRule.getCandidateId(),
                firstRule.getCandidateName(),
                score,
                matchedWeight,
                firstRule.getPrice(),
                firstRule.getPriceNote()));
      }
    }

    results.sort(
        new Comparator<DiagnosticResult>() {
          @Override
          public int compare(DiagnosticResult first, DiagnosticResult second) {
            int scoreComparison = second.getScore().compareTo(first.getScore());
            if (scoreComparison != 0) {
              return scoreComparison;
            }
            int weightComparison =
                Integer.compare(second.getMatchedWeight(), first.getMatchedWeight());
            if (weightComparison != 0) {
              return weightComparison;
            }
            int nameComparison = first.getCandidateName().compareTo(second.getCandidateName());
            if (nameComparison != 0) {
              return nameComparison;
            }
            return Long.compare(first.getCandidateId(), second.getCandidateId());
          }
        });
    return results;
  }
}
