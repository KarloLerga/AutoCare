package hr.unizd.autocare.strategy;
import hr.unizd.autocare.model.Data.*;
import java.text.Normalizer;
import java.math.*;
import java.util.*;
/** Ponderirana pokrivenost aktivnih pravila kandidata; nije vjerojatnost kvara. */
public final class KeywordDiagnosticStrategy implements DiagnosticStrategy {
    public static String normalize(String value) {
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace("\u0111", "d"), Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replaceAll("[^a-z0-9]+", " ").trim();
    }
    @Override public List<DiagnosticResult> analyze(String description, List<RuleData> rules) {
        String text=" "+normalize(description)+" ";
        Map<Long, List<RuleData>> groups=new LinkedHashMap<>();
        for(RuleData rule:rules)groups.computeIfAbsent(rule.getCandidateId(), id->new ArrayList<>()).add(rule);
        List<DiagnosticResult> result=new ArrayList<>();
        for(List<RuleData> group:groups.values()) {
            int total=0, matched=0;
            Set<String> seen=new HashSet<>();
            for(RuleData rule:group) {
                String phrase=normalize(rule.getPhrase());
                if(phrase.isEmpty() || !seen.add(phrase))continue;
                total+=rule.getWeight();
                if(text.contains(" "+phrase+" "))matched+=rule.getWeight();
            }
            if(matched>0 && total>0) {
                RuleData first=group.get(0);
                BigDecimal score=BigDecimal.valueOf(matched).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
                result.add(new DiagnosticResult(first.getCandidateId(), first.getCandidateName(), score, matched, first.getPrice(), first.getPriceNote()));
            }
        }
        result.sort(Comparator.comparing(DiagnosticResult::getScore).reversed().thenComparing(Comparator.comparingInt(DiagnosticResult::getMatchedWeight).reversed()).thenComparing(DiagnosticResult::getCandidateName).thenComparingLong(DiagnosticResult::getCandidateId));
        return List.copyOf(result);
    }
}
