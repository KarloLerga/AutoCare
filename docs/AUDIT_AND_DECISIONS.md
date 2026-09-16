# Audit prilozenog koda i odluke REVIEW-CLEAN-2

## V2 napomena

Ovaj audit opisuje raniji REVIEW-CLEAN-2 snapshot. Nakon njega je stvarno integriran V2 studentski
pass: servisni `ServiceRecord.requestKey`/idempotency tok, owner lockovi, DTO verzije, session ticketi,
service-history paging i `EstimateSelection` uklonjeni su; dijagnosticki `Problem.requestKey` ostaje
zamrznut. Odrzavanje sada koristi `NO_DATA`, `OK`, `SOON`, `DUE`, uz nullable WorkDefinition
default intervale. Zato tvrdnje nize o tim uklonjenim mehanizmima citaj kao povijest odluke, ne kao
aktualni API.

## 1. Izvori i red prvenstva

1. Zadnja profesorova poruka koju je korisnik prenio u razgovoru (sources/PROFESSOR_REVIEW.md).
2. Aktualne korisnikove odluke: Hibernate ostaje, Azure SQL Server ostaje, jednostavnost i finalni runtime;
   stari izgled dijagrama i nazivi iz koncepta nisu obveza kopiranja.
3. `AutoCare_PROJECT_646c563.zip` kao stvarni prilozni snapshot koda.
4. `DoradjeniKoncept (1)(1).docx` s odjeljcima 2, 7 i 9. U izvornom tekstu pise MySQL;
   to je dokumentacijsko starije stanje, ne razlog za vracanje implementacije na MySQL.
5. Prethodni CLEAN-1 paket za nazive, mapiranja, izolaciju alata i citljivost.
6. Sluzbeni JPA/Hibernate/SQL izvori za dodatna tehnicka tumacenja, odvojeni od profesorovih zahtjeva.

Archive SHA256: a6ace15836d1a6d3de1cb9584868b8e936ecb782212bc58b1f82f1d0da6e52c0.
Popis izvora i izdvojeni redci: evidence/SOURCE_AUDIT.json i SOURCE_EXCERPTS.md.
Ne tvrdimo da smo ovom provjerom pregledali sve postojece videolekcije ili aktualnu udaljenu bazu.

## 2. Sto profesor trazi, a sto kod vec radi

| Zahtjev iz poruke | Stvarno u snapshotu | Odluka |
|---|---|---|
| Service odredjuje transakcijsku granicu | ServiceRecordService.create poziva jedan tx.write za cjelokupan servis. | Zadrzati; precizno dokumentirati. |
| Repository ne upravlja poslovnom transakcijom | Nijedna od 5 Jpa*Repository implementacija ne otvara EM/begin/commit/rollback. | Zadrzati; ne seliti commit u njih. |
| Vise repositoryja sudjeluje u istom use-caseu | JpaTransactionRunner stvara jedan EM i predaje ga svim pet repositoryjima. | Zadrzati mali runner, bez novog frameworka. |
| Domain je poslovni model | Vehicle.updateMileage, Problem.resolve, ServiceRecord.addItem/total provode pravila. | Ne micati domenu prema bazi; ispraviti objasnjenje dijagrama. |
| Registracija je cjelina | AuthService.register koristi isti tx.write za User+Vehicle+history preko saveInside. | Zadrzati helper bez nested transakcije; dodatni rollback test. |
| Rezultati/algoritmi nisu tablice | Data.DiagnosticResult nema @Entity; Strategy je zaseban paket. | Ne stvarati tablicu za rezultat; razdvojiti UML prikaze. |
| MVC klase moraju biti vidljive u dokumentaciji | Controller/Service/Repository klase postoje, ali stari UML ih ne prikazuje. | Dopuniti aplikacijski class prikaz, ne dodavati duplicirane klase. |

Zakljucak je o implementiranom smjeru odgovornosti, a ne potvrda da je profesor odobrio sve detalje
ili da su svi SQL/GUI testovi prosli. Kritika dijagrama ne znaci automatski da je kod transakcije pogresan.

## 3. Nasli smo tri mala mjesta za doradu, ne novi arhitekturni problem

### R-01: cleanup mogao prikriti izvornu gresku

Originalni `catch` poziva `tx.isActive()` prije zasticenog try-a. JPA API izricito dopusta iznimku i
na isActive. Takav sekundarni kvar moze prekriti razlog zbog kojeg je spremanje prvotno propalo.
Nova implementacija stiti cijeli isActive+rollback, sekundarnu gresku dodaje kao suppressed i zadrzava
izvornu. Zatvaranje nakon potvrdenog commita ne pretvara uspjesno spremanje u neuspjeh.
To je dodatni tehnicki bugfix koji smo identificirali, a ne profesorov izriciti zahtjev za tom metodom.

### R-02: jasnija klasifikacija commit gresaka

Original uvijek tvrdi da je veza prekinuta kad bilo sto padne tijekom write commita.
Sada je poruka neutralna: ishod nije potvrdjen. Prepoznate JPA/SQL integrity konflikte bez transportnog
uzroka mozemo prijaviti kao CONFLICT. Nejasan ili komunikacijski commit ostaje COMMIT_UNKNOWN.
Nema slijepog ponavljanja. Sam RollbackException ili uspjeh kasnijeg rollback poziva nije dokaz
server-side ishoda. Ovo ne uvodi novu poslovnu klasu ni recovery framework.

### R-03: citljivost servisne operacije i identican requestKey

ServiceRecordService je razlomljen na pregledne blokove i nekoliko privatnih helpera. Ocuvani su
jedna transakcija, stvarne cijene, OTHER_ napomena, ownership, retry sadržaj, max kilometraza i problemi.
Entitet je vec spremao canonical UUID, ali lookup je koristio originalni tekst. Sada su lookup/save/check
jednako kanonizirani. Case-insensitive SQL kolacija mogla je ranije prikriti razliku; ne tvrdimo da je
ona vec izazvala kvar na korisnikovu serveru. Javna create/page/detail/findSaved sucelja ostaju ista.

TransactionRunner sucelje ima isti API, uz pojasnjen ugovor. JPA mehanika ostaje u istoj implementaciji.
Obrada ozbiljnog Error-a pokusava cleanup i ponovno baca istu gresku, ne izmisljeni GUI uspjeh.

## 4. Sto nije promijenjeno

- 9 persistentnih entiteta, 5 repository implementacija, postojeci Service/Domain odnosi.
- Hibernate/JPA, Azure SQL, Maven/Java25/Swing/FlatLaf i njihove postojece verzije.
- Katalog i izracuni cijena/intervala, prava primjenjivosti, statusi ScheduleKind.
- SQL vrijednosti, ID-evi, servisna povijest, stvarno placene cijene, imagePath.
- Glavni GUI tokovi i observer dogadjaj nakon uspjesne transakcije.
- Lokalno poboljsane ilustracije/fallbackovi, drugi novi popravci iz priloznog ZIP-a.

Zbog profesorova reviewa dodano je **0 tablica, 0 entiteta i 0 runtime slojeva**.
Sam R1 patch dira 3 postojece produkcijske datoteke (jedna uglavnom ugovor/komentari).
Veci tekstualni diff nastaje razmicanjem jednocrtnih izraza i komentarima, ne novim funkcijama.

## 5. Prethodni cleanup jos je sastavni dio

CLEAN-1 je odvojena korisnikova zelja: AppUser/defaultna SQL imena, manje suvisnih name= anotacija,
setup izvan runtimea, normalni Main i validate. Time se mijenjaju nazivi 9 tablica prema manifestu,
ne poslovno znacenje podataka. Ovo NIJE novi zahtjev profesora i nije potrebno za ispravnu transakciju.
Migracija mora biti testirana i odvojena od R1 popravka. Nema DROP/TRUNCATE ni ponovnog seeda.

Posebna dopuna: stari CoreChecks i SqlOfflineChecks takodjer koriste tools klase. Samo njihovi
CSV/seed/discovery testovi izlaze u setup projekt. Runtime logicki testovi, TLS i slike ostaju.
Novi SQL test ima vlastiti mali fixture, bez DevelopmentSeed ovisnosti.

## 6. Potrebni dokazi

Pripremljeno je 25 novih JUnit unit testova i zamjenski SQL IT s 3 scenarija. Testovi nisu lazno
proglaseni izvrsenima u ovom okruzenju. Za SQL posebno trebaju: uspjeh s dva problema, greska nakon
izmjene prvog problema i registracija koja padne na drugom povijesnom servisu. Provjerava se svjezim EM-om.
Git diff i syntax-only provjera nisu zamjena za te testove. Status je u VERIFICATION.md.
