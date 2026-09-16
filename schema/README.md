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
- `06_student_runtime_compat.sql` je aktualni minimalni patch: zadano read-only provjerava
  `work_definition.default_interval_km/default_interval_months` i legacy `service_record.request_key`,
  a primjena dodaje samo nedostajuce nullable stupce i DB default za legacy request key.
- `naming_map.csv` i `naming_manifest.json` cuvaju povijesnu before/after mapu 9 entiteta i 69
  mapiranih stupaca. Za aktualni fizicki runtime ugovor vrijedi snake_case shema i physical naming
  strategy; stari `ServiceRecord.request_key` vise nije Java polje, ali se ne brise automatski.

Prije `06_student_runtime_compat.sql` prvo provjeriti tocnu bazu, backup i ovisnosti. Skripta ima
`@Apply = 0` kao read-only zadanu vrijednost i placeholder za `@ExpectedDatabase`; ne uklanjati guard
niti nagađati naziv baze. Ne koristiti Hibernate `update` za rename i ne stvarati paralelnu shemu.
