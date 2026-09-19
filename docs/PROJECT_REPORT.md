# AutoCare - trenutačni koncept projekta

## Problem i cilj

Aplikacija je namijenjena vlasniku vozila koji želi na jednom mjestu pratiti svoja vozila, kilometražu, servisnu povijest, preventivno održavanje i stvarne troškove. Korisnik također može zapisati stvar koju je primijetio na vozilu kako je ne bi zaboravio prije odlaska kod mehaničara.

Aplikacija ne pokušava dijagnosticirati kvar. Informativne cijene odvojene su u pretraživi Katalog, dok stvarni trošak nastaje tek kada korisnik nakon popravka spremi servis.

## Glavne funkcije

### Korisnik i vozila

Korisnik se registrira i kod registracije dodaje prvo vozilo. Može imati više vozila, ali jedno je aktivno. Aktivno vozilo mijenja samo na ekranu Vozila.

### Bilješke

Korisnik upisuje slobodan tekst, npr. "Klima slabije hladi nego prije". Može odabrati grubu kategoriju: Motor, Kočnice, Ovjes, Klima, Elektrika ili Ostalo. Bilješka je aktivna dok se ručno ne zatvori ili ne označi riješenom kod stvarnog servisa.

### Katalog

Katalog sadrži 120 standardnih održavanja i popravaka. Može se pretraživati po nazivu i filtrirati po kategoriji. Za aktivno vozilo prikazuje informativni min-max raspon cijene prema široj cjenovnoj klasi vozila.

### Servisi

Servisni zapis sadrži datum, kilometražu, napomenu i jednu ili više maintenance/repair stavki. Korisnik za svaku stavku ručno upisuje stvarno plaćenu cijenu. Servis po potrebi zatvara jednu ili više aktivnih bilješki.

Ako servisna kilometraža prelazi trenutačnu kilometražu vozila, vozilo se ažurira. Povijesni zapis s manjom kilometražom ne smanjuje trenutačnu kilometražu.

### Održavanje

Održavanje koristi servisnu povijest kao source of truth. Za svaku maintenance stavku koja je već evidentirana računa se posljednje izvršenje, sljedeći km/datum i status.

Interval može biti:
- kilometarski;
- vremenski;
- kombinirani.

Strategy pattern razdvaja ta tri načina računanja.

### Dashboard

Prikazuje:
- ukupni stvarni trošak;
- sljedeće praćeno održavanje;
- broj aktivnih bilješki;
- trenutnu kilometražu.

## Arhitektura

`Swing View -> Controller -> Service -> Repository -> JPA EntityManager/Hibernate -> Azure SQL`

- View ne sadrži poslovna pravila ni JPA.
- Controller obrađuje GUI događaje i poziva Service.
- Service upravlja poslovnim pravilima i transakcijom.
- Repository s proslijeđenim EntityManagerom sadrži JPQL/dohvat/spremanje.
- Domain sadrži entitete i njihove jednostavne invarijante.

Kod složene operacije spremanja servisa jedan Service otvara jedan EntityManager i jednu transakciju. Svi Repositoryji u tom use-caseu koriste isti EntityManager.

## Obrasci

### MVC

Osnovna organizacija GUI aplikacije.

### Strategy

`MileageMaintenanceStrategy`, `TimeMaintenanceStrategy` i `CombinedMaintenanceStrategy` računaju status održavanja ovisno o vrsti intervala. Novi način računanja može se dodati bez mijenjanja GUI-a.

### Repository/DAO

JPA upiti odvojeni su od GUI-a i poslovnih pravila.

### Observer/listener

Mali `AppEvents` mehanizam osvježava povezane ekrane nakon promjene aktivnog vozila, spremanja servisa ili bilješke.

## Persistencija

Koristi se JPA API s Hibernate providerom i Azure SQL Database. Runtime koristi `EntityManager`, ne Hibernate Session API. `EntityManagerFactory` se inicijalizira jednom, a `EntityManager` je kratkotrajan po operaciji.

Finalni model nema automatsku dijagnostiku i nema per-variant tablicu s milijunima pravila cijena.
