# Finalni GUI tok

Vizualni stil ostaje postojeći dark FlatLaf Swing stil. Zadnja profesorova promjena mijenja funkciju ekrana, ne uvodi novi UI framework.

## Login / registracija

Ostaju u jednom JFrame/CardLayout toku bez otvaranja novih glavnih prozora.

## Sidebar

- Dashboard
- Vozila
- Održavanje
- Katalog
- Servisi
- Bilješke
- Profil

## Dashboard

Četiri kartice:
- Ukupni stvarni troškovi
- Sljedeće održavanje
- Aktivne bilješke
- Trenutna kilometraža

## Vozila

Popis korisnikovih vozila. Dodavanje koristi Marka -> Model -> Godina -> Varijanta. Identitet postojećeg vozila se ne uređuje; može se ažurirati kilometraža, aktivirati ili obrisati vozilo.

## Održavanje

Prikazuje samo održavanja za koja postoji stvarna servisna povijest:

`Rad | Zadnji datum | Zadnji km | Sljedeći datum | Sljedeći km | Status`

Cijene nisu na ovom ekranu; za procjenu postoji Katalog.

## Katalog

Search polje + filter kategorije. Tablica:

`Zahvat | Kategorija | Vrsta | Okvirna cijena | Interval`

Search je obični klik/Enter, bez regexa i kompleksnog live filter frameworka.

## Servisi

History tablica ostaje kronološka. Novi servis ima:
- datum;
- kilometražu;
- vrstu rada;
- standardni zahvat;
- stvarno plaćeno;
- draft listu stavki;
- napomenu;
- opcionalne aktivne bilješke riješene tim servisom.

Sve ostaje u memoriji dok se ne klikne Spremi servis.

## Bilješke

Nova bilješka:
- gruba kategorija;
- slobodni opis;
- Spremi bilješku.

Tablica:

`Kategorija | Bilješka | Status | Datum`

Aktivna bilješka može se ručno zatvoriti ili označiti riješenom kod spremanja servisa.

Nema polja Mogući popravak, Procijeni cijenu, Podudaranje ili Analiziraj.
