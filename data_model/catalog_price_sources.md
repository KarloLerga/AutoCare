# AutoCare - izvori i metodologija informativnih raspona cijena

Datum kalibracije: 18.09.2026.

Cijene u aplikaciji nisu ponude servisa ni dijagnoza. One su unaprijed pripremljeni informativni rasponi za planiranje troška. Stvarna cijena se u aplikaciji evidentira isključivo kada korisnik nakon posjeta mehaničaru spremi servisni zapis.

## Hrvatski javni cjenici korišteni za kalibraciju

1. AutoZubak - Servis Plus
   - https://www.autozubak.hr/servis-plus/
   - servis zamjene ulja od 108,09 EUR
   - zupčasti remen + pumpa vode od 413,80 EUR
   - kočione obloge od 81,95 EUR
   - kočiona tekućina od 46,06 EUR
   - filtar goriva od 54,70 EUR
   - filtar zraka od 37,06 EUR
   - set spojke od 405,60 EUR
   - akumulator od 161,39 EUR
   - svjećice od 98,05 EUR

2. Auto Centar Duo, Osijek
   - https://autocentarduo.hr/servis
   - mali servis od 89 EUR
   - veliki servis od 249 EUR
   - dijagnostika od 45 EUR
   - klima servis od 69 EUR
   - kočnice i ovjes od 120 EUR

3. Auto Servis Bošnjak, Osijek
   - https://www.as-bosnjak.hr/cjenik-usluga.html
   - radni sat osobnih vozila 50 EUR
   - računalna dijagnostika 30 EUR
   - pregled vozila 40 EUR
   - zamjena ulja u kočnicama 35 EUR (usluga)
   - balansiranje/montaža prikazani su kao pojedinačne usluge

4. Auto Servis Meić - orijentacijski cjenik objavljen na Automobil.hr
   - https://automobil.hr/autoservis/auto-servis-meic/
   - osnovna dijagnostika 25-45 EUR
   - R134a klima 50-80 EUR
   - R1234yf klima 80-130 EUR
   - dezinfekcija klime 30-50 EUR
   - mali servis 80-160 EUR
   - veliki servis 220-450 EUR
   - diskovi + pločice po osovini 150-320 EUR
   - razvodni remen 250-500 EUR
   - amortizeri par 200-400 EUR

## Kako su dobiveni finalni rasponi

- Finalni katalog ima 120 standardnih zahvata i 5 širokih cjenovnih klasa vozila: ECONOMY, STANDARD, PREMIUM, PERFORMANCE i EXOTIC.
- Za reprezentativne česte zahvate STANDARD klasa je ručno kalibrirana prema gore navedenim hrvatskim javnim cjenicima.
- Za ostale zahvate iskorištene su medijalne vrijednosti iz prethodno pripremljenog detaljnog AutoCare modela cijena, ali su milijuni varijanta/rad kombinacija zatim sažeti u samo 600 finalnih raspona (120 radova x 5 klasa).
- Razlike između klasa vozila preuzete su iz starog modela gdje je podatak postojao; ako nije postojao, raspon je unaprijed izračunat jednostavnim klasnim faktorom.
- Rasponi su namjerno relativno uski kako bi korisniku dali koristan orijentir, ali se ne predstavljaju kao točna ponuda za konkretan VIN, servis ili proizvođača dijela.
- U runtimeu nema fallback računanja, AI poziva, pravila po motoru ni automatske dijagnostike. Sve vrijednosti koje se prikazuju već postoje u bazi.

Datoteke koje predstavljaju finalni seed:

- `work_catalog_final.csv`
- `work_price_ranges.csv`
- `vehicle_price_classes.csv`
