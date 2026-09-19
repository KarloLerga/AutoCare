# AutoCare final SQL artifacts

Runtime koristi postojeću Azure SQL Database i Hibernateovu `CamelCaseToUnderscoresNamingStrategy`.
Java ostaje camelCase, fizička baza ostaje `dbo`/snake_case.

## Aktivne datoteke

### `11_professor_model.sql`

Guarded one-time migracija na profesorov završni model. Po defaultu je read-only (`@Apply = 0`). Setup alat ga prebacuje na `@Apply = 1` samo uz eksplicitni `--apply --confirm-final-schema`.

Migracija:

- dodaje/puni `vehicle_variant.price_class` za 30.366 varijanti;
- dodaje/puni `work_definition.catalog_category`, `interval_km`, `interval_months`;
- dodaje/puni `problem.category`;
- uklanja `problem.suggested_repair_id` i `problem.estimated_cost` ako postoje;
- kreira i puni `work_price_range` s 600 redaka;
- uklanja staru `vehicle_work_rule` tablicu;
- uklanja staru `diagnostic_rule` tablicu;
- radi finalne business sanity checkove prije commita.

### `12_professor_model_audit.sql`

Read-only završni audit. Nakon stvarne migracije mora završiti s:

```text
final_error_count = 0
```

### `naming_map.csv`

Čitljiva mapa aktualnih Java polja na snake_case SQL stupce finalnog modela. Nije migracija.

## Kako pokrenuti

Ne izvršavati ručno izmjenom `@Apply` vrijednosti. Koristiti `scripts/Complete-Setup.ps1` i privatni connection config:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

Nakon read-only provjere:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel
```

Runtime ima `hibernate.hbm2ddl.auto=none`; normalno pokretanje aplikacije ne mijenja shemu.
