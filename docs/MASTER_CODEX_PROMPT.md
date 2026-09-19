# Master Codex prompt - finalna integracija profesorovog modela

Pročitaj prvo `/00_CODEX_START_HERE.md`, zatim `docs/PROFESSOR_MODEL_HANDOFF.md`, `docs/UPDATED_CONCEPT_FOR_PROFESSOR.md` i najnoviji profesorov dokument u `reference/PROFESSOR_LATEST_SPECIFICATION.docx`.

Ovaj ZIP već sadrži projektirano rješenje. Ne radi novi redesign. Tvoj posao je integracija u stvarni repo, stvarni build, stvarna Azure migracija, GUI smoke i minimalni bugfix.

## Ne mijenjaj zaključani model

- Bilješke = description + coarse category + status.
- Nema auto-dijagnostike.
- Katalog = searchable standard works + min/max price range po 5 vehicle price classes.
- Servis = stvarno odrađeni radovi + actualPrice.
- Održavanje = servisna povijest + Strategy za km/time/both.
- 30.366 variants / 120 works / 600 price ranges.
- Nema VehicleWorkRule i DiagnosticRule runtime modela.
- Nema posebnog EV applicability enginea.
- Nema računa/privitaka, vehicle images, runtime API poziva ili Springa.

## Integracija

1. U stvarnom `KarloLerga/AutoCare` repo napravi diff prema ovom paketu.
2. Primijeni Java, tests, pom, scripts, schema, data_model i final docs.
4. Sačuvaj korisnikove lokalne connection/secrets datoteke i nemoj ih commitati.

## Provjere

Pokreni:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javadoc:javadoc
python .\scripts\validate_professor_catalog.py
powershell -ExecutionPolicy Bypass -File .\scripts\check-runtime-style.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-secrets.ps1
```

Ako nešto ne prođe, popravi uz najmanju moguću studentski čitljivu izmjenu.

## Azure

Prvo dry-run `Complete-Setup.ps1`, zatim uz korisnikovu potvrđenu privatnu konfiguraciju `-ApplyProfessorModel`.

Nakon toga `schema/12_professor_model_audit.sql` mora dati `final_error_count = 0`.

Ne tvrdi da je Azure PASS ako nije stvarno izvršen.

## GUI smoke

Stvarno pokreni aplikaciju i prođi cijeli tok iz `00_CODEX_START_HERE.md`. Posebno provjeri:

- Catalog search/filter i range;
- create/close Bilješke;
- Service editor actual prices;
- rješavanje Bilješke kroz servis;
- maintenance recalculation;
- Dashboard refresh;
- active vehicle nakon ponovne prijave.

## Git

Commitaj po stvarnim fazama bez poruka tipa "simplify for defense". Primjeri:

```text
refactor: align notes and catalog with approved workflow
data: add catalog price ranges and vehicle classes
database: migrate to catalog price range model
docs: align project documentation with approved concept
fix: complete final verification findings
```

Na kraju pushaj i vrati korisniku:

- finalni HEAD hash;
- listu commitova;
- exact PASS/BLOCKED provjere;
- Azure `final_error_count`;
- kratak rezultat ručnog GUI smokea;
- sve što je još stvarno ostalo, bez izmišljanja PASS statusa.
