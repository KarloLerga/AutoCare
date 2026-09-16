package hr.unizd.autocare.strategy;
import hr.unizd.autocare.model.Data.*;
import java.util.List;
/** Zamjenjiv algoritam; samo ulazni podaci, bez pristupa bazi. */
public interface DiagnosticStrategy {
    List<DiagnosticResult> analyze(String description, List<RuleData> rules);
}
