# AutoCare — stvarni status implementacije

Updated: 2026-09-17. Ovo je evidence log za završni data/UI handoff. Radni tree je izveden iz baseline commita `3c7c941`; postojeća povijest nije prepisivana, resetirana ni force-pushana.

## Završene faze

| Faza | Status | Dokaz |
|---|---|---|
| Kompletni katalog | PASS | Reproducibilni generator i validator: 30.366 varijanti, 120 konkretnih radova, 2.944.248 pravila, 0 validacijskih grešaka. |
| Modelirana pravila cijena i intervala | PASS | 728.470 maintenance pravila s pozitivnom cijenom i intervalom; 2.215.778 repair pravila s pozitivnom cijenom i bez izmišljenog intervala. |
| Runtime uklanjanje dijagnostike i fallbackova | PASS | DiagnosticRule/strategije/dialog i runtime fallback putanje uklonjeni; persistence.xml više ih ne registrira. |
| Maintenance/Problems/Service tokovi | PASS | Manualni problemi, konkretni repair izbor, tracked maintenance, interval strategies i inline servisni editor implementirani. |
| Dashboard i navigacija | PASS | Četiri bordered dashboard kartice, Ikonli ikone, aktivno vozilo u sidebaru i isti JFrame/CardLayout onboarding tok. |
| Azure SQL kompletni import | PASS | JDBC batch importer je atomarno uvezao 2.944.248 pravila u ciljnu bazu. |
| Završni schema cleanup | PASS | Guarded migracija je uklonila `OTHER_*`, `diagnostic_rule` i legacy stupce nakon provjere referenci. |

## Reproducibilni podaci

Generirani artefakti u `tools/reference-data/data/`:

- `vehicle_work_rules_complete.csv.gz`
- `vehicle_work_rules_complete_audit.csv.gz`
- `vehicle_work_rules_complete_summary.json`
- `vehicle_work_rules_complete_validation.json`

Generator je `scripts/complete_catalog_rules.py`, a provjera `scripts/validate_complete_catalog.py`. Zadnja validacija je vratila `error_count 0`, `rule_count 2944248`, `min_rules_per_variant 56` i `max_rules_per_variant 112`.

## Azure SQL audit nakon migracije

Ciljana baza je potvrđena kroz privatni connection config; vjerodajnice nisu u repozitoriju niti u ovom dokumentu.

```text
current_database                 free-sql-db-0650603
vehicle_variant_count            30366
work_definition_count            120
vehicle_work_rule_count          2944248
MAINTENANCE                      728470
REPAIR                           2215778
null_or_nonpositive_prices       0
maintenance_without_interval     0
repair_with_interval             0
duplicate_variant_work_groups    0
removed_other_work_count         0
diagnostic_rule_table_exists     0
legacy_rule_columns              0
legacy_work_default_columns      0
legacy_problem_columns           0
BEV forbidden-work rows          0
```

Read-only audit je izvršen s `java -jar tools/setup/target/autocare-setup-1.0.0.jar final-audit schema/final_catalog_audit.sql`. Import i cleanup koriste eksplicitne apply guardove; prvi import pokušaj s kraćim timeoutom je rollbackan i audit je potvrdio da stari katalog nije bio djelomično izmijenjen. Drugi pokušaj je uspješno završen.

## Izvršene provjere

| Naredba/provjera | Rezultat |
|---|---|
| `java -version` | PASS; JDK 25.0.4.101 |
| `./mvnw.cmd -q test` | PASS; offline Java provjere završile s exit code 0 |
| `./mvnw.cmd -q clean verify` | PASS |
| `./mvnw.cmd -q -f tools/setup/pom.xml clean test package` | PASS |
| `./mvnw.cmd -q install -DskipTests` | PASS |
| `scripts/check-runtime-style.ps1` | PASS; 66 Java datoteka |
| `scripts/validate_complete_catalog.py` | PASS; `error_count 0` |
| `sql-check` | PASS; Azure SQL TLS veza, `trustServerCertificate=false` |
| `final_catalog_audit.sql` prije cleanup-a | PASS; import preduvjeti potvrđeni |
| `final_catalog_audit.sql` nakon cleanup-a | PASS; svi završni brojevi potvrđeni |

## NOT_RUN / ograničenja

- Maven SQL Server integration profil s odvojenom bazom koja završava na `_test`: NOT_RUN; glavna baza nije korištena za fixture testove.
- Ručni registracijski/login/CRUD GUI smoke, DPI i native Windows interakcija: NOT_RUN; native GUI kanal nije dostupan u ovoj sesiji. Build potvrđuje kompilaciju, ali ne predstavlja vizualni smoke test.
- Fotografije vozila i runtime image API nisu dio završnog opsega; UI koristi samo dekorativne Ikonli ikone.

## Arhitektura

Java 25 + Maven + Swing/FlatLaf + JPA/Hibernate + Microsoft SQL Server/Azure SQL. Runtime koristi `hbm2ddl=none`, camelCase Java imena i Hibernate snake_case naming strategy. Schema i veliki katalog mijenjaju se samo kroz developerski setup alat; GUI ih ne mijenja.

Fazni commitovi ovog handoffa: `8275ed7` katalog/database tooling, `eb64b0d` runtime model i servisi, `99585d4` UI tokovi. Dokumentacijski/evidence commit dolazi nakon ove provjere.
