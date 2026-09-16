# Local implementation status (resume point)
Updated: 2026-09-16 during the local Codex integration run. This file records only commands actually executed on this workstation.

| Phase | Status | Evidence / next action |
|---|---|---|
| Reference preparation | OFFLINE CHECKS EXECUTED | See VERIFICATION.md and docs/evidence |
| F0 local Git/JDK25/Azure name | PASS with DB discovery pending | JDK 25 and external private config confirmed; Azure database name still requires read-only discovery or portal value. See docs/evidence/local-environment.md |
| F1 real Maven/JDK25 build | PASS | Official Maven 3.9.16 bootstrap/checksum, wrapper generation, `mvnw clean verify`; initial integration commit follows after staged review |
| F2 local full unit run | NOT_RUN | Preparation core checks are separately documented |
| F3 actual SQL/JPA/schema/sample | NOT_RUN | Retry read-only database discovery and SQL Server validation locally; do not use an unisolated production-like database for destructive tests |
| F4-F6 actual GUI flows | NOT_RUN | Windows FlatLaf/DB interactions |
| F7 actual full seed/rerun | NOT_RUN | Sample then full; benchmark free compute |
| F8 actual photos | NOT_RUN | Plans/tools ready; candidates require review |
| F9 final docs/Javadoc/Git DAG | NOT_RUN | Actual executed evidence and screenshots needed |

## Local execution log

- 2026-09-16: JDK/Javac `25.0.4.1` detected from Eclipse Adoptium.
- 2026-09-16: Maven 3.9.16 downloaded into ignored `.tools/` and verified against Apache SHA-512; the bootstrap script required two Windows PowerShell compatibility fixes.
- 2026-09-16: Official only-script Maven wrapper generated with `maven-wrapper-plugin:3.3.4`.
- 2026-09-16: `.\mvnw.cmd clean verify` passed with 86 production sources, 5 test sources and 8 tests total (7 `CoreTest`, 1 `SqlInfrastructureTest`).
- 2026-09-16: SQL credentials are loaded only from an external local configuration; no password or token is stored in this repository or this log.
- 2026-09-16: Azure SQL schema, authentication, database discovery, seed, GUI and Javadoc evidence are still pending and must not be described as executed.

Record each future run with date, phase, exact non-secret command, outcome, relevant commit SHA, blocker and next step. Do not replace blocked results with imagined success.
