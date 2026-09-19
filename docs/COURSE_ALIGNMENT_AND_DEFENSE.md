# Usklađenost s NOOP gradivom i obrana

## MVC + baza

Projekt jasno odvaja:

- View - Swing komponente i prikaz;
- Controller - GUI event i poziv use-casea;
- Service - poslovna pravila i transakcija;
- Domain - stanje i ponašanje entiteta;
- Repository/DAO - JPA dohvat/spremanje;
- Azure SQL - trajna pohrana.

To odgovara profesorovom zahtjevu da Swing ne sadrži SQL niti glavna poslovna pravila.

## Strategy

Strategy više nije umjetno vezan uz dijagnostiku. Koristi se tamo gdje stvarno postoje tri izmjenjiva načina računanja maintenance intervala:

- `MileageMaintenanceStrategy`
- `TimeMaintenanceStrategy`
- `CombinedMaintenanceStrategy`

`MaintenanceCalculator` bira strategiju prema tome ima li WorkDefinition km interval, vremenski interval ili oba.

Na obrani se može objasniti Open/Closed korist: novi način računanja može se dodati kao nova implementacija bez mijenjanja Viewa ili baze servisne povijesti.

## Repository

Repository sučelja su mala i konkretna. JPA implementacije koriste postojeći EntityManager. Ne postoji jedan generički repository koji skriva cijelu aplikaciju.

## Transakcija

Najbolji primjer je spremanje servisa. ServiceRecord, njegove stavke, nova kilometraža i rješavanje odabranih bilješki moraju uspjeti ili pasti kao jedna cjelina. Zato `ServiceRecordService` eksplicitno radi begin/commit/rollback.

## Observer/listener

`AppEvents` je mali pomoćni mehanizam. Nakon spremanja servisa, promjene vozila ili bilješke povezani ekran se može ponovno učitati bez direktnog vezanja svih Controllera međusobno.

## BigDecimal

Stvarne i informativne cijene koriste BigDecimal. `actualPrice` je stvarno plaćeno, a `WorkPriceRange.minPrice/maxPrice` je informativna procjena. Ta dva podatka se ne miješaju.

## Što je namjerno izostavljeno

- Spring / Spring Data
- automatska dijagnostika
- State pattern za samo OPEN/RESOLVED
- Factory/Command/Decorator bez stvarne potrebe
- runtime AI/API integracije
- velika per-variant rules matrica

Cilj je obranjiv studentski projekt, a ne enterprise arhitektura.
