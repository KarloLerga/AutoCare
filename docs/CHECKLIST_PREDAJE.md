# Checklist za konačnu predaju

- [ ] Sve vježbe predane na Merlin prema zahtjevu kolegija.
- [x] Projekt se builda preko Maven Wrappera.
- [x] JPA read-only upit prema katalogu uspješno se izvršava nad bazom.
- [ ] Registracija i prijava rade.
- [ ] Vozila i aktivno vozilo rade.
- [ ] Servisna povijest i stvarni trošak rade.
- [ ] Problemi se rješavaju kroz servis.
- [ ] Održavanje i Strategy izračun rade.
- [ ] Katalog prikazuje informativne cijene.
- [x] ERD je priložen kao završna slika s `owns` i `activeVehicle` vezama, uz Mermaid izvor.
- [x] UML je priložen kao završna slika, uz Mermaid izvor.
- [x] GUI wireframe PDF je priložen i u dokumentaciji označen kao Figma.
- [x] Završna dokumentacija opisuje problem, rješenje, UML, ERD, wireframeove i obrasce.
- [x] Popis vanjskih biblioteka s verzijama i službenim linkovima je priložen.
- [x] Javadoc HTML je generiran iz izvornog koda.
- [x] Git DAG je generiran iz stvarnog završnog repozitorija.
- [x] `git diff --check` prolazi nakon svih završnih promjena.
- [x] `mvnw.cmd --no-transfer-progress clean package` prolazi.
- [ ] Napravljen je finalni smoke test glavnih GUI tokova.

**Napomena:** read-only katalog je dostupan, ali kompletni tokovi prijave, unosa i spremanja nisu ručno testirani. Smoke-test kroz Swing prozor ostaje otvoren jer Windows UI automatizacija nije bila dostupna tijekom izrade dokumentacije.
