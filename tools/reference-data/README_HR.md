# AutoCare referentni podaci - AF3 / SQL Server
Ovaj folder vec sadrzi GOTOVE podatke. Za prvi uvoz ne treba Python, LLM, internet, originalni ZIP ili Excel. Potrebni su tablice i izgradjeni Java projekt. Ne izvrsavati MySQL importere iz history/.

## Brojevi / semantika
30.366 varijanti, 122 definicije, 1.282.916 brojcanih modeliranih procjena. Uvozi se 1.650.435 varijanta/rad pravila: dio je primjenjiv, ali bez cijene i s uputom za individualnu ponudu. 1.179.688 uvjetnih kombinacija ostaje izvan zadanog uvoza. 760.404 kombinacije nisu primjenjive. Ukupni audit je 3.704.652 kombinacije; kategorije se ne zbrajaju s seed_rules kao dodatna nezavisna skupina.

Preciznost cijena nije izmjerena. Cijena ukljucuje modelirane dijelove, rad i potrosni materijal, gross EUR, nearest10 HALF_UP. Nije ponuda niti nacionalni prosjek. 3.500 varijanti nema brojcanu procjenu. Battery/EV/high-voltage zahvati postoje, ali bez izmisljenih univerzalnih iznosa i rokova. UNKNOWN/QUOTE_REQUIRED nisu nula. 122 nije 'svaki moguci popravak svakog auta'; OTHER_ uz napomenu sluzi stvarnoj evidenciji izvan kataloga.

## Jedan Java importer
Iz KORIJENA projekta, nakon Hibernate schema-update i validate:
```powershell
java -Xmx768m -jar target/autocare-1.0.0.jar seed-validate tools/reference-data/data
# Bez --apply seed-all je offline DRY RUN.
java -Xmx768m -jar target/autocare-1.0.0.jar seed-all tools/reference-data/data
# AUTOCARE_DB_* ucitati lokalno; exact target potvrda:
$env:AUTOCARE_SEED_TARGET=$env:AUTOCARE_DB_NAME
java -Xmx768m -jar target/autocare-1.0.0.jar seed-all tools/reference-data/data --apply --acknowledge-model-estimates --with-diagnostics
```
`data/sample` je8varijanti/403pravila za prvi test. Poslije toga puni uvoz preskace postojece kljuceve. `--plain-jdbc` iskljucuje driver bulk-copy optimizaciju radi usporednog testiranja, ne validacije/TLS. SeedFiles cita gzip CSV streamingom. SHA256 dokaz je integriteta isporuke, ne tocnosti automobilskog podatka.

Uvoz: jedno povezivanje -> provjera cilja/sheme -> session application lock -> #temp staging u paketima1000 -> parametrizirani T-SQL insert/update -> commit paketa. Ne koristi MERGE/TRUNCATE ni JPA milijunsku listu. Jedinstveni kodovi/pair kljucevi omogucuju ponovni uvoz nakon prekida. Ne prepisuje ne-praznu cijenu/izvor/interval; dopune idu zasebnim pregledanim patchom. Ne mijenja korisnike, stvarna vozila, servicerecord/items ni snapshot procjene problema.

## Planovi odrzavanja
`UNKNOWN`: plan nije poznat; `CONDITION_BASED`: prema pregledu/stanju; `VEHICLE_INDICATOR`: pokazivac vozila; `FIXED`: broj km/mjeseci. Broj moze postojati za cijenu bez perioda. Fixed ne oznacava automatsku zamjenu dijela kada izvor govori samo o provjeri.

34 intervala u `referenced_intervals.csv` su usko referencirani modeli/generacije, ne potpuna VIN verifikacija. `--with-referenced-intervals` je svjesni opt-in. 676 u `market_review_intervals.csv` NISU za automatski uvoz (673Kia drugi market,3ModelYraspon ne poklapa manual). `schedule_exceptions.csv` cuva first/repeat/conditional ogranicenja koja postojeci jednostavni model ne predstavlja.

Za mali pregledani intervalni patch koristi `config/reviewed_intervals.template.csv`. Svaki red mora imati stvarni izvor/review datum/pregledavatelja/APPROVED i potvrdu da vrijedi za CIJELI raspon kataloske varijante. Ako vrijedi samo za dio godina, ne oznaciti YES i ne primijeniti na cijelu varijantu; ne siriti model naslijepo. Tada ga zadrzati kao review candidate.
```powershell
java -jar target/autocare-1.0.0.jar import-intervals C:\reviewed\intervals.csv
java -jar target/autocare-1.0.0.jar import-intervals C:\reviewed\intervals.csv --apply
```
Ova naredba NIKAD ne mijenja cijenu. Konflikt postojeceg intervala trazi pregled; `--replace-interval-only` je izricita zamjena samo intervalnih polja. Stari `import-rules --replace-existing` zamjenjuje cijeli red i nije preporucen za cisti intervalni patch.

## Uvjetne cijene i opseg paketa
`conditional_estimates.csv.gz` ima iznos koji se NE nudi automatski jer nema potvrde ugradnje/opreme. Remen, DPF, plin klime, zamasnjak i drugi sklopovi ne pretvaraju se u APPROVED samo zbog zeljenog veceg broja redaka. `config/review_template.csv` i `scripts/prepare_reviewed_rules.py` pripremaju mali eksplicitni pregledani patch. Ne koristiti cijenu kao dokaz opreme.

OIL_SERVICE sadrzi i filtar. Diskovi+plocice po osovini nisu jedan kotac. INJECTOR je jedan injektor. Hv battery replacement nije rutinska zamjena svakih X godina. `config/works.json` ima tocan opseg, `config/model.json` polazista i overlap parove. Stari servisni nazivi se ne preimenuju retroaktivno da bi racun promijenio znacenje. Kalkulator ne zbraja alternative DPF clean/replace ili paket+njegov sadrzaj. Stvarna transkripcija racuna ipak moze imati vise razdvojenih stavki.

## Reprodukcija / audit
Opciono Python3.11+ iz ovog foldera:
```powershell
py -3 -m unittest discover -s tests -v
py -3 scripts/validate_af3.py
py -3 scripts/build_af3.py
```
Puni build koristi `base-input/` normaliziranu bazu + trenutni works/model, pise data/ i manifest. Pokretanje builda mijenja artefakte; pregledati diff/hashes prije uvoza. Za potpunu reprodukciju IZ SIROVOG user ZIP-a originalni normalizer prvo pokrenuti s `config/base-v1`, ne sa122radnomconfig:
```powershell
py -3 scripts/build_seed.py --input C:\sources\vehicle-makes-models-main.zip --config config/base-v1 --output C:\temp\autocare-base
```
Usporediti novo normalizirane vehicle_variants/traits sa zabiljezenim base hashovima prije zamjene base-input/. Odstupanje prijaviti, ne mijenjati stabilne kodove vec koristenih vozila. Veliki audit.gz ne otvarati u Excelu kao da nema ogranicenje redaka.

## Fotografije / licence
`image_groups.csv` i `image_group_variants.csv` daju6.697skupina/mapping. To nije skup skinutih fotografija. Image alat je odvojeni tools/images/, izvodi se zadnji. ODbL/attribution/licence sacuvati. CSV source registers razlikuju izvorno potvrden podatak od model pretpostavke. Nije dovoljno preimenovati status u VERIFIED.
