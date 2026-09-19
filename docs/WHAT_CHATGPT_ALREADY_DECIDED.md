# Zaključane odluke finalnog modela

Ove odluke ne treba ponovno otvarati tijekom Codex integracije.

1. Aplikacija je za privatnog vlasnika vozila, ne za auto-servis.
2. Bilješka je samo opis onoga što korisnik primjećuje + gruba kategorija + status.
3. Bilješka nema suggested repair, estimated cost, score ni automatsku dijagnostiku.
4. Informativne cijene postoje samo u zasebnom Katalogu.
5. Katalog ima 120 standardnih zahvata i search/filter.
6. Procjena je min-max raspon, ne jedna "točna" cijena.
7. Postoji 5 širokih cjenovnih klasa: ECONOMY, STANDARD, PREMIUM, PERFORMANCE, EXOTIC.
8. Svaki WorkDefinition ima svih 5 WorkPriceRange zapisa - ukupno 600.
9. VehicleVariant pamti samo svoju cjenovnu klasu; nema runtime per-variant rules matrice.
10. Nema posebnog EV applicability enginea; katalog ostaje jednostavan i širok.
11. Maintenance interval je na WorkDefinitionu i može biti km, mjeseci ili oba.
12. Strategy je isključivo Mileage/Time/Combined maintenance calculation.
13. Održavanje se prati samo kada postoji stvarni ServiceItem u povijesti.
14. Stvarna cijena postoji samo kao ServiceItem.actualPrice.
15. Procjena iz Kataloga nikad se automatski ne kopira u actualPrice.
16. Servis može zatvoriti nula, jednu ili više aktivnih bilješki.
17. Dashboard prikazuje stvarni total, sljedeće praćeno održavanje, aktivne bilješke i kilometražu.
18. Vehicle picker je Marka -> Model -> Godina -> Varijanta.
19. Identitet postojećeg vozila se ne uređuje; korisnik ažurira kilometražu.
20. Nema računa/privitaka ni slika vozila u finalnom studentskom prolazu.
21. Runtime koristi Swing + JPA EntityManager + Hibernate + Azure SQL, bez Springa.
22. Kod ostaje eksplicitan i početnički čitljiv, u stilu profesorovih vježbi.
