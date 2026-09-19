# Finalni JPA i SQL model

## Persistentni entiteti

### AppUser
Korisnički račun i veza na aktivno vozilo.

### Vehicle
Korisnikovo konkretno vozilo: owner, VehicleVariant, godina proizvodnje i trenutačna kilometraža.

### VehicleVariant
Referentni katalog vozila. Uz marku/model/generaciju/motor sadrži samo jednu novu informaciju potrebnu za Katalog: `VehiclePriceClass priceClass`.

### WorkDefinition
Standardni zahvat:
- code
- name
- WorkCategory (`MAINTENANCE` / `REPAIR`)
- CatalogCategory
- intervalKm
- intervalMonths

Repair nema preventivni interval. Maintenance ima najmanje jedan interval.

### WorkPriceRange
Informativna cijena jednog WorkDefinitiona za jednu VehiclePriceClass:
- work
- priceClass
- minPrice
- maxPrice

Baza mora imati točno jednu kombinaciju work + priceClass. Finalni seed: 600 redaka.

### ServiceRecord
Stvarni servis korisnikovog vozila: datum, kilometraža, napomena i stavke.

### ServiceItem
Jedna stvarno napravljena stavka servisnog zapisa. Pokazuje na WorkDefinition i čuva `actualPrice` koji korisnik ručno unosi.

### Problem
Tehničko ime entiteta ostaje `Problem`, ali GUI ga prikazuje kao Bilješku:
- vehicle
- description
- ProblemCategory
- ProblemStatus
- createdAt
- optional resolvedByService

Nema suggested repair, estimated cost ni diagnostic score.

## Uklonjeno iz finalnog modela

- VehicleWorkRule
- DiagnosticRule
- KeywordDiagnosticStrategy i analiza simptoma
- suggested_repair_id
- estimated_cost na problemu
- per-variant cijene i intervali

## Transakcije

Primjer `ServiceRecordService.create`:

1. otvori EntityManager;
2. `transaction.begin()`;
3. dohvati Vehicle;
4. kreira ServiceRecord i ServiceItem zapise;
5. po potrebi podigne currentMileage;
6. riješi odabrane aktivne bilješke;
7. commit;
8. rollback na RuntimeException;
9. zatvori EntityManager.

Repositoryji u koracima 3-6 koriste isti EntityManager.

## Naming

Java ostaje camelCase, Azure SQL snake_case. Hibernate `CamelCaseToUnderscoresNamingStrategy` mapira, primjerice:

- `priceClass` -> `price_class`
- `catalogCategory` -> `catalog_category`
- `intervalKm` -> `interval_km`
- `resolvedByService` -> `resolved_by_service_id`

Zato nisu potrebne `@Column(name=...)` anotacije na svakom polju.
