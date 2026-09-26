# UML - opis

Priložena finalna slika UML dijagrama je `diagrams/UML_AutoCare_FINAL.png` i nije mijenjana. Tekstualni izvor za čitanje i uređivanje nalazi se u `diagrams/UML_AutoCare_FINAL.mmd` i napisan je u **Mermaid notaciji**.

Dijagram je organiziran prema stvarnim paketima izvornog koda: `app`, `view`, `controller`, `service`, `repository`, `observer`, `domain` i `strategy`. Takvo grupiranje omogućuje da se odmah vidi arhitekturna uloga pojedine klase. Glavni tok aplikacije čita se od pokretanja aplikacije prema View, Controller, Service i Repository dijelovima, dok su Observer i Strategy strukture izdvojene u svojim stvarnim paketima.

Za prikazane klase navedeni su važni atributi, konstruktori i glavne metode. Nisu navedeni svi trivijalni getteri, setteri i privatne pomoćne metode jer njihov prikaz ne bi dodatno objasnio arhitekturu aplikacije.

Veze su prikazane selektivno. Strelice prikazuju ključne odnose između slojeva, stvaranje glavnih objekata, Observer komunikaciju i Strategy strukturu. Kada je neka ovisnost već jasno navedena kao atribut klase, dodatna strelica nije nužna ako bi samo ponavljala istu informaciju i smanjila čitljivost dijagrama. Na taj se način zadržava stvarno značenje modela bez nepotrebnog križanja velikog broja linija.

## Paket `app`

`Main` je ulazna točka aplikacije. Postavlja Swing izgled, otvara `EntityManagerFactory`, stvara glavne Service objekte, `MainFrame`, `Session`, `Subject` i `MainController`, a pri zatvaranju aplikacije zatvara i `EntityManagerFactory`.

`DatabaseConfig` sadrži konfiguraciju potrebnu za otvaranje JPA `EntityManagerFactoryja` prema Azure SQL bazi. Na UML-u su prikazani nazivi konfiguracijskih atributa, ali ne i njihove stvarne vrijednosti.

`Session` je Singleton koji čuva identitet prijavljenog korisnika i trenutno aktivno vozilo. Smješten je u stvarnom paketu `app`, a stereotip `<<Singleton>>` pokazuje obrazac dizajna koji klasa ostvaruje.

## Paket `view`

`MainFrame` je glavni Swing prozor i sadrži glavne ekrane aplikacije. `LoginView` i `RegistrationView` služe prijavi i registraciji. `DashboardView` prikazuje sažetak aktivnog vozila. `VehiclesView` prikazuje korisnikova vozila i omogućuje odabir aktivnog vozila. `MaintenanceView` prikazuje održavanja. `CatalogView` prikazuje informativni katalog zahvata. `ServicesView` prikazuje servisnu povijest i detalj servisa. `ProblemsView` prikazuje i omogućuje unos problema aktivnog vozila.

Kompozicijske veze iz `MainFramea` prema glavnim Viewovima pokazuju da su ti paneli sastavni dio glavnog prozora.

## Paket `controller`

Controlleri primaju korisničke događaje iz Swing sučelja i pokreću odgovarajuće use-caseove.

`MainController` koordinira navigaciju, prijavu u glavni dio aplikacije, aktivno vozilo, osvježavanje ekrana i Observer događaje. Funkcionalne Controllere drži kao atribute, pa njihove veze nije potrebno dodatno ponavljati svim mogućim strelicama.

`AuthController` upravlja prijavom i registracijom. `VehiclesController` upravlja vozilima i aktivnim vozilom. `CatalogController` upravlja prikazom i filtriranjem kataloga. `ServicesController` koordinira servisnu povijest i unos servisa. `MaintenanceController` učitava održavanja, a `ProblemsController` upravlja prikazom i unosom problema.

Na dijagramu su posebno istaknute veze Controller -> Service jer one predstavljaju glavni prijelaz iz korisničkog sučelja prema poslovnom use-caseu.

## Paket `service`

Service klase sadrže poslovni tok aplikacije. Svaki Service prima `EntityManagerFactory` kroz konstruktor, otvara `EntityManager` za pojedinu operaciju i po potrebi određuje transakcijsku granicu.

`AuthService` provodi prijavu i registraciju. `VehicleService` upravlja vozilima i aktivnim vozilom. `CatalogService` čita katalog vozila i radova. `ServiceRecordService` sprema i čita servisnu povijest. `MaintenanceService` izračunava plan održavanja. `ProblemService` radi s korisničkim problemima, a `DashboardService` priprema podatke za Dashboard.

Veze Service -> Repository prikazuju koje skupove persistentnih podataka pojedini use-case koristi.

## Paket `repository`

Repository klase predstavljaju pristup persistentnim podacima. Svaki Repository prima postojeći `EntityManager` kroz konstruktor. Repository izvršava JPA/JPQL dohvat i spremanje, dok Service sloj upravlja cijelim poslovnim use-caseom i transakcijom.

`UserRepository`, `VehicleRepository`, `CatalogRepository`, `ServiceRecordRepository` i `ProblemRepository` pokrivaju glavne skupove podataka aplikacije.

## Paket `observer`

`Observer` je sučelje s metodom `update(AppEvent)`. `MainController` implementira `Observer`. `Subject` čuva registrirane Observere i šalje im `AppEvent` događaje. `VehiclesController` i `ServicesController` obavještavaju `Subject` nakon promjena koje zahtijevaju osvježavanje glavnog konteksta aplikacije.

`AppEvent` je enum s događajima `VEHICLE_CHANGED`, `ACTIVE_VEHICLE_CHANGED` i `SERVICE_SAVED`.

Observer dio nalazi se u stvarnom paketu `observer`, a obrazac je vidljiv iz implementacije `Observer` sučelja i komunikacije preko `Subjecta`.

## Paketi `domain` i `strategy`

`MaintenanceCalculator` se nalazi u paketu `domain` i odabire odgovarajuću `MaintenanceStrategy` ovisno o vrsti servisnog intervala.

`MaintenanceStrategy` je Strategy sučelje. `MileageMaintenanceStrategy`, `TimeMaintenanceStrategy` i `CombinedMaintenanceStrategy` njegove su konkretne implementacije. `CombinedMaintenanceStrategy` koristi kilometarsku i vremensku strategiju te vraća kriterij koji prije dospijeva.

## Klase koje nisu prikazane kao zasebni UML elementi

Dijagram ne prikazuje svaku Java datoteku. Izostavljene klase postoje u projektu i imaju sljedeće uloge.

### Pomoćni GUI dijelovi

- `VehicleDialog` - modalni prozor za dodavanje vozila.
- `MileageDialog` - modalni prozor za promjenu kilometraže.
- `ServiceEditorDialog` - modalni prozor za unos novog servisa, servisnih stavki, stvarnih cijena i riješenih problema.
- `VehicleForm` - forma za odabir marke, modela, godine, varijante i kilometraže.
- `VehicleFormController` - upravlja kaskadnim punjenjem odabira marka -> model -> godina -> varijanta.

Oni pripadaju konkretnim GUI tokovima i njihovo dodavanje na glavni UML povećalo bi broj lokalnih veza bez boljeg objašnjenja glavne arhitekture.

### Utility klase

- `Ui` - zajednički Swing layout helperi, parsiranje korisničkog unosa, formatiranje datuma, kilometraže i novca te prikaz poruka.
- `Checks` - osnovne provjere teksta, e-maila, lozinke, kilometraže i novčanih iznosa.

Utility klase nemaju vlastiti poslovni use-case i zato nisu zasebno crtane.

### Pomoćni podatkovni tipovi

`Data` sadrži tipove `ItemInput`, `ServiceInput`, `ServiceRow`, `ServiceDetail`, `MaintenanceRow` i `Dashboard`. Oni služe unosu, prijenosu ili pripremi podataka za prikaz. Nisu zasebni persistentni entiteti niti samostalni arhitekturni sloj, pa se njihova imena pojavljuju samo tamo gdje su važna u potpisima metoda.

### Persistentni domenski model

`AppUser`, `Vehicle`, `VehicleVariant`, `WorkDefinition`, `ServiceRecord`, `ServiceItem` i `Problem` detaljno su prikazani zasebnim ERD-om s atributima, ključevima i kardinalnostima. Na aplikacijskom UML-u njihova se imena pojavljuju kao tipovi u atributima i metodama, ali se njihove strukture ne dupliciraju.

Domenski enumi `WorkCategory`, `CatalogCategory` i `ProblemCategory` također se koriste kao tipovi, ali nisu zasebno crtani jer predstavljaju ograničene skupove vrijednosti.

## Čitanje dijagrama

Glavni arhitekturni tok je:

`Main -> View / Controller -> Service -> Repository`

`Session` daje zajednički korisnički kontekst. Observer povezuje promjene iz `VehiclesControllera` i `ServicesControllera` s osvježavanjem u `MainControlleru`. Strategy struktura koristi se u izračunu održavanja.

Dijagram tako prikazuje stvarnu strukturu aplikacije, važne konstruktorske ovisnosti i korištene obrasce dizajna, ali zadržava dovoljno malo grafičkih veza da se svaka važna strelica može pratiti od izvora do odredišta.
