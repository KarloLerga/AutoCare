# AutoCare `— verification evidence

Date: 2026-09-17. Rezultati ispod su stvarno izvršeni; `NOT_RUN` i `BLOCKED` nisu preimenovani u
PASS. Credentials nisu zapisane u dokumentaciju.

## Maven/JDK25

- `java -version` — PASS: OpenJDK/Temurin 25.0.4.1.
- `./mvnw.cmd -q test` — PASS; dodatne offline provjere: 6.
- `./mvnw.cmd -q clean verify` — PASS.
- `./mvnw.cmd -q package` — PASS.
- `./mvnw.cmd -q javadoc:javadoc` — PASS.
- `./mvnw.cmd -q install -DskipTests` — PASS.
- `./mvnw.cmd -q -f tools/setup/pom.xml clean test package` — PASS.
- `py -3 -m unittest discover -s tests -v` — PASS; 17 referentnih-data testova.
- `py -3 scripts/validate_af3.py` — PASS_STRUCTURAL_ONLY; ukupno 3.704.652 parova.
- Graphviz render `domain.dot`/`erd.dot` u PNG i SVG — PASS.
- `scripts/check-runtime-style.ps1` — PASS nakon proširenja na legacy password/image reference.
- `scripts/check-secrets.ps1` — PASS nad završnim staged sadržajem; rezultat uključuje ručni pregled diff-a.

## Azure SQL / JPA

- Read-only `master.sys.databases` — PASS; pronađena je postojeća baza `free-sql-db-0650603`.
- Read-only audit prije migracije — PASS: 0 users, 30.366 variants, 122 works, 1.650.435 rules,
  87 diagnostic rules; `password_hash` je postojao i bio prazan, `image_path` je postojao.
- Read-only audit indeksa/ograničenja — PASS; nema indeksa/constraints na ciljanim legacy stupcima.
- Prvi apply pokušaj dostavljenog `08` patcha — FAIL na statičkom SQL referenciranju novog `password`
  stupca; transakcija je rollbackana, a naknadni audit potvrdio stare stupce.
- Popravak `schema/08_plain_password_and_remove_images.sql` — PASS; dinamički SQL uklanja problem
  batch-kompilacije.
- Guardirani apply popravljenog `08` patcha — PASS: `password_hash` -> `password`, `image_path` uklonjen,
  brojevi redaka očuvani.
- Završni read-only `08` audit — PASS: `password_hash_column=NULL`, `password_column=510`,
  `image_path_column=NULL`, counts 0 / 30.366 / 122 / 1.650.435 / 87.
- `sql-check` — PASS s Microsoft JDBC driverom, SQL Server engine edition 5 i
  `encrypt=true;trustServerCertificate=false`.
- `db-check` — PASS s `hibernate.hbm2ddl.auto=none`; Hibernate je otvorio aktualni mapping i
  pročitao 30.366 kataloških varijanti.
- `scripts/verify-database.sql` — PASS; 9 tablica, 63 stupca, 12 FK-ova, counts potvrđeni, duplikati 0
  i svih šest legacy cleanup kolona odsutno. `null_estimates=367519` je postojeća semantika nepoznatih
  procjena, ne greška migracije.

## Završen runtime model

- `AppUser.password` je običan `String`; login/register uspoređuju i spremaju String vrijednost.
- Profil sprema samo ime/e-mail; nema password-change UI ili servisni API.
- `VehicleVariant`, `Data.VariantRow` i `Mapping` nemaju image path.
- `VehicleImage`, `PasswordHasher`, `ImagePathTool`, image enrichment folder, image-only resources i
  image-group katalog su uklonjeni.
- `MainFrame` koristi samo Ikonli FontAwesome6 dekorativne ikone: automobil, šest navigacijskih ikona
  i refresh.
- MVC, Service, Repository, Strategy, Observer, maintenance, diagnostics i transaction flow nisu
  redizajnirani u ovoj fazi.

## Nije izvršeno

- Maven SQL Server integration profil s odvojenom `_test` bazom: `NOT_RUN`.
- Ručni registracijski/login/CRUD GUI smoke, DPI i native Windows test: `NOT_RUN`; native GUI kanal
  nije dostupan.
- Vizualna provjera Ikonli ikona na stvarnom Windows prozoru: `NOT_RUN`; build/Javadoc provjeravaju
  compile/link, ne izgled.
