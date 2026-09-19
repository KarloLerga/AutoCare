# Finalni acceptance checklist

## Build

- [x] Java 25 aktivna na korisnikovom računalu
- [x] `mvnw.cmd clean verify` PASS
- [x] `mvnw.cmd javadoc:javadoc` PASS
- [x] Java main source interni compile sanity PASS u ChatGPT okruženju (API stubovi; nije zamjena za Maven)
- [x] `scripts/validate_professor_catalog.py` PASS
- [x] runtime static style scan PASS
- [x] nema starih diagnostic runtime klasa

## Finalni podaci

- [x] 30.366 vehicle variants
- [x] 120 work definitions
- [x] 30 maintenance
- [x] 90 repair
- [x] 5 vehicle price classes
- [x] 600 work price ranges
- [x] svaki work ima svih 5 price ranges
- [x] svaki price range ima pozitivan min i max >= min
- [x] svaki maintenance ima km i/ili month interval
- [x] repair nema preventivni interval

## Bilješke

- [x] slobodan opis
- [x] gruba kategorija
- [x] OPEN/RESOLVED
- [x] manual close
- [x] opcionalno rješavanje stvarnim servisom
- [x] nema suggested repair / estimated cost / match score

## Katalog

- [x] zaseban ekran
- [x] search po nazivu/kodu
- [x] filter kategorije
- [x] min-max cijena prema VehiclePriceClass aktivnog vozila
- [x] maintenance interval vidljiv u katalogu
- [x] procjena se ne sprema kao actualPrice

## Servisi i održavanje

- [x] datum + kilometraža + stavke + stvarno plaćeno + napomena
- [x] jedna transakcija za servis, stavke, kilometražu i riješene bilješke
- [x] povijesni servis ne smanjuje trenutnu kilometražu
- [x] maintenance se računa iz servisne povijesti
- [x] Mileage / Time / Combined Strategy

## Azure SQL

- [x] dry-run `Complete-Setup.ps1` PASS na stvarnoj bazi
- [x] `-ApplyProfessorModel` PASS
- [x] `final_error_count = 0`
- [x] `vehicle_work_rule` ne postoji
- [x] `diagnostic_rule` ne postoji
- [x] `problem.suggested_repair_id` ne postoji
- [x] `problem.estimated_cost` ne postoji

## Ručni GUI

- [ ] registracija i prvo vozilo
- [ ] login/logout
- [ ] više vozila i active vehicle persistence
- [ ] ažuriranje kilometraže
- [ ] create/close bilješke
- [ ] catalog search/filter
- [ ] novi servis s maintenance i repair stavkom
- [ ] rješavanje bilješke servisom
- [ ] novi maintenance interval nakon servisa
- [ ] Dashboard vrijednosti osvježene
