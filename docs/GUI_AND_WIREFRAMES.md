# GUI, wireframeovi i event tokovi - AF3

Prilozeni izvorni PDF ima 12 low-fidelity stranica. To je funkcionalna podloga, ne nepromjenjivi layout. Aktualni kod je standardni Swing uz FlatLaf Light, bez custom nacrtanih kontrola/animacijskog frameworka. Stare preview PNG slike su sintetski Nimbus layout prikazi, NE snimke Azure SQL/Windows/FlatLaf integracije. Za predaju zamijeniti ili dodatno priloziti stvarne lokalne slike s naznacenim porijeklom.

## Kontrole
Godina i km: JSpinner s commitEdit i rasponima. Marka/model: ovisni JComboBoxovi. Veliki popis varijanti: pretraziva JTable sa stabilnim ID-om, model/generacija/motor/snaga/mjenjac. Rad: WorkPicker pretrazivi JTable, ne ogromni dropdown. Datum: formatirano dd.MM.uuuu polje s strict parsingom. Cijena: decimalni unos, zarez ili tocka, najvise dvije decimale za racun. Opis: JTextArea s prelamanjem. Vise problema: checkbox table. Bool nije slobodan tekst. ID ne pokazivati kao naziv vozila.

## Skup wireframeova (mreze su konceptualni prikaz aktualnoga toka)
### 1 Prijava
```text
                  AutoCare
             E-mail [____________]
             Lozinka [__________]
             [Prijavi se]
             Registriraj se
```
Nema sidebara. Login Controller -> AuthService u SwingWorkeru. Greska ne otkriva postoji li taj racun. Hashing nije na EDT-u.

### 2 Registracija
```text
       Ime / E-mail
       Lozinka / Ponovi lozinku
       [Nastavi]  [Natrag na prijavu]
```
Lokalni podaci; jos nema INSERT-a. Slab/prazan input pokazati uz formu. Escape/X/back rade dirty confirmation.

### 3 Prvo/dodatno vozilo
```text
 Godina [spinner]  Marka [combo]  Model [combo]
 Pretraga varijante [____________________]
 [Generacija | Motor | Snaga | Mjenjac] tabela
 Kilometraza [spinner]   lokalna slika / fallback
 [Nastavi/Spremi] [Odustani]
```
Godina i marka resetiraju podredene filtre/odabir. Lista se ucitava u pozadini. Ne pretpostaviti da 2017 automatski znaci jednoznacnu generaciju. Slika nije dokaz identiteta.

### 4 Opcionalna pocetna povijest
```text
 Datum / km / Rad [Odaberi] / Placeno (opcionalno)
 Napomena [________________________________]
 [Dodaj zapis]
 [Datum | km | Rad | Placeno | Ukloni]
 [Dovrsi] [Natrag] [Preskoci]
```
Promjena prethodno odabranog vozila zahtijeva svjesno brisanje ili povratak na odgovarajucu povijest. NULL cijena nije nula. Zavrsna registracija atomarno sprema korisnika+vozilo+active+povijest.

### 5 Dashboard
```text
 [aktivno vozilo] |  Naslov / loading
 Dashboard       |  [Poznati stvarni trosak] [Otvoreni problemi]
 Vozila          |  [Odrzavanje: due/soon/?] [Kilometraza]
 Odrzavanje      |
 Servisi         |
 Problemi        |
 Profil          |
```
Samo cetiri kartice. Ne proracunavati koji je 'najblizi' od 1000 km i 20 dana bez modela voznje. Prikazati broj nepoznatih troskova ako postoji, ne tvrditi da poznati zbroj jest potpuni trosak.

### 6 Vozila
```text
 [Dodaj vozilo]
 [slika | model/generacija | motor | godina | km]
 [Aktiviraj] [Uredi] [Obrisi]
```
Aktivacija samo ovdje. Zadnje vozilo ne moze se obrisati; aktivno brisanje trazi zamjenu. Identitet se mijenja samo prije servisa/problema. Dugi nazivi ne smiju skrivati akcije.

### 7 Odrzavanje i privremena procjena
```text
 [Rad | Zadnje | Sljedece | Preostalo | Status | Procjena]
 [Odaberi rad] [Dodaj u procjenu] [Ukloni]
 Procijenjena ukupna cijena: priblizno ... EUR
```
FIXED rok dolazi iz izvora; CONDITION_BASED 'Prema stanju'; VEHICLE_INDICATOR 'Prema indikatoru'; UNKNOWN jasno 'Interval nije poznat'. Ne prikazivati sve kao zdravo. Cijena NULL -> 'Nema procjene'. Planski kalkulator ne sprema servis niti mijenja interval. Zabraniti dvostruko racunanje sadrzaja paketa (diskovi+plocice i plocice iste osovine itd.) uz jasnu poruku. Prikaz cijene zaokruzen na 10 EUR, stvarni racuni nisu.

### 8 Servisna povijest
```text
 [Dodaj servis] [Prethodna/sljedeca stranica]
 [Datum | km | Stavke | Stvarno placeno | Napomena]
```
ID paging pa fetch items, bez paginiranja collection fetch joina. Odabir sortiranog retka mora se prevesti view->model. Odabir daje detalj, ne automatski write.

### 9 Novi servis
```text
 Datum / km
 [Dodaj rad] [Kategorija | Rad | Stvarno placeno | Ukloni]
 Rjesava probleme: [checkbox | Opis]
 Napomena [________________________________]
 [Spremi cijeli servis] [Odustani]
```
Jedna tablica za obje kategorije smanjuje dupli layout. Potvrditi zadnju celiju prije snapshot-a. Cijena obvezna osim povijesti u onboardingu. OTHER_ zahtijeva napomenu. Procjena ne popunjava actual. Busy zakljucava dvostruki submit. Sve jedan commit. Failure refresh nakon commita nije failure savea.

### 10 Problemi
```text
 [Otvoreni] [Rijeseni]
 [Opis | Predlozen rad | Podudaranje | Procjena]
 [Analiza novog problema]
```
U analizu idu samo primjenjivi REPAIR kandidati aktivne varijante. HV kvar ne daje upute korisniku da sam popravlja visoki napon. Nema oznake 'sigurno za voznju' iz text matcha.

### 11 Analiza / rezultat
```text
 Simptomi [vise redaka]
 [Analiziraj]
 [Kandidat | Podudaranje | Procijenjena ukupna cijena]
 Glavna procjena: prvi kandidat, ne zbroj alternativa
 [Spremi problem] [Nova analiza] [Odustani]
```
Nema podudaranja je valjano stanje; dopustiti spremiti opis bez kandidat/cijena. Rezultat nije dijagnoza niti postotna vjerojatnost. Izvor/napomena dostupni kroz detalj/tooltip.

### 12 Profil
```text
 Ime / E-mail / Trenutna lozinka za osjetljive promjene
 Nova lozinka / potvrda
 [Spremi] [Odjava]
```
Ne slati hash u UI. Dirty warning prije odjave. Reset session epocha odbacuje zaostale rezultate starog korisnika.

## Prikaz / pristupacnost / testiranje
Pozadina svijetla, kartice bijele, dosljedni razmaci, primarna akcija naglasena, opasna akcija odvojena. Koristiti layout manager, ne absolute position. Status ima tekst, ne samo boju. Tipkovnica Tab/Enter/Escape, fokus u prvo relevantno polje; duge napomene sa scrollom. Native standardni dialogi prihvatljivi.

Svaki read/write u SwingWorkeru: UI snapshot prije backgrounda, DB/izracun izvan EDT-a, done prikaz na EDT-u. Session epoch + request ticket blokiraju kasni odgovor A nakon izbora B. Observer objava tek nakon commita; hidden panel dirty, visible refresh. Ne osvjezavati svaki panel paralelno na svaki event.

Lokalno ispitati 100/125/150% scaling, 1366x768 i veci ekran, duge modele, 0 rezultata, 30k kataloskih varijanti, prekid mreze, odustajanje, konflikt verzije i izbor drugog auta tijekom ucitavanja. Snimke ne mogu zamijeniti ove akcijske testove.
