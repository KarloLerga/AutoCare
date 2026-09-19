# AutoCare - Codex final integration handoff

## Cilj

Ovaj ZIP je završni paket za integraciju profesorovog zadnjeg modela u stvarni AutoCare repo. Ne projektiraj model ponovno i ne vraćaj stare ideje.

Autoritativni izvori, ovim redom:

1. `reference/PROFESSOR_LATEST_SPECIFICATION.docx`
2. ovaj dokument
3. `docs/PROFESSOR_MODEL_HANDOFF.md`
4. aktualni Java/SQL/data kod u ovom ZIP-u
5. `docs/UPDATED_CONCEPT_FOR_PROFESSOR.md`


## Konačna funkcionalna odluka

Aplikacija je za vlasnika vozila.

Tri odvojene funkcije:

- **Bilješke**: korisnik zapisuje što primjećuje. Slobodan tekst + gruba kategorija + OPEN/RESOLVED. Nema pogađanja kvara, scorea ni cijene unutar bilješke.
- **Katalog**: pretraživi informativni cjenik standardnih zahvata. Prikazuje min-max raspon cijene prema široj cjenovnoj klasi aktivnog vozila.
- **Servisi**: nakon odlaska kod mehaničara korisnik sprema stvarno odrađene radove i stvarno plaćene cijene. Servis može zatvoriti jednu ili više aktivnih bilješki.

Održavanje ostaje odvojeno od Kataloga i računa sljedeći interval samo iz stvarne servisne povijesti.

## Konačni model podataka

Persistentni entiteti:

- `AppUser`
- `VehicleVariant`
- `Vehicle`
- `WorkDefinition`
- `WorkPriceRange`
- `ServiceRecord`
- `ServiceItem`
- `Problem`

Namjerno NE postoje:

- `DiagnosticRule`
- `DiagnosticResult`
- `DiagnosticStrategy`
- `KeywordDiagnosticStrategy`
- `VehicleWorkRule`
- per-variant matrica cijena/intervala

Novi ključni podaci:

- `VehicleVariant.priceClass : VehiclePriceClass`
- `WorkDefinition.catalogCategory : CatalogCategory`
- `WorkDefinition.intervalKm`
- `WorkDefinition.intervalMonths`
- `WorkPriceRange(work, priceClass, minPrice, maxPrice)`
- `Problem.category : ProblemCategory`

Finalni seed:

- 30.366 vehicle variants
- 120 standardnih zahvata
- 30 maintenance + 90 repair
- 5 cjenovnih klasa
- 600 price range zapisa

## Stil koda

Ovo je studentski projekt za NOOP i mora ostati vrlo čitljiv.

Ne uvoditi:

- Spring / Spring Data
- Lombok
- DI framework
- lambda izraze u runtime kodu
- Stream API
- `Optional`
- `var`
- recorde
- `java.util.function`
- generičke enterprise helper slojeve

Zadrži klasične anonimne Swing listenere, obične `for` petlje, eksplicitne `if/else` grane i izravno vidljive JPA transakcije.

Tok:

`View -> Controller -> Service -> Repository -> EntityManager/Hibernate -> Azure SQL`

Service je vlasnik poslovnih pravila i transakcije. Repository koristi isti proslijeđeni `EntityManager` u složenom use-caseu.

## Što ovaj ZIP već sadrži

- finalni Java model i GUI za profesorov model;
- novi `CatalogView/Controller`;
- Bilješke bez dijagnostike;
- Strategy za km/time/combined maintenance interval;
- `data_model/work_catalog_final.csv`;
- `data_model/work_price_ranges.csv`;
- `data_model/vehicle_price_classes.csv`;
- validator finalnog kataloga;
- generator guarded migracije;
- `schema/11_professor_model.sql`;
- `schema/12_professor_model_audit.sql`;
- ažuriranu dokumentaciju i upute.

## Što Codex mora napraviti

### 1. Integracija

Primijeni sadržaj ovog ZIP-a na stvarni `KarloLerga/AutoCare` repo. Sačuvaj stvarnu Git povijest. Ne force-pushati i ne fabricirati commitove.

Ako lokalni repo već sadrži dio ovih izmjena, napravi normalan diff i spoji samo nedostajuće promjene.

### 2. Realni build

Na korisnikovom Windows računalu s Temurin Java 25 pokreni:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javadoc:javadoc
python .\scripts\validate_professor_catalog.py
powershell -ExecutionPolicy Bypass -File .\scripts\check-runtime-style.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-secrets.ps1
```

Ništa ne označavaj PASS ako naredba nije stvarno završila uspješno.

### 3. Azure SQL migracija

Koristi postojeći privatni `connection.local.json`. Ne ispisuj ni commitaj lozinku.

Prvo dry-run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

Ako je dry-run uredan, stvarna migracija:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel
```

Audit mora završiti s:

```text
final_error_count = 0
```

Očekivano nakon migracije:

- `vehicle_variant = 30366`
- `work_definition = 120`
- `work_price_range = 600`
- 30 maintenance radova
- 90 repair radova
- svaki rad ima 5 raspona cijene
- nema maintenance rada bez intervala
- nema repair rada s intervalom
- nema vehicle varijante bez price class
- `vehicle_work_rule` ne postoji
- `diagnostic_rule` ne postoji
- `problem.suggested_repair_id` ne postoji
- `problem.estimated_cost` ne postoji

### 4. Ručni GUI smoke

Obavezno stvarno prođi:

1. registracija + prvo vozilo;
2. login;
3. dodavanje drugog vozila i aktiviranje;
4. ažuriranje kilometraže;
5. Bilješke: create, kategorija, manual close;
6. Katalog: search, filter kategorije, price range aktivnog vozila;
7. novi servis s maintenance i repair stavkom + stvarne cijene;
8. rješavanje aktivne bilješke kroz servis;
9. Održavanje nakon servisa - novi sljedeći km/datum/status;
10. Dashboard - stvarni total, aktivne bilješke, sljedeće održavanje, kilometraža;
11. logout/login i očuvanje aktivnog vozila.

Ako nešto pukne, popravi minimalno i studentski čitljivo. Nemoj dodavati novi framework ili novi sloj.

## Zabranjene regresije

Ne vraćati:

- automatsku analizu simptoma;
- keyword scoring;
- mogući kvar/predloženi popravak u Bilješkama;
- milijune `VehicleWorkRule` redaka;
- jednu lažno preciznu procijenjenu cijenu umjesto raspona;
- posebni EV applicability engine;
- privitke računa u ovom prolazu;
- vehicle images;
- password hashing;
- runtime vanjske API pozive.

## Završni Git commitovi

Nemoj u commit porukama pisati da je projekt "pojednostavljen" radi lakše obrane. Piši neutralno o stvarnoj promjeni, npr.:

- `refactor: align notes and catalog with approved workflow`
- `data: add catalog price ranges and vehicle classes`
- `database: migrate to catalog price range model`
- `docs: align project documentation with approved concept`
- `fix: complete final verification findings`

Na kraju pushaj samo stvarne promjene i javi korisniku commit hash, točne PASS/BLOCKED provjere i eventualnu jednu konkretnu ručnu radnju koja još treba.
