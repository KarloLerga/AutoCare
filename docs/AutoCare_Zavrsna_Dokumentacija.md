# AutoCare - završna dokumentacija

**Kolegij:** Napredno objektno programiranje
**Projekt:** AutoCare
**Vrsta aplikacije:** Java Swing desktop aplikacija povezana s Azure SQL bazom

## 1. Opis problema

AutoCare je aplikacija namijenjena vlasniku vozila koji želi na jednom mjestu voditi osnovne podatke o svojim vozilima, servisnu povijest, stvarne troškove servisa, intervale održavanja i probleme koje želi prenijeti mehaničaru. Uz to aplikacija sadrži informativni katalog standardnih zahvata i okvirnih raspona cijena.

Korisnik može imati više vozila, pri čemu je jedno vozilo odabrano kao aktivno. Aktivno vozilo predstavlja kontekst za Dashboard, Održavanje, Servise i Probleme. Katalog je opći informativni pregled i ne ovisi o aktivnom vozilu.

## 2. Konceptualni model i arhitektura

Aplikacija je organizirana prema MVC pristupu uz jasno odvojene Service i Repository odgovornosti:

- **View** - Swing forme, paneli, dijalozi i tablice za prikaz i unos.
- **Controller** - prima korisničke događaje, čita podatke iz Viewa i pokreće odgovarajući use-case.
- **Service** - poslovna pravila, složenije operacije i granice transakcija.
- **Domain** - persistentni entiteti i osnovna pravila domenskih objekata.
- **Repository/DAO** - dohvat i spremanje podataka pomoću JPA `EntityManagera`.

`EntityManagerFactory` kreira se jednom pri pokretanju aplikacije. Service klase otvaraju `EntityManager` za pojedinu operaciju, a Repository klase dobivaju taj isti `EntityManager`. Operacije koje mijenjaju više povezanih zapisa, posebno spremanje servisa i rješavanje problema, izvršavaju se u jednoj transakciji.

## 3. GUI wireframeovi

GUI wireframeovi izrađeni su u **Figmi** i priloženi su u datoteci:

`wireframes/AutoCare_GUI_Wireframes_FINAL.pdf`

PDF prikazuje glavne korisničke tokove: prijavu, registraciju, Dashboard, vozila, dodavanje vozila, održavanje, katalog, servisnu povijest, unos novog servisa i probleme.

Katalog prikazuje informativne raspone cijena standardnih zahvata, dok servisna povijest prikazuje stvarno evidentirane radove i **stvarni trošak**.

## 4. UML dijagram klasa

Završna PNG slika `diagrams/UML_AutoCare_FINAL.png` priložena je od korisnika i sačuvana bez izmjena. Mermaid izvor dijagrama nalazi se u `diagrams/UML_AutoCare_FINAL.mmd`.

UML dijagram izrađen je u **Mermaid notaciji**. Izvor dijagrama nalazi se u:

`diagrams/UML_AutoCare_FINAL.mmd`

Dijagram je organiziran prema stvarnim paketima izvornog koda: `app`, `view`, `controller`, `service`, `repository`, `observer`, `domain` i `strategy`. Takav raspored grupira klase prema odgovornosti, smanjuje križanje veza i omogućuje da se glavni tok aplikacije čita od pokretanja prema korisničkom sučelju, Controllerima, Service sloju i Repository sloju. Observer i Strategy strukture prikazane su u vlastitim paketima jer njihove klase sudjeluju u specifičnim obrascima dizajna i nisu zaseban MVC sloj.

Za prikazane klase navedeni su važni atributi, konstruktori i glavne metode. Nisu prikazani svi trivijalni getteri, setteri i privatne pomoćne metode jer ne mijenjaju razumijevanje arhitekture.

### 4.1. Paket `app`

`Main` je ulazna točka aplikacije. Postavlja Swing izgled, preko `DatabaseConfig.open()` otvara zajednički `EntityManagerFactory`, stvara glavne Service objekte, `MainFrame`, `Session`, `Subject` i `MainController`. Pri zatvaranju glavnog prozora zatvara se i `EntityManagerFactory`.

`DatabaseConfig` sadrži konfiguraciju potrebnu za stvaranje `EntityManagerFactoryja` i povezivanje s Azure SQL bazom. Na dijagramu su prikazani nazivi konfiguracijskih atributa, ali ne i njihove konkretne vrijednosti.

`Session` je Singleton koji čuva identitet prijavljenog korisnika i trenutno aktivno vozilo.

### 4.2. Paket `view`

`MainFrame` je glavni Swing prozor i sadrži glavne View panele. `LoginView` i `RegistrationView` služe prijavi i registraciji. `DashboardView` prikazuje sažetak aktivnog vozila. `VehiclesView` prikazuje korisnikova vozila i omogućuje rad s aktivnim vozilom. `MaintenanceView` prikazuje održavanja. `CatalogView` prikazuje i filtrira standardne zahvate. `ServicesView` prikazuje servisnu povijest i detalj servisa. `ProblemsView` prikazuje i sprema probleme aktivnog vozila.

Kompozicijske veze iz `MainFramea` prema Viewovima pokazuju da su ti paneli sastavni dio glavnog prozora.

### 4.3. Paket `controller`

Controlleri primaju Swing događaje, čitaju podatke iz Viewa i pokreću odgovarajuće use-caseove. `MainController` koordinira navigaciju, aktivno vozilo, osvježavanje ekrana i Observer događaje. `AuthController`, `VehiclesController`, `CatalogController`, `ServicesController`, `MaintenanceController` i `ProblemsController` upravljaju pripadajućim funkcionalnim cjelinama.

### 4.4. Paket `service`

Service klase predstavljaju application/business sloj. Svaki Service dobiva `EntityManagerFactory` kroz konstruktor, otvara `EntityManager` za pojedinu operaciju i koristi potrebne Repository objekte. Operacije koje mijenjaju podatke određuju transakcijsku granicu u Service sloju. `ServiceRecordService` je primjer složenije operacije jer u jednoj transakciji sprema servis, njegove stavke, po potrebi ažurira kilometražu i povezuje odabrane probleme sa servisom koji ih je riješio.

### 4.5. Paket `repository`

Repository klase sadrže JPA/JPQL dohvat i spremanje podataka. Svaki Repository dobiva postojeći `EntityManager` kroz konstruktor. Repository ne određuje cijeli poslovni use-case niti samostalno određuje transakcijsku granicu.

### 4.6. Paket `observer`

`Observer` je sučelje s metodom `update(AppEvent)`. `MainController` implementira to sučelje. `Subject` čuva registrirane Observere i šalje `AppEvent` događaje, dok `VehiclesController` i `ServicesController` obavještavaju `Subject` nakon promjena koje zahtijevaju osvježavanje aplikacije.

### 4.7. Paketi `domain` i `strategy`

`MaintenanceCalculator` se nalazi u paketu `domain` i odabire odgovarajuću `MaintenanceStrategy`. `MaintenanceStrategy` je Strategy sučelje, a `MileageMaintenanceStrategy`, `TimeMaintenanceStrategy` i `CombinedMaintenanceStrategy` njegove su konkretne implementacije. Kod kombiniranog intervala koristi se kriterij koji prije dospijeva.

### 4.8. Klase koje nisu prikazane kao zasebni UML elementi

Dijagram prikazuje klase koje su potrebne za razumijevanje glavne strukture sustava. Pomoćni GUI dijelovi `VehicleDialog`, `MileageDialog`, `ServiceEditorDialog`, `VehicleForm` i `VehicleFormController` pripadaju pojedinim korisničkim tokovima i zato nisu zasebno crtani.

`Ui` sadrži zajedničke Swing layout, parsiranje, formatiranje i poruke, dok `Checks` sadrži osnovne validacije. To su utility klase bez vlastitog poslovnog use-casea.

`Data` sadrži pomoćne tipove `ItemInput`, `ServiceInput`, `ServiceRow`, `ServiceDetail`, `MaintenanceRow` i `Dashboard`. Oni služe unosu, prijenosu ili pripremi podataka za prikaz i nisu zaseban arhitekturni sloj.

Persistentni entiteti `AppUser`, `Vehicle`, `VehicleVariant`, `WorkDefinition`, `ServiceRecord`, `ServiceItem` i `Problem` detaljno su prikazani zasebnim ERD-om, pa se njihove strukture ne dupliciraju na aplikacijskom UML-u. Njihova imena pojavljuju se u atributima i potpisima metoda gdje su potrebna za razumijevanje ovisnosti.

## 5. ERD baze podataka

Završna PNG slika `diagrams/ERD_AutoCare_FINAL.png` priložena je od korisnika i sačuvana bez izmjena. Mermaid izvor modela nalazi se u `diagrams/ERD_AutoCare_FINAL.mmd`.

ERD je također izrađen u **Mermaid notaciji** i priložen je kao izvor i PNG:

- `diagrams/ERD_AutoCare_FINAL.mmd`
- `diagrams/ERD_AutoCare_FINAL.png`

Persistentni model sadrži sedam glavnih entiteta: `APP_USER`, `VEHICLE`, `VEHICLE_VARIANT`, `WORK_DEFINITION`, `SERVICE_RECORD`, `SERVICE_ITEM` i `PROBLEM`.

Korisnik može posjedovati više vozila preko veze `VEHICLE.owner_id -> APP_USER.id`. Odvojena veza `APP_USER.active_vehicle_id -> VEHICLE.id` označava trenutno odabrano aktivno vozilo. Te dvije veze imaju različitu poslovnu svrhu: `owns` predstavlja vlasništvo nad svim vozilima, a `activeVehicle` trenutno odabrano vozilo.

Svako vozilo pripada jednoj kataloškoj varijanti. Vozilo može imati više servisnih zapisa i više problema. Svaki servis sadrži jednu ili više stavki, a svaka servisna stavka povezana je s jednim standardnim radom. Problem je otvoren dok `resolved_by_service_id` nema vrijednost, a riješen je kada se poveže sa servisom koji ga je riješio.

## 6. Primijenjeni principi i obrasci dizajna

### MVC

MVC odvaja korisničko sučelje od obrade događaja i poslovne logike. Prednost je jasnija podjela odgovornosti i lakše održavanje. Nedostatak je veći broj klasa i potreba za njihovim povezivanjem, ali je to opravdano zbog većeg broja ekrana i use-caseova.

### Singleton - Session

`Session` koristi Singleton zato što aplikacija tijekom rada treba jedno zajedničko mjesto za identitet prijavljenog korisnika i trenutno aktivno vozilo.

### Observer - Subject, Observer i AppEvent

Observer se koristi za osvježavanje aplikacije nakon važnih promjena. `VehiclesController` i `ServicesController` šalju događaje preko `Subjecta`, a `MainController` kao Observer reagira i osvježava aktivni kontekst i vidljivi ekran. Time kontroleri ne moraju izravno pozivati jedan drugoga.

### Strategy - izračun održavanja

`MaintenanceStrategy` definira način izračuna preostalog servisnog intervala. Postoje strategije za kilometražu, vrijeme i kombinaciju oba kriterija. Kod kombiniranog intervala odlučuje kriterij koji prvi dospijeva.

## 7. Servisna povijest, problemi i cijene

`WorkDefinition.minPrice` i `maxPrice` predstavljaju informativni raspon cijene u Katalogu. Stvarna cijena korisnikove konkretne servisne stavke sprema se kao `ServiceItem.actualPrice`. Time su procjena i stvarno plaćeni iznos jasno odvojeni.

Problemi nisu automatska dijagnoza kvara. Korisnik unosi opis i kategoriju problema, a problem se smatra riješenim tek kada je pri spremanju servisa povezan sa `ServiceRecordom`.

## 8. Persistencija i transakcije

Aplikacija koristi Jakarta Persistence API i Hibernate ORM. Baza je Azure SQL Database / Microsoft SQL Server. Veza se ostvaruje Microsoft JDBC driverom.

Hibernate naming strategy automatski pretvara Java camelCase nazive u SQL nazive s podvlakama, primjerice `serviceRecord` -> `service_record_id`. Zbog toga nisu potrebne dodatne `@Column(name=...)` ili `@JoinColumn(name=...)` anotacije kada konvencija već daje ispravan naziv.

Kod `ServiceRecord.items`, `mappedBy = "serviceRecord"` označava Java polje na vlasničkoj strani veze u `ServiceItem`, a ne SQL naziv stupca. `CascadeType.PERSIST` omogućuje da se pri spremanju novog `ServiceRecorda` spreme i njegove nove stavke.

## 9. Vanjske biblioteke i Maven

Projekt koristi Maven za upravljanje zavisnostima i build. Glavne vanjske komponente su:

| Komponenta | Verzija | Namjena |
|---|---:|---|
| Jakarta Persistence API | 3.2.0 | Standardni JPA API |
| Hibernate ORM | 7.4.8.Final | JPA provider i ORM |
| Microsoft JDBC Driver for SQL Server | 13.4.0.jre11 | Veza prema Azure SQL / SQL Server bazi |
| FlatLaf | 3.6.2 | Swing Look and Feel |
| Ikonli Swing | 12.4.0 | Integracija ikona sa Swingom |
| Ikonli FontAwesome 6 pack | 12.4.0 | FontAwesome ikone |
| Maven Compiler Plugin | 3.14.1 | Kompajliranje Java 25 projekta |
| Maven JAR Plugin | 3.4.2 | Izrada izvršnog JAR-a |
| Maven Dependency Plugin | 3.8.1 | Kopiranje runtime zavisnosti u `target/lib` |
| Maven Javadoc Plugin | 3.12.0 | Generiranje HTML API dokumentacije |

Službeni izvori i poveznice nalaze se u `docs/VANJSKE_BIBLIOTEKE.md`.

## 10. API dokumentacija - Javadoc

Generirani HTML output nalazi se u mapi `javadoc/`, s početnom stranicom `javadoc/index.html`.

Uz projekt se prilaže generirana Javadoc API dokumentacija. Javadoc komentari opisuju javne klase i važne javne/protected konstruktore i metode, posebno Controller, Service, Repository, Domain, `Session`, Observer i Strategy dijelove. Dokumentacija opisuje odgovornost klase ili metode, parametre, povratne vrijednosti i relevantne iznimke bez nepotrebnog ponavljanja trivijalnog koda.

HTML Javadoc generira se Mavenom iz završnog sourcea i prilaže uz projekt.

## 11. Git aciklički graf

Stvarni tekstualni snapshot povijesti pohranjen je u `git/git_graph.txt`.

Uz projekt se prilaže stvarni Git aciklički graf povijesti repozitorija. Graf se generira iz Git povijesti naredbom `git log --graph --oneline --decorate --all --date-order`, čime se prikazuju stvarni commitovi i njihove veze.

Uz tekstualni `git_graph.txt` može se priložiti i čitljivi PNG/SVG prikaz istog grafa.

## 12. Prilozi

1. Priložena finalna ERD slika `diagrams/ERD_AutoCare_FINAL.png`.
2. `diagrams/ERD_AutoCare_FINAL.mmd` - Mermaid izvor ERD-a.
3. Priložena finalna UML slika `diagrams/UML_AutoCare_FINAL.png`.
4. `diagrams/UML_AutoCare_FINAL.mmd` - Mermaid izvor UML-a.
5. `wireframes/AutoCare_GUI_Wireframes_FINAL.pdf` - GUI wireframeovi izrađeni u Figmi.
6. Generirana Javadoc HTML dokumentacija.
7. Git aciklički graf povijesti repozitorija.
