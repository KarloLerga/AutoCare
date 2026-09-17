# GUI, wireframeovi i event tokovi - AF3

Prilozeni izvorni PDF ima 12 low-fidelity stranica. To je funkcionalna podloga, ne nepromjenjivi layout. Aktualni kod je standardni Swing uz FlatLaf Dark, bez custom nacrtanih kontrola/animacijskog frameworka. Stare preview PNG slike su sintetski Nimbus layout prikazi, NE snimke Azure SQL/Windows/FlatLaf integracije. Za predaju zamijeniti ili dodatno priloziti stvarne lokalne slike s naznacenim porijeklom.

## Kontrole
Godina i km: JSpinner s osnovnim rasponom. Marka/model: ovisni JComboBoxovi. Veliki popis varijanti: pretraziva JTable sa stabilnim ID-om, model/generacija/motor/snaga/mjenjac. Rad: jednostavan modalni JList s odabirom po kategoriji, bez live filter frameworka. Datum: obicni JTextField u obliku dd.MM.uuuu s parsiranjem kroz `Ui.parseDate`. Cijena: decimalni unos, zarez ili tocka, najvise dvije decimale za racun. Opis: JTextArea s prelamanjem. Vise problema: checkbox table. Bool nije slobodan tekst. ID ne pokazivati kao naziv vozila.

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
 Vozila          |  [Odrzavanje: due/soon/no-data] [Kilometraza]
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
Status je jedan od `Nema podataka`, `U redu`, `Uskoro` ili `Dospjelo`; `NO_DATA` pokriva nepoznat interval, nedostajucu povijest i planove prema stanju/indikatoru. WorkDefinition default interval vrijedi samo kada nema specificnog VehicleWorkRulea, a specificni rule je autoritativan kao cjelina. Cijena NULL -> 'Nema procjene'. Planski kalkulator ne sprema servis niti mijenja interval. Obicni multi-select zbroj koristi CostSummary jednom; nema posebne tablice za rucno prepoznavanje preklopa. Prikaz cijene zaokruzen je na 10 EUR, stvarni racuni nisu.

### 8 Servisna povijest
```text
 [Dodaj servis] [Detalj]
 [Datum | km | Stavke | Stvarno placeno | Napomena]
```
Povijest se ucitava za aktivno vozilo kao jednostavan sortirani popis s fetchom stavki/radova. Odabir sortiranog retka mora se prevesti view->model. Odabir daje detalj, ne automatski write.

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
Ne slati hash u UI. Dirty warning prije odjave. Session sadrzi samo vlasnika i aktivni DTO; callbackovi ne nose epoch/ticket stanje.

## Prikaz / pristupacnost / testiranje
Tamna FlatLaf pozadina, kartice s jasnim kontrastom, dosljedni razmaci, primarna akcija naglasena, opasna akcija odvojena. Koristiti layout manager, ne absolute position. Status ima tekst, ne samo boju. Tipkovnica Tab/Enter/Escape, fokus u prvo relevantno polje; duge napomene sa scrollom. Native standardni dialogi prihvatljivi.

Svaki read/write u jednostavnom SwingWorkeru: UI snapshot prije backgrounda, DB/izracun izvan EDT-a, done prikaz na EDT-u. Cursor se vraca u `done`, a greska se prikazuje kroz standardni UI helper. Observer objava ide nakon commita; vidljivi paneli se osvjezavaju kroz dogovoreni event tok.

Lokalno ispitati 100/125/150% scaling, 1366x768 i veci ekran, duge modele, 0 rezultata, 30k kataloskih varijanti, prekid mreze, odustajanje, konflikt verzije i izbor drugog auta tijekom ucitavanja. Snimke ne mogu zamijeniti ove akcijske testove.
