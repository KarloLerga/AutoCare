# Finalna verifikacija

Datum: 20.09.2026.

## PASS

- Java 25 / Maven Wrapper: `mvnw.cmd clean verify` — 69 Java klasa, 12 testova, 0 grešaka.
- Setup alat: `mvnw.cmd -f tools/setup/pom.xml clean package` — 3 Java klase, build PASS.
- Javadoc: `mvnw.cmd javadoc:javadoc` — PASS.
- Katalog validator: 30.366 varijanti, 120 radova, 30 maintenance, 90 repair, 600 raspona, 0 grešaka.
- Generator migracije i `FINAL_DATA_SHA256.txt` — deterministički izlaz i podudarni hash.
- Runtime style scan, secret check i Python `py_compile` — PASS.
- Najnoviji minimal clean source paket — pregledan i integriran bez uklanjanja zaključanih modelskih i sigurnosnih pravila.
- Ultra clean source paket od 20.09.2026. — pregledan; nije prebrisan preko repozitorija jer bi uklonio obavezne validacije, testni setup i katalog modele.
- `AutoCare_FINAL_ULTRA_CLEAN_2026-09-20.zip` nije sadržavao `docs`, `data`, `schema`, `scripts`, `style`, `tools` ni testove; ti dijelovi ostaju u projektu radi stvarne reprodukcije i provjere.
- Student clean paket od 20.09.2026. — primijenjen je kompatibilni UI/runtime dio: `Problemi`, servisni combo s valjanim početnim odabirom, održavanje bez statusne kolone i direktno čitanje lokalnog DB configa.

## Azure SQL PASS

- Read-only dry-run `Complete-Setup.ps1` — PASS; postojeća baza je imala 30.366 varijanti i 120 radova.
- `-ApplyProfessorModel` — PASS; migracija `schema/11_professor_model.sql` završila je s 30.366 varijanti, 120 radova i 600 raspona.
- Audit `schema/12_professor_model_audit.sql` — PASS; `final_error_count = 0`.
- Stari `vehicle_work_rule`, `diagnostic_rule`, `suggested_repair_id` i `estimated_cost` — ne postoje.
- Potvrđeno je i očuvanje postojećih podataka: 2 bilješke, 6 servisa i 11 stavki servisa.

## GUI status

- Aplikacija je pokrenuta iz `target/autocare-1.0.0.jar` uz privatnu config putanju; Java proces odgovara i prozor ima naslov `AutoCare`.
- Interaktivni klik-smoke nije izvršen jer ugrađeni Windows Computer Use kanal nije bio dostupan. To nije označeno kao PASS.

Privatni SQL config i vjerodajnice ostaju izvan repozitorija.
