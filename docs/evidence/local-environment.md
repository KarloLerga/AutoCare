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
- Azure SQL: not yet contacted in this phase. The host is the documented Azure SQL server; the actual database name remains unresolved. No schema or seed write was attempted.
- Credentials: external local configuration only; password intentionally redacted and never written here.
