# Verification evidence - live delta, cleanup, preparation baseline and local run

## Current delta evidence - live Azure SQL compatibility

Date: 2026-09-17. Live evidence commit: `d18fde7`. This is the first live validation of the post-F7 runtime naming delta. No
history reset, rebase or force-push was used, and no 100-row synthetic seed was inserted.

- `sql-check`: PASS against the selected existing Azure SQL database; SQL Server EngineEdition 5,
  TLS remained `encrypt=true; trustServerCertificate=false`.
- `schema/06_student_runtime_compat.sql` with its default `@Apply = 0`: PASS/read-only. The first
  result showed the existing `dbo.work_definition` and `dbo.service_record`, missing only the two
  nullable default interval columns and the legacy `request_key` default.
- After exact database verification, the guarded statements from `06_student_runtime_compat.sql`
  were applied once. They added only `default_interval_km int NULL`, `default_interval_months int NULL`
  and `DF_autocare_service_record_request_key`; no rename, drop, truncate, rebuild or data backfill
  was run. A second read-only execution returned all three compatibility flags present.
- Hibernate `db-check`: PASS in `validate` mode with the real Microsoft SQL Server driver and
  Hibernate 7.4.8; it opened the database and reported 30,366 catalog variants.
- `scripts/verify-database.sql`: PASS after updating its active diagnostics to the current
  snake_case physical names. Read-only counts were 30,366 vehicle variants, 122 work definitions,
  1,650,435 scoped rules and 87 diagnostic rules; duplicate variant codes and duplicate
  variant/work pairs were both 0. The existing database has no user/service rows to exercise GUI
  ownership CRUD.
- The isolated Maven `sqlserver-it` profile remains `NOT_RUN` because no separately approved `_test`
  database is available. Native Windows GUI/manual CRUD, DPI and cancellation checks remain
  `BLOCKED` because the Computer Use native pipe is unavailable.

## Current delta evidence — after F7

Date: 2026-09-17. Delta commit `be08831` is based on the previously pushed V2 history; no reset,
rebase or force-push was used.

- `git diff --check`: PASS before the code commit; only expected Git line-ending warnings were emitted.
- `.\mvnw.cmd -q clean verify`: PASS with `Additional offline checks passed: 17`.
- `.\mvnw.cmd -q package`: PASS.
- `.\mvnw.cmd -q javadoc:javadoc`: PASS on JDK 25.
- `.\mvnw.cmd -q install -DskipTests`: PASS; the updated runtime artifact was installed for setup verification.
- `.\mvnw.cmd -q -f tools\setup\pom.xml clean test package`: PASS.
- `persistence.xml`: PASS static review; `CamelCaseToUnderscoresNamingStrategy` is configured and runtime remains `validate`.
- `VehicleVariant`, `ServiceItem` and `Maintenance*` delta: PASS static review and Maven compilation; no diagnostics or diagram file changed.
- `schema/06_student_runtime_compat.sql`: PASS static safety review; it defaults to `@Apply = 0`, checks the exact database before apply, adds only the two nullable snake_case interval columns and an optional legacy request-key default. It was not executed because no private connection configuration/database target was available in this environment.
- Actual Hibernate `validate`, compat apply, Azure CRUD/manual GUI and native Windows interaction: `NOT_RUN`/`BLOCKED`; no skipped scenario is labelled PASS.

## V2 baseline evidence (before delta)

Date: 2026-09-16. V2 phases F1-F7 are committed and pushed through `1d4bc11`. The diagnostics
implementation remains frozen; this section records the new simplification evidence separately from
the historical AF3/CLEAN runs below.

- `.\mvnw.cmd -q test`: PASS after F7; `Additional offline checks passed: 18`.
- `git diff --check`: PASS before the F7 commit; only expected Git line-ending warnings were emitted.
- Schema manifest and entity manifest: PASS static JSON parse; both contain 69 mapped target columns,
  including two nullable WorkDefinition defaults and excluding ServiceRecord.requestKey.
- `schema/05_student_simplification_v2.sql`: REVIEWED, not executed. It defaults to `@Apply = 0`,
  requires explicit database/recovery/application/dependency confirmation for apply, and has rollback
  handling. No live Azure schema was changed by V2.
- Current source counts after F7: 78 production Java files / 6,282 lines and 9 test Java files /
  1,017 lines. These counts are descriptive, not a quality score.

F8 final docs/build/package/secret evidence:

- `.\mvnw.cmd -q clean verify`: PASS; Maven/JDK25 tests and `Additional offline checks passed: 18`.
- `.\mvnw.cmd -q install -DskipTests`: PASS.
- `.\mvnw.cmd -q -f tools\setup\pom.xml clean test package`: PASS.
- `.\mvnw.cmd -q javadoc:javadoc`: PASS on JDK 25.
- `scripts/check-secrets.ps1` on staged docs: PASS.
- Runtime/package audit: PASS; runtime JAR has no setup classes or private/reference-data payloads and
  includes the three local vehicle fallback/provenance resources. Setup JAR contains the developer CLI.
- `git diff --cached --check` before F8 commit: PASS; no diagram files were changed.
- F8 docs commit is `336a695`; it is pushed to `origin/main`.

Target-name Hibernate validation against Azure, the isolated `_test` SQL profile and native Windows GUI
interaction remain `BLOCKED`/`NOT_RUN`; no skipped scenario is labelled PASS.
Date: 2026-09-16. This file keeps historical evidence separate from the current REVIEW-CLEAN-2 worktree. No
unexecuted SQL, JPA or GUI scenario is labelled PASS.

## REVIEW-CLEAN-2 cleanup run (historical source baseline)

- R1 is committed as `f821f69`: `JpaTransactionRunner` preserves the first failure, service helpers do not open nested transactions, and request keys are canonicalized consistently.
- The runtime source contains the nine target entities (`AppUser`, `VehicleVariant`, `Vehicle`, `WorkDefinition`, `VehicleWorkRule`, `ServiceRecord`, `ServiceItem`, `Problem`, `DiagnosticRule`) and no developer tools package. Developer tooling is in `tools/setup`.
- JPA/XML and native SQL source were previously described by `schema/naming_manifest.json`; the current delta adds the runtime physical naming strategy, while that manifest remains a historical before/after map. Runtime XML explicitly uses `hibernate.hbm2ddl.auto=validate` and does not contain a password value.
- The existing Azure database was not changed by this cleanup. The current runtime is intended for its populated old snake_case contract after the minimal `schema/06_student_runtime_compat.sql` patch; the patch defaults to read-only and was not executed in this pass.
- Cleanup validation is limited to the actual commands recorded below. Actual Hibernate validation, compat apply, SQL CRUD/rollback tests and the separate `_test` profile are `NOT_RUN`/`BLOCKED` without an available approved target.
- Cleanup source/setup commit `1450b55` passed `mvnw -q clean verify`, `mvnw -q install -DskipTests`, independent setup `clean test package`, and `mvnw -q javadoc:javadoc` on JDK 25.
- The cleanup package test suite passed under UTF-8 Python: 39/39 tests. The source parser passed for 79 runtime Java files; the runtime JAR audit reported zero errors/warnings and no setup classes.
- `scripts/check-secrets.ps1` passed. Static SQL safety checks confirmed read-only defaults and rollback guards in both reviewed rename directions.

## Pre-cleanup Azure integration run (historical evidence)

The following Azure evidence belongs to the pre-cleanup schema and is retained only as historical context.

### Local integration run (2026-09-16)

- `db-list` reached the configured Azure SQL server after the user allowed the workstation IP and discovered one existing database; its name remains external.
- `sql-check`, explicit `schema-update --confirm-development-schema`, and `db-check` passed against that existing database before the naming cleanup.
- Sample seed and identical sample rerun passed: 8 variants / 122 works / 403 rules / 87 diagnostics.
- Full AF3 seed resumed after a partial first process and completed idempotently: 30,366 variants / 122 works / 1,650,435 rules / 87 diagnostics. Referenced intervals remained opt-in.
- Read-only `scripts/verify-database.sql` passed; final counts, indexes, FKs and invariants are in `docs/evidence/azure-seed-validation.md`.
- Real `.\mvnw.cmd clean verify` and `.\mvnw.cmd javadoc:javadoc` passed on Java 25. Python AF3/data validators passed under Python 3.12.4 with explicit UTF-8 and resource-safe file handling.
- GUI/manual flows are BLOCKED by the unavailable Windows Computer Use native pipe, and the isolated `_test` SQL profile remains NOT_RUN because no separate approved database exists. Local generated category illustrations were added and verified as classpath resources; exact model-specific image lookup, manual generation review and licence approval remain NOT_RUN.

## Preserved preparation baseline

The following evidence was produced before the local deployment and remains intentionally separate from the later Azure results:

## Executed
| Check | Result | Meaning / evidence |
|---|---|---|
| CoreChecks | 57 assertions passed | Business/input calculations executed on JDK21; entity annotations available through preparation-only API substitutes; no ORM operation performed. evidence/core_checks.txt |
| SqlOfflineChecks | 25 assertions passed | Config, SQL-target guard, estimate/schedule behaviour; no DB connection. evidence/sql_offline_checks.txt |
| AF3 Python tests | 31 tests passed | Full source catalog counts, scoped operations, schedule exclusions, image URL/licence/resize safeguards, checksums. evidence/python_tests.txt |
| Existing offline Python tools | 9 tests passed | Original cost-input/catalog-normalizer helpers. evidence/legacy_tools_tests.txt |
| Full structural data audit | 3,704,652 pairs checked | Correct category counts, consistency/rounding; not market accuracy. evidence/full_data_audit.json |
| Full Java seed dry run | 30,366 variants /122 works /1,650,435 rules /34 referenced intervals /87 diagnostic rules | Streaming hash/content validation, no DB contacted. evidence/full_java_seed_validation.txt |
| Pure JDBC/CSV/config sources | javac21 compiled | SqlSettings, CsvReader, SeedFiles, SqlSeedTool, ImagePathTool, ReviewedIntervalTool, no third-party substitutes for this subset |
| Entire 86-file production source | STATIC CHECK ONLY | javac21 with TEMPORARY JPA/FlatLaf API substitutes OUTSIDE delivery; not real Maven/API/provider proof. evidence/static_compile_context.txt |
| Diagram sources | Graphviz rendering completed | Current DOT -> PNG/SVG, conceptual diagrams, not live schema introspection |
| Image grouping | 6,697 plans | No network lookup, photo, generation or licence approval from a real response |
| Azure network probe | FAILED | Temporary failure in name resolution; authentication_attempted=false; schema_modified=false. evidence/azure_network_check.json |

Python tests used the installed preparation Pillow. The pinned Pillow12.3.0 environment must still be installed/tested locally. Passing data tests does not prove accurate individual Croatian prices or OEM schedules. Multiple build iterations were run while fixing configuration; do not describe that as a measured identical-rebuild guarantee for the final AF3 data without a fresh independent reproduction.

## NOT executed / mandatory local work
- Java25 with real pinned Maven dependencies, actual clean verify and actual Javadoc generation.
- Real Hibernate schema generation/validate against Azure SQL Server, nullable-FK/index/identity/type inspection.
- SQL Server integration profile, locking/rollback/ownership/concurrency behaviours in a real engine.
- Real Microsoft JDBC bulk-copy/temporary-table import, first+second seed, speed/compute measurement and interrupted-import recovery.
- Windows PowerShell5.1/7 setup wrapper, actual VS Code Java runtime and MSSQL extension connectivity.
- Actual Windows FlatLaf GUI, all user interactions at100/125/150%DPI, delayed callbacks/cancellation/focus.
- Actual Wikidata/Commons lookup/download, per-image generation/licence review and final imagePath import.
- Real commits/pushes into user's repo and resulting Git DAG. Repo was inspected as empty; no remote mutations were made.

## Why the distinction matters
An API substitute makes it possible to catch ordinary Java errors offline, but cannot tell whether Hibernate7.4 accepts a mapping, the JDBC driver returns a result as assumed, or SQL Server accepts the generated DDL. Those substitutes are not shipped, are not dependencies and must never be recreated in the actual project to bypass a failure. A successful photo helper test does not approve a photograph. A1.65million-row CSV is not a survey of1.65million real service quotes.

For local completion follow ACCEPTANCE.md and update IMPLEMENTATION_STATUS.md. Keep original preparation evidence and add dated local evidence, never relabel an unexecuted scenario PASS.
