# AutoCare local agent instructions

The final runtime/code/data in this repository are authoritative. Some older files in `docs/` describe earlier iterations and will be reconciled only in the final documentation pass; do not reintroduce removed architecture from an old prompt or diagram.

Use Java 25, Maven, Swing/FlatLaf, JPA `EntityManager`, Hibernate and Azure SQL Server. Do not add Spring, MySQL, runtime AI, image APIs or a new UI framework.

Keep the student-readable runtime style:
- no lambdas, Stream API, Optional, `java.util.function`, `var`, records or generic callback frameworks in `src/main/java`;
- classic anonymous Swing listeners, ordinary loops and explicit `if/else`;
- Service owns business rules/transactions, Repository owns persistence queries, Controller handles GUI events, View renders Swing.

Final functional decisions:
- keyword/scoring diagnostic rules are removed; do not recreate `DiagnosticRule`, `DiagnosticStrategy`, `KeywordDiagnosticStrategy` or `AnalysisDialog`;
- Strategy is used for mileage/time/combined maintenance interval calculation;
- problem entry is manual description + selected applicable repair + materialized price estimate;
- existing vehicle identity is immutable in the UI; only mileage can be updated;
- maintenance tracking shows only works that have service history, while the inline estimator may estimate every applicable maintenance work for the active vehicle;
- service entry records actual paid price and never displays modelled price as actual price;
- no manual refresh/status footer, vehicle images, password hashing or `OTHER_*` work flow.

Final materialized catalogue:
- 30,366 vehicle variants;
- 120 final work definitions;
- 2,908,857 applicable variant/work rules;
- every stored rule has a positive concrete modelled price;
- every stored maintenance rule has a concrete km and/or month interval;
- repair rules have no preventive interval;
- runtime fallback prices/intervals are not allowed;
- obvious non-applicable hardware pairs are absent (for example BEV combustion jobs and explicit FWD transfer-case/differential service).

Credentials stay outside the repository in the user's private connection file. Never print or commit them. To update the live Azure database, use the guarded import/migrations in `scripts/Complete-Setup.ps1`; do not claim live DB PASS unless the commands actually ran.

Preserve user work and real Git history. Never force-push or fabricate verification. If a check cannot run, report `NOT_RUN`/`BLOCKED` instead of PASS. Respond to the user in Croatian.
