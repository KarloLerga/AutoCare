# AutoCare — verification evidence

Date: 2026-09-17. Rezultati ispod su stvarno izvršeni; `NOT_RUN` i `BLOCKED` nisu preimenovani u
PASS. Credentials nisu zapisane u dokumentaciju.

## Maven/JDK25

- `.\mvnw.cmd -q clean verify` — PASS; `Additional offline checks passed: 6`.
- `.\mvnw.cmd -q package` — PASS.
- `.\mvnw.cmd -q javadoc:javadoc` — PASS.
- `.\mvnw.cmd -q install -DskipTests` — PASS.
- `.\mvnw.cmd -q -f tools\setup\pom.xml clean test package` — PASS.
- Završni polish runtime/test promjene (`c78fd49`, `4b7e8ec`, `c842caa`) — PASS; replacement
  datoteke su integrirane, tri helper klase uklonjene, a `ServiceRecordServiceValidationTest` je
  usklađen s aktualnim pravilima.
- `powershell -ExecutionPolicy Bypass -File scripts\check-runtime-style.ps1` — PASS; 69 runtime
  Java datoteke.
- `scripts\check-secrets.ps1` — PASS nad staged sadržajem; privatna konfiguracija nije u repozitoriju.

## Azure SQL / JPA

- `sql-check` — PASS s Microsoft JDBC driverom, SQL Server engine edition 5 i
  `encrypt=true;trustServerCertificate=false`.
- `schema/07_final_student_cleanup.sql` — prvo read-only PASS. Audit je pronašao šest legacy kolona,
  default za stari service key, dva unique indeksa i schedule check constraint, bez FK ovisnosti.
- Guardirana primjena `07` — PASS u eksplicitno potvrđenoj aplikacijskoj bazi; promjena je bila
  transakcijska i nije resetirala tablice niti brisala katalog.
- `db-check` — PASS s `hibernate.hbm2ddl.auto=none`; Hibernate je otvorio postojeći mapping i
  pročitao 30.366 kataloških varijanti.
- `scripts/verify-database.sql` — PASS; 30.366 / 122 / 1.650.435 / 87, bez duplikata varijanti ili
  parova varijanta/rad, svih šest legacy kolona odsutno.

## Završne runtime odluke

Runtime nema lambda listenere, Stream API, `Optional`, `var`, generički transaction runner, generički
`DataTable`, `UiTasks`, `ScheduleKind`, `@Version` ni request-key/idempotency tok. Service write metode
izravno pokazuju begin/commit/rollback/close; repositoryji ne bacaju service iznimke; pogledi koriste
obične `JTable`/`DefaultTableModel` strukture; dijagnostička formula i 87 pravila nisu redizajnirani.

## Nije izvršeno

- Maven SQL Server integration profil s odvojenom `_test` bazom: `NOT_RUN`.
- Ručni registracijski/login/CRUD GUI smoke, DPI i native Windows test: `BLOCKED` zbog nedostupnog
  native GUI kanala.
- Exact model-specific photo candidate review/licence approval: `NOT_RUN`; runtime ostaje lokalni.
