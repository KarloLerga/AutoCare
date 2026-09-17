# AutoCare — aktualni katalog tipova

Katalog je usklađen sa završnim runtime sourceom. Javadoc je izvršen naredbom
`.\mvnw.cmd -q javadoc:javadoc`; trivijalni getteri nisu ponavljani ovdje.

## app

- `DatabaseConfig` — čita `AUTOCARE_DB_*`, gradi TLS SQL Server URL i otvara `EntityManagerFactory`.
- `Main` — FlatLaf setup, wiring servisa/kontrolera i zatvaranje tvornice.
- `Session` — owner ID, aktivno vozilo i logout stanje.

## domain

- `AppUser` — račun, profil, hash lozinke i aktivno vozilo.
- `VehicleVariant` — kataloška varijanta, godine, tehnički podaci i lokalna slika.
- `Vehicle` — owner, varijanta, proizvodna godina i kilometraža; kilometraža samo raste.
- `WorkDefinition` — opća definicija održavanja/popravka i opcionalni default interval/procjena.
- `VehicleWorkRule` — primjenjivost rada na varijantu i opcionalni interval/procjena.
- `ServiceRecord` — datum, kilometraža, napomena i kolekcija stavki.
- `ServiceItem` — jedan rad i stvarno plaćena cijena.
- `Problem` — opis, status, snapshot informativne analize i opcionalni servis rješenja.
- `DiagnosticRule` — aktivna fraza, težina i kandidat popravka.
- `MaintenanceCalculator` — status po km i/ili mjesecima.
- `Checks` — osnovna validacija teksta, e-maila, lozinke, kilometraže i novca.
- `CostSummary` — poznati zbroj i broj nepoznatih iznosa.
- `MaintenanceStatus`, `ProblemStatus`, `WorkCategory` — mali enumi domene.

## model.Data

`Data` je jedna obična klasa s nested immutable-style modelima za granicu slojeva:

- `Account`, `VariantRow`, `VehicleInput`, `VehicleRow`
- `WorkRow`, `ItemInput`, `ServiceInput`, `ItemRow`
- `ServiceRow`, `ServiceDetail`, `MaintenanceRow`
- `Analysis`, `RuleData`, `DiagnosticResult`, `ProblemRow`, `Dashboard`

Modeli su konstruktori + getteri; nema Java recorda ni persistence verzijskih polja.

## repository i persistence

Pet sučelja: `UserRepository`, `VehicleRepository`, `CatalogRepository`, `ServiceRecordRepository`,
`ProblemRepository`. Pet JPA implementacija (`JpaUserRepository`, `JpaVehicleRepository`,
`JpaCatalogRepository`, `JpaServiceRecordRepository`, `JpaProblemRepository`) rade samo JPQL/JPA nad
proslijeđenim `EntityManagerom`; nestali objekti vraćaju `null`.

## service

- `AuthService` — login, registracija s početnom poviješću i profil.
- `VehicleService` — list/add/update/activate/delete uz owner provjeru.
- `CatalogService` — marke, modeli, varijante i radovi.
- `ServiceRecordService` — servis, stavke, kilometraža i rješavanje problema u jednoj transakciji.
- `MaintenanceService` — history + rule/default podaci + kalkulator.
- `ProblemService` — analiza i jednostavno spremanje već prikazanog previewa.
- `DashboardService` — pregled troška, otvorenih problema i maintenance statusa.
- `PasswordHasher` — salted PBKDF2 format `salt:hash`.
- `Mapping` — pretvara managed entitete u modele za View.
- `AppException` — jednostavna runtime poslovna iznimka.

## strategy i event

- `DiagnosticStrategy` — sučelje analize teksta.
- `KeywordDiagnosticStrategy` — postojeće ponderirano grupiranje, score i sortiranje.
- `AppEvent`, `AppListener`, `AppEvents` — mali Observer za osvježavanje GUI-ja.

## controller

`AuthController`, `MainController`, `VehiclesController`, `VehicleFormController`,
`MaintenanceController`, `ServicesController`, `ServiceEditorController`, `ProblemsController` i
`ProfileController` koordiniraju View i Service slojeve klasičnim Swing listenerima.
`ServiceEditorListener` je mali konkretni listener za onboarding ili stvarni servis.

## view

`MainFrame`, `LoginView`, `OnboardingDialog`, `AnalysisDialog`, `ServiceEditorDialog`,
`DashboardView`, `VehiclesView`, `MaintenanceView`, `ServicesView`, `ProblemsView` i `ProfileView`
grade Swing prikaz. Glavne tablice koriste `JTable` + `DefaultTableModel`; pogledi posjeduju prikaz
detalja. Komponente su `ServiceItemsModel`, `Ui`, `VehicleForm` i `VehicleImage`.

## setup alat

`tools/setup` je odvojeni developerski Maven projekt s `DatabaseTool`, `SqlSeedTool`,
`ReviewedIntervalTool`, `ImagePathTool`, CSV/manifest pomoćnicima i `DevelopmentSeed`. Nije dio
runtime JAR-a. Setup koristi postojeće fizičke `snake_case` SQL objekte, TLS i eksplicitne target
consent provjere.
