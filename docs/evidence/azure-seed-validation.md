# Azure SQL schema and seed evidence

Date: 2026-09-16

All commands below were run from `C:\src\AutoCare` with credentials loaded from an external private file. The database name and password are intentionally omitted.

## Seed commands

```powershell
java -Xmx768m -jar target/autocare-1.0.0.jar seed-validate tools/reference-data/data
java -Xmx768m -jar target/autocare-1.0.0.jar seed-all tools/reference-data/data/sample --apply --acknowledge-model-estimates --with-diagnostics
java -Xmx768m -jar target/autocare-1.0.0.jar seed-all tools/reference-data/data --apply --acknowledge-model-estimates --with-diagnostics
```

The Java dry run passed before the database write:

`Offline seed: 30366 varijanti, 122 radova, 1650435 pravila, 34 referenciranih intervala, 87 tekstualnih pravila.`

The sample seed completed with:

`8 varijanti / 122 radova / 403 pravila / 87 dijagnostickih pravila.`

An identical sample seed was run again and produced the same counts without duplicate keys. The full seed was initially left with 1,438,118 committed rules when its first Java process ended without a final report. The same idempotent seed command was then rerun; its final output was:

`Ukupno u bazi: 30366 varijanti / 122 radova / 1650435 pravila / 87 dijagnostickih pravila.`

`Uvoz dovrsen. Cijene/povijest drugih izvora nisu prepisane. Ponovni uvoz je dopusten.`

The full command used bounded staging batches and committed chunks; no `TRUNCATE`, disabled foreign keys or TLS bypass was used. `referenced_intervals.csv` was not applied because those 34 interval candidates are an explicit opt-in review phase.

## Read-only database verification

`scripts/verify-database.sql` completed with exit code 0. The final read-only checks returned:

| Table | Rows |
|---|---:|
| `vehicle_variant` | 30,366 |
| `work_definition` | 122 |
| `vehicle_work_rule` | 1,650,435 |
| `diagnostic_rule` | 87 |
| `app_user` | 0 |
| `vehicle` | 0 |
| `service_record` | 0 |
| `service_item` | 0 |
| `problem` | 0 |

Additional invariants:

- `schedule_kind`: 1,304,769 `CONDITION_BASED`, 345,666 `UNKNOWN`.
- NULL estimated prices: 367,519; these remain unknown/quote-required, not zero.
- Duplicate variant codes: 0.
- Duplicate variant/work groups: 0.
- Primary/unique keys, `idx_vehicle_owner`, `idx_service_vehicle_date`, `idx_problem_vehicle_status` and the expected foreign keys were present and enabled.
- Hibernate/JPA `db-check` passed after the seed with SQL Server 17.0 and TLS certificate verification enabled.

The isolated Maven `sqlserver-it` profile was not run because no separate database ending in `_test` was available. GUI interaction, DPI/cancellation scenarios and photo enrichment remain NOT_RUN; these are not inferred from the seed result.
