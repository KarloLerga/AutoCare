# Azure SQL setup

Projekt koristi postojeću Azure SQL Database. Ne stvara server ni novu bazu.

## Privatna konfiguracija

Konekcijske podatke držati izvan repozitorija u lokalnom JSON-u. `Load-Connection.ps1` ih pretvara u procesne `AUTOCARE_DB_*` varijable.

JDBC koristi:
- Microsoft SQL Server driver
- port 1433
- `encrypt=true`
- `trustServerCertificate=false`

## Prvi prolaz nakon profesorove promjene modela

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

Bez `-ApplyProfessorModel` skripta radi build, SQL connection check i read-only dio migracije. Baza se ne mijenja.

Kad je potvrđena točna postojeća baza:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel
```

Migracija `schema/11_professor_model.sql` radi unutar jedne SQL transakcije i završni audit je read-only.

## Svakodnevno pokretanje

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Run-App.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

Runtime ima `hibernate.hbm2ddl.auto=none`; aplikacija ne mijenja shemu pri normalnom pokretanju.

## VS Code MSSQL

Za ručnu provjeru može se koristiti službena MSSQL ekstenzija. SQL login i lozinka nisu isto što i AutoCare korisnički račun.

## Sigurnosne napomene

- ne commitati `connection.local.json`;
- ne stavljati password u URL ili source;
- ne uključivati `trustServerCertificate=true`;
- ne pokretati migraciju nad `master`, `model`, `msdb` ili `tempdb`;
- ne označavati migraciju PASS bez stvarnog `final_error_count = 0`.
