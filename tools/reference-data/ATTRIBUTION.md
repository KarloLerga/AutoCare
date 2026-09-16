# Podrijetlo podataka i obavijesti

Izvor vozila: **vehicle-makes-models**, autor/repozitorij gor3a, `https://github.com/gor3a/vehicle-makes-models`, snapshot `b4965631140fa82404359167621274601da69a5b`, dostavljen kao korisnikov ZIP. Autor izvora navodi **autoevolution.com** kao upstream izvor podataka o markama, modelima, generacijama i motorima.

Izvorni podaci pod `data/` tog repozitorija označeni su **Open Database License (ODbL) 1.0**, odvojeno od njegove MIT licence za kod. Dostavljena kopija autorove obavijesti je `licenses/UPSTREAM-LICENSE-DATA.txt`; mjerodavni puni tekst naveden je u toj datoteci i na `https://opendatacommons.org/licenses/odbl/1-0/`.

Ovaj paket sadrži **prilagođeni podatkovni skup**, a ne neizmijenjeni original: uklonjeni su potpuno jednaki redci, određene opcionalne vrijednosti prilagođene su AF2 stupcima, dodani su stabilni kodovi, izvedene oznake, modelirane cijene i autorska pravila. Promjene su dokumentirane u auditu. Adaptirani podatkovni izlazi u `data/` isporučuju se uz ODbL obavijest; pri daljnjem javnom korištenju/distribuciji sačuvati atribuciju i ispuniti primjenjive uvjete licence. Nemoj cijelu bazu pogrešno označiti MIT samo zato što originalni repozitorij odvojeno koristi MIT za kod.

Cijene nisu preuzeti potpuni cjenici tuđih servisa. Sadrže ograničene javno navedene tarifne reference i jasno odvojene autorske pretpostavke. Točni izvori/primjena navedeni su u `data/source_register.csv`; nema tvrdnje da servis, proizvođač ili autor izvornog vehicle dataseta podržava ili jamči procjene AutoCarea.

Programske skripte/helperi u ovom paketu su razvojni dio korisnikova AutoCare projekta. Ne mijenjaju prava ili licencu već postojećega korisnikova koda. Prije javne distribucije projekta uskladiti obavijesti o svim stvarno uključenim bibliotekama i datasetima.
