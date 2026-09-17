# AutoCare `— stvarni status implementacije

Updated: 2026-09-17. Ovaj dokument opisuje trenutno stanje repozitorija i stvarne provjere na
ovoj radnoj stanici. Nema reseta, rebasea ni force-pusha.

## Završene faze

| Faza | Status | Dokaz |
|---|---|---|
| Runtime transakcije, repositoryji i JPA mapping | PASS | Raniji fazni commitovi; eksplicitni EntityManager/EntityTransaction i pet repository sučelja. |
| Klasični Swing kontroleri i pogledi | PASS | Raniji fazni commitovi; MVC/Service/Strategy/Observer struktura ostala je nepromijenjena. |
| Račun i autentikacija | PASS | `aab2703`; `AppUser.password`, String login/register i profil samo ime/e-mail. `PasswordHasher` i njegove testne provjere uklonjeni. |
| Navigacijske ikone | PASS | `004bcfc`; Ikonli Swing + FontAwesome6 12.4.0, generička ikona automobila, šest navigacijskih ikona i refresh ikona. |
| Uklanjanje image pipelinea | PASS | `baea586`, `12466bf`; uklonjeni su VehicleImage, imagePath model/DTO, ImagePathTool, image enrichment alati/resursi i image-only katalog. |
| Referentni katalog | PASS | `12466bf`; generator, CSV zaglavlja, manifest i checksum usklađeni; 30.366 varijanti i 1.650.435 pravila ostali su isti. |
| Aktualna baza/migracija | PASS | `bb29613`; `schema/08_plain_password_and_remove_images.sql` je primijenjena nakon read-only audita. |
| Runtime style scanner | PASS | `scripts/check-runtime-style.ps1`; proširen i na legacy password/image reference i `Arrays.fill`. |

## Azure SQL rezultat

Read-only audit ciljne baze potvrdio je prije promjene 0 korisnika, 30.366 varijanti, 122 definicije
rada, 1.650.435 pravila i 87 dijagnostičkih pravila. `password_hash` je bio prazan, a `image_path`
je postojao za katalog. Pregled indeksa i ograničenja nije pronašao ovisnost na ta dva stupca.

Prvi pokušaj izvršavanja dostavljene migracije zaustavljen je SQL greškom pri statičkom referenciranju
novog stupca `password` u istom batchu nakon `sp_rename`; transakcija je rollbackana i read-only provjera
je potvrdila da je baza ostala na staroj shemi. Skripta je zatim popravljena dinamičkim SQL-om za
rename/nullable reset i ponovno primijenjena.

Završni audit potvrđuje:

- `app_user.password_hash`: odsutan
- `app_user.password`: prisutan, nullable, 255 NVARCHAR znakova
- `vehicle_variant.image_path`: odsutan
- `vehicle_variant`: 30.366
- `work_definition`: 122
- `vehicle_work_rule`: 1.650.435
- `diagnostic_rule`: 87
- duplikati kataloškog koda i para varijanta/rad: 0
- legacy cleanup kolone iz faze 07: odsutne

Nije brisan nijedan korisnik, katalog, vozilo, servisna povijest ili dijagnostičko pravilo. Baza je prije
promjene imala 0 korisnika, pa nije bilo starih vjerodajnica koje bi trebalo poništiti.

## Izvršene provjere

| Naredba/provjera | Rezultat |
|---|---|
| `java -version` | PASS; OpenJDK/Temurin 25.0.4.1 |
| `./mvnw.cmd -q test` | PASS; `Additional offline checks passed: 6` |
| `./mvnw.cmd -q clean verify` | PASS |
| `./mvnw.cmd -q package` | PASS |
| `./mvnw.cmd -q javadoc:javadoc` | PASS |
| `./mvnw.cmd -q install -DskipTests` | PASS |
| `./mvnw.cmd -q -f tools/setup/pom.xml clean test package` | PASS |
| `py -3 -m unittest discover -s tests -v` | PASS; 17 testova |
| `py -3 scripts/validate_af3.py` | PASS_STRUCTURAL_ONLY; 3.704.652 parova |
| Graphviz domain/ERD render | PASS; DOT izvori i PNG/SVG ponovno generirani |
| `sql-check` | PASS; Azure SQL, TLS i `trustServerCertificate=false` |
| `db-check` | PASS; Hibernate/JPA `hbm2ddl=none`, 30.366 varijanti |
| `schema/08...sql` read-only audit | PASS prije i nakon promjene |
| `scripts/verify-database.sql` | PASS; counts, FK/index audit, duplikati 0, legacy kolone 0 |
| `scripts/check-runtime-style.ps1` | PASS nakon proširenja provjera |
| `scripts/check-secrets.ps1` | PASS; osnovni skener staged sadržaja prošao, uz ručni pregled diff-a |

## Blokade i NOT_RUN

- Maven SQL Server integration profil s odvojenom bazom koja završava na `_test`: NOT_RUN; nije odobrena
  zasebna testna baza, a glavna baza se nije koristila za fixture testove.
- Ručni registracijski/login/CRUD GUI smoke, DPI, tipkovnica i native Windows interakcija: NOT_RUN jer
  native GUI kanal nije dostupan u ovoj sesiji. Kompajliranje potvrđuje ikone, ali nije vizualni smoke test.
- Nema fotografija po vozilu ni runtime image API-ja; aktualni UI koristi samo dekorativne Ikonli ikone.

## Trenutna arhitektura

Java 25 + Maven + Swing/FlatLaf + JPA/Hibernate + Microsoft SQL Server/Azure SQL. Runtime koristi
camelCase Java imena i Hibernate `CamelCaseToUnderscoresNamingStrategy`; fizička baza ostaje
snake_case, a DDL je izvan GUI runtimea (`hbm2ddl=none`). Service klase određuju granicu transakcije,
repositoryji samo dohvaćaju/persistiraju, Strategy ostaje za dijagnostiku, a Observer ostaje mali
in-memory listener.

Stvarni fazni commitovi: `aab2703`, `004bcfc`, `baea586`, `12466bf`, `bb29613`, `5ee295e`.
Raniji funkcionalni commitovi i detaljna povijest ostaju u Git DAG-u bez rewritea.
