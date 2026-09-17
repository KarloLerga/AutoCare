# AutoCare - Java 25, Swing, JPA/Hibernate, Azure SQL Database
Projekt za Napredno objektno programiranje. Privatna evidencija vozila, odrzavanja, stvarnih servisa i problema. MVC + Service + Repository, Strategy za analizu teksta i mali Observer za osvjezavanje. Bez runtime LLM-a i vanjskog vehicle/image API-ja.

## Pokretanje
Potreban je JDK 25 i lokalna konekcijska datoteka IZVAN repozitorija. Maven 3.9.16 bootstrap i sluzbeni wrapper priprema `scripts/Complete-Setup.ps1`. Skript ne stvara Azure bazu i ne mijenja billing; korisnikova postojeca baza mora vec postojati.
```powershell
.\scripts\Complete-Setup.ps1 -ConfigPath 'C:\private-autocare\connection.local.json' -Launch
```
Format privatne datoteke (NE commitati stvarne vrijednosti):
```json
{"host":"auto-care.database.windows.net","port":1433,"database":"","user":"karlolerga","password":"LOCAL_ONLY"}
```
Prazan database pokrece read-only pokusaj `db-list`. Vise dostupnih baza zahtijeva odabir. `-DatabaseName 'stvarni-naziv'` nadjacava izbor; provjeren naziv sprema se samo u tu privatnu datoteku. SQL login nije login u AutoCare aplikaciju; prvo se registrira aplikacijski korisnik.

Skript po defaultu uvozi mali uzorak, ne 1,65 milijuna pravila. Nakon testiranja:
```powershell
.\scripts\Complete-Setup.ps1 -ConfigPath 'C:\private-autocare\connection.local.json' -FullData
# Izvorno referencirani, ali ne VIN-verificirani intervali su odvojeni opt-in:
.\scripts\Complete-Setup.ps1 -ConfigPath 'C:\private-autocare\connection.local.json' -FullData -WithReferencedIntervals
.\scripts\Run-App.ps1 -ConfigPath 'C:\private-autocare\connection.local.json'
```

## Rucne naredbe
```powershell
.\mvnw.cmd clean verify
. .\scripts\Load-Connection.ps1 -ConfigPath 'C:\private-autocare\connection.local.json'
java -jar tools/setup/target/autocare-setup-1.0.0.jar sql-check
# Normalni runtime koristi postojece snake_case SQL nazive; ne pokretati rename predloske.
# schema/07_final_student_cleanup.sql prvo pokrenuti read-only s @Apply = 0.
# Nakon provjere tocne baze, backupa i ovisnosti promjenu primijeniti samo na tu bazu.
java -jar tools/setup/target/autocare-setup-1.0.0.jar db-check
java -Xmx768m -jar tools/setup/target/autocare-setup-1.0.0.jar seed-validate tools/reference-data/data
$env:AUTOCARE_SEED_TARGET=$env:AUTOCARE_DB_NAME
java -Xmx768m -jar tools/setup/target/autocare-setup-1.0.0.jar seed-all tools/reference-data/data --apply --acknowledge-model-estimates --with-diagnostics
.\mvnw.cmd javadoc:javadoc
```
Runtime distribucija je `target/autocare-1.0.0.jar` s `target/lib/`; setup distribucija je odvojeni `tools/setup/target/autocare-setup-1.0.0.jar` s vlastitim `lib/`. Samo kopiranje JAR-a bez pripadajucih ovisnosti ne radi. Runtime JAR pokrece samo GUI; `sql-check`, `schema-update`, seed i import naredbe pripadaju setup JAR-u. Runtime ne upravlja DDL-om (`hbm2ddl=none`); `schema-update` je iskljucivo eksplicitna developerska naredba setup artefakta. Ne izvrsavati stare MySQL ili rename skripte.

## Podaci i istinitost
Katalog 30.366 varijanti; 122 radova; 1.282.916 modeliranih brojcanih procjena; 1.650.435 redaka za uvoz ukljucuje NULL iznose za individualnu ponudu. Jedna planska cijena sadrzi dijelove/rad, najblizih 10 EUR. To nije statisticki hrvatski prosjek ni servisna ponuda. Stvarno placeno cuva cente i nikad se ne preuzima iz procjene.

34 referencirana intervala predstavljaju uzak OEM modelski podskup, 676 kandidata zahtijeva dodatnu provjeru. Maintenance racun koristi samo kilometarski i mjesecni interval; nepoznat raspored ostaje bez izmisljene vrijednosti. U korisnickom prikazu postoje samo statusi `NO_DATA`, `OK`, `SOON` i `DUE`; `NO_DATA` nije preporuka niti potvrda da je rad nepotreban. Evidencija stvarnog servisa ostaje moguca kada cijena/interval nedostaje. Posebna OTHER_ stavka uz napomenu pokriva rad izvan kataloga.

## Dokumentacija
- `docs/ARCHITECTURE_FREEZE_AF3.md`: odluke A-O, transakcije, GUI, validacija.
- `docs/DATABASE_AND_JPA.md`, `schema/`, `docs/TYPE_CATALOG.md`, dijagrami DOT/Mermaid/PNG/SVG.
- `docs/architecture.*`: dependency prikaz; `docs/domain.*`: persistentni UML; `docs/design.*`: aplikacijski UML; `docs/erd.*`: fizicki target ERD.
- `docs/AZURE_SETUP.md`, `docs/ACCEPTANCE.md`, `docs/VERIFICATION.md`.
- `docs/DIAGRAM_SCOPES.md`, `docs/TRANSACTION_REVIEW.md`, `docs/AUDIT_AND_DECISIONS.md`.
- `docs/PROJECT_REPORT.md`, `docs/GUI_AND_WIREFRAMES.md`, `docs/COURSE_ALIGNMENT_AND_DEFENSE.md`.
- `docs/DEPENDENCIES_AND_SOURCES.md`, `tools/reference-data/README_HR.md`, `tools/images/README.md`.
- `docs/MASTER_CODEX_PROMPT.md`: lokalna fazna integracija i provjere.

## Sigurnost / besplatna ponuda
Ne commitati local JSON, lozinke, tokene ni cijeli isporuceni ZIP. Provjera certifikata ostaje ukljucena. SQL Server Object Explorer i aplikaciju zatvoriti kada nisu potrebni da konekcije ne ometaju serverless mirovanje. Ne ukljucivati placeni nastavak koristenja radi prolaza testa. Detalji i izvori su u Azure vodicu (AZURE_SETUP.md).

## Test status
Runtime Java koristi camelCase logicka imena, a Hibernate `CamelCaseToUnderscoresNamingStrategy` ih mapira na postojeci snake_case Azure SQL ugovor. `schema/07_final_student_cleanup.sql` je zavrsni patch i zadano je read-only; ne radi se masovni rename. Offline Java/Python provjere nisu dokaz performansi importa ni GUI rada. Tocne izvrsene i neizvrsene provjere nalaze se u `docs/VERIFICATION.md` i `docs/IMPLEMENTATION_STATUS.md`; ne oznacavaj izolirani SQL/GUI scenarij kao PASS bez stvarnog prolaza.
