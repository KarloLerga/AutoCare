# Handoff - profesorov finalni model

## Zašto je projekt promijenjen

Profesorov zadnji review definira aplikaciju isključivo iz perspektive vlasnika vozila i razdvaja funkcije koje su prije bile spojene:

- **Bilješke i zapažanja** - korisnik zapisuje simptom ili stvar koju želi zapamtiti.
- **Informativni katalog** - korisnik izravno pretražuje standardne zahvate i vidi okvirni raspon cijene.
- **Servisna povijest** - tek nakon odlaska kod mehaničara sprema stvarno napravljene radove i stvarno plaćene iznose.

Stari keyword/scoring analyzer i milijuni pravila po varijanti više nisu dio finalnog rješenja.

## Što je već implementirano u ovom paketu

### Domain / JPA

- `VehicleVariant.priceClass`
- `VehiclePriceClass`: ECONOMY, STANDARD, PREMIUM, PERFORMANCE, EXOTIC
- `WorkDefinition.catalogCategory`
- `WorkDefinition.intervalKm`
- `WorkDefinition.intervalMonths`
- novi `WorkPriceRange(work, priceClass, minPrice, maxPrice)`
- `Problem` je bilješka: description + ProblemCategory + status + createdAt + optional resolvedByService
- uklonjen Java `VehicleWorkRule`
- nema diagnostic entiteta

### GUI

Sidebar:

`Dashboard | Vozila | Održavanje | Katalog | Servisi | Bilješke | Profil`

Katalog ima search, filter kategorije i tablicu:

`Zahvat | Kategorija | Vrsta | Okvirna cijena | Interval`

Bilješke imaju grubu kategoriju, free-text opis, aktivan/riješen status i mogu se ručno zatvoriti ili riješiti stvarnim servisom.

Održavanje više nije cjenik. Prikazuje samo praćene stavke koje postoje u servisnoj povijesti i računa sljedeći interval.

Servis i dalje sprema stvarne cijene. Procjena iz Kataloga se nikad ne kopira u actual price.

### Podaci

`data_model/work_catalog_final.csv`
- 120 standardnih zahvata
- 30 maintenance, 90 repair
- maintenance ima km i/ili mjesečni interval

`data_model/work_price_ranges.csv`
- 600 raspona
- 120 radova x 5 cjenovnih klasa

`data_model/vehicle_price_classes.csv`
- cjenovna klasa za svih 30.366 postojećih VehicleVariant kodova

`data_model/catalog_price_sources.md`
- javni hrvatski izvori i objašnjenje kalibracije

## Migracija postojeće Azure SQL baze

`schema/11_professor_model.sql` je one-time guarded migracija starog AutoCare modela. Ona:

1. dodaje i puni `vehicle_variant.price_class`;
2. dodaje `catalog_category`, `interval_km`, `interval_months` u `work_definition`;
3. ažurira svih 120 radova;
4. dodaje `problem.category`;
5. uklanja `problem.suggested_repair_id` i `problem.estimated_cost`;
6. kreira i puni 600 `work_price_range` zapisa;
7. briše staru `vehicle_work_rule` tablicu;
8. briše `diagnostic_rule` ako još postoji;
9. radi poslovne sanity checkove prije commita.

`schema/12_professor_model_audit.sql` je samo read-only audit.

## Točno što Codex treba napraviti na lokalnom računalu

Kod iz ovog ZIP-a je pripremljen kao finalna verzija. Codex ne treba ponovno projektirati model.

1. zamijeniti/primijeniti ove datoteke u stvarnom Git repozitoriju;
2. pokrenuti `mvnw.cmd clean verify`;
3. pokrenuti `mvnw.cmd javadoc:javadoc`;
4. pokrenuti `python scripts/validate_professor_catalog.py`;
5. s privatnim SQL configom pokrenuti:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath 'C:\private-autocare\connection.local.json' `
  -ApplyProfessorModel
```

6. potvrditi `final_error_count = 0`;
7. pokrenuti aplikaciju i ručno proći:
   - registracija + prvo vozilo;
   - login;
   - odabir/izmjena kilometraže vozila;
   - Bilješke: create + close;
   - Katalog: search + category filter;
   - novi servis s maintenance i repair stavkom;
   - rješavanje aktivne bilješke servisom;
   - Održavanje: novi sljedeći interval nakon servisa;
   - Dashboard: trošak, aktivne bilješke, sljedeće održavanje, kilometraža.

## Očekivani audit

- 30.366 vehicle variants
- 120 work definitions
- 600 price ranges
- 30 maintenance works
- 90 repair works
- 0 invalid price ranges
- 0 maintenance bez intervala
- 0 repair s intervalom
- 0 vehicle bez price class
- 0 work bez catalog category
- 0 note bez category
- 0 radova bez svih 5 price ranges
- `vehicle_work_rule` ne postoji
- `diagnostic_rule` ne postoji
- `suggested_repair_id` ne postoji
- `estimated_cost` ne postoji
- `final_error_count = 0`

## Što ne raditi

- ne vraćati automatsku dijagnostiku;
- ne vraćati 2,9 milijuna per-variant pravila;
- ne dodavati poseban EV applicability engine;
- ne dodavati račune/privitke u ovom prolazu;
- ne uvoditi Spring ili nove frameworke;
- ne mijenjati postojeći vizualni stil osim novog Katalog ekrana i potrebnih naziva Bilješke;
- ne zamijeniti price range jednom "točnom" procjenom.
