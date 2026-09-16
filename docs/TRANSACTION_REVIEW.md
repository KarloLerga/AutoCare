# Tko odredjuje transakciju, a tko je tehnicki provodi

## V2 aktualizacija

V2 zadrzava jednu EM/transakciju po poslovnoj write operaciji i pet repository implementacija nad
istim EM-om, ali uklanja servisni request-key recovery, owner pessimistic lock i dodatnu commit-state
mašineriju. `ServiceRecordService.create` prima jednostavan `ServiceInput`; `JpaTransactionRunner`
ima samo begin/commit, rollback aktivne transakcije kod `RuntimeException` i close. Dijagnosticki
`ProblemService.save` i `Problem.requestKey` nisu dio ove promjene.

## Kratko objasnjenje za obranu

ServiceRecordService zna da servis, njegove stavke, kilometraza i odabrani problemi moraju uspjeti
zajedno. Zato cijeli taj rad predaje jednom TransactionRunner.write pozivu. JpaTransactionRunner
tehnicki otvara jedan EntityManager i jednu transakciju te daje svim repositoryjima isti EntityManager.
Repositoryji dohvacaju i persistiraju objekte, a domenske metode mijenjaju njihovo poslovno stanje.
Runner nakon cijelog callbacka potvrdi sve ili pokusa vratiti sve. Controller tek nakon uspjeha
obavjestava prikaze da trebaju svjeze podatke.

To sto commit() nije napisan neposredno u ServiceRecordService datoteci ne znaci da Repository odredjuje
granicu. Granicu odredjuje Serviceov odabir CIJELOG callbacka. Tehnicka implementacija nije poslovna odluka.

## Tocni objekti u jednoj operaciji

| Vrsta | Broj | Uloga |
|---|---:|---|
| ServiceRecordService.create poziv | 1 | Jedan poslovni zahtjev iz forme. |
| TransactionRunner.write poziv | 1 | Sve povezane promjene unutar callbacka. |
| EntityManager | 1 | Jedan kratkotrajni persistence context. |
| EntityTransaction | 1 | Jedna resource-local transakcija. |
| Jpa repository objekti | 5 | Isti EM, pojedinacno potrebni upiti/spremanje. |
| Commit | 1 pri uspjehu | Potvrda tek nakon cijelog callbacka i flusha. |
| GUI SERVICE_SAVED | Nakon potvrde | Lokalno osvjezavanje, nije trajni messaging sustav. |

Read callback takodjer koristi kratku transakciju, ali to samo po sebi ne obecava snapshot svih upita:
vidljivost ovisi o stvarnoj izolaciji SQL veze. Ne predstavljati to kao REPEATABLE READ/SERIALIZABLE bez postavke.

## Pravo mjesto poslovnih pravila

Vehicle stiti kilometrazu od smanjivanja. Problem zna kada se smije rijesiti i kojem vozilu servis pripada.
ServiceRecord kontrolira kolekciju svojih ServiceItem objekata. Service orkestrira ta ponasanja kroz vise
objekata, provjerava ownership i poslovni tok. Repository nema odluku koji simptomi znace koji popravak.
Domain nije element iza Repositoryja u lancu tehnickih slojeva: obje strane koriste domain tipove.

JPA anotacije u domeni su namjerni prakticni izbor; poslovne metode ne koriste EntityManager ili SQL.
Ne treba zato duplicirati svih 9 entiteta u drugi persistence model da bi dijagram izgledao cisce.

## Registracija i ugnijezdeni pozivi

AuthService.register ima svoju jednu transakciju za korisnika, prvo vozilo, aktivaciju i pocetnu povijest.
Zove saveInside s postojecim Repositories. Taj package-private helper NE zove transactions.write.
Ne smije se zamijeniti javnim ServiceRecordService.create za svaku povijesnu stavku: to bi otvorilo nove
kontekste i razbilo atomarnost registracije.

## Iznimke i obavijesti

- Neuspjela validacija prije commita: rollback aktivne transakcije i jasna pogreska.
- Greska prilikom flusha: nema naseg commit poziva; best-effort rollback, ne lazni uspjeh.
- Poznat optimistic/integrity konflikt: korisnik osvjezava, nema automatskog overwritea.
- Nejasan ishod potvrde: V2 ne radi automatski retry servisnog INSERT-a; korisnik treba svjesno provjeriti povijest.
- Rollback/close greska ne smije biti razlog za lazno prikazivanje uspjeha.
- Uspjesan commit pa greska zatvaranja/GUI refresha: ne nuditi novi INSERT kao rjesenje refresh problema.

SERVICE_SAVED je in-memory UI dogadjaj. U trenutku gasenja procesa moze izostati iako je commit uspio;
iduca prijava zato cita istinu iz baze. Nije potrebno dodavati outbox/Kafka/framework za ovaj projekt.

## Zasto ne druga rjesenja

Ne novi Spring ili @Transactional proxy. Ne commit po repositoryju. Ne globalni EntityManager.
Ne kopirati pun lifecycle kod u svaki service. Postojeci mali runner rjesava ponavljanje i zadrzava
jednostavan, vidljiv callback bez skrivenog frameworka. Tehnicka poboljsanja su lokalna u postojecoj klasi.

## Izvori za dodatna tehnicka tumacenja

- Koncept: odjeljak 7 (koraci 3-7) i 9. Isto pravilo jedne transakcije vec je tamo opisano.
- Profesor: zadnja prenesena poruka o Service granicama i Repository ulozi.
- Jakarta EntityTransaction: https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/entitytransaction
- Hibernate ORM 7.4.8.Final guide: https://docs.hibernate.org/orm/7.4/userguide/html_single/

Nase klasifikacije AppException i testovi nisu profesorovi doslovni primjeri nego primjena na konkretan kod.
