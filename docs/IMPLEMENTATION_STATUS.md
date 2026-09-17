# AutoCare — stvarni status implementacije

Updated: 2026-09-17. Ovaj dokument opisuje trenutno stanje repozitorija i stvarne provjere na
ovoj radnoj stanici. Nema reseta, rebasea ni force-pusha.

## Završene faze

| Faza | Status | Dokaz |
|---|---|---|
| Runtime transakcije, repositoryji i JPA mapping | PASS | `f545934`; eksplicitni `EntityManager`/`EntityTransaction`, pet repository sučelja, `hbm2ddl=none`. |
| Klasični Swing kontroleri i pogledi | PASS | `74aae6f`, `f869795`; anonimni `ActionListener`, obične tablice i prikaz detalja u View klasama. |
| Testovi i događaji | PASS | `c2cecff`; testovi više ne ovise o uklonjenim pomoćnim klasama i `AppEvents` izravno obilazi listenere. |
| Setup/import alati | PASS | `33cebd9`; SQL alati koriste postojeće `snake_case` tablice i ne traže legacy plan kolonu. |
| Završni DB cleanup | PASS | `39522e7`; `schema/07_final_student_cleanup.sql` je prvo pokrenut read-only, zatim guardirano primijenjen. |
| Završni studentski polish | PASS | `c78fd49`, `4b7e8ec`, `c842caa`; datum/radovi koriste obične Swing komponente, a provjere i service flow su usklađeni s aktualnim runtimeom. |
| Runtime style scanner | PASS | `scripts/check-runtime-style.ps1`; 69 runtime Java datoteka bez zabranjenih konstrukcija. |

## Azure SQL rezultat

Read-only audit je pronašao točno šest legacy kolona: tri `version`, dva `request_key` i
`vehicle_work_rule.schedule_kind`. Pronađeni su samo njima pripadajući default, unique indeksi i
check constraint. Nakon provjere točne baze primijenjena je transakcijska skripta; nisu brisane
tablice, kataloški redovi, servisi ni korisnički podaci.

Završni audit potvrđuje:

- `vehicle_variant`: 30.366
- `work_definition`: 122
- `vehicle_work_rule`: 1.650.435
- `diagnostic_rule`: 87
- duplikati kataloškog koda i para varijanta/rad: 0
- svih šest legacy kolona: nema ih više

## Izvršene provjere

| Naredba/provjera | Rezultat |
|---|---|
| `git diff --check` | PASS |
| `.\mvnw.cmd -q clean verify` | PASS; offline provjere: 6 |
| `.\mvnw.cmd -q package` | PASS |
| `.\mvnw.cmd -q javadoc:javadoc` | PASS |
| `.\mvnw.cmd -q install -DskipTests` | PASS |
| `.\mvnw.cmd -q -f tools\setup\pom.xml clean test package` | PASS |
| `powershell -ExecutionPolicy Bypass -File scripts\check-runtime-style.ps1` | PASS; 69 datoteka |
| `scripts\check-secrets.ps1` | PASS nad staged sadržajem |
| `sql-check` | PASS; Azure SQL, TLS provjera uključena |
| `db-check` | PASS; Hibernate/JPA s `hbm2ddl=none`, 30.366 varijanti |
| `schema/07_final_student_cleanup.sql` | PASS; read-only audit i guardirana primjena |
| `scripts/verify-database.sql` | PASS; završni counts, indeksi, FK i legacy audit |

## Blokade i NOT_RUN

- Izolirani SQL Server Maven profil je `NOT_RUN`: nema zasebne odobrene baze s nastavkom `_test`.
- Ručni GUI smoke, DPI, tipkovnica i native Windows interakcija su `BLOCKED` jer native GUI kanal
  nije dostupan u ovoj sesiji. Nisu označeni kao PASS.
- Runtime ne koristi image API. Slike su lokalni resources i njihovo model-specific/licence odobrenje
  ostaje developer enrichment, ne automatska potvrda kandidata.

## Trenutna arhitektura

Java 25 + Maven + Swing/FlatLaf + JPA/Hibernate + Microsoft SQL Server/Azure SQL. Runtime koristi
camelCase Java imena i Hibernate `CamelCaseToUnderscoresNamingStrategy`; fizička baza ostaje
`snake_case`, a DDL je izvan GUI runtimea (`hbm2ddl=none`). Service klase izravno određuju granicu
transakcije, repositoryji samo dohvaćaju/persistiraju, Strategy ostaje za dijagnostiku, a Observer
ostaje mali in-memory listener.

Stvarni fazni commitovi: `f545934`, `74aae6f`, `c2cecff`, `33cebd9`, `39522e7`, `f869795`,
`c78fd49`, `4b7e8ec`, `c842caa`.
Dokumentacijski završni commit: `9f50aec`.
