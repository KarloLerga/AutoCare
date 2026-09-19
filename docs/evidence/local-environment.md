# Local environment evidence

Date: 2026-09-19

- Repository: `C:\src\AutoCare`, existing Git history preserved.
- Toolchain: Eclipse Adoptium JDK 25.0.4.101 and Maven Wrapper.
- Private Azure SQL configuration: external `C:\private-autocare\connection.local.json`; credentials are not stored in Git.

## PASS

- `mvnw.cmd clean verify`: 12 tests, 0 failures/errors.
- `mvnw.cmd javadoc:javadoc`.
- setup `clean package`.
- `scripts/validate_professor_catalog.py`: 30.366 variants, 120 works, 600 ranges, 0 errors.
- `scripts/build_professor_migration.py`: generated migration hash matches `FINAL_DATA_SHA256.txt`.
- `scripts/check-runtime-style.ps1`: 71 Java files.
- `scripts/check-secrets.ps1` and Python `py_compile`.

## BLOCKED

`scripts/Complete-Setup.ps1 -ConfigPath ...` completed the local build/setup stages but Azure SQL `sql-check` was rejected by the server firewall for the current public client IP. Therefore the professor migration, final SQL audit and GUI smoke are not marked PASS.
