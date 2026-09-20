# Implementation status

Datum provjere: 2026-09-20

## Finalni runtime

- Java 25 Swing aplikacija koristi View -> Controller -> Service -> Repository -> JPA/Hibernate -> Azure SQL.
- Runtime model sadrzi biljeske/probleme, katalog informativnih min-max raspona i stvarne servisne zapise.
- Nema runtime AI dijagnostike, `DiagnosticRule` ili `VehicleWorkRule` modela.
- Baza je provjerena s 30.366 varijanti, 120 radova i 600 raspona cijena.
- `DatabaseConfig` cita ignored `connection.local.properties` iz roota ili privatni fallback izvan repozitorija.

## Provjere prije source-only cleanupa

| Provjera | Rezultat |
|---|---|
| `mvnw.cmd --no-transfer-progress clean verify` | PASS; 69 source klasa, 12 testova, 0 gresaka |
| `mvnw.cmd javadoc:javadoc` | PASS |
| katalog validator | PASS; 30.366 / 120 / 600, 0 errors |
| runtime style i secrets checks | PASS |
| Azure SQL read-only preflight/audit | PASS; `final_error_count = 0` |
| GUI launch | PASS; prozor `AutoCare` otvoren |
| interaktivni GUI click-smoke | NOT_RUN; Windows Computer Use kanal nije bio dostupan |

Prvi Maven clean bio je blokiran jer je AutoCare proces drzao JAR zakljucanim. Nakon zatvaranja tocno identificiranog AutoCare procesa ponovljeni `clean verify` je prosao.

## Source-only cleanup

Uklonjeni su razvojni i migracijski artefakti: `src/test`, `data`, `data_model`, `schema`, `scripts`, `style`, `tools`, `.vscode`, `.tools`, `target`, stari README/status/hash/paketni dokumenti i svi ostali dokumenti osim ovog zapisa.

Ovaj zapis ostaje samo radi stvarnog evidence zahtjeva projekta; nije runtime ovisnost.

## Git

Prethodni integracijski HEAD: `002a9f6`.

Cleanup commit i tocne SHA vrijednosti provjeravaju se s `git log` nakon commitiranja.
