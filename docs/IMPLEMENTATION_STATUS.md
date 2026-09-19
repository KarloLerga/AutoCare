# Implementation status - profesorov finalni model

Datum provjere: 2026-09-19

## Implementirano

- Profesorov finalni domain/JPA model: `VehiclePriceClass`, `WorkPriceRange`, `CatalogCategory` i `ProblemCategory`.
- Katalog: 30.366 varijanti, 120 radova, 30 maintenance + 90 repair i 600 min-max raspona.
- Bilješke bez dijagnostike, scoringa, suggested repaira i procijenjenog troška.
- Servisi sa stvarnom cijenom; održavanje se računa iz stvarne servisne povijesti kroz Strategy obrazac.
- Uklonjen runtime `VehicleWorkRule` model i stari complete-catalog/reference-data importer.
- Novi `CatalogView` / `CatalogController` tok.
- Guarded migracija `schema/11_professor_model.sql` i read-only audit `schema/12_professor_model_audit.sql`.

## Stvarne lokalne provjere

| Provjera | Rezultat |
|---|---|
| `mvnw.cmd clean verify` | PASS; 71 Java klasa, 12 testova, 0 grešaka |
| setup `clean package` | PASS; 3 setup Java klase, bez setup testova u profesorovom paketu |
| `mvnw.cmd javadoc:javadoc` | PASS |
| `scripts/validate_professor_catalog.py` | PASS; 30.366 / 120 / 600, 0 errors |
| `scripts/build_professor_migration.py` | PASS; determinističan LF izlaz, hash odgovara `FINAL_DATA_SHA256.txt` |
| `check-runtime-style.ps1` | PASS; 71 Java datoteka |
| `check-secrets.ps1` | PASS; nema commitanih vjerodajnica |
| Python `py_compile` | PASS za finalne Python skripte |

## Azure / GUI status

- Azure SQL dry-run: PASS; firewall sada dopušta vezu i preflight je read-only.
- `schema/11_professor_model.sql`: PASS; migracija je primijenjena u transakciji.
- `schema/12_professor_model_audit.sql`: PASS; `final_error_count = 0`.
- GUI launch: PASS; završni Java proces ima prozor `AutoCare`.
- Interaktivni GUI klik-smoke: NOT_RUN jer Windows Computer Use kanal nije dostupan.
- Privatni connection config i lozinka ostaju izvan repozitorija.

## Potvrđeno nakon Azure migracije

- `vehicle_variant = 30366`
- `work_definition = 120`
- `work_price_range = 600`
- `vehicle_work_rule`, `diagnostic_rule`, `suggested_repair_id` i `estimated_cost` ne postoje
- `final_error_count = 0`

## Git

Prethodni HEAD prije ove integracije: `de14d5d`.

| Faza | Commit |
|---|---|
| Runtime i katalog workflow | `b91cca8` — `refactor: align notes and catalog with approved workflow` |
| Katalog podaci i rasponi cijena | `e74c250` — `data: add catalog price ranges and vehicle classes` |
| SQL migracija i čišćenje starih importera | `c6f928e` — `database: migrate to catalog price range model` |
| Sigurni dry-run setup alata | `5dc2433` — `fix: make final migration dry-run safe` |
| SQL kompatibilnost postojeće baze | `ab1f858` — `fix: make professor migration compatible with existing schema` |
| Runtime code-clean integracija | `8513faa` — `refactor: align runtime with final catalog model` |
| Testovi za read-only katalog | `2a72ae3` — `test: align checks with read-only catalog entities` |
