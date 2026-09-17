# AutoCare - Java 25, Swing, JPA/Hibernate, Azure SQL Database
Projekt za Napredno objektno programiranje. Privatna evidencija vozila, odrzavanja, stvarnih servisa i problema. MVC + Service + Repository, Strategy za izracun intervala odrzavanja i mali Observer za osvjezavanje. Bez runtime LLM-a i vanjskog vehicle/image API-ja.

## Pokretanje
Potreban je JDK 25 i lokalna konekcijska datoteka IZVAN repozitorija. Maven 3.9.16 bootstrap i sluzbeni wrapper priprema `scripts/Complete-Setup.ps1`. Skript ne stvara Azure bazu i ne mijenja billing; korisnikova postojeca baza mora vec postojati.
```powershell
.\scripts\Complete-Setup.ps1 -ConfigPath 'C:\private-autocare\connection.local.json' -ImportCompleteCatalog -ApplyFinalSchema -Launch
```
Format privatne datoteke (NE commitati stvarne vrijednosti):
```json
{"host":"auto-care.database.windows.net","port":1433,"database":"","user":"karlolerga","password":"LOCAL_ONLY"}
```
Prazan database pokrece read-only pokusaj `db-list`. Vise dostupnih baza zahtijeva odabir. `-DatabaseName 'stvarni-naziv'` nadjacava izbor; provjeren naziv sprema se samo u tu privatnu datoteku. SQL login nije login u AutoCare aplikaciju; prvo se registrira aplikacijski korisnik. Kompletni katalog i schema cleanup izvrsavaju se samo uz eksplicitne switch-e iz gornje naredbe.

Skripta bez `-ImportCompleteCatalog` samo validira kompletni katalog read-only; bez `-ApplyFinalSchema` samo prikazuje migraciju read-only. Za svakodnevno pokretanje nakon inicijalnog setupa:
```powershell
.\scripts\Run-App.ps1 -ConfigPath 'C:\private-autocare\connection.local.json'
```

## Rucne naredbe
```powershell
.\mvnw.cmd clean verify
. .\scripts\Load-Connection.ps1 -ConfigPath 'C:\private-autocare\connection.local.json'
java -jar tools/setup/target/autocare-setup-1.0.0.jar sql-check
# Normalni runtime koristi postojece snake_case SQL nazive; ne pokretati rename predloske.
# schema/08_plain_password_and_remove_images.sql prvo pokrenuti read-only s @Apply = 0.
# Nakon provjere tocne baze, backupa i ovisnosti promjenu primijeniti samo na tu bazu.
java -Xmx768m -jar tools/setup/target/autocare-setup-1.0.0.jar import-complete-catalog tools/reference-data/data/vehicle_work_rules_complete.csv.gz
java -Xmx768m -jar tools/setup/target/autocare-setup-1.0.0.jar apply-final-schema schema/09_complete_catalog_and_runtime_cleanup.sql
java -Xmx768m -jar tools/setup/target/autocare-setup-1.0.0.jar final-audit schema/final_catalog_audit.sql
.\mvnw.cmd javadoc:javadoc
```
Runtime distribucija je `target/autocare-1.0.0.jar` s `target/lib/`; setup distribucija je odvojeni `tools/setup/target/autocare-setup-1.0.0.jar` s vlastitim `lib/`. Samo kopiranje JAR-a bez pripadajucih ovisnosti ne radi. Runtime JAR pokrece samo GUI; `sql-check`, `schema-update`, seed i import naredbe pripadaju setup JAR-u. Runtime ne upravlja DDL-om (`hbm2ddl=none`); `schema-update` je iskljucivo eksplicitna developerska naredba setup artefakta. Ne izvrsavati stare MySQL ili rename skripte.

## Podaci i istinitost
Katalog sadrzi 30.366 varijanti, 120 konkretnih radova i 2.944.248 materijaliziranih pravila. Svako pravilo ima pozitivnu modeliranu cijenu; odrzavanje ima eksplicitni kilometarski ili mjesecni interval, a popravci nemaju izmisljeni interval. Jedna planska cijena sadrzi dijelove/rad i modelirana je po pravilima kataloga; to nije statisticki hrvatski prosjek ni servisna ponuda. Stvarno placeno cuva cente i nikad se ne preuzima iz procjene.

Intervali su rezultat pregledanih pravila i deterministickog modela; procjena ne tvrdi da zamjenjuje servisni prirucnik. Odrzavanje se prikazuje samo kada postoji u povijesti servisa, a statusi su `OK`, `SOON` i `DUE`. Problemi se unose rucno i povezuju s konkretnim radom iz kataloga. `OTHER_*` radovi nisu dio konacne sheme.

## Dokumentacija
- `docs/ARCHITECTURE_FREEZE_AF3.md`: odluke A-O, transakcije, GUI, validacija.
- `docs/DATABASE_AND_JPA.md`, `schema/`, `docs/TYPE_CATALOG.md`, dijagrami DOT/Mermaid/PNG/SVG.
- `docs/architecture.*`: dependency prikaz; `docs/domain.*`: persistentni UML; `docs/design.*`: aplikacijski UML; `docs/erd.*`: fizicki target ERD.
- `docs/AZURE_SETUP.md`, `docs/ACCEPTANCE.md`, `docs/VERIFICATION.md`.
- `docs/DIAGRAM_SCOPES.md`, `docs/TRANSACTION_REVIEW.md`, `docs/AUDIT_AND_DECISIONS.md`.
- `docs/PROJECT_REPORT.md`, `docs/GUI_AND_WIREFRAMES.md`, `docs/COURSE_ALIGNMENT_AND_DEFENSE.md`.
- `docs/DEPENDENCIES_AND_SOURCES.md`, `tools/reference-data/README_HR.md`, `schema/08_plain_password_and_remove_images.sql`.
- `docs/MASTER_CODEX_PROMPT.md`: lokalna fazna integracija i provjere.
- `docs/ACCEPTANCE_CHECKLIST.md`, `docs/DATA_EXPLANATION_FOR_DEFENSE.md`: zavrsni kriteriji i obrana modela podataka.
- `data_model/`, `scripts/complete_catalog_rules.py`, `scripts/validate_complete_catalog.py`: reproducibilni generator i validacija kompletnog kataloga.
- `schema/09_complete_catalog_and_runtime_cleanup.sql`, `schema/final_catalog_audit.sql`: guarded cleanup i read-only zavrsni audit.

## Sigurnost / besplatna ponuda
Ne commitati local JSON, lozinke, tokene ni cijeli isporuceni ZIP. Provjera certifikata ostaje ukljucena. SQL Server Object Explorer i aplikaciju zatvoriti kada nisu potrebni da konekcije ne ometaju serverless mirovanje. Ne ukljucivati placeni nastavak koristenja radi prolaza testa. Detalji i izvori su u Azure vodicu (AZURE_SETUP.md).

## Test status
Runtime Java koristi camelCase logicka imena, a Hibernate `CamelCaseToUnderscoresNamingStrategy` ih mapira na snake_case Azure SQL ugovor. Konacna shema ne sadrzi image pipeline, dijagnosticku tablicu ni `OTHER_*` radove. Runtime ne upravlja DDL-om (`hbm2ddl=none`); schema migracija je iskljucivo eksplicitna naredba setup artefakta. Offline Java/Python provjere nisu dokaz performansi importa ni GUI rada. Tocne izvrsene i neizvrsene provjere nalaze se u `docs/VERIFICATION.md` i `docs/IMPLEMENTATION_STATUS.md`; ne oznacavaj izolirani SQL/GUI scenarij kao PASS bez stvarnog prolaza.
