# Opseg dijagrama

Dijagrami su tri različita pogleda na isti aktualni kod. DOT/Mermaid izvori i renderirane PNG/SVG
datoteke moraju ostati međusobno usklađeni.

## 1. Arhitektura

`architecture.*` prikazuje View → Controller → Service → Repository → JPA/Hibernate → Azure SQL.
Domain tipovi koriste se između Service i Repository sloja. Service je vlasnik poslovne granice
transakcije; repositoryji imaju samo dohvat/persistiranje.

## 2. Persistentna domena

`domain.*` prikazuje devet aktualnih entiteta i enumove koji su dio Java domene. Ne prikazuje
Controller, Strategy ni privremene DTO rezultate kao tablice. Kardinalnosti su fizičke/poslovne:
vozilo ima ownera i varijantu, servis pripada vozilu i ima stavke, problem pripada vozilu i može biti
riješen jednim servisom.

## 3. Aplikacijski odnosi

`design.*` prikazuje reprezentativne Controller/Service/Repository odnose te
`DiagnosticStrategy`/`KeywordDiagnosticStrategy` i `Data` rezultate. Transakcija je nacrtana kao
odgovornost Service klase i neposredni JPA lifecycle, bez dodatnog generičkog posrednika.

## 4. ERD

`erd.*` prikazuje fizičke `dbo` `snake_case` tablice, PK/FK i važne podatke. Ne prikazuje pomoćne
Java klase. Nakon završnog cleanup-a u ERD-u nema legacy `version`, `request_key` ni `schedule_kind`
stupaca. Brojevi i tipovi odgovaraju live SQL auditu gdje je navedeno u `docs/VERIFICATION.md`.
