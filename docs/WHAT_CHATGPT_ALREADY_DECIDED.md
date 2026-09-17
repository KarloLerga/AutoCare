# Što je već odlučeno u ovom paketu

Codex ne treba sam donositi ove odluke:

1. Neprimjenjivi vehicle/work par nema VehicleWorkRule.
2. Svaki spremljeni rule ima pozitivnu cijenu.
3. Svaki maintenance rule ima konkretan interval.
4. Postojeće dobre cijene se čuvaju; rupe se popunjavaju; preniski tier/outlier slučajevi se auditirano korigiraju.
5. Fixed i ručno modelirani radovi imaju tier multiplikatore 0.90 / 1.00 / 1.25 / 1.65 / 2.80.
6. Eksplicitne 0-model osnovice su u price_overrides.csv.
7. Svi maintenance intervali su u maintenance_intervals.csv.
8. OTHER_MAINTENANCE i OTHER_REPAIR odlaze.
9. Keyword dijagnostika odlazi.
10. Strategy prelazi na Mileage / Time / Combined maintenance.
11. Problemi postaju opis + ručni repair + Procijeni cijenu.
12. Maintenance screen prikazuje samo tracked radove i ima inline estimator.
13. Service editor odmah prima stvarno plaćenu cijenu i ne prikazuje procijenjenu.
14. Vehicle picker je Marka -> Model -> Godina -> Varijanta.
15. Identitet postojećeg vozila se ne uređuje; samo kilometraža.
16. Registration/onboarding ide u isti JFrame.
17. Sidebar nema refresh, DB ID ni NOOP; ima datum i automatski refresh kilometraže.
18. Dashboard ima 4 bordered Ikonli kartice i jedno sljedeće održavanje.
19. User-visible hrvatski tekst koristi kvačice.
