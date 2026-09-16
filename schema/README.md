# AutoCare naming migration artifacts

Ovaj direktorij je developerski artefakt, nije runtime resurs. Mapa opisuje kontroliranu
name-only migraciju sa starog AF3 snake_case SQL ugovora na ciljane PascalCase tablice i
camelCase scalar stupce koje koriste JPA entityji.

- `01_inspect_read_only.sql` pregledava stvarne objekte, tipove, kljuceve i ovisnosti.
- `02_rename_reviewed.sql` je naprijedna migracija; zadano je `@Apply = 0` i trazi
  izoliranu kopiju, potvrden recovery plan, zaustavljenu aplikaciju i tocno ime baze.
- `03_final_data_read_only.sql` provjerava poslovne FK/schedule uvjete nakon renamea.
- `04_reverse_names_reviewed.sql` je samo pregledani reverzni rename predlozak.
- `05_student_simplification_v2.sql` eksplicitno dodaje dva nullable intervala na
  `dbo.WorkDefinition`; zadano je read-only i ne radi reset baze.
- `naming_map.csv` i `naming_manifest.json` sadrze mapu 9 tablica i 69 aktualnih
  mapiranih stupaca. Stari `ServiceRecord.request_key` vise nije dio Java modela;
  name-only rename ga namjerno ne preimenuje niti brise, pa ga treba zasebno pregledati
  na izoliranoj kopiji ako se ikad bude cistio.

Ne pokretati ove skripte na jedinoj radnoj Azure bazi bez potvrdene kopije/backupa i
pregleda svih SQL ovisnosti. Hibernate `update` nije alat za rename i nije zamjena za
ovu migraciju. U ovom commitu skripte nisu izvršene; trenutni live schema audit ostaje
na starom snake_case ugovoru, dok novi runtime namjerno radi `validate` nad ciljanim
imenima. Nakon target-name renamea treba prvo pregledati `05_student_simplification_v2.sql`,
zatim potvrditi da `defaultIntervalKm` i `defaultIntervalMonths` postoje prije pokretanja
runtimea.
