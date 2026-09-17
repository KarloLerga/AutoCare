# Kratko objašnjenje modela podataka za obranu

Cijene u AutoCare nisu servisne ponude. To su okvirne procjene za studentsku aplikaciju.

Za svaki konkretni `VehicleVariant` i svaki popravak/održavanje koji ima smisla za taj tip vozila unaprijed postoji `VehicleWorkRule`.
Zato runtime ne pogađa cijenu i ne koristi fallback vrijednost.

Procjena je pripremljena offline iz:
- osnovne cijene dijelova,
- procijenjenog vremena rada,
- satnice,
- karakteristika vozila,
- cjenovnog razreda marke (economy/mainstream/premium/performance/exotic),
- postojećih ranije modeliranih podataka.

Postojeća dobra procjena ostaje sačuvana. Model popunjava rupe i korigira samo očito preniske outliere.
Zbog tier faktora egzotični automobili imaju znatno više okvirne cijene od mainstream automobila.

Neprimjenjivi radovi nemaju rule. Primjer: električni Tesla nema zupčasti remen motora, svjećice ili DPF.

Kod održavanja je interval također unaprijed materijaliziran u ruleu. Ako je postojao konkretniji interval, zadržan je. Inače je unaprijed upisan okvirni km/vremenski interval iz finalne intervalne tablice.

Strategy pattern nije za cijene. Koristi se za izračun statusa održavanja:
- samo kilometri,
- samo vrijeme,
- kilometri + vrijeme.
