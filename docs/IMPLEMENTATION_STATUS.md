# Local implementation status (resume point)

## V2 studentska simplifikacija — aktualni zapis

Updated: 2026-09-16. Ovaj odjeljak je aktualan za commitove nakon REVIEW-CLEAN-2 i nadopunjuje
povijesne AF3/CLEAN redove ispod. Svaka faza je stvarno implementirana u repozitoriju i pushana na
`main`; dijagnosticki kod je ostao izvan funkcionalnog V2 reza.

| V2 faza | Status | Commit / dokaz |
|---|---|---|
| F1 dark FlatLaf i jednostavnija tema | PASS | `9d68dd9`; Maven testovi nakon faze |
| F2 jednostavan SwingWorker/session tok | PASS | `e053fc4`; Maven testovi nakon faze |
| F3 servisni save bez request-key flowa | PASS | `e05eb5c`; ServiceRecord/ServiceInput/forma uskladeni |
| F4 puna servisna povijest bez paging statea | PASS | `2e582ea`; list API i SQL IT uskladeni |
| F5 repository/runner simplifikacija | PASS | `4f0762d`; runner lifecycle testovi prosli |
| F6 aplikacijski servisi i DTO version flow | PASS | `e06888c`; auth/vehicle/controller potpisi uskladeni |
| F7 maintenance model i schema delta | PASS | `1d4bc11`; `mvnw -q test`, dodatne offline provjere: 18 |
| F8 dokumentacija i završna verifikacija | U TIJEKU | Nakon doc uskladivanja slijede finalni build/Javadoc/secret/package auditi |

V2 SQL promjena nije pokrenuta nad postojećom Azure bazom. `schema/05_student_simplification_v2.sql`
je read-only po defaultu i dodaje samo nullable `defaultIntervalKm`/`defaultIntervalMonths` na ciljnu
`dbo.WorkDefinition` tablicu. Target-name Hibernate validate, izolirani SQL Server profil i native
Windows GUI smoke ostaju `BLOCKED`/`NOT_RUN` bez zasebne odobrene `_test` baze i dostupnog GUI kanala.
Updated: 2026-09-16 during the local Codex integration and REVIEW-CLEAN-2 cleanup. This file records only commands actually executed on this workstation.

## REVIEW-CLEAN-2 cleanup status

The status below applies to the cleanup worktree, not to the earlier Azure run whose schema used the old
snake_case names. The target JPA contract is in `schema/naming_manifest.json`; the existing Azure database was
not renamed in this phase.

| Cleanup phase | Status | Evidence / next action |
|---|---|---|
| R1 transaction review | PASS | Commit `f821f69`; one transaction boundary remains in the service and the runner preserves original failures. |
| C1 readability | PASS | Google Java Format 1.27.0 and the final explicit-import/brace audit passed over all repository Java sources. |
| C2 setup isolation | PASS | Seven developer classes are under `tools/setup`; the root POM has no modules and setup depends on installed `hr.unizd:autocare`. |
| C3/C4 naming and JPA | PASS (source) | `AppUser`, `productionYear`, `serviceDate`, standard implicit names and preserved constraints are in the runtime sources. |
| C5 existing-database migration | BLOCKED / NOT_RUN | The available database is the populated old snake_case schema and there is no separately approved copy/restore target. Migration scripts default to read-only. |
| C6 simplification | PASS | Runtime `Main` is GUI-only; `specific` and the legacy schedule fallback/revise path are removed. |
| C7 distribution boundary | PASS | Runtime and setup have separate Maven artifacts; the clean runtime JAR has no setup tools or private/reference-data payloads. |
| C8 final verification | PASS with explicit blockers | Commit `1450b55` passed clean Maven verify, setup test/package, Javadoc, parser, runtime-JAR audit, UTF-8 cleanup-package tests (39/39), secret check and SQL-script safety checks. Target-name SQL/JPA integration and GUI remain NOT_RUN/BLOCKED under C5/F4-F6. |

### REVIEW-CLEAN-2 executed evidence

- Source/setup commit: `1450b55` (`refactor: isolate setup tools and align JPA naming`).
- `.\mvnw.cmd -q clean verify`: PASS; all Maven tests passed and `Additional offline checks passed: 20`. The expected runner test warning logs a simulated close failure after a successful commit.
- `.\mvnw.cmd -q install -DskipTests`: PASS; installed the main artifact for the independent setup build.
- `.\mvnw.cmd -q -f tools\setup\pom.xml clean test package`: PASS.
- `.\mvnw.cmd -q javadoc:javadoc`: PASS on JDK 25.
- Cleanup `runtime_audit.py`: PASS; 79 runtime Java files, zero source errors/warnings and zero runtime-JAR errors. Runtime JAR contains no setup tools; setup JAR contains `DatabaseTool`.
- Cleanup `ParseSources.java`: PASS; 79 main Java sources parsed.
- Cleanup package tests under UTF-8 Python: PASS, 39/39.
- `scripts/check-secrets.ps1`: PASS; credentials remain outside the repository.
- Reviewed SQL scripts: PASS static safety checks; rename/reverse scripts default to `DECLARE @Apply bit = 0`, contain rollback guards and do not execute against the current Azure database.

The F0-F9 rows below preserve earlier implementation evidence. F3 and F7 are historical because they were
executed before this name-only JPA refactor; they are not validation of the target schema.

| Phase | Status | Evidence / next action |
|---|---|---|
| Reference preparation | OFFLINE CHECKS EXECUTED | See VERIFICATION.md and docs/evidence |
| F0 local Git/JDK25/Azure name | PASS | JDK 25 and external private config confirmed; read-only `db-list` reached Azure and discovered one existing database. The confirmed name remains only in the external private config. See docs/evidence/local-azure-check.md |
| F1 real Maven/JDK25 build | PASS | Official Maven 3.9.16 bootstrap/checksum, wrapper generation and `mvnw clean verify`; commit `dfd301c` |
| F2 local full unit run | PASS | Java 25 Maven tests plus Windows UTF-8 Python data tests; validator resource-warning fix is included in commit `ae9cd52` |
| F3 actual SQL/JPA/schema/sample | PASS | `sql-check`, explicit `schema-update`, `db-check`, read-only schema/FK/index inspection, sample seed and identical sample rerun all passed against the existing Azure SQL database; evidence commit `ae9cd52` |
| F4-F6 actual GUI flows | BLOCKED | Windows Computer Use native pipe nije dostupan; neautomatizirani login/CRUD scenariji nisu označeni kao PASS. Nije stvoren testni korisnik u razvojnoj bazi. |
| F7 actual full seed/rerun | PASS | Sample first+second run and full AF3 seed/resume completed: 30,366 variants / 122 works / 1,650,435 rules / 87 diagnostics. Referenced intervals stayed opt-in; evidence commit `ae9cd52` |
| F8 local visual fallback | PASS | Added two local generated category illustrations, classpath provenance and offline `VehicleImageTest`; exact model-specific Commons candidates remain unapproved. See `docs/evidence/local-image-enrichment.md` |
| F9 final docs/Javadoc/Git DAG | PARTIAL | Javadoc i Git DAG stvarno provjereni; screenshot/DPI/manual evidence čeka dostupni Windows GUI helper. |

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
- 2026-09-16: Added `generic-vehicle.jpg` and `generic-electric.jpg` as local generated category illustrations, with packaged CSV/HTML provenance and classpath regression tests. They are not exact model photos and do not auto-approve any catalog group.
- 2026-09-16: Real `mvnw -q clean verify` passed after the image phase with 10 tests total (7 `CoreTest`, 1 `SqlInfrastructureTest`, 2 `VehicleImageTest`); all three local vehicle JPEG resources are packaged under `target/classes`.
- 2026-09-16: Real `mvnw javadoc:javadoc` passed on Java 25. Windows GUI interaction/DPI/cancellation scenarios are BLOCKED because the Computer Use native pipe is unavailable; the isolated SQL Server `_test` profile is NOT_RUN because no separate approved database exists. Exact model-specific Commons photo enrichment remains NOT_RUN; local generic fallback is covered by F8 and its evidence file.

Record each future run with date, phase, exact non-secret command, outcome, relevant commit SHA, blocker and next step. Do not replace blocked results with imagined success.
