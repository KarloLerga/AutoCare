# AutoCare - audit podataka i metodologija procjena

**AC-MODEL-1.0, 15. 9. 2026.** Ovo je izvještaj o stvarno obrađenom datasetu i provjerenom izračunu. Nije izvještaj o mjerenju tržišne točnosti niti o izvršenom Azure uvozu.

## 1. Izvor i razina identiteta

Ulaz je korisnikov cjeloviti GitHub ZIP, čiji komentar navodi commit `b4965631140fa82404359167621274601da69a5b`. SHA256 ZIP-a je `fdbcf629600fbb8a4ca6f5725a4e351ca8e11d50f4044202cd092beb94c9ccef`.

U hijerarhiji JSON/SQLite evidentirano je 164 marke, 2.621 model, 7.169 generacija, 30.390 redaka s motorom i 777.844 sirove specifikacije. Dio roditeljskih marka/modela/generacija nema motor. Zato katalog stvarnih odabirljivih varijanti ne može imati 164 marke s izmišljenim motorima.

CSV sadrži 23 stupca. JSON ima i dodatne opise poput gorivnog sustava i kočnica. Obrađeni su svi redci i provjerena je njihova pripadnost istom make/model/generation/engine identitetu prije pridruživanja sirovih specifikacija. Puna testna usporedba potvrđuje i jednakost relevantnih CSV/JSON vrijednosti. SQLite je poslužio provjeri strukture i brojanja, nije baza aplikacije.

Nakon uklanjanja 24 potpuno identična retka ostaje 30.366 varijanti: 134 marke, 2.423 para marka/model i 6.794 generacijska raspona. To nisu 30.366 jedinstvenih OEM oznaka motora. Jednaki vidljivi nazivi mogu imati različite tehničke vrijednosti. `variant_code` je stabilni hash svih izvornih normaliziranih 23 vrijednosti; numeric DB ID nastaje tek u bazi.

## 2. Provjera kvalitete i transparentne prilagodbe

Nije odbačen nijedan cijeli redak zbog problema opcionalnog HP-a ili predugog opisa mjenjača. Sve prilagodbe su u `normalization_adjustments.csv`:

- 416 decimalnih snaga zaokruženo je za postojeći `Integer powerHp` stupac. Izvorna vrijednost ostaje u auditu; nije proglašena krivom.
- 8 predugih opisa mjenjača sažeto je samo na doslovno prisutni CVT / broj-stupnjeva-automatic izraz. Izvorni tekst je sačuvan u auditu.
- 1 očito nevjerodostojna brojčana snaga (`190150`) nije "ispravljena" nagađanjem: opcionalni stupac je NULL.

U svih 30.390 izvornih CSV redaka `body_type` je prazan. Iz naziva karoserije postoje ograničene audit-inferencije, ali one se ne potajno upisuju kao izvorne OEM činjenice.

Sirovi podaci sadrže primjere poput 400 cilindara kod EV400 ili vrijednosti voltaže koja je završila u cilindrima. EV ne dobiva izmišljen motor s tim brojem cilindara. Neslaganja zapremnine, snage i mjenjača između naziva i specifikacije označavaju se; algoritam ne bira nasumce pobjednika. Detaljni brojevi oznaka su u `data/build_manifest.json:quality_flags`. Jedna varijanta može imati više oznaka, pa se ti brojevi ne smiju zbrojiti kao disjunktni skupovi.

Izvor ne daje: hrvatske servisne cijene, normative rada za svaki točan motor, VIN identifikaciju, servisne intervale, pouzdanu klasifikaciju remen/lanac, DPF opremu svake izvedbe, točnu vrstu plina klime ni OEM katalog rezervnih dijelova. Takve informacije nisu izvučene iz ZIP-a; one bi morale doći iz dodatnog izvora ili ostati nepoznate.

## 3. Jedna cijena, ali objašnjiv izračun

Aplikacija dobiva jednu cijenu: dijelovi + rad + uobičajeni potrošni materijal, kao bruto EUR iznos. U izračunu se zadržavaju međuvrijednosti samo radi audita, ne radi novih aplikacijskih tablica ili kompliciranijeg GUI-ja.

```text
polazni dijelovi i radno vrijeme za zahvat
    -> prilagodba prema relevantnim osobinama vozila
    -> bruto dijelovi + radni sati * 60 EUR + potrošni materijal
    -> najbližih 10 EUR, Decimal HALF_UP
    -> jedna VehicleWorkRule.estimatedPrice vrijednost
```

Svi polazni iznosi i formule nalaze se u `config/works.json`, `config/model.json` te funkcijama `price_factors` i `estimate` u `scripts/build_seed.py`. Nema skrivenog vanjskog AI poziva ili slučajne generacije.

**Granica dokaza:** samo navedeni javni tarifni podaci i OEM planski podskup imaju te navedene izvore. Cijene dijelova, satnice operacija, pretpostavljene količine i koeficijenti klasa su dizajn modela. Nisu prikupljeni stvarni računi za 30.366 varijanti. Model nije statistički istreniran, kalibriran na neovisnom uzorku niti validiran poznatom pogreškom. `confidence=LOW/VERY_LOW` je kvalitativna oznaka opreza, ne statistička vjerojatnost.

Nije isti multiplikator primijenjen na svaki zahvat. Ovisno o zahvatu koriste se zapremnina/broj cilindara, moment, masa, tip mjenjača, doslovni opis kočnica, gorivo, eksplicitni turbo podatak i planska klasa cijene dijelova. Marka je jedan modelni faktor, ne dokaz da je svaki dio te marke skuplji za točan postotak. Točne obitelji motora, pristup dijelu i vrijeme skidanja sklopa mogu dati veće razlike nego ovaj jednostavni model; takva pogreška ostaje moguća.

Model predstavlja uobičajeni neovisni servis bez nesvakidašnje korozije, puknutih vijaka, hitnosti, posljedičnih kvarova, nepoznatog programiranja ili nedostupnosti dijelova. Nije premium ovlaštena ponuda. Dijelovi su pretpostavljena srednja zamjenska kvaliteta; pojedini zahvati imaju jasnu posebnu pretpostavku, npr. kvalitetno reparirana turbina. Puni opseg svakog zahvata je niže.

## 4. Izvori i njihova stvarna uporaba

| Izvor ID | Stvarno poduprt podatak | Što NIJE zaključeno |
|---|---|---|
| `VMM_INPUT` | Marka/model/generacija/motor i sirove specifikacije iz ZIP-a. | Da su svi izvorni podatci bez pogreške ili da sadrže cijene. |
| `HR_AKR_2026` | AK Rijeka cjenik od 2. 1. 2026.: osobni radni sat 60 EUR, osnovno spajanje dijagnostike 30 EUR, R134a do 600 g 70 EUR, R1234yf 200 EUR; s PDV-om. | Da je 60 EUR hrvatski prosjek ili da je 30 EUR potpuna dijagnoza kvara. |
| `HR_KANTOCI_2026` | Auto Kantoci od 7. 1. 2026.: osobni mehaničarski sat 47 EUR bez PDV-a. | Da je taj neto iznos izravno usporediv s bruto konačnom cijenom. |
| `UK_RENAULT` | UK cjenik daje opseg paketa i razlike zahvata; cijene su u GBP i s ograničenjima primjene. | Da se UK benzinsko/hibridna cijena smije kopirati na hrvatski dizel. |
| `UK_CLICKMECHANIC_TIMING` | Procjenitelj pokazuje da normativi ovise o izvedbi i pristupu sklopu. | Da svaki motor u općoj tablici stvarno ima zupčasti remen. |
| `TOYOTA_CHR_HR_2021` | Ponavljajući intervali malog modelskog podskupa, nakon pregleda PDF tablice. | VIN potvrda svih C-HR generacija ili svih Toyotinih hibrida. |
| `TESLA_MODEL3_EU` | Kabinski filtar 2 godine; provjera kočione tekućine 4 godine. | Automatska zamjena kočione tekućine svake 4 godine. |
| `DACIA_HR_OIL` | Opća preporuka povezivanja ulja i filtra ulja. | Jedan obvezni univerzalni interval za svaki Dacia motor. |
| `MODEL_ASSUMPTIONS` | Transparentne autorske pretpostavke u konfiguraciji. | Vanjski dokaz ili objavljeni cjenik. |
| `DIAGNOSTIC_DESIGN` | Edukativne hrvatske fraze i težine. | Potvrđena mehanička dijagnoza ili vjerojatnost kvara. |

Točni URL-ovi i datumi su u `data/source_register.csv`. Strane cijene nisu pretvarane tečajem i proglašavane hrvatskim prosjekom. Ne postoji skriveni "90% točan" rezultat.

## 5. Primjenjivost i pokrivenost

Sve 30.366 varijante provjerene su prema svih 39 zahvata: 1.184.274 odluke. Uvoz uključuje 625.668 brojčanih procjena. Dodatnih 215.616 uvjetnih brojčanih procjena nalazi se u zasebnoj datoteci. Preostalih 342.990 kombinacija nema cijenu: 228.865 je neprimjenjivo, a 114.125 zahtijeva individualnu ponudu.

26.866 varijanti ima barem jednu cijenu za zadani uvoz; 3.500 nema. Cijene za zadani uvoz pojavljuju se kod 102 marke; katalog i dalje sadrži svih 134 marke s izvornim motorima. Status je određen za pojedini zahvat, ne samo za cijelo vozilo.

Za rijetka/egzotična i određena stara vozila nije uvezen naivan multiplikator kao pouzdana cijena. Za zastarjele/nejasne/kontradiktorne podatke važna je `QUOTE_REQUIRED` ili uvjetna grana. To je namjerno ograničenje, ne tvrdnja da takav popravak nije moguć.

EV ne dobiva ulje motora, grijače ili druge zahvate nepostojećeg izgaranja. Hibrid nije automatski benzinac i ne dobiva automatski klasični starter/alternator. Zadnje pakne i pločice razlikuju se prema doslovnim podacima o bubnju/disku. Keramičke kočnice ne dobivaju cijenu običnih čeličnih diskova. Zupčasti remen, DPF, EGR, rashladni plin i točni automatski mjenjač ostaju uvjetni kada podatci nisu dostatni.

`all_pair_decisions.csv.gz` sadrži razlog odluke i međuizračun. `prices_by_vehicle.csv` je pregledniji: jedna varijanta po retku, samo cijene predviđene za zadani uvoz. Prazni stupac znači da cijena nije dostupna u automatskom seeding putu; razlog se provjerava u auditu, ne pretpostavlja.

## 6. Servisni intervali - 14 referenciranih redaka, ne milijunska izmišljena tablica

Toyota hrvatski C-HR Hybrid plan iz 2021. mapiran je samo na tri hibridne varijante iz generacija `C-HR (2016)` / `C-HR (2019)`:
- ulje i filtar: 15.000 km / 12 mjeseci;
- kočiona tekućina: 30.000 km / 24 mjeseca;
- filtar zraka: 60.000 km / 48 mjeseci;
- filtar kabine: 30.000 km / 24 mjeseca.

To daje 12 redaka. Za dvije varijante iz `Model 3 (2023)` dodan je model-scope podatak zamjene kabinskog filtra svaka 24 mjeseca prema EU priručniku. Ukupno 14. Korisnik i dalje treba potvrditi plan svojeg konkretnog vozila/izvedbe/tržišta.

Početna zamjena Toyotine rashladne tekućine pri 150.000 km / 10 godina nije pretvorena u ponavljajući interval. Teslina četverogodišnja provjera tekućine nije zamijenjena pojmom zamjene. Nije dodijeljen interval samo zato što postoji cijena zahvata. Uvoz ovih 14 intervala traži odvojeni opt-in; zadani uvoz ne aktivira ih.

## 7. Dijagnostička pravila

67 fraza ponderira 23 kandidata popravka. Pravila su podatkovna; ne uvode novi LLM poziv u aplikaciju. To je edukativni keyword matching, ne mehanički dijagnostički sustav. Težine nisu statistički naučene. Negacije, dvosmisleni opisi, sinonimi i kontekst mogu dati pogrešan poredak. Na rezultate se ne smije oslanjati kao na potvrdu da je vozilo sigurno ili da treba zamijeniti dio.

Prije aktivacije novih pravila izolirati stari DEMO skup kako nazivnik bodovanja ne bi neprimjetno ovisio o duplikatima. Aplikacija treba filtrirati kandidate na zahvate s varijantnom primjenjivošću. Uvjetni zahvat bez provjere ne treba postati prvo automatsko rješenje problema samo zbog prepoznate riječi.

## 8. Testovi i reproducibilnost

Izvršeno u ovoj isporuci:
- 45 Python testova: izvorni redci/identiteti, kompatibilni AF2 kodovi, zaokruživanje, rubne grane primjenjivosti, intervali i odluke čuvanja postojećih cijena;
- puna provjera svih 30.366 kataloških redaka, 39 radova, 625.668 uvoznih pravila, 14 intervala, 67 fraza i 1.184.274 odluke;
- offline `import_seed.py` dry run koji ne uspostavlja konekciju;
- potpuni neovisni drugi build iz istog ZIP-a: identičan manifest i svi hashovi 17 izlaznih podatkovnih datoteka;
- kompajliranje i 11 provjera dvaju izoliranih Java helpera na JDK 21.

Nije izvršeno: MySQL/Azure povezivanje ili uvoz, Windows PowerShell wrapper, puni Maven/JDK 25 build, Hibernate integracijski test ili ručni GUI tok. Također nije izvršena empirijska usporedba procjena s uzorkom stvarnih hrvatskih računa. Uredni hashovi, testovi i potpuna obrada ne dokazuju tržišnu točnost.

Zapisnici su u `reports/`. Promjenom `model.json` ili `works.json` mijenja se model, a zatim treba ponovno generirati sve podatke, provjeriti izlaz, podići verziju modela i ciljano uvesti izmjene. Nemoj ručno mijenjati CSV bez ažuriranja manifesta i izvora.

## 9. Opseg svih 39 zahvata

Opseg određuje što jedna cijena uključuje. Npr. jedan injektor/kalem/lezaj nije komplet svih, a kočnice po osovini nisu jedan kotač. Tehnički scope tekst ispod dolazi iz iste konfiguracije koju koristi generator.

| Kod | Naziv | Kategorija | Opseg cijene (iz konfiguracije) |
|---|---|---|---|
| `OIL_SERVICE` | Motorno ulje i filtar ulja - kompletna izmjena | MAINTENANCE | oil+filter+washer; entire engine; no engine flush |
| `AIR_FILTER` | Zamjena filtra zraka motora | MAINTENANCE | one filter |
| `CABIN_FILTER` | Zamjena filtra kabine | MAINTENANCE | standard cabin filter set; no special HEPA system |
| `FUEL_FILTER` | Zamjena filtra goriva | MAINTENANCE | one diesel fuel filter; no pump |
| `BRAKE_FLUID` | Izmjena kočione tekućine | MAINTENANCE | standard passenger-car brake circuit; bleeding included |
| `COOLANT` | Izmjena rashladne tekućine motora | MAINTENANCE | engine circuit; no flush of contaminated system |
| `TIMING_BELT_PUMP` | Zupčasti remen, natezači i vodena pumpa | MAINTENANCE | belt kit+rollers+compatible pump+coolant; only if this engine has this arrangement |
| `SPARK_PLUGS` | Zamjena kompleta svjećica | MAINTENANCE | all conventional spark plugs; number of cylinders modelled; no seized-plug extraction |
| `AUX_BELT` | Pomoćni remen i natezač | MAINTENANCE | one accessory drive belt+tensioner; no alternator |
| `MANUAL_GEARBOX_OIL` | Izmjena ulja ručnog mjenjača | MAINTENANCE | drain/refill; ordinary manual gearbox; no differential service |
| `AUTO_GEARBOX_OIL` | Servis ulja automatskog mjenjača | MAINTENANCE | drain/refill+serviceable filter; not a guaranteed full fluid exchange; gearbox type must be confirmed |
| `FRONT_BRAKES` | Prednje kočione pločice - par kotača | REPAIR | pads for front axle; ordinary steel discs retained |
| `REAR_BRAKES` | Stražnje kočione pločice - par kotača | REPAIR | pads for rear axle; ordinary steel discs retained |
| `FRONT_DISCS_PADS` | Prednji diskovi i pločice - par kotača | REPAIR | two ordinary steel front discs+front pad set |
| `REAR_DISCS_PADS` | Stražnji diskovi i pločice - par kotača | REPAIR | two ordinary steel rear discs+rear pad set |
| `REAR_DRUM_SHOES` | Stražnje kočione pakne - par kotača | REPAIR | rear shoes set; existing drums retained |
| `AC_COMPRESSOR` | Zamjena kompresora klime | REPAIR | one conventional compressor+ordinary recharge/oil; no major contamination, condenser or evaporator repair |
| `RADIATOR_FAN` | Zamjena ventilatora hladnjaka | REPAIR | one fan assembly; not complete radiator |
| `AC_R134A` | Servis klime R134a | REPAIR | ordinary passenger-car charge up to 600 g; no leak repair |
| `AC_R1234YF` | Servis klime R1234yf | REPAIR | ordinary passenger-car charge; no leak repair; confirm charge quantity |
| `BATTERY` | Zamjena 12 V akumulatora | REPAIR | one conventional lead-acid/EFB/AGM low-voltage battery; never traction battery or 16/48 V lithium system |
| `GLOW_PLUGS` | Zamjena kompleta grijača dizela | REPAIR | one ordinary glow plug per cylinder; no broken-plug extraction |
| `DIAGNOSIS` | Osnovno očitanje OBD kodova | REPAIR | scan/readout only; not guaranteed root-cause diagnosis or unlimited troubleshooting |
| `STARTER` | Zamjena elektropokretača | REPAIR | one conventional starter; no belt starter generator |
| `ALTERNATOR` | Zamjena alternatora | REPAIR | one conventional alternator; not hybrid motor generator |
| `TURBO` | Zamjena jednog turbopunjača | REPAIR | one quality remanufactured turbo with standard seals/oil; no internal engine damage |
| `EGR_VALVE` | Zamjena EGR ventila | REPAIR | one EGR valve; cooler not included |
| `DPF_CLEAN` | Demontaža, čišćenje i montaža DPF-a | REPAIR | serviceable intact filter; legal cleaning, not removal or ECU disabling |
| `DPF_REPLACEMENT` | Zamjena DPF filtra | REPAIR | one homologated replacement filter; no SCR/AdBlue system repair |
| `DIESEL_INJECTOR` | Zamjena jedne dizelske brizgaljke | REPAIR | one remanufactured compatible injector with ordinary coding; not the entire injector set |
| `IGNITION_COIL` | Zamjena jedne bobine | REPAIR | one compatible coil; no full ignition harness |
| `WHEEL_BEARING` | Zamjena jednog ležaja kotača | REPAIR | one ordinary wheel bearing/hub; axle and exact side must be confirmed |
| `FRONT_SHOCKS` | Prednji amortizeri - par | REPAIR | two conventional passive dampers+basic mounts+alignment; no air/adaptive suspension |
| `REAR_SHOCKS` | Stražnji amortizeri - par | REPAIR | two conventional passive dampers; no air/adaptive suspension |
| `CLUTCH_KIT` | Zamjena kompleta spojke | REPAIR | manual gearbox clutch kit; flywheel retained; no robotic/DCT clutch pack |
| `CLUTCH_DMF` | Komplet spojke i dvomaseni zamašnjak | REPAIR | manual clutch kit+dual-mass flywheel only if fitted |
| `WATER_PUMP` | Zamjena vodene pumpe motora | REPAIR | one conventional mechanically driven pump+coolant; excludes timing belt kit |
| `THERMOSTAT` | Zamjena termostata motora | REPAIR | one ordinary engine thermostat/housing and coolant top-up |
| `LAMBDA_SENSOR` | Zamjena jedne lambda sonde | REPAIR | one exhaust oxygen sensor; no catalytic converter |

## 10. Očuvanje projekta

Nema nove tablice za raspon cijena, rada/materijala, status održavanja ili runtime AI. `estimated_price` je jedna vrijednost. `estimate_note` i datoteke daju provenijenciju. Puni seed ne znači potrebu da GUI odjednom učita sve cijene; upit za aktivnu varijantu ostaje mali i indeksiran.

Slike nisu izmišljene niti se neprovjereno preuzimaju s weba. Koristi se lokalni fallback iz postojećeg projekta; prava slika može se dodati tek s poznatim pravom korištenja. Posebne preporuke ulja/dijelova, VIN-level intervali, zamašnjak, kod mjenjača i vrsta plina nisu neprimjetno dopunjeni LLM nagađanjem.
