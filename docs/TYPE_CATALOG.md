# Katalog tipova - finalni profesorov model

## Domain / persistentni entiteti

- `AppUser` - korisnički račun i aktivno vozilo.
- `VehicleVariant` - referentna kataloška varijanta vozila i `VehiclePriceClass`.
- `Vehicle` - konkretno korisnikovo vozilo, owner, varijanta, godina i kilometraža.
- `WorkDefinition` - standardni zahvat, WorkCategory, CatalogCategory i eventualni maintenance interval.
- `WorkPriceRange` - min/max informativni raspon cijene jednog zahvata za jednu cjenovnu klasu.
- `ServiceRecord` - stvarni servis: datum, kilometraža, napomena i stavke.
- `ServiceItem` - jedan stvarno odrađeni zahvat i `actualPrice`.
- `Problem` - korisnikova bilješka: opis, gruba kategorija, OPEN/RESOLVED, datum i opcionalni servis koji ju je riješio.

## Domain helperi i enumi

- `VehiclePriceClass` - ECONOMY, STANDARD, PREMIUM, PERFORMANCE, EXOTIC.
- `CatalogCategory` - grupe za pregled/pretragu zahvata.
- `WorkCategory` - MAINTENANCE / REPAIR.
- `ProblemCategory` - Motor, Kočnice, Ovjes, Klima, Elektrika, Ostalo.
- `ProblemStatus` - OPEN / RESOLVED.
- `MaintenanceStatus` - OK / SOON / DUE.
- `MaintenanceCalculator` - bira Strategy prema dostupnom intervalu.
- `Checks` - osnovna validacija unosa.
- `CostSummary` - zbroj poznatih stvarnih cijena i broj nepoznatih povijesnih cijena.

## Strategy

- `MaintenanceStrategy`
- `MileageMaintenanceStrategy`
- `TimeMaintenanceStrategy`
- `CombinedMaintenanceStrategy`

Nema dijagnostičkog Strategyja.

## Repository

- `UserRepository`
- `VehicleRepository`
- `CatalogRepository`
- `ServiceRecordRepository`
- `ProblemRepository`

JPA implementacije su `Jpa...Repository` i koriste proslijeđeni EntityManager.

## Service

- `AuthService`
- `VehicleService`
- `CatalogService`
- `ServiceRecordService`
- `MaintenanceService`
- `ProblemService`
- `DashboardService`

## Controller / View

Controlleri i Viewovi postoje za login/onboarding, vozila, dashboard, održavanje, katalog, servise, bilješke i profil.

## Namjerno uklonjeni tipovi

Finalni runtime nema:

- `VehicleWorkRule`
- `DiagnosticRule`
- `DiagnosticResult`
- `DiagnosticStrategy`
- `KeywordDiagnosticStrategy`
- `AnalysisDialog`
- transient score/match modele.
