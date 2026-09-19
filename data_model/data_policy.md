# Finalna politika kataloga, cijena i intervala

## Katalog

Finalni runtime nema per-variant matricu radova. Postoji:

- 30.366 `VehicleVariant` zapisa;
- 120 `WorkDefinition` zapisa;
- 5 `VehiclePriceClass` vrijednosti;
- 600 `WorkPriceRange` zapisa.

## Cijene

Svaki standardni zahvat ima unaprijed spremljen min-max raspon za svaku cjenovnu klasu vozila. Runtime ne poziva AI i ne računa fallback cijenu.

Raspon je informativan i ne predstavlja ponudu servisa, VIN specifičnu cijenu ili dijagnozu. Stvarna cijena postoji samo u `ServiceItem.actualPrice` nakon što korisnik evidentira servis.

Metodologija i izvori: `catalog_price_sources.md`.

## Intervali

Maintenance interval pripada `WorkDefinition` zapisu:

- samo kilometri;
- samo vrijeme;
- kilometri + vrijeme.

Ako postoje oba kriterija, maintenance Strategy smatra stavku dospjelom kada prvi kriterij dospije.

Repair radovi nemaju preventivni interval.

## Bilješke

Bilješka nema suggested repair, estimated cost, match score ni dijagnostička pravila. Sprema samo slobodan opis, grubu kategoriju, status i eventualni servis kojim je riješena.
