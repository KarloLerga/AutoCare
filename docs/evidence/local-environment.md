# Local environment evidence

Date: 2026-09-16

## Repository

- Working directory: `C:\src\AutoCare`
- Initial branch state: `main`, no commits before this integration run.
- Source integrated from the ZIP's `project/` directory only. `private/`, `history/`, `source-materials/` and the original ZIP remain outside the repository.

## Toolchain

- Java: Eclipse Adoptium OpenJDK `25.0.4.1` (LTS)
- Javac: `25.0.4.1`
- Maven: Apache Maven `3.9.16`, downloaded to ignored `.tools/` and checksum-verified.
- Maven wrapper: official only-script wrapper generated with `maven-wrapper-plugin:3.3.4`.

## Commands executed

- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\bootstrap-maven.ps1`
- `& .\\.tools\\apache-maven-3.9.16\\bin\\mvn.cmd -N org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper -Dmaven=3.9.16 -Dtype=only-script`
- `.\\mvnw.cmd clean verify`
- `.\\mvnw.cmd -q test`

## Results

- `clean verify`: PASS on Java 25.
- Unit tests: PASS, 8 total; 7 `CoreTest` tests and 1 `SqlInfrastructureTest` test.
- Azure SQL: contacted successfully after the user allowed the current client IP. Read-only `db-list` discovered one existing database; its confirmed name remains only in the external private configuration.
- Azure SQL `sql-check`: PASS with SQL Server 17.0, engine edition 5, and `encrypt=true;trustServerCertificate=false`.
- Azure SQL schema: PASS via explicit `schema-update --confirm-development-schema`; subsequent `db-check`: PASS with 30,366 catalog variants.
- Azure SQL seed: PASS for the sample and full AF3 data; see `azure-seed-validation.md` for counts and read-only structural checks.
- Azure directory: the user confirmed the Azure default directory; the application connection itself uses the supplied SQL authentication settings, not an Azure API token.
- Credentials: external local configuration only; password intentionally redacted and never written here.
