# Veza s OOP/NOOP i obrana

Ovo nije tvrdnja da je profesor napisao identicne AutoCare klase. Pregledani su relevantni prilozeni izvorni primjeri, a njihove tehnike prenesene su na drugi domenski problem. Ne kopiraju se nepotrebne ovisnosti ni eventualne greske edukativnih primjera.

## Konkretni pregledani tragovi

| Materijal | Sto se vidi / koristi | AutoCare |
|---|---|---|
| NOOP/02_Strategy/StrategyPtrn_T1V1.zip, StrategyPattern/src/pckg/Zad_1_Calculator/CalcStrategyInterfc i Abs_Calculator | Malo sucelje algoritma; objekt drzi strategiju i delegira joj posao | DiagnosticStrategy / KeywordDiagnosticStrategy; ProblemService ne sadrzi algoritam bodovanja |
| NOOP/03_Observer/wth_station_pckg.zip, ObserverInt i WeatherStationObservable | Suclje update, lista pretplatnika, dodavanje/uklanjanje i obavjestavanje | AppListener / AppEvents; nakon commita objavljuje se domenski event, kontroler osvjezava vidljivu stranicu |
| NOOP/10_MVC_GUI_Baza_EventDriven/MVC_simple-20260901.zip | SwingUtilities.invokeLater, AppFrame, raspored panela, GUI listeneri | Main, MainFrame i konkretni kontroleri; prikaz ne radi SQL |
| NOOP/10_MVC_GUI_Baza_EventDriven/MVC_V8.zip, MVC_T/controllerTable/Controller.java | Controller delegira podatkovni posao; zasebni GUI paneli i rad s bazom | Controller -> Service -> repository/JPA; dodatni Service potreban za atomarno vise entiteta |
| NOOP/08_SOLID/T7 - SOLID.pdf | Podjela odgovornosti, kompozicija, ovisnost o ugovorima | Mali repo interfacei, zamjenjiva strategija, View odvojen od business pravila |
| OOP teme iz arhive: enkapsulacija, konstruktori, kolekcije, iznimke, Swing | Obicne Java klase, kontrola promjene stanja, event-driven GUI | Entiteti s poslovnim metodama, List/Map, try/finally i listeneri |
| NOOP teme/syllabus: generics, visedretvenost, MVC + baza/ORM, dokumentacija | Parametrizirana sucelja, rad izvan EDT-a, Maven/Javadoc/Git | TransactionRunner<T>, DataTable<T>, SwingWorker i dokumentirani slojevi |

Putanje unutar arhive mogu sadrzavati dodatni direktorij raspakiranog projekta; nazive provjeri prema izvornom paketu, nemoj ih tretirati kao URL javnog repozitorija. Videozapisi i svaki duplicirani zadatak nisu zasebno pregledani. Prilozeni review prvenstveno zahtijeva jasne odgovornosti, transakciju, smislen Strategy i dovrsene dijagrame, a ne maksimalan broj obrazaca.

## Sto je inzenjerska dopuna, a ne preuzeta profesorova implementacija

JPA `EntityManager` lifecycle i mali TransactionRunner razrada su dogovorenog Hibernate/JPA koncepta; pregledani MVC primjer koristi i JDBC/DataBase pristup. FlatLaf je dopuna izgleda, ne gradivo koje profesor navodno zahtijeva. PBKDF2/600000, Azure TLS identity i dijagnosticki `Problem.requestKey` su prakticne odluke za ovaj problem. Raniji AF3 servisni request-key/row-lock tok uklonjen je u V2 studentskoj simplifikaciji; to nije funkcionalnost koju treba prezentirati kao aktualnu.

Nema Springa, Lomboka, DI frameworka, MapStructa, RxJava, recorda za svaku sitnicu, generic BaseRepositoryja ni odvojenog modela entiteta i ORM modela. JDK25 koristi se kao alat, ne kao razlog za demonstraciju naprednih jezicnih mogucnosti koje problem ne treba.

## SOLID - tocna obrazlozenja

SRP: ServiceEditorDialog prikuplja unos, njegov Controller vodi akciju, ServiceRecordService odredjuje poslovni use-case, JpaServiceRecordRepository radi upite. Jedna promjena razloga ne zahtijeva SQL u Viewu.

OCP: drugi DiagnosticStrategy moze zamijeniti KeywordDiagnosticStrategy ako postuje isti ulaz/izlaz. Dodavanje keyword podataka nije zaseban dokaz OCP-a, nego data-driven konfiguracija. Formula nije hardkodirana u GUI.

LSP: implementacija strategije mora zadrzati ugovor (isti tip ulaza/rezultata, valjane granice scorea, nema neocekivanog GUI/DB rada). Sama cinjenica da klasa ima implements nije dokaz da je ugovor ocuvan.

ISP: pet smislenih repository sucelja umjesto jednog sucelja s metodama svih tablica. Svaki getter ne treba zasebno sucelje.

DIP: Service prima TransactionRunner i repo sucelja te DiagnosticStrategy, ne konstruira Jpa implementacije. Main povezuje konkretne klase; DI ne zahtijeva framework.

## Kompozicija, nasljedjivanje i Singleton

ServiceRecord sadrzi ServiceItem: stavka nema samostalan zivotni ciklus, zato kompozicija i cascade/orphanRemoval samo ovdje. MAINTENANCE i REPAIR razlikuju se enumom jer imaju istu strukturu stavke; podklase bez razlicitog ponasanja bile bi umjetne. ProblemStatus ne trazi State pattern s dva jednostavna stanja. Jedna EMF instanca nije automatski GoF Singleton s privatnim konstruktorom/getInstance; u ovoj implementaciji lifecycle vodi Main.

## Scenarij prezentacije do35 minuta

0-4min: problem, ograniceni funkcionalni opseg, arhitektura, pravi vs demo podaci.
4-10min: registracija s obveznim vozilom i opcionalnom starom cijenom; pokazi da odustajanje nema retke baze.
10-18min: problem -> lokalna analiza -> spremanje -> servis s vise stavki i rjesavanjem problema; pokazi promjenu kilometraze i odrzavanja.
18-23min: otvori kod ServiceRecordService i JpaTransactionRunner; nacrtaj isti EM i rollback. Razlika persist/managed/dirty checking.
23-28min: Strategy formula, Observer i SwingWorker done na EDT-u. Pokazi granice nepoznatih podataka.
28-32min: testovi, Javadoc, ERD/UML, izvori, sigurnost, stvarni Git DAG.
32-35min: ogranicenja i pitanja. Ne trositi vecinu vremena na boje.

## Pitanja koja student treba znati odgovoriti

1. Zasto @Id na polju znaci field access? Zasto je no-arg protected?
2. Zasto mappedBy sadrzi Java ime serviceRecord, a defaultni FK stupac je serviceRecord_id?
3. Sto se dogadja ako svaki repository commit-a zasebno? Pokazi izgubljenu atomicnost na primjeru servisa/problema.
4. Zasto to-one LAZY nije problem kad se DTO sastavi prije zatvaranja EM-a?
5. Zasto se actual NULL ne pretvara u0 niti u estimate?
6. Zasto datum i kilometraza koriste OR za dospijece? Sto znaci unknown history?
7. Sto tocno predstavlja75% podudaranja pravila i sto NE predstavlja?
8. Koji thread izvrsava query/hash, a koji setText? Sto se dogadja ako odgovor A stigne nakon odabira B?
9. Kako jednostavan SwingWorker tok prikazuje uspjeh ili gresku spremanja, i koje napredne retry/idempotency zastite V2 namjerno nema?
10. Koja je razlika izmedu poslovne provjere zadnjeg vozila u jednoj transakciji i pune zastite od konkurentnih klijenata?
11. Zasto JPA update nije seed i zasto ne kreira Azure server?
12. Zasto broj/model auta ne odredjuje tocan hrvatski racun servisa?

O autorstvu i dozvoljenoj pomoci AI-ja izvijesti prema pravilima kolegija. Git mora dokumentirati stvaran razvoj/integraciju, ne glumiti da je vec generirani kod nastajao ranijih dana bez pomoci.


## AF3 dopuna
Baza je korisnikovom zadnjom odlukom Azure SQL Database, ne izvorni MySQL. To nije tvrdnja da je profesor propisao SQL Server. Java JDBC seed sa staging paketima i Python image tooling su razvojna infrastruktura, ne novi runtime slojevi. ScheduleKind je jedan enum/stupac: fiksni, prema stanju, prema indikatoru ili nepoznat plan. Ne uvodimo Strategy za svaki zahvat. Izvorne materijale nosi zasebna privatna mapa source-materials, koja se ne objavljuje u Git.

## V2 aktualno stanje

V2 zadrzava Strategy za dijagnostiku, Observer, JPA/EntityManager i pet repository sucelja s pet
JPA implementacija. Slozenost koja nije potrebna za studentski use-case uklonjena je iz servisnog
spremanja, povijesti, sessiona i background helpera. `Problem` dijagnostika ostaje zamrznuta,
ukljucujuci svoj `requestKey`; `ServiceRecord` ga vise nema. Maintenance prikaz koristi samo
`NO_DATA`, `OK`, `SOON` i `DUE`, a `WorkDefinition` moze imati nullable default intervale.
