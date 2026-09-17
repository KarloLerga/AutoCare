# AutoCare SQL schema artifacts

Ovaj direktorij je developerski artefakt, nije runtime resurs. Java koristi normalna camelCase
logicka imena, a `src/main/resources/META-INF/persistence.xml` ukljucuje Hibernateovu
`CamelCaseToUnderscoresNamingStrategy`. Normalni runtime zato koristi postojece `dbo` snake_case
tablice i stupce; ne treba masovno preimenovati bazu.

- `01_inspect_read_only.sql` pregledava stvarne objekte, tipove, kljuceve i ovisnosti.
- `02_rename_reviewed.sql` i `04_reverse_names_reviewed.sql` povijesni su, kontrolirani name-only
  predlosci. Ne pokretati ih na jedinoj radnoj Azure bazi niti za normalan runtime.
- `03_final_data_read_only.sql` je povijesna provjera nakon target-name migracije.
- `05_student_simplification_v2.sql` je povijesni patch za target-name shemu i nije potreban za
  postojecu snake_case bazu.
- `06_student_runtime_compat.sql` je raniji kompatibilni patch koji je vec primijenjen na postojeću
  bazu: dodao je nullable default intervale i default za stari request key.
- `07_final_student_cleanup.sql` je završni patch: zadano je read-only, auditira točnu bazu i
  ovisnosti, a uz eksplicitni target i `@Apply = 1` transakcijski uklanja samo šest legacy kolona
  (`version`, `request_key` i `schedule_kind`) bez resetiranja tablica ili kataloga.
- `naming_map.csv` i `naming_manifest.json` cuvaju povijesnu before/after mapu 9 entiteta i 69
  mapiranih stupaca. Za aktualni fizicki runtime ugovor vrijedi snake_case shema i physical naming
  strategy; manifest je povijesni zapis i nije popis aktivnih Java polja.

- `08_plain_password_and_remove_images.sql` je aktualni read-only-by-default patch za prijelaz na
  `app_user.password` i uklanjanje `vehicle_variant.image_path`. Stare hashirane vjerodajnice nisu
  reverzibilne; eksplicitni reset mod ih postavlja na `NULL` bez brisanja korisnickih redaka.

Prije `08_plain_password_and_remove_images.sql` prvo provjeriti tocnu bazu, backup i ovisnosti. Skripta ima
`@Apply = 0` kao read-only zadanu vrijednost i placeholder za `@ExpectedDatabase`; ne uklanjati guard
niti nagađati naziv baze. Ne koristiti Hibernate `update` za rename i ne stvarati paralelnu shemu.
