# Local Azure SQL connectivity evidence

Date: 2026-09-16

## Initial probe

The first read-only probe reached `auto-care.database.windows.net` but Azure SQL rejected the workstation's client IP by firewall policy. No firewall rule, database, schema or seed was changed by the repository tooling.

## Follow-up after the firewall change

The user allowed the current client IP on the existing Azure SQL server. The following commands were then executed with the private configuration outside the repository; the confirmed database name is intentionally represented by a placeholder here and remains only in that private file.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "& { . '.\scripts\Load-Connection.ps1' -ConfigPath 'EXTERNAL_PRIVATE_CONFIG'; & java -jar '.\target\autocare-1.0.0.jar' db-list }"

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "& { . '.\scripts\Load-Connection.ps1' -ConfigPath 'EXTERNAL_PRIVATE_CONFIG' -DatabaseName 'CONFIRMED_EXISTING_DATABASE'; & java -jar '.\target\autocare-1.0.0.jar' sql-check }"
```

Results:

- `db-list`: PASS; exactly one existing user database was discovered.
- `sql-check`: PASS; SQL Server connection succeeded with `encrypt=true;trustServerCertificate=false`.
- Explicit `schema-update --confirm-development-schema`: PASS against the exact discovered database.
- `db-check`: PASS; Hibernate/JPA opened the database in validate mode and reported 30,366 catalog variants.

The user's Azure default directory was noted as deployment context. The application uses SQL authentication through environment variables loaded from the external file; no directory token or password is committed.

The initial firewall rejection and the follow-up success are both retained here so the evidence does not rewrite history. Full seed, counts, FK/index checks and the resume after the partial first import are recorded in `azure-seed-validation.md`.
