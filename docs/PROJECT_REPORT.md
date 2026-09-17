# AutoCare - popratna projektna dokumentacija
Autor/student: Karlo Lerga. Predmet: Napredno objektno programiranje. Referentna implementacija pripremljena uz AI pomoc; stvarne lokalne integracijske promjene i rezultati zasebno se evidentiraju.

## Problem
Privatni korisnik s vise automobila ima rasprsene podatke o servisima, kilometrazi, problemima i stvarno placenim iznosima. Bez pouzdane povijesti nije moguce znati zadnje odrzavanje. Genericki servisni intervali nisu isto sto i plan za konkretnu varijantu. AutoCare objedinjuje evidenciju i transparentne informativne procjene, bez glumljenja profesionalne dijagnoze.

## Rjesenje / konceptualni model
User ima vlastita Vehicle vozila i pamti aktivno. Vehicle koristi shared VehicleVariant katalog. ServiceRecord s datumom/km ima ServiceItem stavke, svaka povezana s WorkDefinition. WorkCategory razlikuje odrzavanje i popravak bez praznih podklasa. VehicleWorkRule veze varijantu/rad i drzi eventualni period i cijenu. Problem pripada vozilu, moze imati predlozen popravak i rijesen je jednim stvarnim servisom. DiagnosticRule mapira frazu/tezinu u kandidat popravka. Ostatak prikaza i draftova nije baza.

Slika `architecture.png` pokazuje ovisnosti slojeva. `domain.png` prikazuje samo persistentnu domenu, a `design.png` zasebno prikazuje Service/Repository/Strategy odnose i `Data.DiagnosticResult`. `erd.png` prikazuje fizicke snake_case tablice/FK koje runtime koristi uz Hibernate physical naming strategy; potpuna polja i owning-side odluke su u DATABASE_AND_JPA.md i schema manifestu. DOT/Mermaid izvori su prilozeni za reprodukciju, a ERD nije tvrdnja o novom live introspectionu.

## Implementacija
Java25, Swing i FlatLaf; Maven. Jakarta Persistence API preko Hibernate providera. EntityManagerFactory lifecycle je jedan po aplikaciji; svaki write use-case eksplicitno otvara EM i transakciju, a read ga zatvara nakon mapiranja. Pet malih repository sucelja; implementacije primaju isti EM unutar operacije. Constructor injection radi Main, bez DI frameworka. Entiteti ne napustaju Service u GUI; scalar result modeli nastaju dok je EM otvoren.

### Jedna slozena operacija
Unos servisa je lokalni draft. Spremanje validira datum/km/stavke i korisnika, kreira record/items, povecava kilometrazu samo prema gore i rjesava oznacene OPEN probleme istog vozila. Sve se commita ili rollbacka u jednoj operaciji. Nakon commita Observer osvjezava relevantne prikaze. Posljednje odrzavanje pronalazi se po izvrsenom datumu/km/ID, ne redoslijedu insertanja. Finalni studentski flow namjerno nema request-key/idempotency sloj.

### Algoritmi
Interval dospijeva kad bilo koji definirani km ili kalendarski rok bude dosegnut. Bez potrebnih intervala/povijesti prikazuje se `NO_DATA`; ostali statusi su `OK`, `SOON` i `DUE`. Specificni VehicleWorkRule ima prednost kao cjelina, a WorkDefinition defaulti koriste se samo kada specificno pravilo ne postoji. Ukupni poznati trosak plus broj nepoznatih cijena sprecava zamjenu NULL nulom. Dijagnostika normalizira fraze i rangira pokrivenost aktivnih ponderiranih pravila. Ponavljanje rijeci ne napuhuje bodove; negacije nisu pouzdano shvacene i to je ogranicenje. Rezultat nije vjerojatnost.

## GUI
Svi konceptualni ekrani/tokovi, kontrole, layouti i objasnjenja nalaze se u GUI_AND_WIREFRAMES.md. Izvorni PDF koristi se kao zahtjev funkcionalnosti, ne pixel template. Katalog koristi pretragu/tablicu, ne golemi JComboBox. DB pozivi su namjerno sinkroni i vidljivi u kontrolerima radi studentske citljivosti; manualni GUI smoke je zasebno oznacen BLOCKED u verification dokumentu.

## Principi i gradivo
MVC dijeli UI i poslovne akcije. Strategy izolira zamjenjivi algoritam. Observer spaja promjene stanja i zainteresirane prikaze bez globalne magije. Kompozicija ServiceRecord/ServiceItem ima stvarni zivotni ciklus; enum je dovoljan za kategoriju/status. Repository izolira pristup bazi. SRP/OCP/DIP/ISP i ugovori zamjenjivosti obrazlozeni su u COURSE_ALIGNMENT_AND_DEFENSE.md; ne tvrdi se da sama rijec implements dokazuje LSP. Ne dodajemo Factory/Command/Memento samo za popis bodova.

## Podaci i ogranicenja
Katalog je normaliziran iz korisnikova ZIP-a vehicle-makes-models, uz stabilne kodove, licence i evidentirane prilagodbe. 122 rada su prakticni prosireni katalog, ne iscrpna lista svakog dijela svakog automobila. 1.282.916 cijena su modelirane planske bruto vrijednosti, a ne nacionalni prosjeci ili verificirane ponude. Za dio izvedbi broj se ne daje. 34 izvorno referencirana perioda pokrivaju uzak modelski podskup; 676 kandidata nije automatski odobreno. Nepoznata OEM/VIN informacija ostaje nepoznata.

Katalog nema fotografije po vozilu ni image-enrichment/import pipeline. UI koristi samo dekorativne FontAwesome6 Ikonli ikone; one ne predstavljaju identitet ili podudaranje vozila.

## Sigurnost i trosak infrastrukture
SQL Server umjesto prvotnog MySQL-a je korisnikova nova odluka zbog free offera. SQL vjerodajnica je u lokalnoj vanjskoj konfiguraciji; TLS provjera identiteta ukljucena. U ovom studentskom modelu aplikacijska lozinka je obican tekstualni `String` u bazi; to je svjesni obrazovni kompromis i nije produkcijska sigurnosna preporuka. Owner-scoped upiti i jedna transakcija cuvaju aplikacijske invarijante. Direktna shared SQL vjerodajnica u desktopu nije produkcijska izolacija korisnika. Serverless potrosnja prati se u portalu, konekcije se zatvaraju, paid overage ne aktivira se automatski.

## Dokumentacija API-ja i povijest
Maven Javadoc generira `target/reports/apidocs` ili putanju koju prijavi stvarna verzija plugina; zabiljeziti stvarno mjesto. Opisati javne poslovne ugovore, ne dodavati prazne komentare svakom getteru. Biblioteke i sluzbeni izvori su u DEPENDENCIES_AND_SOURCES.md.

Git povijest mora odrazavati stvarnu implementaciju/integraciju. `scripts/export-git-history.ps1` izvozi aktualni DAG i HEAD u dokumentaciju. Nema backdate, uklanjanja koda pa vracanja radi izgleda ili izmisljanja autora/komitova. Za predaju navesti uporabu AI pomoci prema pravilima kolegija.

## Testiranost
VERIFICATION.md razlikuje stvarno izvrsene offline provjere od neizvrsenih SQL Server/JDK25/Maven/GUI provjera. ACCEPTANCE.md definira dokaz dovrsenosti. Ovaj izvjestaj ne tvrdi da su neizvrseni dijelovi vec prosli. Za obranu je pripremljen 35-minutni demonstracijski tok te kratka teorijska pitanja uz kod.
