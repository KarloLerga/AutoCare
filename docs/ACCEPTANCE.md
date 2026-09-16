# Obavezni uvjeti prihvata

Svaki redak treba stvarni rezultat i naredbu/dokaz. Oznake su PASS, FAIL, NOT_RUN ili
BLOCKED. Preskoceni integracijski test nije PASS.

| Provjera | Ocekivani rezultat |
|---|---|
| Java/Javac/Maven runtime | Stvarni JDK 25 za sva tri, ne samo terminalski `java`. |
| Finalni build | `clean verify` sa stvarnim JPA/Hibernate/FlatLaf ovisnostima, bez API stubs. |
| Javadoc | Generiran je tocni javni ugovor i class list. |
| Package/JAR | Glavni JAR nema setup alate, importere, seed dataset ili privatne resurse. |
| Resursi | Lokalna slika/fallback rade; credits/licence ostaju uz distribuciju. |
| Config | GUI nema `create/update` parametar; runtime koristi samo `validate`. |
| Secrets | Privatna lozinka nije u sourceu, buildu, Javadocu, commitu ili release ZIP-u. |
| Naming | Java camelCase imena uz `CamelCaseToUnderscoresNamingStrategy` mapiraju na postojeci snake_case SQL ugovor; constraint/index literalna imena su fizicka. |
| Schema/migration | Tipovi, NVARCHAR, nullability, identity, FK, unique, check i index potvrdeni na izoliranoj kopiji. |
| CRUD/rollback | Login, ownership, atomic rollback, actualPrice i povijest potvrdeni stvarnim SQL Serverom; servisni V2 flow nema request-key idempotency. |
| Standalone app | GUI radi iz odvojene kopije bez `tools/`, Pythona i seed CSV-a. |
| Developer setup | Setup projekt se builda neovisno nakon `mvnw install`; sample import je ponovljiv. |
| GUI | Rucni tokovi, DPI, dugi tekst, stale response i shutdown provjereni na Windowsu. |
| Git | Stvarni smisleni commitovi i DAG, bez reset/backdate/force-pusha. |

## Naredbe

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javadoc:javadoc
.\mvnw.cmd install
.\mvnw.cmd -f tools\setup\pom.xml clean package
jar tf target\autocare-1.0.0.jar
```

SQL Server integration profil smije koristiti samo zasebnu odobrenu bazu s nastavkom
`_test`, `AUTOCARE_TEST_*` varijablama i tocno potvrdenim `AUTOCARE_TEST_SCHEMA_TARGET`.
Bez nje je status `BLOCKED` ili `NOT_RUN`, nikad `PASS`.

V2 delta dodatak: postojeca shema mora imati nullable `dbo.work_definition.default_interval_km` i
`dbo.work_definition.default_interval_months`; `schema/06_student_runtime_compat.sql` je
eksplicitni read-only-by-default patch. Ako legacy `dbo.service_record.request_key` postoji,
novi insert mora dobiti DB default. Dijagnostika ostaje zamrznuta, ukljucujuci `Problem.requestKey`;
`ServiceRecord.requestKey` vise nije dio runtime mapiranja.
