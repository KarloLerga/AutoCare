# Verification - finalni profesorov model

## Lokalno PASS u ChatGPT paketu

### Finalni katalog

```text
python scripts/validate_professor_catalog.py
```

Rezultat:

- vehicle_variants = 30366
- work_definitions = 120
- maintenance_works = 30
- repair_works = 90
- price_ranges = 600
- errors = 0

### Core/offline provjere

Lokalnim compile sanity okruženjem pokrenuti su:

- `CoreChecks` -> 37 checks PASS
- `SqlOfflineChecks` -> 4 checks PASS

### Runtime stil

`src/main/java` nema:

- lambda izraze
- Stream API
- Optional
- java.util.function
- var
- record
- stare diagnostic runtime klase
- VehicleWorkRule runtime model

### Java source consistency

Main i test Java source se u ovom containeru uspješno kompajliraju protiv minimalnih lokalnih API stubova za vanjske biblioteke. Ovo provjerava Java sintaksu i interne tipove/metode, ali nije zamjena za pravi Maven/JPA/Hibernate build.

### SQL/data tooling

- `validate_professor_catalog.py` PASS
- `py_compile` finalnih Python skripti PASS
- `build_professor_migration.py` je determinističan: ponovni generator daje identičan `schema/11_professor_model.sql`

## Mora stvarno izvršiti Codex na korisnikovom računalu

### Maven/JDK25

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javadoc:javadoc
```

Ovaj container ima Java 21 i nema Maven dependency cache; Maven Wrapperu je onemogućen download s Maven Centrala. Zato se ovo ovdje ne smije označiti PASS.

### Azure SQL

Prvo dry-run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

Zatim stvarna migracija:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel
```

Završni audit mora prikazati:

```text
final_error_count = 0
```

### GUI smoke

Na Windows/Swing okruženju ručno provjeriti registraciju, login, vozila, Bilješke, Katalog, Servise, Održavanje, Dashboard i persistence aktivnog vozila.

Detaljan tok je u `00_CODEX_START_HERE.md`.
