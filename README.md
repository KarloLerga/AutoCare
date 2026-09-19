# AutoCare

Studentski projekt za kolegij **Napredno objektno programiranje**. AutoCare je Java Swing aplikacija za vlasnika vozila: vodi njegova vozila, servisnu povijest, stvarno plaćene troškove, preventivno održavanje, osobne bilješke i informativni katalog cijena.

## Završni funkcionalni model

Profesorov zadnji review razdvaja tri stvari koje se u aplikaciji ne smiju miješati:

1. **Bilješke** - korisnik slobodnim tekstom zapisuje što primjećuje. Može odabrati samo grubu kategoriju. Nema automatske dijagnoze, bodovanja ni pogađanja kvara.
2. **Katalog** - pretraživi informativni cjenik standardnih zahvata. Cijena je raspon prilagođen široj cjenovnoj klasi aktivnog vozila, a ne točna ponuda za VIN.
3. **Servisi** - nakon odlaska kod mehaničara korisnik sprema ono što je stvarno napravljeno, stvarnu kilometražu i stvarno plaćenu cijenu. Servis može zatvoriti raniju bilješku i postaje izvor istine za sljedeće intervale održavanja.

Navigacija nakon prijave:

`Dashboard | Vozila | Održavanje | Katalog | Servisi | Bilješke | Profil`

## Tehnologije

- Java 25
- Swing + FlatLaf
- Maven
- Jakarta Persistence / JPA
- Hibernate ORM
- Azure SQL Database / Microsoft SQL Server
- Ikonli Font Awesome ikone
- bez Springa, Lomboka, runtime AI-ja i vanjskog vehicle API-ja

Arhitekturni tok ostaje namjerno jednostavan:

`View -> Controller -> Service -> Repository -> EntityManager/Hibernate -> Azure SQL`

Service sloj određuje transakcijske granice. Repository klase dobivaju postojeći `EntityManager` i ne otvaraju vlastite transakcije.

## Katalog i cijene

Finalni katalog sadrži:

- **30.366** varijanti vozila
- **120** standardnih zahvata
- **30** maintenance zahvata
- **90** repair zahvata
- **5** širokih cjenovnih klasa vozila: `ECONOMY`, `STANDARD`, `PREMIUM`, `PERFORMANCE`, `EXOTIC`
- **600** unaprijed spremljenih raspona cijena (`120 x 5`)

Stari model s milijunima `VehicleWorkRule` kombinacija više nije dio finalne aplikacije. `VehicleVariant` samo pamti svoju cjenovnu klasu, a `WorkPriceRange` sadrži informativni `minPrice` i `maxPrice` za standardni zahvat i klasu vozila.

Maintenance interval (`intervalKm` i/ili `intervalMonths`) pripada `WorkDefinition` zapisu. Strategy pattern koristi kilometarski, vremenski ili kombinirani način izračuna. Servisna povijest je source of truth; `Zadnje`, `Sljedeće` i `Status` se ne spremaju kao duplicirano stanje.

Metodologija raspona i korišteni javni hrvatski izvori nalaze se u `data_model/catalog_price_sources.md`.

## Bilješke

`Problem` ostaje tehničko ime domenskog entiteta, ali u GUI-u predstavlja **Bilješku** vlasnika vozila. Sprema:

- vozilo
- slobodni opis
- grubu `ProblemCategory`
- status `OPEN` / `RESOLVED`
- vrijeme kreiranja
- opcionalni servis kojim je bilješka riješena

Bilješka se može ručno zatvoriti ili označiti riješenom kod spremanja stvarnog servisa. Ne sadrži suggested repair, estimated cost, match score ni diagnostic rule.

## Azure SQL migracija postojećeg projekta

Konekcijska datoteka ostaje izvan repozitorija, npr.:

```json
{"host":"auto-care.database.windows.net","port":1433,"database":"NAZIV_BAZE","user":"KORISNIK","password":"LOKALNO"}
```

Na računalu s JDK 25 pokrenuti:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel `
  -Launch
```

Skripta:

1. radi `mvnw clean verify`;
2. gradi mali setup alat;
3. provjerava Azure SQL vezu;
4. primjenjuje `schema/11_professor_model.sql` samo uz `-ApplyProfessorModel`;
5. pokreće read-only `schema/12_professor_model_audit.sql`;
6. opcionalno pokreće GUI.

Bez `-ApplyProfessorModel` migracija se izvršava samo u read-only modu (`@Apply = 0`).

Očekivano stanje nakon migracije:

- `vehicle_variant = 30366`
- `work_definition = 120`
- `work_price_range = 600`
- stara `vehicle_work_rule` tablica ne postoji
- `diagnostic_rule` ne postoji
- `problem.suggested_repair_id` i `problem.estimated_cost` ne postoje
- svi maintenance radovi imaju km i/ili mjesečni interval
- repair radovi nemaju preventivni interval

Za svakodnevno pokretanje:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Run-App.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json'
```

## Lokalna validacija seed podataka

```powershell
python .\scripts\validate_professor_catalog.py
```

Validator očekuje 30.366 vozila, 120 radova i 600 raspona te provjerava intervale, duplikate, klase i raspon cijena.

## Dokumentacija

- `docs/PROFESSOR_MODEL_HANDOFF.md` - točan handoff za Codex i redoslijed završetka
- `docs/PROJECT_REPORT.md` - trenutačni koncept aplikacije
- `docs/DATABASE_AND_JPA.md` - finalni entiteti i tablice
- `docs/GUI_AND_WIREFRAMES.md` - finalni GUI tok bez stare dijagnostike
- `docs/COURSE_ALIGNMENT_AND_DEFENSE.md` - što pokazati na obrani
- `docs/IMPLEMENTATION_STATUS.md` - što je napravljeno i što mora biti provjereno na lokalnom računalu
- `docs/VERIFICATION.md` - provjere i ograničenja ovog paketa
- `data_model/` - finalni CSV seedovi i metodologija cijena
- `schema/11_professor_model.sql` - guarded migracija postojeće Azure baze
- `schema/12_professor_model_audit.sql` - read-only završni audit

## Važno

Procjene iz Kataloga su informativne. One se nikada automatski ne kopiraju u `actualPrice`. Stvarni trošak postoji tek kada korisnik nakon odlaska kod mehaničara spremi servisni zapis.
