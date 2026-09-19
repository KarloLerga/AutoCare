# Dorađeni koncept projekta nakon zadnjeg reviewa

## Svrha aplikacije

Aplikacija je namijenjena vlasniku vozila. Korisnik vodi vlastitu evidenciju vozila, kilometraže, održavanja, servisne povijesti i stvarno plaćenih troškova. Aplikacija ne pokušava dijagnosticirati kvar i ne govori korisniku što je uzrok simptoma.

## Glavni moduli

### 1. Bilješke

Korisnik može zapisati ono što primjećuje na vozilu, primjerice "Klima slabije hladi nego prije". Uz opis može odabrati samo grubu kategoriju: Motor, Kočnice, Ovjes, Klima, Elektrika ili Ostalo.

Bilješka ima status OPEN ili RESOLVED. Može se ručno zatvoriti ili označiti riješenom prilikom spremanja stvarnog servisa.

### 2. Katalog

Katalog je odvojen od Bilješki. Korisnik izravno pretražuje standardne zahvate i vidi informativni raspon cijene za cjenovnu klasu aktivnog vozila.

Finalni katalog sadrži 120 standardnih zahvata. Svaki zahvat ima kategoriju, a preventivno održavanje dodatno ima kilometarski interval, vremenski interval ili oba. Informativna cijena se sprema kao min-max raspon za pet širih cjenovnih klasa vozila.

Procjena iz Kataloga se nikada ne sprema kao stvarno plaćeni iznos.

### 3. Servisi i servisna povijest

Nakon odlaska kod mehaničara korisnik dodaje stvarni servisni zapis. Upisuje datum, kilometražu, jedan ili više napravljenih radova, stvarno plaćenu cijenu po stavci i opcionalnu napomenu.

Ako je servis riješio neku aktivnu bilješku, korisnik je može označiti prilikom spremanja servisa.

Servis s većom kilometražom ažurira trenutnu kilometražu vozila. Povijesni servis s manjom kilometražom ne smanjuje trenutnu kilometražu.

### 4. Održavanje

Održavanje se računa iz stvarne servisne povijesti. Spremljeni servisni zapis je izvor istine.

Za održavanje se prikazuju posljednje izvršenje, sljedeći datum/km i status. Interval može ovisiti samo o kilometrima, samo o vremenu ili o oba kriterija.

Za ta tri načina računanja koristi se Strategy obrazac:

- `MileageMaintenanceStrategy`
- `TimeMaintenanceStrategy`
- `CombinedMaintenanceStrategy`

### 5. Vozila i Dashboard

Korisnik može imati više vozila, a jedno je aktivno. Aktivno vozilo mijenja se na ekranu Vozila.

Dashboard prikazuje:

- ukupne stvarno plaćene troškove;
- sljedeće praćeno održavanje;
- broj aktivnih bilješki;
- trenutnu kilometražu.

## Arhitektura

Tok odgovornosti ostaje:

`Swing View -> Controller -> Service -> Repository -> JPA/Hibernate -> Azure SQL`

- View prikazuje podatke i prikuplja unos.
- Controller prima GUI događaje i pokreće use-case.
- Service sadrži poslovna pravila i određuje transakcijsku granicu.
- Domain predstavlja stanje i ponašanje glavnih objekata.
- Repository sadrži JPA dohvat i spremanje podataka.

Kod složenog spremanja servisa jedan Service otvara jedan `EntityManager` i jednu transakciju. U istoj transakciji spremaju se servis, njegove stavke, nova kilometraža i eventualno riješene bilješke.

## Baza i cijene

U finalnom modelu nema dijagnostičkih pravila ni velike matrice pravila za svaku pojedinu varijantu vozila.

Postoje:

- 30.366 kataloških varijanti vozila;
- 120 standardnih zahvata;
- 5 cjenovnih klasa vozila;
- 600 informativnih min-max raspona cijena.

Rasponi su informativni i služe samo za planiranje. Stvarna cijena postoji tek u stavci stvarno evidentiranog servisa.
