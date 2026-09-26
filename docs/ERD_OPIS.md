# ERD - opis

Priložena finalna ERD slika je `diagrams/ERD_AutoCare_FINAL.png` i nije mijenjana. Tekstualni izvor modela nalazi se u `diagrams/ERD_AutoCare_FINAL.mmd` i napisan je u **Mermaid notaciji**. Dijagram daje logički pregled; fizičke SQL tipove i ograničenja definira mapiranje aplikacije i postojeća baza.

## Entiteti

- `APP_USER` - korisnički račun i referenca na trenutno aktivno vozilo.
- `VEHICLE` - konkretno korisnikovo vozilo.
- `VEHICLE_VARIANT` - referentni kataloški podaci o marki, modelu, motoru i razdoblju proizvodnje.
- `WORK_DEFINITION` - katalog standardnih radova, kategorija, intervala i informativnih raspona cijena.
- `SERVICE_RECORD` - jedan evidentirani servis vozila.
- `SERVICE_ITEM` - jedna stvarno izvršena i plaćena stavka servisa.
- `PROBLEM` - problem/bilješka vlasnika vozila koji može biti riješen konkretnim servisom.

## Veze

### APP_USER -> VEHICLE: owns

`VEHICLE.owner_id` pokazuje kojem korisniku vozilo pripada. Jedan korisnik može imati više vozila, a svako vozilo ima jednog vlasnika.

### APP_USER -> VEHICLE: activeVehicle

`APP_USER.active_vehicle_id` označava koje je korisnikovo vozilo trenutno aktivno u aplikaciji. To je zasebna veza od vlasništva: `owns` označava sva korisnikova vozila, a `activeVehicle` samo trenutno odabrano vozilo.

### VEHICLE_VARIANT -> VEHICLE

Jedna varijanta vozila može biti korištena za više korisničkih vozila, dok jedno konkretno vozilo ima jednu varijantu.

### VEHICLE -> SERVICE_RECORD

Vozilo može imati više servisnih zapisa. Svaki servis pripada jednom vozilu.

### SERVICE_RECORD -> SERVICE_ITEM

Servis sadrži jednu ili više stavki. Svaka stavka pripada jednom servisu.

### WORK_DEFINITION -> SERVICE_ITEM

Jedan standardni rad može se pojaviti u više servisnih stavki. Jedna servisna stavka referencira jedan rad.

### VEHICLE -> PROBLEM

Vozilo može imati više evidentiranih problema.

### SERVICE_RECORD -> PROBLEM

Problem može biti neriješen ili riješen jednim servisom. Jedan servis može riješiti više problema.
