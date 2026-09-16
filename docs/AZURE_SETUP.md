# Azure SQL konfiguracija i operativni redoslijed

## 1. Postojeci resurs, ne nova MySQL baza
Korisnik je odabrao Azure SQL Database Free Offer. Poznat host `auto-care.database.windows.net`, port 1433 i SQL korisnik `karlolerga`. Stvarno ime baze nije dostavljeno. NE zakljucivati ga iz servera, resource groupa, imena repozitorija ili starih MySQL primjera.

Ako SQL login moze citati master.sys.databases, `java -jar target/autocare-1.0.0.jar db-list` vraca dostupna imena korisnickih baza. To nije admin Azure API. Ako upit nema dozvolu, portal > SQL databases > postojece ime ili MSSQL ekstenzija daju potreban podatak. Ne stvarati drugi placeni resurs da bi se izbjeglo ovo pitanje.

Free offer sluzbeno dokumentira mjesecni compute i storage limit. Stvarni portal konfiguracije ima prednost pred prepisanim opisom RAM-a/CPU-a. Drzati opciju auto-pause kad je limit dosegnut; ne ukljucivati Continue using for additional charges. Detaljno: https://learn.microsoft.com/en-us/azure/azure-sql/database/free-offer?view=azuresql (provjereno 16.9.2026.). Monitorirati free amount remaining. Dug uvoz trosi compute; ne obecavati koliko vremena ili sekundi ce potrositi. Prvo mali seed.

## 2. Sto provjeriti u portalu
SQL authentication mora biti omogucen za navedeni login. Server networking mora dopustiti korisnikovu trenutnu javnu IP adresu; ne otvarati pristup svim adresama. Privatni endpoint zahtijeva odgovarajuci VPN/mrezu; password to ne zaobilazi. Ne mijenjati firewall/billing/identity automatski bez ovlasti.

MSSQL ekstenzija VS Codea (`ms-mssql.mssql`): server `auto-care.database.windows.net,1433`, SQL Login, stvarni database name, encrypt ukljucen, Trust Server Certificate iskljucen. Lozinku upisati lokalno. Ne spremati je u committane settings/launch datoteke. Nakon provjere zatvoriti Object Explorer jer otvorene konekcije mogu sprjecavati auto-pause.

## 3. Konekcijske varijable
Load-Connection.ps1 ucitava privatni JSON u proces. `AUTOCARE_DB_HOST`, `AUTOCARE_DB_PORT`, `AUTOCARE_DB_NAME`, `AUTOCARE_DB_USER`, `AUTOCARE_DB_PASSWORD`. Schema operacija dodatno zahtijeva `AUTOCARE_SCHEMA_TARGET` tocno jednak stvarnom imenu. Seed/image/review import zahtijeva `AUTOCARE_SEED_TARGET`. Privremene su to eksplicitne potvrde cilja, ne passwordi.

JDBC URL: `jdbc:sqlserver://HOST:1433;databaseName=NAME;encrypt=true;trustServerCertificate=false;loginTimeout=60;socketTimeout=120000;applicationName=AutoCare`. User/password u Properties, ne URL-u. Ne koristiti MySQL `sslMode`, `allowPublicKeyRetrieval`, port 3306 ili mysql driver.

## 4. Redoslijed
Pravi JDK25/Maven build -> read-only sql-check -> explicit schema-update -> validate/db-check -> sample seed -> stvarni transakcijski/GUI test -> full seed -> opcionalni izvorni intervali -> kraj, image enrichment. Hibernate stvara tablice u postojecoj bazi. Ne stvara Azure logical server/database i ne puni seed. Java importer koristi JDBC batching/staging jer milijun JPA persist-ova nije prikladno za inicijalno punjenje.

## 5. Sigurno testiranje
Automatski SqlServerIT koristi zaseban config namespace AUTOCARE_TEST_HOST/PORT/NAME/USER/PASSWORD. Ime mora zavrsavati na `_test`, razlikovati se od normalne baze, a AUTOCARE_SCHEMA_TARGET mora izricito imenovati testnu bazu. Test ostavlja DEMO katalog, a cisti vlastite korisnicke retke. Nije test za jedinu bazu s vrijednim korisnickim podacima.

```powershell
$env:AUTOCARE_TEST_HOST='auto-care.database.windows.net'
$env:AUTOCARE_TEST_PORT='1433'
$env:AUTOCARE_TEST_NAME='POSTOJECA_test'
$env:AUTOCARE_TEST_USER='LOKALNI_TEST_LOGIN'
# password postaviti lokalno bez transcript/log ispisa
$env:AUTOCARE_SCHEMA_TARGET=$env:AUTOCARE_TEST_NAME
.\mvnw.cmd -Psqlserver-it verify
```
Ne kreirati Azure testni resurs automatski i ne pretpostavljati da je besplatan. Ako ga nema, profil BLOCKED; ostale offline i nenasilne manual smoke provjere mogu se nastaviti. H2 nije dokaz SQL Server kompatibilnosti.

## 6. Least privilege
Pocetni schema/seed login smije napraviti inicijalizaciju. Za svakodnevni GUI preporucen je zaseban lokalno konfiguriran login: SELECT na katalogu, DML na vlastitim aplikacijskim tablicama, bez schema promjena. Ne postoji zasebna sigurnosna granica korisnika kada se svim desktop klijentima distribuira ista SQL vjerodajnica. Owner-scoped upiti su aplikacijska kontrola; izmijenjeni klijent nije tim putem izoliran. Ovo je kontrolirani studentski deployment, ne tvrdnja o produkcijskoj multi-tenant sigurnosti.

## 7. Greske i ponavljanje
DNS failure -> provjeri mrezu/DNS; firewall -> odobri vlastiti IP; login failed -> provjeri SQL auth/login/password/bazu; certificate -> ispravan FQDN/trust store, nikada trustServerCertificate=true; paused/limit -> status u portalu, ne placeni upgrade. Kratki backoff moguc samo kod otvaranja veze/idempotentnog citanja. Izgubljena potvrda write commita nije dokaz rollbacka; servis koristi isti requestKey, seed se sigurno ponavlja po kljucevima nakon uspostave veze.

Hikari minIdle=0/keepalive=0 i mali pool ogranicavaju nepotrebne stalne veze. Ne pokretati rasporedjeno polling provjeravanje jer trosi besplatni compute. Zatvoriti aplikaciju i SQL explorer po zavrsetku.
