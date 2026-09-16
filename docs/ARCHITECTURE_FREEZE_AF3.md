# AutoCare AF3 - finalna objedinjena arhitekturna odluka

## V2 napomena o aktualnom kodu

Ovaj dokument cuva AF3 odluke kao povijesni kontekst. Za aktualni studentski kod vrijedi V2
simplifikacija iz zadnjeg predanog plana: dijagnostika (`Problem`, `ProblemService`, `RuleData`,
`DiagnosticResult`, `Analysis`, Strategy i AnalysisDialog) je zamrznuta, dok su servisno spremanje,
session, UI background helper, servisna povijest i odrzavanje pojednostavljeni. `ServiceRecord` vise
nema `requestKey`, servisni flow nema idempotency/retry state machine ni owner pessimistic lock, a
`Problem.requestKey` ostaje zbog zamrznutog dijagnostickog toka. `MaintenanceStatus` ima samo
`NO_DATA`, `OK`, `SOON`, `DUE`; `WorkDefinition` moze imati nullable default km/month intervale.
V2 ne uvodi Maintenance Strategy pattern. Povijesne AF3 tvrdnje nize koje opisuju uklonjene mehanizme
ne treba citati kao aktualni API ugovor; za tocne potpise vrijedi `docs/TYPE_CATALOG.md` i izvorni kod.

Delta nakon F7: Java logicka camelCase imena runtimea mapiraju se Hibernateovom
`CamelCaseToUnderscoresNamingStrategy` na postojece snake_case Azure SQL tablice i stupce. Ne radi se
masovni rename baze; `schema/06_student_runtime_compat.sql` je jedini mali kompatibilni DB patch i
zadano je read-only. `ScheduleKind` ostaje samo legacy persistence/setup metadata, a maintenance
izracun koristi intervale u kilometrima i mjesecima.

Datum: 16. 9. 2026. Izvor zahtjeva: korisnikova zadnja odluka, obavezni kriteriji kolegija, izvorni handoff/review i dorađeni koncept. Ovo je specifikacija i referentni kod, **ne potvrda izvrsenog Azure/Java25 end-to-end testa**.

## 0. Sto se mijenja i sto ostaje

Azure SQL Database Free Offer zamjenjuje Azure Database for MySQL u SVIM aktivnim dijelovima. Stare dokumente i pakete cuvamo u history/ samo kao povijesne izvore. Njihov MySQL Connector/J, port3306, sslMode, SHOW/INFORMATION_SCHEMA specifikacije i stari seed importer NISU upute za novu instalaciju.

Ostaje isti funkcionalni projekt: Java25 desktop Swing, Maven, Jakarta Persistence EntityManager, Hibernate provider, MVC/Service/Domain/Repository, devet entiteta, mali Observer i stvarni DiagnosticStrategy. Nema Springa, Lomboka, runtime AI-ja, REST-a, odvojenog troskovnog modula, image API-ja tijekom koristenja, blobova niti dodatnih image tablica.

Novi podaci ne zahtijevaju stotine klasa. WorkDefinition i VehicleWorkRule ostaju nositelji definicija i varijantnih pravila. Dodan je jedan opravdan stupac `schedule_kind` i enum ScheduleKind: `FIXED`, `CONDITION_BASED`, `VEHICLE_INDICATOR`, `UNKNOWN`. Time odsutnost roka ne znaci automatski da treba izmisliti redovni interval. Popravak pogonske baterije ima cijenu samo kada je opravdano poznata i nema proizvoljan rok zamjene.

## A. Paketi i ovisnosti

`hr.unizd.autocare`: app, domain, model, repository, persistence, service, strategy, event, controller, view, view.components, tools.

View -> Controller -> Service -> Repository sucelja / Domain. Jpa repository implementacije drze isti EM dobiven za transakciju. Main spaja implementacije konstruktorima. Domain ima JPA anotacije, ali ne zna za Swing, SQL upite ili servise. Model/Data nosi nepromjenjive ulaze/rezultate i kopije lista, bez managed entiteta. Tools su eksplicitni developerski CLI, ne dio korisnicke navigacije. Python priprema/obrada slika nisu runtime ovisnosti aplikacije.

## B. Svaki Java tip

`TYPE_CATALOG.md` generiran je iz stvarnih deklaracija svih produkcijskih datoteka, ukljucujuci unutarnje tipove i potpise javnih metoda. Kod u `src/main/java` je konkretna implementacija, ne samo predlozak. Nove SQL tools klase su SqlSettings, SqlSeedTool, SeedFiles, ImagePathTool i ReviewedIntervalTool; nijedna nije persistentni entitet. Sve metode i njihove tocne argumente citati u prilozenom kodu, ne u starom AF1 manifestu.

## C. Tablice i stupci

`DATABASE_AND_JPA.md` i `ENTITY_MANIFEST.json` sadrze svih devet tablica i svako mapirano polje. Tablice: app_user, vehicle_variant, vehicle, work_definition, vehicle_work_rule, service_record, service_item, problem, diagnostic_rule; default schema dbo. Identitet je BIGINT IDENTITY. Novac je DECIMAL(9,2), score DECIMAL(5,2), datum DATE, created_at DATETIME2, boolean BIT. Tekst mora sacuvati Unicode kroz NVARCHAR/nationalized data. Enum stringove daje Hibernate SQLServerDialect. @Version je obicni BIGINT brojac, ne SQL Server timestamp/rowversion.

Ne dodaje se tabela za zbroj troska, status odrzavanja, posljednji servis, sliku, draft, dijagnosticki rezultat ni source audit. Podrijetlo intervala/procjene je u postojecim tekstualnim poljima i dodatnim CSV manifestima izvan baze. Ne spremati izvedene rokove koji ce zastarjeti.

## D. JPA relacije

Relacijska matrica je u DATABASE_AND_JPA. Field access, protected no-arg, smisleni konstruktori, domenske metode, ne setter za sve. To-one veze eksplicitno LAZY. Jedina inverse kolekcija je ServiceRecord.items mappedBy=serviceRecord s ALL/orphanRemoval; owning FK je ServiceItem.service_record_id. Prema zajednickom katalogu NEMA cascade remove.

User.activeVehicle je nullable ManyToOne BEZ unique=true. Na SQL Serveru obicni nullable UNIQUE nije ekvivalent MySQL nacinu dopustanja vise NULL vrijednosti; ne stvarati nepotrebnu registracijsku prepreku. Jedan User logicki ima jedno aktivno vlastito vozilo nakon commita. Vlasnistvo je provjereno servisom/domenom, ne izmisljeno pomocu unique FK-a. SQL NULL je dopusten samo kao tehnicki medjukorak unutar registracije/brisanja. Nema potrebe za odvojenim filtered indexom za ovo svojstvo.

## E. Transakcije

| Use-case | Jedna transakcija i promjene |
|---|---|
| Registracija | User, prvo Vehicle, activeVehicle, cijela opcionalna povijest. Odustajanje prije finish nema redaka. |
| Novo/uredi vozilo | Owner lock, validacija kataloga/godine/identiteta, insert ili dirty checking. |
| Aktivacija | Owner lock, dokaz vlastitog vozila, promjena aktivnog FK. |
| Brisanje | Owner lock; zadnje zabranjeno; zamjena aktivnog -> problemi -> stavke -> servisi -> vozilo. |
| Novi servis | Owner lock, UUID requestKey, servis/stavke, kilometraza samo raste, odabrani problemi OPEN -> RESOLVED. |
| Spremi problem | Owner lock, ponovni izracun usporedbe previewa, requestKey, snapshot najboljeg kandidata. |
| Profil | Owner lock, optimistic verzija, aktualna lozinka za osjetljive promjene, hash izvan locka/EDT-a. |

JpaTransactionRunner kreira jedan EM/RESOURCE_LOCAL transakciju i pet repository objekata nad tim EM-om. Repository ne begin/commit/close. Ne ugnijezditi drugi javni service koji ponovno otvara runner. Password hashing je prije dugotrajnog DB locka. Read koristi kratku transakciju. EntityManager se ne dijeli izmedju SwingWorkera.

Write vraca ID ili void. Fresh read je nova operacija: greska osvjezavanja ne znaci da spremanje nije uspjelo. Dvostruki klik se blokira, ServiceRecord/Problem imaju trajni jedinstveni requestKey. Pri nepoznatom ishodu commita ne stvarati novi UUID i novi INSERT. Isti requestKey i isti sadrzaj smiju vratiti vec postojeci ID; promijenjeni sadrzaj je konflikt. Ne prikazati rollback kao cinjenicu kada je veza nestala tijekom commita.

## F. Query/fetch plan

Katalog: godina -> distinct marke -> modeli -> najvise201 varijanta (200 prikaz, indikator suzavanja). Nema30k stavki u ComboBoxu. Search koristi bindane parametre/locate i ne tumaci korisnikov % kao proizvoljan wildcard.

Vozila: owner-scoped lista + fetch potrebne varijante. Servisi: prvo stranica50 ID-eva, onda fetch stavki/radova za te ID-eve; ne paginirati collection fetch join. Detail i problem su owner-scoped. Zbroj stvarnih troskova SUM plus razlika count(all)/count(actual) cuvaju nepoznate iznose.

Odrzavanje: samo pravila aktivne varijante (najvise mali broj zahvata), posljednje izvrsene stavke tog jednog vozila sortirane datum DESC, km DESC, id DESC. Milijunski seed se ne ucitava u Swing ili u List svih JPA entiteta. Nema lazy dereferenciranja iz Viewa nakon zatvaranja EM-a. SQL Server-specific paginaciju generira dialect; ne pisati LIMIT.

## G. GUI tokovi i ergonomija

Jedan JFrame, auth i main CardLayout, lijeva navigacija Dashboard/Vozila/Odrzavanje/Servisi/Problemi/Profil. Aktivno vozilo mijenja se samo na Vozila. Login/onboarding nemaju sidebar. FlatLaf Light mijenja izgled standardnih Swing komponenti, ne arhitekturu.

Godina/km: brojcani spinner s commitEdit. Marka/model: ovisni JComboBox. Generacija/motor i duzi popis radova: pretraziva JTable. Datum: strogo polje dd.MM.uuuu. Cijene: decimalni editor s validacijom. Problemi rijeseni servisom: checkbox tablica. Simptomi: multiline wrap. Ne raditi skupe custom animacije/drawing widgete.

Svaki DB poziv i hash ide u SwingWorker. Controller snapshotira input na EDT-u; doInBackground ne cita promjenjive Swing komponente; done prikazuje rezultat. Session epoch/request ticket odbacuje odgovor prethodnog vozila i prijavu koja je stigla nakon odustajanja. Listeneri se ne dodaju ponovno pri svakom loadu; hidden panel postaje dirty umjesto nepotrebnih deset queryja.

New service je jedna tablica s kategorijom, odabranim radom i stvarnom cijenom. Nema persista nakon svakog dodavanja retka. Prije Save stopCellEditing mora uspjeti. Tablicni view index pretvara se u model index. Cancel/Escape/X imaju isti dirty/write guard. Promjena varijante tijekom onboardinga trazi odbacivanje povijesti prethodne varijante.

Podatak 'procjena ne postoji' ne znaci da korisnik ne moze evidentirati racun: poznati primjenjivi rad s NULL cijenom i OTHER_MAINTENANCE/OTHER_REPAIR s obaveznom napomenom omogucuju povijest. Tesla se ne puni uljem/zupcastim remenom samo zato sto ti radovi postoje globalno.

## H. Poslovna pravila/validacija

Ista pravila provjeravaju se u Service/Domain i u UI radi upotrebljivosti. Canonical ASCII email trim/lowercase i unique; ime do100; lozinka po Checks.password, PBKDF2WithHmacSHA256 sa saltom, bez plaintext app-passworda. Mileage 0..3,000,000, ne smanjuje trenutni brojac. Servis ne smije biti u buducnosti ni prije godine vozila. Barem jedna stavka, rad najvise jednom u istom servisu. Normalni actualPrice obavezan nenegativni BigDecimal s dvije decimale; pocetna povijest moze NULL, sto NIJE nula.

Procjena je jedan bruto planski iznos: dijelovi+rad+uobicajeni materijal. HALF_UP na desetice, oznaka 'Procijenjena ukupna cijena' / znak pribliznosti. Nema tvrdnje 'prosjecna cijena u Hrvatskoj'. ActualPrice od53,47 ostaje53,47. Ne kopirati estimate u actual. U privremenom kalkulatoru sprijeciti preklapanje uljni paket+isti filtar, plocice+diskovi/plocice itd. Ne zabraniti stvarni racun koji legitimno ima odvojene stavke.

FIXED: prvi dosegnuti km ILI vrijeme; granica ukljuciva. SOON koristi min(3000,20%km) ili min(30dana,20%stvarnog kalendarskog intervala). plusMonths, ne30dana puta broj mjeseci. Nema povijesti -> UNKNOWN_HISTORY. Nema roka -> UNKNOWN_INTERVAL. CONDITION_BASED i VEHICLE_INDICATOR imaju vlastiti tekst, ne lazni OK/DUE. Ne pretvarati pregled u zamjenu, prvi veliki interval u ponavljajuci ni jamstvo baterije u rok zamjene.

DiagnosticStrategy je algoritam nad pripremljenim RuleData, bez JPA/GUI. Ponderirana pokrivenost svih aktivnih pravila kandidata, normalizacija fraza, ponavljanje ne pumpa bodove, deterministicki tie-break. Score NIJE vjerojatnost niti sigurna dijagnoza. Glavna procjena uzima prvi kandidat, ne zbroj alternativnih kvarova. Nema pogodaka je valjan rezultat. Negacije/uzrocnost nisu pouzdano razumjene; ozbiljni simptomi zahtijevaju strucni pregled.

## I. Pogreske

Validacija daje korisnicki tekst. DB/debug detalji nisu korisnicki racun ni signal ponoviti write. SQLException/ORM cause ostaje u kontroliranom logu, bez lozinki; nemoj dumpati env ili connection properties. Kod foreign/unique konflikta podatak se ne 'popravlja' gasenjem ogranicenja. SQL auth pogreska, firewall, DNS, pogresan DB i potrosena besplatna kvota su razliciti problemi.

Importer: prije veze provjeri svih pet SHA i sve retke; temp staging1000, commit paketa. Raniji paketi ostaju nakon prekida, rerun koristi code/unique pair. Cijene i intervali drugih izvora nisu prepisani. Ne brisati postojece korisnike/katalog da bi sve 'postalo zeleno'. Nepoznat zadnji commit provjeriti idempotentnim rerunom, ne DROP/TRUNCATE.

## J. Ovisnosti/verzije

Tocne verzije su u pom.xml i DEPENDENCIES_AND_SOURCES.md. Java25; Jakarta3.2; Hibernate7.4.8.Final; Microsoft JDBC13.4.0.jre11; FlatLaf3.6.2; Maven3.9.16. 'jre11' oznacava minimalnu Java granu drivera, nije zahtjev za downgrade Jave25. Python samo developer stdlib; Pillow12.3.0 samo image alat. Ne dodavati MySQL Connector ili Python DB driver.

## K. Konfiguracija/Azure Free Offer

Hostname je auto-care.database.windows.net, port1433, SQL auth user je unaprijed zadan lokalno. Naziv baze NIJE naveden i ne izvodi se iz servera. `db-list` je read-only master discovery. Ne stvara bazu, server ni placeni resource. Ako ne moze otkriti ime, potreban je tocni Database name iz portala. Odabrana postojeca razvojna baza potvrđuje se AUTOCARE_SCHEMA_TARGET/AUTOCARE_SEED_TARGET.

Hibernate radi schema-update samo eksplicitno i nakon builda; GUI uvijek validate. TLS encrypt=true i trustServerCertificate=false; ne gasiti provjeru radi brzeg povezivanja. SQL password nikada u committed sourceu. Lokalni JSON je izvan project/ i izvan Gita. To nije aplikacijska korisnicka lozinka.

Free offer ima ogranicen compute i storage. Ne prebacivati na placeni overage, ne povecavati SKU, ne kreirati paid DB radi testa. Aktivne SQL veze/Object Explorer mogu sprijeciti auto-pause. Hikari minIdle0, keepalive0, max2; zatvoriti aplikaciju i SQL alate kada se ne koriste. Nema garantiranog trajanja/compute troska punog importa prije stvarnog benchmarka. Drugi testni DB je samo ako korisnik ima posebno potvrdjen besplatni/izolirani resurs.

Desktop s distribuiranom SQL vjerodajnicom nije produkcijski sigurnosni zid izmedju nepovjerljivih korisnika. Owner-scoped upiti i hash login ne uklanjaju tu arhitekturnu granicu. Primjereno kontroliranom kolegijskom deploymentu; za proizvodni multi-tenant sustav potreban bi bio backend, koji NIJE dodan u ovaj projekt.

## L. Podaci i slike

Gotov skup:30.366 varijanti,122 definicije,1.650.435 seed pravila od kojih1.282.916 imaju modeliranu cijenu. Ukupni audit3.704.652 kombinacije;1.179.688 uvjetnih kombinacija ostaju odvojene.3500 varijanti nema brojcanu plansku cijenu; ne izmisljati je samo zbog popunjenosti.34 referencirana pravila intervala su zasebni opt-in,676 dodatnih kandidata ceka pregled trzista/model-yeara. Potpunost podatkovne obrade nije pouzdanost automobilskih cinjenica.

Prvo Hibernate/validate -> mali sample -> test osnovnih tokova -> puni Java seed -> namjerno odobreni intervali -> slikovna faza. Python build_af3.py reproducira model iz spremljenog normaliziranog baznog skupa; originalni ZIP i v1 normalizacija ostaju dostupni. Izvorni dataset/izvedeni katalog nose ODbL attribution. Nema poziva LLM-u za vrijeme uvoza.

Slike:6.697 make/model/generation grupa. Direktni Wikidata+Commons API koristi se samo u Python developer alatu. carapi PHP servis nije runtime dependency ni automatski autoritet za generaciju. lookup proizvodi DRAFT kandidata; odobravanje trazi stvarni pregled generacije i licence. download standardizira<=960x600, cuva provenance, izbjegava ponovni download dobrih slika. Konacni imagePath import tek nakon ponovnog builda JAR-a i provjere hashova classpath resursa. Fallback je uvijek lokalni /images/vehicles/fallback-car.jpg. Niti jedna stvarna fotografija nije sada automatski odobrena.

## M. Testovi i prihvat

Procitaj VERIFICATION.md za stvarno izvrsene provjere i ogranicenja. CoreChecks57 + dodatne25 assertion provjere,31 Python test i potpuni streaming audit provjereni su u pripremi. JPA/FlatLaf cijeli source dobio je samo dodatnu staticku provjeru s privremenim API zamjenama; one nisu isporucene i NE dokazuju provider/JDBC rad. Pure SQL/CSV helperi kompajlirani su stvarnim JDK21 bez zamjena. Nema laznog PASS za Maven25, Azure import, SQL lock/DDL ili Windows GUI. Te provjere izvrsava lokalni Codex.

Obavezni SQL prihvat: nullable aktivni FK za vise korisnika, rollback servisa, requestKey rerun, nepoznati actual iznos, vlastita/tuđa vozila, optimistic konflikt, brisanje aktivnog/zadnjeg, seed rerun i zastita rucne cijene/intervala. Obavezni GUI:100/125/150%DPI, tipkovnica, Edit zadnje celije, Cancel/X/Escape, duge oznake, kasni odgovor A nakon B, cold-start/error, svi osnovni tokovi. Obavezna provjera podataka: EV bez ulja/remena, unknown/condition-based, izvor intervala, rounding procjene ne racuna.

## N. Veza s kolegijem

Poseban COURSE_ALIGNMENT_AND_DEFENSE.md navodi pregledane izvorne primjere Strategy, Observer, MVC i SOLID. Ovo je prilagodba istih principa drugoj domeni, ne tvrdnja da je profesor napisao seed pipeline, PBKDF2 ili SQL Server konfiguraciju. Git povijest su stvarne faze integracije/testa/ispravaka, bez backdatea, fake screenshotova ili brisanja/vracanja istog koda radi dojma samostalnog razvoja. Javadoc, opis problema/rjesenja, UML, ERD, wireframe, biblioteke, izvori i stvarni Git DAG su obavezni deliverables.

## O. Promjene u odnosu na prethodne verzije

| Prije | AF3 | Zasto |
|---|---|---|
| Azure MySQL | Azure SQL Free Offer / SQL Server | Korisnik vec ima ovaj resurs; ne placa drugi motor. |
| MySQL JDBC/Python importer | Microsoft JDBC + Java chunked seed | Jedan Java runtime, odgovarajuci SQL dijalekt, efikasni masovni upisi. |
|39 zahvata |122 definicije i eksplicitna primjenjivost | Sire pokrice bez novih domena/UI modula. |
|NULL interval bez znacenja |ScheduleKind | Razlikuje nepoznato od prema stanju/indikatoru. |
|nullable UNIQUE active FK |nullable nonunique FK + invariant | Registracija na SQL Serveru bez nepotrebne unique-NULL prepreke. |
|Globalni cjenovni fallback |Samo varijantna primjenjivost, OTHER za rucni zapis | EV ne dobiva ICE radove i nepoznata cijena nije nula. |
|Stari podaci/intervali izmijesani |Zasebni model/reference/review CSV | Cijena ne odobrava interval ili opremu. |
|Slike rano ili runtime API |Kasna developer faza/review/cache/local resource | Bez vanjskih servisa u GUI-ju i bez pogresnih generacija po defaultu. |
|Moguce rasponi u prikazu |Jedna ukupna procjena na10EUR | Korisnikov zahtjev; ruke nisu poseban UI modul. |

Svi aktualni izmijenjeni izvori su u project/. Ako lokalni Codex pronadje stvarni problem, treba ga minimalno popraviti i dodati regresijski dokaz; ne smije odrzavati gresku samo zato sto je u referentnom paketu.

## REVIEW-CLEAN-2 aktualizacija

Ovaj AF3 dokument cuva povijesne odluke i raniji dokazni kontekst. Aktualni cleanup kod koristi Java
logicka imena opisana u `docs/DATABASE_AND_JPA.md`, a physical naming strategy ih prevodi na postojeci
snake_case SQL ugovor. `AppUser`, `VehicleVariant`, `Vehicle`, `WorkDefinition`, `VehicleWorkRule`,
`ServiceRecord`, `ServiceItem`, `Problem` i `DiagnosticRule` ostaju runtime entiteti. Runtime `src/main`
vise ne sadrzi setup/import alate; oni su u neovisnom `tools/setup` Maven projektu. `domain.*` i `design.*`
su odvojeni UML prikazi, a `architecture.*` je dependency prikaz. Povijesni target-name manifesti i
rename skripte nisu potrebni za normalni runtime; aktualni live DB patch je `schema/06_student_runtime_compat.sql`.
