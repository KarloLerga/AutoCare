# Konačna politika cijena, intervala i primjenjivosti

Ovo je namjerno modelirani studentski katalog. Procjene nisu ponude servisa niti VIN/OEM potvrda.

## Glavno pravilo

U runtime bazi nema fallbacka.

Za svaki **primjenjivi** par `VehicleVariant × WorkDefinition` postoji točno jedan `VehicleWorkRule`.
Taj red uvijek ima konkretnu `estimated_price > 0`.
Ako je rad `MAINTENANCE`, red uvijek ima `interval_km`, `interval_months` ili oba.
Ako je rad `REPAIR`, intervali su NULL.

Za očito neprimjenjive parove red se uopće ne sprema. Primjer: Tesla BEV nema OIL_SERVICE, SPARK_PLUGS, TIMING_BELT_PUMP, DPF itd.

## Postojeći podaci

Postojeća pozitivna cijena se zadržava ako je razumna. Skripta je mijenja samo ako je tehnički očiti outlier ili ako fixed-work cijena ignorira veliki premium/performance/exotic troškovni faktor.
Postojeći konkretni interval se uvijek zadržava.
Nove vrijednosti popunjavaju samo rupe ili korigiraju očiti outlier uz audit zapis.

## Cijene

1. postojeći kvalitetan pozitivan iznos -> KEPT_EXISTING
2. inače postojeći AutoCare model (`base_parts`, `base_hours`, `consumables`, vehicle traits, tier, snaga, masa, cilindri...)
3. za 0/0/0 i stare quote-only radove koristi `price_overrides.csv`
4. ako se pojavi novi 0/0/0 kod koji nema eksplicitnu osnovicu, generator NE SMIJE tiho napraviti nulu; mora FAIL-ati i ispisati nedostajući work code. Time nema skrivenog fallbacka.

## Tier logika

Postojeći model već skuplje marke čini skupljima kroz parts/labour tier.
Za fixed i ručno modelirane radove dodatno se koristi:

- ECONOMY 0.90
- MAINSTREAM 1.00
- PREMIUM 1.25
- PERFORMANCE 1.65
- EXOTIC 2.80

Zato npr. Bugatti, Ferrari i Lamborghini ne završavaju s mainstream cijenom za radove koji su prije bili fixed/quote-only.

## Intervali

`maintenance_intervals.csv` je eksplicitna politika za svaki maintenance work koji ostaje u katalogu.
Prije generiranja skripta provjerava da nema maintenance koda bez unosa. Ako ga ima, FAIL.

Bolji postojeći vehicle/model interval ima prioritet i ostaje sačuvan.
Ako ga nema, eksplicitni interval iz CSV-a se materijalizira u konkretni `VehicleWorkRule`.
Za PERFORMANCE i EXOTIC te izrazito snažne varijante kilometarski interval se može konzervativno skratiti prilikom materijalizacije, ali konačna vrijednost je fizički zapisana u retku i runtime ne računa fallback.

## OTHER radovi

`OTHER_MAINTENANCE` i `OTHER_REPAIR` se uklanjaju iz finalnog user kataloga. Aplikacija već ima 120 konkretnih radova i posebni generički red samo uvodi posebne slučajeve.
