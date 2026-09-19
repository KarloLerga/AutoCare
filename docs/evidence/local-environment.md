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

## Azure SQL PASS

`scripts/Complete-Setup.ps1 -ConfigPath ...` completed the local build/setup stages, passed the Azure SQL connection and read-only dry-run, applied migration `11`, and ran audit `12` with `final_error_count = 0`.

Confirmed database counts: `vehicle_variant=30366`, `work_definition=120`, `work_price_range=600`, `problem_count=2`, `service_record_count=6`, `service_item_count=11`. The old `vehicle_work_rule`, `diagnostic_rule`, `suggested_repair_id` and `estimated_cost` structures are absent.

GUI smoke for the new model remains pending until the application window is launched and inspected.
