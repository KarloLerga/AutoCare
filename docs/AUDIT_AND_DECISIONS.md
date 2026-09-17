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
- Clock dependency kroz sve Service konstruktore

Razlog je čitljivost za kolegij: poslovna odluka i lifecycle trebaju biti vidljivi u klasi, a ne
raspršeni kroz dodatne apstrakcije.

## Baza

Fizički SQL ostaje `snake_case`; Java mapiranje radi naming strategy. Runtime ne upravlja shemom
(`hbm2ddl=none`). `schema/07_final_student_cleanup.sql` je read-only po defaultu, pronalazi stvarne
ovisnosti preko `sys.*`, a primijenjena je tek nakon pregleda. Kataloški counts su ostali
30.366 / 122 / 1.650.435 / 87; šest legacy kolona je uklonjeno.

## Podaci i izvori

Modelirane procjene nisu nacionalni prosjek. Review izvori i rasporedi nisu automatsko odobrenje.
Fotografije su zadnja, lokalna enrichment faza s odvojenim review/licence zapisom; runtime ne zove
vanjski API.

## Evidencija

Stvarni commitovi i naredbe nalaze se u `docs/IMPLEMENTATION_STATUS.md` i `docs/VERIFICATION.md`.
Nema izmišljene SQL, GUI ili integration PASS oznake.
