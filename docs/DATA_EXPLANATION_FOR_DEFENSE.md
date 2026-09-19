# Kako objasniti podatke i procjene na obrani

Aplikacija više nema milijune cijena po točnoj varijanti vozila.

Postoje tri razine podataka:

1. `VehicleVariant` - identitet vozila i široka `VehiclePriceClass`.
2. `WorkDefinition` - 120 standardnih zahvata i, za održavanje, interval.
3. `WorkPriceRange` - min/max informativna cijena zahvata za jednu od pet cjenovnih klasa.

Zato je finalna tablica cijena samo 120 x 5 = 600 redaka.

Raspon je namjerno informativan. Ne tvrdi da zna VIN, konkretne dijelove, satnicu odabranog mehaničara ili stvarnu dijagnozu. Stvarno plaćena cijena unosi se tek u `ServiceItem.actualPrice` nakon odlaska kod mehaničara.

Za česte zahvate STANDARD klasa kalibrirana je prema javnim hrvatskim servisnim cjenicima. Ostali radovi sažeti su iz ranije pripremljenog detaljnog modela u široke klase kako bi se izbjegla lažna preciznost i ogromna baza.

Strategy pattern nije za cijene. Koristi se samo za maintenance status prema kilometrima, vremenu ili oba kriterija.
