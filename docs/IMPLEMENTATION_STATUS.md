# Local implementation status (resume point)
Updated: 2026-09-16 during the local Codex integration and Azure SQL run. This file records only commands actually executed on this workstation.

| Phase | Status | Evidence / next action |
|---|---|---|
| Reference preparation | OFFLINE CHECKS EXECUTED | See VERIFICATION.md and docs/evidence |
| F0 local Git/JDK25/Azure name | PASS | JDK 25 and external private config confirmed; read-only `db-list` reached Azure and discovered one existing database. The confirmed name remains only in the external private config. See docs/evidence/local-azure-check.md |
| F1 real Maven/JDK25 build | PASS | Official Maven 3.9.16 bootstrap/checksum, wrapper generation and `mvnw clean verify`; commit `dfd301c` |
| F2 local full unit run | PASS | Java 25 Maven tests plus Windows UTF-8 Python data tests; validator resource-warning fix is included in commit `ae9cd52` |
| F3 actual SQL/JPA/schema/sample | PASS | `sql-check`, explicit `schema-update`, `db-check`, read-only schema/FK/index inspection, sample seed and identical sample rerun all passed against the existing Azure SQL database; evidence commit `ae9cd52` |
| F4-F6 actual GUI flows | NOT_RUN | Windows FlatLaf/DB interactions |
| F7 actual full seed/rerun | PASS | Sample first+second run and full AF3 seed/resume completed: 30,366 variants / 122 works / 1,650,435 rules / 87 diagnostics. Referenced intervals stayed opt-in; evidence commit `ae9cd52` |
| F8 actual photos | NOT_RUN | Plans/tools ready; candidates require review |
| F9 final docs/Javadoc/Git DAG | NOT_RUN | Actual executed evidence and screenshots needed |

## Local execution log

- 2026-09-16: JDK/Javac `25.0.4.1` detected from Eclipse Adoptium.
- 2026-09-16: Maven 3.9.16 downloaded into ignored `.tools/` and verified against Apache SHA-512; the bootstrap script required two Windows PowerShell compatibility fixes.
- 2026-09-16: Official only-script Maven wrapper generated with `maven-wrapper-plugin:3.3.4`.
- 2026-09-16: `.\mvnw.cmd clean verify` passed with 86 production sources, 5 test sources and 8 tests total (7 `CoreTest`, 1 `SqlInfrastructureTest`).
- 2026-09-16: SQL credentials are loaded only from an external local configuration; no password or token is stored in this repository or this log.
- 2026-09-16: The initial read-only `db-list` was blocked by the Azure firewall; after the user allowed the current client IP, the same read-only command discovered one existing database. The name is intentionally omitted here and remains external.
- 2026-09-16: `sql-check` passed with SQL Server TLS verification enabled; explicit `schema-update --confirm-development-schema` created the JPA schema in the selected existing database, and `db-check` passed with 30,366 catalog variants.
- 2026-09-16: Sample seed passed and an identical sample rerun preserved 8 variants / 122 works / 403 rules / 87 diagnostics without duplicates.
- 2026-09-16: The full seed resumed after an earlier process ended with 1,438,118 committed rules; the idempotent rerun completed with 30,366 variants / 122 works / 1,650,435 rules / 87 diagnostics. No `TRUNCATE`, disabled FK or TLS bypass was used.
- 2026-09-16: Read-only schema verification passed with zero duplicate variant codes, zero duplicate variant/work groups, 367,519 NULL estimates, and the expected primary/unique/index/FK structures. Full details are in docs/evidence/azure-seed-validation.md.
- 2026-09-16: Python AF3 structural audit, 31 AF3 tests, 5 estimate tests and 4 catalog tests pass under Windows Python 3.12.4 with `-W error::ResourceWarning`; the validator file-close fix is included in commit `ae9cd52`.
- 2026-09-16: Real `mvnw javadoc:javadoc` passed on Java 25. GUI interaction/DPI/cancellation scenarios, isolated SQL Server `_test` profile and photo enrichment remain NOT_RUN.

Record each future run with date, phase, exact non-secret command, outcome, relevant commit SHA, blocker and next step. Do not replace blocked results with imagined success.
