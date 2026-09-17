# AutoCare — aktualni arhitekturni ugovor

Ovaj dokument je završna, pojednostavljena verzija ranijih AF3 odluka. Vrijedi za trenutni runtime;
stare prijedloge koji nisu u kodu ne treba prezentirati kao aktivne mogućnosti.

## Tehnologija i granice

Java 25, Maven, Swing/FlatLaf, Jakarta Persistence, Hibernate i Microsoft SQL Server/Azure SQL.
Nema Springa, Lomboka, REST-a, runtime AI-ja, vanjskog image API-ja, BLOB slika ni dodatnih tablica.
Slike su lokalni classpath resources i moraju proći zaseban pregled izvora/licence.

## Slojevi

```text
View
  ↓
Controller
  ↓
Service
  ↓
Repository sučelja
  ↓
JPA repositoryji / Hibernate
  ↓
Azure SQL
```

Controlleri ne pristupaju JPA-i. Viewi posjeduju Swing prikaz detalja. Servicei rade use-case i
transakciju. Repositoryji samo dohvaćaju/persistiraju. Domain nema UI ni SQL kod.

## Persistence

Postoji devet entiteta: `AppUser`, `VehicleVariant`, `Vehicle`, `WorkDefinition`, `VehicleWorkRule`,
`ServiceRecord`, `ServiceItem`, `Problem` i `DiagnosticRule`. Java polja su čitljiva camelCase,
postojeće SQL tablice i stupci su `dbo`/`snake_case`, a naming strategy radi prijevod.

Fizički DDL je vlasništvo baze; runtime `hbm2ddl=none`. Entiteti imaju samo ORM anotacije potrebne
za identitet, veze, kolekciju stavki i string enumove. `actual_price` je stvarno plaćeno, dok su
`estimated_price` i default procjene informativne.

## Poslovne odluke

- Korisnik ima više vozila i jedno aktivno vozilo.
- Servis ima datum, kilometražu, napomenu i jednu ili više stavki.
- Kilometraža se ne smanjuje; odabrani otvoreni problemi istog vozila mogu se riješiti servisom.
- Maintenance koristi kilometarske i/ili mjesečne intervale te statuse `NO_DATA`, `OK`, `SOON`, `DUE`.
- Dijagnostika zadržava postojeću formulu i 87 pravila; rezultat je informativan, ne dijagnoza.
- Izvori i review podataka nisu automatski odobrenje; modelirane cijene nisu nacionalni prosjek.

## Transakcije

Svaki Service write use-case izravno radi `begin → poslovni koraci → commit`, a na RuntimeException
rollback i u `finally` zatvara EntityManager. Registracija koristi istu granicu za račun, vozilo i
početnu povijest. Read operacije otvore EM, mapiraju rezultat i zatvore ga. Nema skrivene transakcijske
infrastrukture ni globalnog EntityManagera.

## Baza i cleanup

`schema/07_final_student_cleanup.sql` je povijesni cleanup za legacy kolone. Aktualni
`schema/08_plain_password_and_remove_images.sql` je read-only po defaultu: preimenuje
`app_user.password_hash` u `password`, eksplicitno poništava stare nereverzibilne vjerodajnice i
uklanja `vehicle_variant.image_path`. Ne briše tablice, katalog, korisničke podatke ni servisnu
povijest. `scripts/verify-database.sql` ostaje završna read-only provjera.

## Status

Live Azure counts nakon cleanupa: 30.366 varijanti, 122 rada, 1.650.435 scoped pravila i 87
dijagnostičkih pravila. Maven/JDK25, Javadoc, setup package, style scanner, SQL check i JPA mapping
smoke su PASS. Odvojeni `_test` profil je NOT_RUN, a ručni Windows GUI smoke je BLOCKED; detalji su u
`docs/VERIFICATION.md`.
