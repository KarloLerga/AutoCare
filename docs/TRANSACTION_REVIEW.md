# Tko određuje transakciju

## Aktualni V2/finalni stil

Service određuje poslovnu granicu, a JPA/Hibernate i `EntityTransaction` tehnički je provode.
`ServiceRecordService.create` u jednom otvorenom `EntityManageru`:

1. počinje transakciju
2. provjerava korisnika i vlasništvo vozila
3. sprema servis i sve stavke
4. povećava kilometražu ako treba
5. rješava odabrane otvorene probleme
6. commit-a ili rollback-a
7. zatvara EntityManager

Repositoryji su samo objekti nad tim EntityManagerom. Ne otvaraju vlastitu transakciju i ne donose
odluke o vlasništvu. `AuthService.register` koristi istu ideju za račun, vozilo i početnu povijest;
package-private `saveInside` ne stvara novi EntityManager.

## Zašto je kod izravan

Ovaj studentski projekt ne skriva lifecycle iza dodatnog generičkog helpera. U kodu se mogu jasno
pokazati `EntityManager`, `EntityTransaction`, `begin`, `commit`, `rollback` i `finally/close`.
To smanjuje broj koncepata na obrani. Sinkroni pozivi su namjerna studentska odluka; ručni GUI test
ostaje zasebno evidentiran.

## Observer nakon uspjeha

Controller nakon uspješnog commita objavi `AppEvent`. `AppEvents` prolazi običnom listom listenera,
a `MainController` ponovo učita vidljivi ekran. To je in-memory UI događaj, ne trajni messaging sustav.

## Greške

Validacija i neuspjeh flush-a prekidaju operaciju, aktivna transakcija se vraća, a originalna iznimka
se prosljeđuje korisničkom sloju. Kod refresh greške nakon commita ne treba stvarati novi zapis.
Detaljna stvarna provjera nalazi se u `docs/VERIFICATION.md`.
