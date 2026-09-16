# Verification evidence - preparation baseline and local run
Date: 2026-09-16. The first section below is the preserved preparation baseline from the delivered reference package. The later local-run section records the actual Java 25/Azure SQL integration performed afterward; neither section claims GUI or photo work that was not executed.

## Local integration run (2026-09-16)

- `db-list` reached the configured Azure SQL server after the user allowed the workstation IP and discovered one existing database; its name remains external.
- `sql-check`, explicit `schema-update --confirm-development-schema`, and `db-check` passed against that existing database.
- Sample seed and identical sample rerun passed: 8 variants / 122 works / 403 rules / 87 diagnostics.
- Full AF3 seed resumed after a partial first process and completed idempotently: 30,366 variants / 122 works / 1,650,435 rules / 87 diagnostics. Referenced intervals remained opt-in.
- Read-only `scripts/verify-database.sql` passed; final counts, indexes, FKs and invariants are in `docs/evidence/azure-seed-validation.md`.
- Real `.\mvnw.cmd clean verify` and `.\mvnw.cmd javadoc:javadoc` passed on Java 25. Python AF3/data validators passed under Python 3.12.4 with explicit UTF-8 and resource-safe file handling.
- GUI/manual flows, isolated `_test` SQL profile and image enrichment remain NOT_RUN.

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
