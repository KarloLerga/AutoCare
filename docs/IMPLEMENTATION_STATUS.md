# AutoCare — stvarni status implementacije

Updated: 2026-09-18. Ovo je evidence log za završni data/UI handoff. Postojeća povijest nije prepisivana, resetirana ni force-pushana.

## Završene faze

| Faza | Status | Dokaz |
|---|---|---|
| Kompletni katalog | PASS | Reproducibilni generator i validator: 30.366 varijanti, 120 konkretnih radova, 2.908.857 pravila, 0 validacijskih grešaka. |
| Modelirana pravila cijena i intervala | PASS | 693.784 maintenance pravila s pozitivnom cijenom i intervalom; 2.215.073 repair pravila s pozitivnom cijenom i bez izmišljenog intervala. |
| Runtime uklanjanje dijagnostike i fallbackova | PASS | DiagnosticRule/strategije/dialog i runtime fallback putanje uklonjeni; persistence.xml više ih ne registrira. |
| Maintenance/Problems/Service tokovi | PASS | Manualni problemi, konkretni repair izbor, tracked maintenance, interval strategies i inline servisni editor implementirani. |
| Dashboard i navigacija | PASS | Četiri bordered dashboard kartice, Ikonli ikone, aktivno vozilo u sidebaru i isti JFrame/CardLayout onboarding tok. |
| Azure SQL kompletni import | PASS | JDBC batch importer je atomarno uvezao 2.908.857 pravila u ciljnu bazu `free-sql-db-0650603`. |
| Završni schema cleanup i hrvatski nazivi | PASS | Guarded migracije su uklonile `OTHER_*`, `diagnostic_rule` i legacy stupce te ažurirale svih 120 naziva radova. |

## Reproducibilni podaci

Generirani artefakti u `tools/reference-data/data/`:

- `vehicle_work_rules_complete.csv.gz`
- `vehicle_work_rules_complete_audit.csv.gz`
- `vehicle_work_rules_complete_summary.json`
- `vehicle_work_rules_complete_validation.json`
- `final_applicability_removed.csv`

Generator je `scripts/complete_catalog_rules.py`, a provjera `scripts/validate_complete_catalog.py`. Zadnja validacija je vratila `error_count 0`, `rule_count 2908857`, `min_rules_per_variant 56` i `max_rules_per_variant 112`. Dodatno je zabilježeno 35.391 uklanjanje očitih neprimjenjivih hardverskih parova.

## Azure SQL audit nakon migracije

Ciljana baza je potvrđena kroz privatni connection config; vjerodajnice nisu u repozitoriju niti u ovom dokumentu.

```text
current_database                 free-sql-db-0650603
vehicle_variant_count            30366
work_definition_count            120
vehicle_work_rule_count          2908857
MAINTENANCE                      693784
REPAIR                           2215073
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
work_names_without_expected...   0
explicit FWD differential/transfer 2
unexpected_final_catalogue_counts 0
```

Read-only audit je izvršen s `java -jar tools/setup/target/autocare-setup-1.0.0.jar final-audit schema/final_catalog_audit.sql` nakon stvarnog importa, cleanup-a i migracije naziva. Import i cleanup koriste eksplicitne apply guardove. `explicit_fwd_differential_or_transfer_rules=2` su dva retka iz izvornog kataloga koji sadrže sufiks `FWD` unutar oznake motora (`5MTFWD`); zabilježeni su kao podatkovno ograničenje izvora.

## Izvršene provjere

| Naredba/provjera | Rezultat |
|---|---|
| `java -version` | PASS; JDK 25.0.4.101 |
| `./mvnw.cmd -q test` | PASS; offline Java provjere završile s exit code 0 |
| `./mvnw.cmd clean verify` | PASS; JDK25, 12 testova |
| `./mvnw.cmd -f tools/setup/pom.xml clean package` | PASS; setup alat, 3 testa |
| `./mvnw.cmd -q install -DskipTests` | PASS |
| `scripts/check-runtime-style.ps1` | PASS; 66 Java datoteka |
| `scripts/validate_complete_catalog.py` | PASS; `error_count 0`, 2.908.857 pravila |
| `sql-check` | PASS; Azure SQL TLS veza, `trustServerCertificate=false` |
| `import-complete-catalog ... --apply --confirm-complete-catalog` | PASS; 2.908.857 pravila, atomic commit |
| `09_complete_catalog_and_runtime_cleanup.sql` | PASS; guarded cleanup primijenjen |
| `10_croatian_work_names.sql` | PASS; 120 naziva ažurirano |
| `final_catalog_audit.sql` nakon cleanup-a | PASS; završni brojevi potvrđeni; 2 suffix-FWD retka zabilježena |

## NOT_RUN / ograničenja

- Maven SQL Server integration profil s odvojenom bazom koja završava na `_test`: NOT_RUN; glavna baza nije korištena za fixture testove.
- Ručni registracijski/login/CRUD GUI smoke, DPI i native Windows interakcija: NOT_RUN; native GUI kanal nije dostupan u ovoj sesiji. Build potvrđuje kompilaciju, ali ne predstavlja vizualni smoke test.
- Fotografije vozila i runtime image API nisu dio završnog opsega; UI koristi samo dekorativne Ikonli ikone.

## Arhitektura

Java 25 + Maven + Swing/FlatLaf + JPA/Hibernate + Microsoft SQL Server/Azure SQL. Runtime koristi `hbm2ddl=none`, camelCase Java imena i Hibernate snake_case naming strategy. Schema i veliki katalog mijenjaju se samo kroz developerski setup alat; GUI ih ne mijenja.

Raniji fazni commitovi: `8275ed7` katalog/database tooling, `eb64b0d` runtime model i servisi, `99585d4` UI tokovi.

Fazni commitovi ove završne integracije:
- `7388123` — `runtime: align desktop workflows with final catalogue`
- `0d773e5` — `data: publish final vehicle work catalogue`
- `cd6da72` — `database: apply guarded catalogue cleanup migrations`
- `ce63097` — `docs: document final implementation and verification`.

Pokretanje aplikacije nakon čiste izgradnje: PASS; `Run-App.ps1` je pokrenuo GUI proces iz `target/autocare-1.0.0.jar`. Ručni login/CRUD i vizualni smoke ostaju NOT_RUN jer native GUI kanal nije dostupan.
