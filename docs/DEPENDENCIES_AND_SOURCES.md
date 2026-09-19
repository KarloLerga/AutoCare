# Vanjske biblioteke i izvori

## Java biblioteke

- Jakarta Persistence API 3.2 - standardni JPA API
- Hibernate ORM 7.4.8.Final - JPA provider
- Microsoft `mssql-jdbc` 13.4.0.jre11 - Azure SQL/JDBC driver
- FlatLaf 3.6.2 - moderniji Swing look-and-feel bez promjene Swing programskog modela
- Ikonli Swing + Font Awesome 6 pack 12.4.0 - jednostavne ikone u sidebaru/dashboardu
- SLF4J JDK14 2.0.17 - runtime logging bridge
- JUnit Jupiter 5.13.4 - testovi

Sve ovisnosti upravlja Maven; nema ručnog kopiranja fontova ili frameworka u source.

## Vozila

Početni katalog vozila izveden je iz korisnički dostavljenog snapshot-a projekta:
`https://github.com/gor3a/vehicle-makes-models`

Finalni runtime ne poziva taj repozitorij niti vanjski vehicle API. U bazi već postoji 30.366 `VehicleVariant` zapisa.

## Informativne cijene

Kalibracija i ograničenja detaljno su navedeni u `data_model/catalog_price_sources.md`.

Korišteni su javno dostupni hrvatski servisni cjenici za reprezentativne zahvate, među ostalim:
- AutoZubak Servis Plus
- Auto Centar Duo, Osijek
- Auto Servis Bošnjak, Osijek
- Auto Servis Meić / javno objavljeni orijentacijski cjenik

Ostali rasponi sažeti su iz prethodno pripremljenog detaljnog AutoCare modela u pet širokih klasa. U runtimeu nema AI računanja ili fallback formule.

Procjena nije servisna ponuda niti statistički nacionalni prosjek.
