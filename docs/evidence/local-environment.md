# Local environment evidence

Date: 2026-09-18

## Repository

- Working directory: `C:\src\AutoCare`
- Branch: `main`; existing project history was preserved and the final fixes are being committed in coherent phases.
- Source integrated from the ZIP's `project/` directory only. `private/`, `history/`, `source-materials/` and the original ZIP remain outside the repository.

## Toolchain

- Java: Eclipse Adoptium OpenJDK `25.0.4.101` (Java 25)
- Javac: `25.0.4.101`
- Maven: Apache Maven `3.9.16`, downloaded to ignored `.tools/` and checksum-verified.
- Maven wrapper: official only-script wrapper generated with `maven-wrapper-plugin:3.3.4`.

## Commands executed

- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\bootstrap-maven.ps1`
- `& .\\.tools\\apache-maven-3.9.16\\bin\\mvn.cmd -N org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper -Dmaven=3.9.16 -Dtype=only-script`
- `.\\mvnw.cmd clean verify`
- `.\\mvnw.cmd clean verify`
- `.\\mvnw.cmd -f tools\\setup\\pom.xml clean package`
- `py -3 -m unittest discover -s tests -v` (from `tools\\reference-data`)
- `py -3 tools\\test_calculate_estimates.py`
- `py -3 tools\\test_prepare_catalog.py`
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\check-runtime-style.ps1`
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\check-secrets.ps1`

## Results

- `clean verify`: PASS on Java 25; 12 tests, 0 failures, 0 errors.
- Setup module `clean package`: PASS; 3 setup tests, 0 failures, 0 errors.
- Reference-data tests: PASS; 17 Python unit tests, 5 estimate tests and 4 catalogue-preparation tests.
- Runtime-style and secret scans: PASS; 66 Java files checked and no repository credential was detected.
- Azure SQL: contacted successfully after the user allowed the current client IP. The database is `free-sql-db-0650603`; credentials remain only in the external private configuration.
- Azure SQL `sql-check`: PASS with SQL Server engine edition 5 and `encrypt=true;trustServerCertificate=false`.
- Complete catalogue import: PASS; 30,366 variants, 120 works and 2,908,857 rules (693,784 maintenance, 2,215,073 repairs).
- Guarded cleanup migrations `09` and `10_croatian_work_names`: PASS.
- Final read-only audit: PASS for expected catalogue counts and all gating checks. It records two source rows whose engine text ends in `5MTFWD`; these are retained to preserve the package's authoritative 2,908,857-row catalogue and are documented in `final_catalog_audit.txt`.
- Azure directory: the user confirmed the Azure default directory; the application connection itself uses the supplied SQL authentication settings, not an Azure API token.
- Credentials: external local configuration only; password intentionally redacted and never written here.
