# Audit i odluke — završni studentski cleanup

## Što je zadržano

- Java 25, Swing/FlatLaf, JPA/Hibernate i Azure SQL.
- devet entiteta, pet repository sučelja, MVC + Service slojevi
- stvarno plaćeni `actualPrice` odvojeno od informativnih procjena
- maintenance kalkulacija po km/mjesecima i statusi `NO_DATA`, `OK`, `SOON`, `DUE`
- `DiagnosticStrategy` i postojeći scoring 87 pravila
- mali Observer za osvježavanje nakon uspješnog spremanja

## Što je namjerno uklonjeno

- generička transakcijska/callback infrastruktura
- asinkroni UI helper za obične DB pozive
- generička tablična komponenta
- request-key/idempotency tok za problem i servis
- optimistic `version` polja
- legacy plan metadata kolona
- hashiranje lozinki i per-vehicle image metadata/pipeline
- Clock dependency kroz sve Service konstruktore

Razlog je čitljivost za kolegij: poslovna odluka i lifecycle trebaju biti vidljivi u klasi, a ne
raspršeni kroz dodatne apstrakcije.

## Baza

Fizički SQL ostaje `snake_case`; Java mapiranje radi naming strategy. Runtime ne upravlja shemom
(`hbm2ddl=none`). `schema/08_plain_password_and_remove_images.sql` je read-only po defaultu i
prebačen je tek nakon read-only pregleda. Kataloški counts su ostali 30.366 / 122 / 1.650.435 / 87;
`app_user.password` je aktualna nullable tekstualna kolona, a `vehicle_variant.image_path` je uklonjen.

## Podaci i izvori

Modelirane procjene nisu nacionalni prosjek. Review izvori i rasporedi nisu automatsko odobrenje.
Per-vehicle fotografije i enrichment/import pipeline nisu dio aktualnog paketa; UI koristi samo
dekorativne FontAwesome6 Ikonli ikone, a runtime ne zove vanjski image API.

## Evidencija

Stvarni commitovi i naredbe nalaze se u `docs/IMPLEMENTATION_STATUS.md` i `docs/VERIFICATION.md`.
Nema izmišljene SQL, GUI ili integration PASS oznake.
