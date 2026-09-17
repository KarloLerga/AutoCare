# AutoCare — veza s kolegijem i obrana

Ovo je studentska Java 25 Swing aplikacija za evidenciju vozila. Kod je namjerno izravan: obični
konstruktori, getteri, kolekcije, petlje, `if/else`, anonimni Swing listeneri i jasna podjela
odgovornosti. AI pomoć i izvorni materijali trebaju se navesti prema pravilima kolegija.

## Arhitektura

```text
View → Controller → Service → Repository interface → JPA repository → Azure SQL
                         ↓
                       Domain
```

- View gradi Swing komponente, čita unos i prikazuje rezultate.
- Controller registrira `ActionListener`, poziva Service i prosljeđuje rezultate Viewu.
- Service provodi use-case, validira poslovni tok i izravno pokazuje granicu transakcije.
- Repository radi samo JPQL/JPA dohvat i spremanje nad proslijeđenim `EntityManagerom`.
- Domain čuva jednostavna pravila poput rasta kilometraže, jedinstvenih stavki i rješavanja problema.

## OOP obrasci koji imaju stvarnu ulogu

Strategy je `DiagnosticStrategy`; `KeywordDiagnosticStrategy` računa postojeće ponderirano
podudaranje 87 aktivnih pravila. Observer je `AppEvents`: Controller se registrira, nakon uspješnog
spremanja objavi događaj, a glavni Controller osvježi trenutni ekran. Repository sučelja su primjer
odvajanja pristupa bazi od poslovne logike. Kompozicija `ServiceRecord`–`ServiceItem` predstavlja
jedan servis s njegovim stavkama.

Nisu uvedeni framework za dependency injection, Spring, Lombok, REST, runtime AI ili vanjski image
API. Java runtime koristi klasične ActionListener objekte i obične `JTable`/`DefaultTableModel`
tablice, što je lakše objasniti na obrani.

## Transakcijski primjer

`ServiceRecordService.create` u jednoj transakciji:

1. otvori `EntityManager` i `EntityTransaction`
2. provjeri korisnika i njegovo vozilo
3. učita radove i kreira `ServiceRecord`/`ServiceItem` objekte
4. poveća kilometražu samo ako je nova vrijednost veća
5. riješi odabrane otvorene probleme istog vozila
6. napravi commit ili rollback na iznimku
7. zatvori `EntityManager`

Registracija analogno sprema račun, prvo vozilo i početnu povijest kao jednu cjelinu.

## Pitanja za obranu

- Zašto je `actualPrice` odvojen od informativne procjene? Zato što se stvarno plaćeni iznos ne smije
  izmišljati iz modela.
- Zašto je SQL u `snake_case`, a Java u camelCase? Hibernate physical naming strategy prevodi
  konvenciju bez masovnog preimenovanja baze.
- Zašto repository ne validira vlasništvo? Service određuje poslovni use-case; repository samo vraća
  objekt ili `null`.
- Zašto `NO_DATA` nije isto što i OK? Nedostatak intervala ili povijesti nije dokaz da rad nije potreban.
- Zašto je dijagnostika Strategy? Algoritam se može zamijeniti bez promjene Controller/Service toka.
- Zašto nema skrivene asinkrone infrastrukture? Za ovaj studentski desktop čitljiv sinkroni poziv je
  dovoljna odluka; GUI provjera ostaje zaseban ručni test.

## Obrambeni redoslijed

Registracija i login → izbor aktivnog vozila → dodavanje/izmjena kilometraže → servis s više stavki
→ stvarne cijene → maintenance status → analiza problema → spremanje problema → rješavanje problema
servisom → dashboard → profil i odjava.

Stvarne build, SQL i blokirane GUI provjere zapisane su u `docs/VERIFICATION.md`.
