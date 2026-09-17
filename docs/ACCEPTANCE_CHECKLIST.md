# FINAL ACCEPTANCE CHECKLIST

## Data
- [ ] 30,366 variants
- [ ] OTHER_MAINTENANCE i OTHER_REPAIR uklonjeni
- [ ] svaki spremljeni VehicleWorkRule ima estimatedPrice > 0
- [ ] svaki maintenance rule ima km ili months interval
- [ ] nijedan repair rule nema preventivni interval
- [ ] nema duplicate variant/work
- [ ] BEV nema OIL_SERVICE, SPARK_PLUGS, TIMING_BELT_PUMP, DPF_REPLACEMENT
- [ ] postojeće dobre cijene auditirane kao KEPT_EXISTING
- [ ] prazne cijene popunjene
- [ ] postojeći intervali sačuvani
- [ ] prazni maintenance intervali popunjeni

## Diagnostics -> Problems
- [ ] diagnostic_rule table nema
- [ ] DiagnosticStrategy/KeywordDiagnosticStrategy nema
- [ ] AnalysisDialog nema
- [ ] RuleData/DiagnosticResult/Analysis nema
- [ ] problem korisnik ručno bira repair
- [ ] Procijeni cijenu čita konkretni VehicleWorkRule
- [ ] OPEN i RESOLVED se vide zajedno sa Status stupcem

## Strategy
- [ ] MileageMaintenanceStrategy
- [ ] TimeMaintenanceStrategy
- [ ] CombinedMaintenanceStrategy
- [ ] MaintenanceCalculator bira strategiju jednostavnim if/else
- [ ] nema lambda/stream

## Maintenance
- [ ] tablica prikazuje samo već servisirana/tracked održavanja
- [ ] nema Nema podataka redaka
- [ ] nakon prvog servisa maintenance se odmah pojavi
- [ ] next km/date se računaju
- [ ] inline Procijeni cijenu i interval radi za svaki ponuđeni maintenance work
- [ ] nema tehničkog AC-MODEL popup teksta

## Dashboard/sidebar
- [ ] 4 bordered cards
- [ ] Ikonli ikone
- [ ] sljedeće održavanje umjesto 0/0/Nema podataka counta
- [ ] nema Osvježi
- [ ] nema Aktivno vozilo #ID
- [ ] nema AutoCare / NOOP
- [ ] datum je vidljiv uz aktivni auto
- [ ] servis odmah osvježi sidebar kilometražu

## Vehicle form
- [ ] Marka -> Model -> Godina -> Varijanta dropdowni
- [ ] nema search tablice varijanti
- [ ] godina je JComboBox
- [ ] kilometraža je JTextField
- [ ] novo vozilo mileage field je prazan
- [ ] postojeće vozilo nema edit identiteta
- [ ] postoji samo Promijeni kilometražu

## Service editor
- [ ] procijenjena cijena se ne prikazuje kod biranja servisne stavke
- [ ] type + work + actualPrice + Dodaj su na istom dialogu
- [ ] nema WorkPicker popup-a
- [ ] nema custom ServiceItemsModel
- [ ] Ukloni odabranu stavku ostaje jednostavan gumb
- [ ] service detail ostaje

## Registration
- [ ] registracija/onboarding u istom JFrameu
- [ ] CardLayout login/register
- [ ] račun -> prvo vozilo -> history

## Croatian UI
- [ ] korisnički stringovi imaju č/ć/ž/š/đ
- [ ] work names očišćeni gdje je očita hrvatska ASCII transliteracija
- [ ] work codes nisu promijenjeni

## Runtime style
- [ ] no lambda
- [ ] no stream
- [ ] no Optional
- [ ] no java.util.function
- [ ] no var/record
- [ ] klasični ActionListener
- [ ] opisna imena

## Build/DB
- [ ] git diff --check
- [ ] Maven clean verify
- [ ] package
- [ ] Javadoc
- [ ] setup build/test
- [ ] runtime style checker
- [ ] secret checker
- [ ] Azure SQL import
- [ ] final_catalog_audit.sql pass
- [ ] sql-check pass
- [ ] db-check pass
- [ ] GUI smoke PASS ili iskreno NOT_RUN
