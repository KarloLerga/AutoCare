# Architecture Freeze - finalni profesorov model

Ovaj dokument zamjenjuje prethodne arhitekturne freeze odluke.

## Slojevi

`Swing View -> Controller -> Service -> Repository -> JPA EntityManager/Hibernate -> Azure SQL`

View prikazuje podatke i prikuplja unos. Controller prima GUI događaj i poziva Service. Service drži poslovna pravila i transakcijske granice. Repository sadrži JPA dohvat/spremanje. Domain ne ovisi o Swingu ni SQL-u.

## Persistentni model

Točno osam entiteta:

1. `AppUser`
2. `VehicleVariant`
3. `Vehicle`
4. `WorkDefinition`
5. `WorkPriceRange`
6. `ServiceRecord`
7. `ServiceItem`
8. `Problem`

Nema `VehicleWorkRule` ni `DiagnosticRule`.

## Bilješke

`Problem` je korisnikova bilješka, ne dijagnoza. Ima opis, `ProblemCategory`, status, datum i opcionalni `resolvedByService`.

## Katalog

`WorkDefinition` je standardni zahvat. `WorkPriceRange` povezuje zahvat i `VehiclePriceClass` s min/max informativnom cijenom.

Finalni seed ima 120 workova x 5 klasa = 600 rangeova.

## Održavanje

Maintenance interval je na `WorkDefinition` kao `intervalKm` i/ili `intervalMonths`. Servisna povijest je izvor istine. Strategy bira Mileage, Time ili Combined izračun.

## Transakcija

`ServiceRecordService.create()` je glavni primjer transakcijske granice: jedan EntityManager, begin, spremanje servisa i stavki, update kilometraže, rješavanje označenih bilješki, commit ili rollback.

## Schema lifecycle

Runtime je `hbm2ddl.auto=none`. Postojeća Azure SQL baza prelazi na finalni model preko `schema/11_professor_model.sql`, a `schema/12_professor_model_audit.sql` mora završiti s `final_error_count = 0`.

## Finalni countovi

- 30.366 vehicle variants
- 120 work definitions
- 600 price ranges
- 30 maintenance
- 90 repair

Live Azure countovi se ne smiju označiti PASS dok migracija stvarno nije pokrenuta na korisnikovom računalu.
