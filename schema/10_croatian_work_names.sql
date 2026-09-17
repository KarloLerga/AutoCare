-- Guarded Croatian display-name migration. Default is read-only; setup tool changes @Apply only on explicit apply.
SET NOCOUNT ON;
DECLARE @Apply bit = 0;

IF DB_NAME() IN (N'master', N'model', N'msdb', N'tempdb')
  THROW 51100, 'Sistemska baza nije dopuštena za AutoCare migraciju.', 1;

DECLARE @Names TABLE (code nvarchar(80) NOT NULL PRIMARY KEY, display_name nvarchar(160) NOT NULL);
INSERT INTO @Names(code, display_name) VALUES
    (N'OIL_SERVICE', N'Motorno ulje i filtar ulja - kompletna izmjena'),
    (N'AIR_FILTER', N'Zamjena filtra zraka motora'),
    (N'CABIN_FILTER', N'Zamjena filtra kabine'),
    (N'FUEL_FILTER', N'Zamjena filtra goriva'),
    (N'BRAKE_FLUID', N'Izmjena kočione tekućine'),
    (N'COOLANT', N'Izmjena rashladne tekućine motora'),
    (N'TIMING_BELT_PUMP', N'Zupčasti remen, natezači i vodena pumpa'),
    (N'SPARK_PLUGS', N'Zamjena kompleta svjećica'),
    (N'AUX_BELT', N'Pomoćni remen i natezač'),
    (N'MANUAL_GEARBOX_OIL', N'Izmjena ulja ručnog mjenjača'),
    (N'AUTO_GEARBOX_OIL', N'Servis ulja automatskog mjenjača'),
    (N'FRONT_BRAKES', N'Prednje kočione pločice - par kotača'),
    (N'REAR_BRAKES', N'Stražnje kočione pločice - par kotača'),
    (N'FRONT_DISCS_PADS', N'Prednji diskovi i pločice - par kotača'),
    (N'REAR_DISCS_PADS', N'Stražnji diskovi i pločice - par kotača'),
    (N'REAR_DRUM_SHOES', N'Stražnje kočione pakne - par kotača'),
    (N'AC_COMPRESSOR', N'Zamjena kompresora klime'),
    (N'RADIATOR_FAN', N'Zamjena ventilatora hladnjaka'),
    (N'AC_R134A', N'Servis klime R134a'),
    (N'AC_R1234YF', N'Servis klime R1234yf'),
    (N'BATTERY', N'Zamjena 12 V akumulatora'),
    (N'GLOW_PLUGS', N'Zamjena kompleta grijača dizela'),
    (N'DIAGNOSIS', N'Osnovno očitanje OBD kodova'),
    (N'STARTER', N'Zamjena elektropokretača'),
    (N'ALTERNATOR', N'Zamjena alternatora'),
    (N'TURBO', N'Zamjena jednog turbopunjača'),
    (N'EGR_VALVE', N'Zamjena EGR ventila'),
    (N'DPF_CLEAN', N'Demontaža, čišćenje i montaža DPF-a'),
    (N'DPF_REPLACEMENT', N'Zamjena DPF filtra'),
    (N'DIESEL_INJECTOR', N'Zamjena jedne dizelske brizgaljke'),
    (N'IGNITION_COIL', N'Zamjena jedne bobine'),
    (N'WHEEL_BEARING', N'Zamjena jednog ležaja kotača'),
    (N'FRONT_SHOCKS', N'Prednji amortizeri - par'),
    (N'REAR_SHOCKS', N'Stražnji amortizeri - par'),
    (N'CLUTCH_KIT', N'Zamjena kompleta spojke'),
    (N'CLUTCH_DMF', N'Komplet spojke i dvomaseni zamašnjak'),
    (N'WATER_PUMP', N'Zamjena vodene pumpe motora'),
    (N'THERMOSTAT', N'Zamjena termostata motora'),
    (N'LAMBDA_SENSOR', N'Zamjena jedne lambda sonde'),
    (N'SCHEDULED_INSPECTION', N'Redovni servisni pregled prema planu proizvođača'),
    (N'BRAKE_FLUID_CHECK', N'Provjera stanja kočione tekućine'),
    (N'WIPER_REPLACE', N'Zamjena prednjih metlica brisača - par'),
    (N'TYRE_ROTATION', N'Rotacija kotača - kompatibilne dimenzije'),
    (N'TYRE_PRESSURE_CHECK', N'Provjera tlaka i stanja guma'),
    (N'TYRE_MOUNT_BALANCE', N'Montaža i balansiranje četiri gume'),
    (N'TYRE_REPLACE_SET', N'Zamjena kompleta četiriju guma'),
    (N'WHEEL_ALIGNMENT', N'Kontrola i podešavanje geometrije kotača'),
    (N'AC_DISINFECTION', N'Dezinfekcija ventilacijskog sustava'),
    (N'BATTERY_TEST', N'Test niskonaponskog akumulatora'),
    (N'BRAKE_INSPECTION', N'Pregled kočnica'),
    (N'BRAKE_CALIPER_CLEAN', N'Čišćenje i podmazivanje kočionih čeljusti'),
    (N'BODY_CORROSION_CHECK', N'Pregled podvozja i korozije'),
    (N'HYBRID_BATTERY_FILTER', N'Čišćenje ili zamjena filtra hlađenja hibridne baterije'),
    (N'TRACTION_BATTERY_TEST', N'Dijagnostika stanja pogonske baterije'),
    (N'EV_DRIVE_FLUID', N'Zamjena ulja reduktora električnog pogona'),
    (N'DIFFERENTIAL_OIL', N'Zamjena ulja zasebnog diferencijala'),
    (N'TRANSFER_CASE_OIL', N'Zamjena ulja razvodnika pogona'),
    (N'ENGINE_OIL_LEAK_TEST', N'Dijagnostika curenja motornog ulja'),
    (N'COOLING_PRESSURE_TEST', N'Ispitivanje nepropusnosti hlađenja motora'),
    (N'OIL_PAN_GASKET', N'Zamjena brtve ili brtvljenje kartera'),
    (N'VALVE_COVER_GASKET', N'Zamjena brtve poklopca ventila'),
    (N'ENGINE_MOUNT', N'Zamjena jednog nosača motora'),
    (N'ENGINE_REBUILD', N'Obnova ili zamjena motora'),
    (N'HEAD_GASKET', N'Popravak brtve glave motora'),
    (N'TIMING_CHAIN', N'Zamjena kompleta razvodnog lanca'),
    (N'AUX_TENSIONER', N'Zamjena natezača pomoćnog remena'),
    (N'RADIATOR', N'Zamjena hladnjaka rashladne tekućine motora'),
    (N'COOLANT_HOSE', N'Zamjena jednog crijeva hlađenja motora'),
    (N'MAF_SENSOR', N'Zamjena mjerača protoka zraka'),
    (N'MAP_SENSOR', N'Zamjena senzora tlaka usisa'),
    (N'CRANKSHAFT_SENSOR', N'Zamjena senzora položaja radilice'),
    (N'CAMSHAFT_SENSOR', N'Zamjena senzora položaja bregaste'),
    (N'THROTTLE_BODY', N'Zamjena kućišta zaklopke usisa'),
    (N'PETROL_INJECTOR', N'Zamjena jednog benzinskog injektora'),
    (N'FUEL_PUMP', N'Zamjena niskotlačne pumpe goriva'),
    (N'HIGH_PRESSURE_PUMP', N'Zamjena visokotlačne pumpe goriva'),
    (N'DIESEL_INJECTOR_SEAL', N'Zamjena brtve jednog dizelskog injektora'),
    (N'GLOW_CONTROLLER', N'Zamjena upravljačke jedinice grijača'),
    (N'TURBO_HOSE', N'Zamjena jednog crijeva turbopunjenja'),
    (N'EXHAUST_MUFFLER', N'Zamjena završnog lonca ispuha'),
    (N'CATALYTIC_CONVERTER', N'Zamjena katalizatora'),
    (N'GPF_REPLACEMENT', N'Zamjena benzinskog filtra čestica'),
    (N'DPF_PRESSURE_SENSOR', N'Zamjena senzora diferencijalnog tlaka DPF-a'),
    (N'NOX_SENSOR', N'Zamjena jednog NOx senzora'),
    (N'ADBLUE_PUMP', N'Popravak ili zamjena AdBlue modula'),
    (N'FRONT_CALIPER', N'Zamjena jedne prednje kočione čeljusti'),
    (N'REAR_CALIPER', N'Zamjena jedne stražnje kočione čeljusti'),
    (N'BRAKE_HOSE', N'Zamjena jednog fleksibilnog kočionog crijeva'),
    (N'BRAKE_MASTER_CYLINDER', N'Zamjena glavnog kočionog cilindra'),
    (N'ABS_SENSOR', N'Zamjena jednog ABS senzora'),
    (N'ABS_HYDRAULIC_UNIT', N'Popravak ABS/ESP hidraulične jedinice'),
    (N'PARKING_BRAKE_CABLE', N'Zamjena sajle parkirne kočnice'),
    (N'CONTROL_ARM', N'Zamjena jednog prednjeg ramena ovjesa'),
    (N'BALL_JOINT', N'Zamjena jednog kuglastog zgloba'),
    (N'STABILIZER_LINK', N'Zamjena jedne spone stabilizatora'),
    (N'STEERING_TIE_ROD', N'Zamjena jednog krajnika upravljača'),
    (N'STEERING_RACK', N'Popravak ili zamjena letve upravljača'),
    (N'COIL_SPRINGS_AXLE', N'Zamjena opruga na jednoj osovini'),
    (N'STRUT_TOP_MOUNT', N'Zamjena jednog gornjeg ležaja amortizera'),
    (N'CV_JOINT', N'Zamjena jednog homokinetičkog zgloba'),
    (N'CV_BOOT', N'Zamjena jedne manžete poluosovine'),
    (N'TRANSMISSION_REBUILD', N'Popravak ili zamjena mjenjača'),
    (N'WINDOW_REGULATOR', N'Zamjena podizača jednog prozora'),
    (N'WIPER_MOTOR', N'Zamjena motora prednjih brisača'),
    (N'DOOR_LOCK_ACTUATOR', N'Zamjena aktuatora brave vrata'),
    (N'BLOWER_MOTOR', N'Zamjena ventilatora kabine'),
    (N'BLOWER_CONTROLLER', N'Zamjena regulatora ventilatora kabine'),
    (N'AC_CONDENSER', N'Zamjena kondenzatora klime'),
    (N'WINDSCREEN_REPLACE', N'Zamjena vjetrobranskog stakla'),
    (N'ADAS_CALIBRATION', N'Kalibracija kamere ili radara asistencije'),
    (N'HEADLAMP_UNIT', N'Zamjena jednog prednjeg svjetla'),
    (N'TPMS_SENSOR', N'Zamjena jednog senzora tlaka u gumi'),
    (N'HV_BATTERY_REPLACE', N'Zamjena pogonske baterije'),
    (N'EV_DRIVE_UNIT', N'Zamjena jednog električnog pogonskog sklopa'),
    (N'ONBOARD_CHARGER', N'Zamjena ugrađenog AC punjača'),
    (N'DC_DC_CONVERTER', N'Zamjena DC/DC pretvarača'),
    (N'CHARGE_PORT', N'Popravak ili zamjena priključka punjenja'),
    (N'HV_INVERTER', N'Popravak ili zamjena pogonskog invertera'),
    (N'HV_ISOLATION_TEST', N'Dijagnostika izolacije visokonaponskog sustava'),
    (N'HV_COOLANT', N'Servis rashladne tekućine pogonske baterije');

SELECT
  DB_NAME() AS database_name,
  (SELECT COUNT(*) FROM @Names) AS expected_names,
  (SELECT COUNT(*) FROM dbo.work_definition work JOIN @Names names ON names.code=work.code) AS matched_names,
  (SELECT COUNT(*) FROM dbo.work_definition work JOIN @Names names ON names.code=work.code WHERE work.name<>names.display_name) AS names_to_update;

IF @Apply = 0
  RETURN;

BEGIN TRY
  BEGIN TRANSACTION;
  IF (SELECT COUNT(*) FROM @Names) <> 120
    THROW 51101, 'Popis mora sadržavati 120 finalnih radova.', 1;
  IF (SELECT COUNT(*) FROM dbo.work_definition) <> 120
    THROW 51102, 'Očekuje se 120 finalnih radova u bazi.', 1;
  IF (SELECT COUNT(*) FROM dbo.work_definition work JOIN @Names names ON names.code=work.code) <> 120
    THROW 51103, 'Kodovi radova u bazi ne odgovaraju finalnom popisu.', 1;
  UPDATE work SET name=names.display_name
  FROM dbo.work_definition work JOIN @Names names ON names.code=work.code;
  COMMIT TRANSACTION;
END TRY
BEGIN CATCH
  IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
  THROW;
END CATCH;

SELECT COUNT(*) AS names_still_different
FROM dbo.work_definition work JOIN @Names names ON names.code=work.code
WHERE work.name<>names.display_name;
