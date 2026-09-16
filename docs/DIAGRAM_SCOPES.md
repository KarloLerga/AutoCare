# Razdvajanje prikaza prema novom reviewu

Ovo nije promjena GUI-ja ni zahtjev da kod slijedi stare slikovne makete. Nakon sto stvarni kod
prodje testove, dokumentaciju uskladiti s tim kodom. Nove finalne slike nisu renderirane u ovom dodatku.

## 1. Arhitektura: odgovornosti i ovisnosti

Na ovoj slici prikazati View, Controller, Application/Service, Domain, repository sucelja i JPA
implementaciju/infrastrukturu. Service koristi domenu i repository/transaction sucelja. Jpa implementacije
implementiraju ta sucelja, mapiraju domain i ovise o JPA/Hibernate infrastrukturi. Baza je vanjski tehnicki
resurs te infrastrukture. Domain nije tehnicka postaja nakon baze i nema SQL pozivnu strelicu prema njoj.

Service oznaciti kao vlasnika POSLOVNE granice transakcije. JpaTransactionRunner oznaciti kao izvrsitelja
TEHNICKOG lifecyclea jedne tako odredjene transakcije. Repository oznaciti samo query/persist.
Na ovu sliku ne stavljati svaki povratni rezultat, ORM mapiranje pojedinog polja ili callback dogadjaj.
Jedna jasna legenda strelice: ovisnost. Tok save-a objasnjava se tekstom ili zasebnim sequence prikazom.

## 2. Prvi UML class prikaz: persistentna domena

Nakon name cleanupa: AppUser, Vehicle, VehicleVariant, ServiceRecord, ServiceItem, WorkDefinition,
VehicleWorkRule, Problem, DiagnosticRule; enumove prikazati kao enum, ne kao dodatne SQL tablice.
Prikazati vazne atribute, domenske metode i stvarne kardinalnosti, ne svih pedeset getter metoda.

Primjeri: Vehicle ima jednog ownera i jednu varijantu; ServiceRecord pripada jednom vozilu i sadrzi
najmanje jednu ServiceItem; Problem ima opcionalni resolvedByService (vise problema na jedan servis).
Ne izmisljati User.vehicles kolekciju samo zato sto FK semanticki opisuje vise vozila. Association moze
postojati i bez Java kolekcije u oba smjera. AppUser.activeVehicle tehnicki nullable pri registraciji,
a dovrseni racun ga poslovno mora imati. Te dvije tvrdnje se objasnjavaju, ne mijesaju.

U ovaj prikaz ne stavljati Controllers, Strategy implementaciju ili DiagnosticResult kao entitete.

## 3. Drugi UML class prikaz: aplikacijski/design odnosi

Prikazati reprezentativni kompletan save/analysis put kroz STVARNE tipove:
ServicesController, ServiceRecordService, TransactionRunner, JpaTransactionRunner,
Repositories i relevantna mala repository sucelja/implementacije. Dodati ProblemService,
DiagnosticStrategy, KeywordDiagnosticStrategy i ne-persistentni Data.DiagnosticResult.
Pojednostaviti prikaz zajednickih ponavljanja i objasniti ostale Service klase u tablici.

ServiceRecordService ovisi o TransactionRunner sucelju, ne concrete JpaTransactionRunneru.
JpaTransactionRunner implementira sucelje i kreira konkretne repository objekte s jednim EM-om.
ProblemService koristi DiagnosticStrategy sucelje; rezultat je plain Java privremeni podatak.
Ne prikazivati nasljedivanje gdje je samo konstruktorom predana ovisnost.

## 4. ERD je treci, razlicit tip dokumenta

ERD prikazuje fizicke SQL tablice i FK stupce nakon naming migracije. Nema Controller/Strategy tablice.
Ostaje kao obavezni dio dokumentacije, odvojen od dva UML class pogleda. Kratki sequence/save opis
moze biti poseban dodatak, ali nije zamjena za arhitekturni dependency prikaz.

## 5. Sto je profesor stvarno trazio

Profesor je predlozio dva dijagrama klasa i jasnu Service transakcijsku odgovornost, ne nove module
ili tocno odredjeni paketni naziv. Nazivi iz ovoga dokumenta prate nas konkretni kod i korisnikov cleanup,
nisu obvezni nazivi prepisani iz njegova komentara. Pri generiranju finalnih slika uzeti aktualni source.
