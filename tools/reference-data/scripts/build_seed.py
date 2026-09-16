#!/usr/bin/env python3
"""Deterministic offline AutoCare catalogue and MODELLED-price builder.
No network, credentials, VIN decoding, OEM schedule inference or runtime API.
The model is a transparent planning scenario, NOT a fitted price predictor.
"""
from __future__ import annotations
import argparse, csv, gzip, hashlib, io, json, re, sys, zipfile
from collections import Counter
from contextlib import ExitStack
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
from typing import Any, Iterator
from af2_catalog import normalize, REQUIRED, OUT, clean


def normalize_source(row):
    """Keep AF2 raw-source codes; adapt optional fields to the existing DB types."""
    original={key:clean(row.get(key)) for key in REQUIRED}
    identity=json.dumps(original,ensure_ascii=False,sort_keys=True,separators=(',',':'))
    code='vmm-'+hashlib.sha256(identity.encode('utf-8')).hexdigest()
    edited=dict(original);changes=[]
    hp=edited['power_hp']
    if hp:
        value=Decimal(hp)
        if not value.is_finite() or not 1<=value<=10000:
            edited['power_hp']='';changes.append(('power_hp',hp,'','INVALID_OPTIONAL_POWER_SET_NULL'))
        elif value!=value.to_integral_value():
            edited['power_hp']=str(value.quantize(Decimal(1),rounding=ROUND_HALF_UP));changes.append(('power_hp',hp,edited['power_hp'],'ROUNDED_FOR_INTEGER_STORAGE'))
    tr=edited['transmission']
    if len(tr)>120:
        # Extract only a literal transmission designation; preserve full raw text in the audit.
        match=re.search(r'(?i)([0-9]+)-speed.*automatic',tr)
        short='CVT' if 'cvt' in tr.lower() else (match[1]+'-speed automatic') if match else ''
        edited['transmission']=short;changes.append(('transmission',tr,short,'SHORTENED_LITERAL_DISPLAY_DESCRIPTION'))
    n=normalize(edited);n['code']=code
    return n,changes


ROOT = Path(__file__).resolve().parents[1]
D = Decimal
RULE_FIELDS = ['variant_code','work_code','interval_km','interval_months','estimated_price','interval_source','estimate_note','price_basis','applicability','review_status']
AUDIT_FIELDS = ['variant_code','work_code','estimated_price','state','reason','confidence','parts_model_eur','hours_model','consumables_model_eur','raw_total_model_eur','source_ids']
TRAIT_FIELDS = ['variant_code','source_row','make','model','generation','engine_label','year_from','year_to','fuel_class','is_hybrid','displacement_cc','cylinders','cylinder_layout','power_hp','torque_nm','mass_kg','transmission_class','turbo','front_brake','rear_brake','fuel_system_raw','front_brake_raw','rear_brake_raw','body_inferred','tier','flags']
ENGINE_JOBS = {'OIL_SERVICE','AIR_FILTER','FUEL_FILTER','COOLANT','TIMING_BELT_PUMP','SPARK_PLUGS','AUX_BELT','GLOW_PLUGS','TURBO','EGR_VALVE','DIESEL_INJECTOR','IGNITION_COIL','WATER_PUMP','THERMOSTAT','LAMBDA_SENSOR','STARTER','ALTERNATOR'}


def dec(value: Any, default: str='0') -> Decimal:
    try:
        x=D(str(value))
        return x if x.is_finite() else D(default)
    except Exception:
        return D(default)


def clamp(x: Decimal, a: str, b: str) -> Decimal:
    return max(D(a),min(D(b),x))


def round_ten(value: Decimal) -> Decimal:
    if not value.is_finite() or value < 0:
        raise ValueError('Invalid monetary value')
    return (value/D(10)).quantize(D(1),rounding=ROUND_HALF_UP)*D(10)


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def flatten(groups: list) -> Iterator[tuple]:
    for group in groups:
        for make in group['makes']:
            for model in make['models']:
                for generation in model['generations']:
                    for engine in generation['engines']:
                        yield group['group'],make['name'],model['name'],generation,engine


def brake_type(raw: str) -> str:
    s=raw.lower()
    if 'ceramic' in s or 'carbon' in s:return 'CERAMIC'
    disc=('disc' in s or 'disk' in s)
    drum='drum' in s
    if disc and drum:return 'UNKNOWN'
    if disc:return 'DISC'
    if drum:return 'DRUM'
    return 'UNKNOWN'


def traits(r: dict, e: dict, model: dict, code: str, number: int) -> dict:
    specs=e['specs']; flags=[]
    fuel=clean(r['fuel_type']).lower()
    hybrid='hybrid' in fuel
    if fuel=='electric':fc='BEV'
    elif 'fuel cell' in fuel:fc='FCEV'
    elif 'diesel' in fuel:fc='DIESEL'
    elif fuel in {'gasoline','natural gas','ethanol','liquefied petroleum gas (lpg)'} or 'gasoline' in fuel:fc='PETROL'
    elif hybrid:fc='COMBUSTION_UNSPECIFIED';flags.append('HYBRID_COMBUSTION_FUEL_UNSPECIFIED')
    else:fc='UNKNOWN';flags.append('FUEL_UNKNOWN')
    # Ambiguous hybrids need explicit combustion evidence. No assumed petrol for a blank power system.
    cc=dec(r['displacement_cc'])
    if hybrid and 'diesel' not in fuel and not cc:
        flags.append('HYBRID_FUEL_UNCERTAIN')
    raw_cyl=specs.get('ENGINE SPECS / Cylinders','')
    cyl=dec(r['cylinders'])
    if fc in {'BEV','FCEV'}:
        if cyl:flags.append('SOURCE_EV_CYLINDERS_DISCARDED')
        cyl=D(0)
    elif cyl not in list(map(D,[2,3,4,5,6,8,10,12,16])):
        flags.append('CYLINDERS_UNKNOWN_OR_INVALID');cyl=D(0)
    if fc not in {'BEV','FCEV'} and not D(500)<=cc<=D(9000):
        flags.append('DISPLACEMENT_UNKNOWN_OR_OUTLIER');cc=D(0)
    hp=dec(r['power_hp']);mass=dec(r['curb_weight_kg']);torque=dec(r['torque_nm'])
    if not D(450)<=mass<=D(4500):flags.append('MASS_UNKNOWN_OR_OUTLIER');mass=D(0)
    if not D(10)<=hp<=D(2500):flags.append('POWER_UNKNOWN_OR_OUTLIER');hp=D(0)
    # Label checks detect disagreements; they DO NOT decide which conflicting value is correct.
    lm=re.search(r'(?i)(\d+(?:\.\d+)?)\s*L\b',r['engine_label'])
    if lm and cc and abs(D(lm[1])*1000-cc)>max(D(180),cc*D('.12')):
        flags.append('DISPLACEMENT_LABEL_CONFLICT')
    ph=re.search(r'(?i)\((\d+)\s*HP\)',r['engine_label'])
    if ph and hp and not hybrid and abs(D(ph[1])-hp)>max(D(10),hp*D('.12')):
        flags.append('POWER_LABEL_CONFLICT')
    tr=(r['transmission']+' '+r['engine_label']).lower()
    if fc=='BEV':tc='EV_REDUCTION'
    elif any(s in tr for s in ['e-cvt','e cvt','e.cvt']):tc='HYBRID_ECVT'
    elif any(s in tr for s in ['dsg','dct','dual clutch','dual-clutch','powershift','s tronic','s-tronic','edc']):tc='DCT'
    elif 'cvt' in tr:tc='CVT_HYBRID_UNCERTAIN' if hybrid else 'CVT'
    elif 'manual' in tr and not any(s in tr for s in ['automatic','automated','robotized','robotised','sequential']):tc='MANUAL'
    elif any(s in tr for s in ['automatic',' at ','at ','tiptronic','steptronic','tronic','automated','robot']):tc='AUTOMATIC_UNSPECIFIED'
    else:tc='UNKNOWN';flags.append('TRANSMISSION_UNKNOWN')
    raw_trans=r['transmission'].lower()
    label=r['engine_label'].lower()
    if (re.search(r'\b[0-9]+mt\b',label) and 'automatic' in raw_trans) or (re.search(r'\b[0-9]+at\b',label) and 'manual' in raw_trans and 'automatic' not in raw_trans):
        tc='UNKNOWN';flags.append('TRANSMISSION_LABEL_CONFLICT')
    fuel_sys=specs.get('ENGINE SPECS / Fuel System','')
    fs=fuel_sys.lower()
    turbo='YES' if 'turbo' in fs else 'NO' if any(s in fs for s in ['naturally aspirated','carburetor','supercharged']) else 'UNKNOWN'
    # A non-turbo fuel-system string is not proof of naturally aspirated operation.
    if turbo=='UNKNOWN' and 'turbo' in r['engine_label'].lower():turbo='LABEL_ONLY'
    if turbo=='YES' and 'naturally aspirated' in fs:flags.append('INDUCTION_CONFLICT');turbo='UNKNOWN'
    tier='MAINSTREAM'
    for name,key in [('ECONOMY','economy_makes'),('PREMIUM','premium_makes'),('PERFORMANCE','performance_makes'),('EXOTIC','exotic_makes')]:
        if r['make'] in model[key]:tier=name
    if r['make'] in model['local_market_uncertain_makes']:flags.append('HR_PARTS_AVAILABILITY_UNCERTAIN')
    if int(r['gen_year_end'] or r['gen_year_start'])<model['legacy_cutoff_end_year'] or int(r['gen_year_start'])<1985:flags.append('LEGACY_QUOTE_REQUIRED')
    if tier=='EXOTIC':flags.append('EXOTIC_QUOTE_REQUIRED')
    if (r['make'],r['model']) in {('BMW','i8'),('Audi','R8'),('Ford','GT'),('Honda','NSX'),('Acura','NSX'),('Nissan','GT-R'),('Mercedes-Benz','SLR McLaren')}:flags.append('EXOTIC_QUOTE_REQUIRED')
    if r['make']=='Porsche' and '911' in r['model'] and int(r['gen_year_start'])<1998:flags.append('COOLING_ARCHITECTURE_UNCONFIRMED')
    body=''
    for token,label in [('estate','ESTATE'),('sedan','SEDAN'),('cabrio','CABRIO'),('convertible','CABRIO'),('coupe','COUPE'),('5 doors','HATCHBACK'),('3 doors','HATCHBACK'),('pickup','PICKUP'),('minivan','VAN')]:
        if token in r['generation'].lower():body=label;break
    layout='BOXER' if re.match(r'(?i)^(H\d|F\d|flat|boxer)',raw_cyl) else 'V' if re.match(r'(?i)^V\d',raw_cyl) else 'OTHER'
    return dict(variant_code=code,source_row=number,make=r['make'],model=r['model'],generation=r['generation'],engine_label=r['engine_label'],year_from=int(r['gen_year_start']),year_to=int(r['gen_year_end']) if r['gen_year_end'] else '',fuel_class=fc,is_hybrid=hybrid,displacement_cc=int(cc) or '',cylinders=int(cyl) or '',cylinder_layout=layout,power_hp=int(hp) or '',torque_nm=str(torque) if torque else '',mass_kg=str(mass) if mass else '',transmission_class=tc,turbo=turbo,front_brake=brake_type(specs.get('BRAKES SPECS / Front','')),rear_brake=brake_type(specs.get('BRAKES SPECS / Rear','')),fuel_system_raw=fuel_sys,front_brake_raw=specs.get('BRAKES SPECS / Front',''),rear_brake_raw=specs.get('BRAKES SPECS / Rear',''),body_inferred=body,tier=tier,flags=';'.join(flags))


def base_applicability(t: dict, w: dict) -> tuple[str,str]:
    c=w['code'];fc=t['fuel_class'];ice=fc in {'PETROL','DIESEL','COMBUSTION_UNSPECIFIED'};hy=t['is_hybrid'];flags=t['flags'];tc=t['transmission_class']
    state,reason='IMPORTABLE','SUPPORTED_PLANNING_SCENARIO'
    def cond(s):return 'CONDITIONAL',s
    def no(s):return 'NOT_APPLICABLE',s
    if c in ENGINE_JOBS and not ice:
        return cond('COMBUSTION_SYSTEM_UNKNOWN') if fc=='UNKNOWN' else no('NO_COMBUSTION_ENGINE')
    if c in {'FUEL_FILTER','GLOW_PLUGS','DIESEL_INJECTOR','DPF_CLEAN','DPF_REPLACEMENT'} and fc!='DIESEL':return cond('COMBUSTION_FUEL_UNSPECIFIED') if fc in {'COMBUSTION_UNSPECIFIED','UNKNOWN'} else no('NOT_DIESEL_WORK')
    if c in {'SPARK_PLUGS','IGNITION_COIL'} and fc!='PETROL':return cond('COMBUSTION_FUEL_UNSPECIFIED') if fc in {'COMBUSTION_UNSPECIFIED','UNKNOWN'} else no('NOT_SPARK_IGNITION')
    if c=='LAMBDA_SENSOR' and fc=='DIESEL':return cond('EXACT_EXHAUST_SENSOR_NOT_IDENTIFIED')
    if c=='TIMING_BELT_PUMP':state,reason=cond('BELT_VS_CHAIN_AND_PUMP_DRIVE_NOT_IN_SOURCE')
    elif c in {'AUX_BELT','STARTER','ALTERNATOR','WATER_PUMP'} and hy:state,reason=cond('HYBRID_ARCHITECTURE_REQUIRES_CONFIRMATION')
    elif c=='TURBO' and t['turbo'] not in {'YES'}:
        if t['turbo']=='NO':return no('SOURCE_NATURALLY_ASPIRATED_OR_SUPERCHARGED')
        state,reason=cond('TURBO_NOT_CONFIRMED_BY_RAW_FUEL_SYSTEM')
    elif c=='EGR_VALVE':state,reason=cond('EGR_HARDWARE_NOT_IN_SOURCE')
    elif c in {'DPF_CLEAN','DPF_REPLACEMENT'}:state,reason=cond('DPF_PRESENCE_NOT_IN_SOURCE')
    elif c=='MANUAL_GEARBOX_OIL' and tc!='MANUAL':
        return cond('GEARBOX_TYPE_UNKNOWN') if tc=='UNKNOWN' else no('NOT_MANUAL_GEARBOX')
    elif c=='AUTO_GEARBOX_OIL':
        if tc in {'MANUAL','EV_REDUCTION'}:return no('NOT_AUTOMATIC_FLUID_SERVICE')
        state,reason=cond('EXACT_GEARBOX_FLUID_FILTER_AND_QUANTITY_REQUIRED')
    elif c in {'CLUTCH_KIT','CLUTCH_DMF'}:
        if tc!='MANUAL':return cond('MANUAL_CLUTCH_NOT_CONFIRMED') if tc=='UNKNOWN' else no('NOT_CONVENTIONAL_MANUAL_CLUTCH')
        if c=='CLUTCH_DMF':state,reason=cond('DUAL_MASS_FLYWHEEL_NOT_IN_SOURCE')
        if hy:state,reason=cond('HYBRID_TRANSMISSION_ARCHITECTURE')
    elif c in {'FRONT_BRAKES','FRONT_DISCS_PADS','REAR_BRAKES','REAR_DISCS_PADS','REAR_DRUM_SHOES'}:
        brake=t['front_brake'] if c.startswith('FRONT') else t['rear_brake']
        target='DRUM' if c=='REAR_DRUM_SHOES' else 'DISC'
        if brake=='CERAMIC':return 'QUOTE_REQUIRED','CERAMIC_BRAKE_NOT_COVERED_BY_STEEL_PRICE'
        if brake=='UNKNOWN':state,reason=cond('BRAKE_HARDWARE_UNKNOWN')
        elif brake!=target:return no('SOURCE_BRAKE_HARDWARE_MISMATCH')
    elif c in {'AC_R134A','AC_R1234YF'}:state,reason=cond('REFRIGERANT_AND_FILL_MASS_NOT_IN_SOURCE')
    elif c=='AC_COMPRESSOR' and (hy or fc in {'BEV','FCEV'}):state,reason=cond('ELECTRIC_COMPRESSOR_AND_REFRIGERANT_NOT_IDENTIFIED')
    elif c=='AC_COMPRESSOR':reason='ASSUMES_FACTORY_AC_AND_CONVENTIONAL_COMPRESSOR'
    elif c=='RADIATOR_FAN' and not ice:state,reason=cond('EV_THERMAL_FAN_ASSEMBLY_NOT_IDENTIFIED')
    elif c=='BATTERY' and (hy or fc in {'BEV','FCEV'}):state,reason=cond('LOW_VOLTAGE_CHEMISTRY_AND_VOLTAGE_UNCONFIRMED')
    elif c in {'STARTER','ALTERNATOR'} and not ice:return no('NO_CONVENTIONAL_ICE_STARTER_OR_ALTERNATOR')
    elif c=='CABIN_FILTER':reason='ASSUMES_STANDARD_FACTORY_CABIN_FILTER_NOT_SPECIAL_HEPA'
    elif c in {'FRONT_SHOCKS','REAR_SHOCKS'}:
        if t['tier'] in {'PREMIUM','PERFORMANCE','EXOTIC'}:state,reason=cond('AIR_OR_ADAPTIVE_SUSPENSION_NOT_EXCLUDED')
        else:reason='ASSUMES_CONVENTIONAL_PASSIVE_SUSPENSION'
    elif c=='DIAGNOSIS' and (t['year_from']<2005 or fc in {'BEV','FCEV'}):state,reason=cond('STANDARD_OBD_ACCESS_OR_PROTOCOL_UNCONFIRMED')
    if c in {'COOLANT','WATER_PUMP','THERMOSTAT'}:
        if 'COOLING_ARCHITECTURE_UNCONFIRMED' in flags:state,reason=cond('AIR_VS_LIQUID_COOLING_REQUIRES_CONFIRMATION')
        else:reason=reason if state!='IMPORTABLE' else 'ASSUMES_CONVENTIONAL_LIQUID_COOLED_ENGINE'
    if any(s in flags for s in ['LEGACY_QUOTE_REQUIRED','EXOTIC_QUOTE_REQUIRED']):return 'QUOTE_REQUIRED','OUTSIDE_ORDINARY_GARAGE_MODEL_SCOPE'
    if c in ENGINE_JOBS and any(s in flags for s in ['DISPLACEMENT_LABEL_CONFLICT','POWER_LABEL_CONFLICT','HYBRID_FUEL_UNCERTAIN']):return 'QUOTE_REQUIRED','CONTRADICTORY_ENGINE_SPECIFICATIONS'
    if c in {'GLOW_PLUGS','SPARK_PLUGS'} and not t['cylinders']:return 'QUOTE_REQUIRED','PLUG_QUANTITY_UNKNOWN'
    return state,reason


def applicability(t: dict, w: dict) -> tuple[str,str]:
    state,reason=base_applicability(t,w)
    if state=='NOT_APPLICABLE':return state,reason
    if any(s in t['flags'] for s in ['LEGACY_QUOTE_REQUIRED','EXOTIC_QUOTE_REQUIRED']):
        return 'QUOTE_REQUIRED','OUTSIDE_ORDINARY_GARAGE_MODEL_SCOPE'
    if w['code'] in ENGINE_JOBS and any(s in t['flags'] for s in ['DISPLACEMENT_LABEL_CONFLICT','POWER_LABEL_CONFLICT','HYBRID_FUEL_UNCERTAIN']):
        return 'QUOTE_REQUIRED','CONTRADICTORY_ENGINE_SPECIFICATIONS'
    return state,reason


def price_factors(t: dict, model: dict):
    tier=t['tier'];pm=D(model['tier_parts'][tier]);hm=D(model['tier_hours'][tier])
    litres=dec(t['displacement_cc'],'1600')/1000 if t['displacement_cc'] else D('1.6')
    cyl=dec(t['cylinders'],'4') if t['cylinders'] else D(4)
    mass=dec(t['mass_kg'],'1400') if t['mass_kg'] else D(1400)
    hp=dec(t['power_hp'],'120') if t['power_hp'] else D(120)
    ef=clamp(D('.60')+litres*D('.25'),'.78','2.5')
    mf=clamp((mass/1400).sqrt(),'.80','1.60')
    access=hm*(1+max(D(0),cyl-4)*D('.045'))
    return pm,hm,litres,cyl,mass,hp,ef,mf,access


def estimate(t: dict, w: dict, model: dict) -> dict:
    """Return ONE rounded all-inclusive number; components exist only in the audit."""
    parts=D(w['base_parts']);hours=D(w['base_hours']);misc=D(w['consumables']);fam=w['family']
    pm,hm,litres,cyl,mass,hp,ef,mf,access=t.get('_price_factors') or price_factors(t,model)
    if t['cylinder_layout']=='BOXER' and fam in {'engine','plugs','timing','glow'}:access*=D('1.25')
    if fam=='oil':
        # Deliberately named assumed capacity: never write this as an engine specification.
        assumed_litres=clamp(D('2.8')+D('.85')*litres+max(D(0),cyl-4)*D('.18'),'3','12')
        parts=assumed_litres*D('12')*(1+(pm-1)*D('.35'))+15*pm
        hours*=access
    elif fam in {'plugs','glow'}:
        parts*=cyl*pm
        hours*=max(D('.75'),cyl/4)*access
    elif fam=='brake':
        power_factor=clamp(1+max(D(0),hp-180)/900,'1','1.5')
        parts*=pm*mf*power_factor;hours*=hm
    elif fam=='chassis':parts*=pm*mf;hours*=hm
    elif fam=='fluid':parts*=clamp(mf,'.90','1.2');hours*=1+(hm-1)*D('.4')
    elif fam=='cabin':parts*=pm;hours*=hm
    elif fam=='fixed':pass
    elif fam=='battery':
        parts*=pm*clamp(D('.8')+litres*D('.12'),'.85','1.6')
        # Start-stop fitment cannot be established, so no claim of exact AGM/EFB type.
        if t['year_from']>=2013:parts*=D('1.18');hours+=D('.15')
    elif fam in {'clutch','gear','auto'}:
        tq=dec(t['torque_nm'],'220') if t['torque_nm'] else D(220)
        parts*=pm*clamp((tq/250).sqrt(),'.85','1.9');hours*=access
        if t['transmission_class']=='DCT':parts*=D('1.10')
    else:
        parts*=pm*ef;hours*=access
        if fam=='timing':hours*=clamp(D('.7')+litres*D('.2'),'.9','1.8')
        if fam=='coolant':parts*=clamp(mf,'.9','1.4')
    total=parts+hours*D(model['labour_hour_gross_eur'])+misc
    return dict(estimated_price=f'{round_ten(total):.2f}',parts_model_eur=f'{parts:.2f}',hours_model=f'{hours:.3f}',consumables_model_eur=f'{misc:.2f}',raw_total_model_eur=f'{total:.2f}')


def intervals(t: dict, code: str) -> tuple[Any,Any,str]:
    # Model-scope source, not VIN verification. No coolant first-change value promoted to a repeating interval.
    if t['make']=='Toyota' and t['model']=='C-HR' and t['generation'] in {'C-HR (2016)','C-HR (2019)'} and t['is_hybrid'] and t['fuel_class'] in {'PETROL','COMBUSTION_UNSPECIFIED'}:
        vals={'OIL_SERVICE':(15000,12),'BRAKE_FLUID':(30000,24),'AIR_FILTER':(60000,48),'CABIN_FILTER':(30000,24)}
        if code in vals:
            return *vals[code],('OEM_MODEL_PLAN | Toyota C-HR Hybrid, HR plan 2021; generation-level match, verify VIN/market. '+
                'https://www.toyotaadria.com/hr/pdf/cjenici_servis/2021/plan-servis-CHR-CRO-2021.pdf')
    if t['make']=='Tesla' and t['model']=='Model 3' and t['generation']=='Model 3 (2023)' and code=='CABIN_FILTER':
        return '',24,('OEM_MODEL_PLAN | Tesla Model 3 EU current manual, 2023-generation match; verify applicability. '+
                     'https://www.tesla.com/ownersmanual/model3/en_eu/GUID-E95DAAD9-646E-4249-9930-B109ED7B1D91.html')
    return '','',''


def writer(stack, path, fields):
    if str(path).endswith('.gz'):
        raw=stack.enter_context(path.open('wb'))
        z=stack.enter_context(gzip.GzipFile(filename='',fileobj=raw,mode='wb',mtime=0))
        f=stack.enter_context(io.TextIOWrapper(z,encoding='utf-8',newline=''))
    else:f=stack.enter_context(path.open('w',encoding='utf-8',newline=''))
    w=csv.DictWriter(f,fieldnames=fields,extrasaction='raise',lineterminator='\n');w.writeheader();return w


def build(input_zip: Path, output: Path, config_dir: Path) -> dict:
    output.mkdir(parents=True,exist_ok=True)
    cfg=json.loads((config_dir/'model.json').read_text(encoding='utf-8'))
    works=json.loads((config_dir/'works.json').read_text(encoding='utf-8'))
    with zipfile.ZipFile(input_zip) as z:
        def member(suffix):
            names=[n for n in z.namelist() if n.endswith('/'+suffix)]
            if len(names)!=1:raise ValueError('Missing or ambiguous ZIP member: '+suffix)
            info=z.getinfo(names[0])
            if info.file_size>400_000_000:raise ValueError('Unexpected oversized ZIP member')
            return z.read(names[0])
        raw_csv=member('data/csv/engines.csv');raw_json=member('data/json/all.json')
        archive_commit=z.comment.decode('ascii',errors='replace')
    groups=json.loads(raw_json);flat=list(flatten(groups))
    rows=list(csv.DictReader(io.StringIO(raw_csv.decode('utf-8-sig'))))
    if len(rows)!=len(flat):raise ValueError('CSV/JSON engine count mismatch')
    hierarchy=Counter();missing_hierarchy=[];all_make_names=set();all_model_names=set()
    for g in groups:
        for ma in g['makes']:
            hierarchy['makes']+=1;all_make_names.add(ma['name'])
            for mo in ma['models']:
                hierarchy['models']+=1;all_model_names.add((ma['name'],mo['name']))
                if not mo['generations']:missing_hierarchy.append([ma['name'],mo['name'],'','NO_GENERATIONS'])
                for ge in mo['generations']:
                    hierarchy['generations']+=1
                    if not ge['engines']:missing_hierarchy.append([ma['name'],mo['name'],ge['name'],'NO_ENGINE_VARIANT'])
                    hierarchy['engines']+=len(ge['engines'])
                    hierarchy['raw_specs']+=sum(len(e['specs']) for e in ge['engines'])
    counts=Counter();by_make={};by_work={};flag_counts=Counter();missing=Counter();seen=set();makes=set();models=set();gens=set();interval_count=0
    with ExitStack() as stack:
        variants=writer(stack,output/'vehicle_variants.csv',OUT)
        trw=writer(stack,output/'vehicle_traits.csv',TRAIT_FIELDS)
        wide_fields=['variant_code','make','model','generation','engine_label','year_from','year_to','price_basis']+[w['code'] for w in works]
        wide_writer=writer(stack,output/'prices_by_vehicle.csv',wide_fields)
        rw=writer(stack,output/'vehicle_work_rules.csv.gz',RULE_FIELDS)
        aw=writer(stack,output/'all_pair_decisions.csv.gz',AUDIT_FIELDS)
        cw=writer(stack,output/'conditional_estimates.csv.gz',AUDIT_FIELDS)
        iw=writer(stack,output/'referenced_intervals.csv',['variant_code','work_code','interval_km','interval_months','interval_source'])
        issues=writer(stack,output/'quality_issues.csv',['source_row','variant_code','flags'])
        rejects=writer(stack,output/'rejected.csv',['source_row','reason'])
        adjustments=writer(stack,output/'normalization_adjustments.csv',['source_row','variant_code','field','original_value','stored_value','reason'])
        duplicates=writer(stack,output/'exact_duplicates.csv',['source_row','variant_code'])
        for number,(r,j) in enumerate(zip(rows,flat),2):
            group,ma,mo,ge,e=j
            if (r['make'],r['model'],r['generation'],r['engine_label'])!=(ma,mo,ge['name'],e['label']):raise ValueError(f'CSV/JSON identity mismatch at {number}')
            # Raw specs are joined only after exact row-order identity parity has been checked.
            counts['input_rows']+=1
            for f in REQUIRED:
                if not clean(r.get(f)):missing[f]+=1
            try:n,changes=normalize_source(r)
            except ValueError as ex:counts['rejected']+=1;rejects.writerow(dict(source_row=number,reason=str(ex)));continue
            if n['code'] in seen:
                counts['duplicates']+=1;duplicates.writerow(dict(source_row=number,variant_code=n['code']));continue
            for field,before,after,reason in changes:
                adjustments.writerow(dict(source_row=number,variant_code=n['code'],field=field,original_value=before,stored_value=after,reason=reason))
                counts['normalization_adjustments']+=1
            seen.add(n['code']);variants.writerow(n);counts['accepted_variants']+=1
            makes.add(ma);models.add((ma,mo));gens.add((ma,mo,ge['name'],ge['yearStart'],ge['yearEnd']))
            t=traits(r,e,cfg,n['code'],number)
            if changes:t['flags']=';'.join(filter(None,[t['flags']]+[x[3] for x in changes]))
            trw.writerow(t)
            if t['flags']:
                issues.writerow(dict(source_row=number,variant_code=n['code'],flags=t['flags']));flag_counts.update(t['flags'].split(';'))
            bm=by_make.setdefault(ma,Counter());bm['variants']+=1;has_price=False
            t['_price_factors']=price_factors(t,cfg)
            wide=dict.fromkeys(wide_fields,'')
            wide.update({f:t[f] for f in ['variant_code','make','model','generation','engine_label','year_from','year_to']})
            wide['price_basis']='MODELLED_NOT_VERIFIED; blank means not available in automatic import'
            for w in works:
                c=w['code'];state,reason=applicability(t,w);bw=by_work.setdefault(c,Counter());bw[state]+=1;bm[state]+=1;counts[state]+=1
                numeric=estimate(t,w,cfg) if state in {'IMPORTABLE','CONDITIONAL'} else dict.fromkeys(['estimated_price','parts_model_eur','hours_model','consumables_model_eur','raw_total_model_eur'],'')
                confidence='LOW' if state=='IMPORTABLE' else 'VERY_LOW' if state=='CONDITIONAL' else 'NOT_ESTIMATED'
                if t['flags'] and confidence=='LOW':confidence='VERY_LOW'
                audit=dict(variant_code=n['code'],work_code=c,**numeric,state=state,reason=reason,confidence=confidence,source_ids='HR_AKR_2026;MODEL_ASSUMPTIONS' if numeric['estimated_price'] else '')
                aw.writerow(audit)
                if state=='CONDITIONAL':cw.writerow(audit)
                if state!='IMPORTABLE':continue
                has_price=True;wide[c]=str(int(D(numeric['estimated_price'])))
                km,months,src=intervals(t,c)
                if km or months:
                    interval_count+=1;iw.writerow(dict(variant_code=n['code'],work_code=c,interval_km=km,interval_months=months,interval_source=src))
                note=(f"{cfg['model_version']} | MODELLED; {cfg['as_of']}; gross EUR incl. parts/labour; nearest 10 EUR; "
                      f"confidence={confidence}; not a market mean or quote. {reason}. Scope: {w['scope']}. "
                      'Labour anchor HR_AKR_2026; parts/hours/multipliers are assumptions. Full provenance: vehicle_traits.csv + config/model.json.')
                if len(note)>1000:raise ValueError('estimate_note too long')
                rw.writerow(dict(variant_code=n['code'],work_code=c,interval_km=km,interval_months=months,estimated_price=numeric['estimated_price'],interval_source=src,estimate_note=note,price_basis='MODELLED',applicability='IMPORTABLE',review_status='MODELLED_NOT_INDIVIDUALLY_VERIFIED'))
            wide_writer.writerow(wide)
            if has_price:bm['variants_with_price']+=1;counts['variants_with_price']+=1
    # Small metadata tables are intentionally readable without opening million-row files.
    with ExitStack() as stack:
        ww=writer(stack,output/'work_definitions.csv',['code','name','category','default_estimated_price','estimate_note'])
        for w in works:ww.writerow(dict(code=w['code'],name=w['name'],category=w['category'],default_estimated_price='',estimate_note=f"AC-WORK-1.0 | Scope: {w['scope']}. Global price deliberately NULL; use variant-specific rules."))
        ww=writer(stack,output/'coverage_by_make.csv',['make','variants','variants_with_price','IMPORTABLE','CONDITIONAL','NOT_APPLICABLE','QUOTE_REQUIRED'])
        for make,cc in sorted(by_make.items()):ww.writerow(dict(make=make,**{f:cc[f] for f in ww.fieldnames if f!='make'}))
        ww=writer(stack,output/'coverage_by_work.csv',['work_code','IMPORTABLE','CONDITIONAL','NOT_APPLICABLE','QUOTE_REQUIRED'])
        for code,cc in sorted(by_work.items()):ww.writerow(dict(work_code=code,**{f:cc[f] for f in ww.fieldnames if f!='work_code'}))
        ww=writer(stack,output/'hierarchy_without_engine.csv',['make','model','generation','reason'])
        for row in missing_hierarchy:ww.writerow(dict(zip(ww.fieldnames,row)))
        for make in sorted(all_make_names-makes):
            if not any(x[0]==make for x in missing_hierarchy):ww.writerow(dict(make=make,model='',generation='',reason='NO_ENGINE_VARIANT'))
    with ExitStack() as stack:
        dw=writer(stack,output/'diagnostic_rules.csv',['code','work_code','phrase','weight','active','basis'])
        diagnostic_data=json.loads((config_dir/'diagnostics.json').read_text(encoding='utf-8'))
        for item in diagnostic_data:dw.writerow(item)
        sw=writer(stack,output/'source_register.csv',['id','type','url','date','scope','use','confidence'])
        for item in json.loads((config_dir/'sources.json').read_text(encoding='utf-8')):sw.writerow(item)
    manifest=dict(diagnostic_rule_count=len(diagnostic_data),model_version=cfg['model_version'],as_of=cfg['as_of'],source_zip_sha256=sha(input_zip.read_bytes()),source_csv_sha256=sha(raw_csv),source_json_sha256=sha(raw_json),archive_commit=archive_commit if re.fullmatch('[0-9a-f]{40}',archive_commit) else None,source_url='https://github.com/gor3a/vehicle-makes-models',counts=dict(counts),hierarchy=dict(hierarchy),accepted_makes=len(makes),accepted_models=len(models),accepted_generation_ranges=len(gens),work_count=len(works),referenced_interval_rows=interval_count,missing_source_fields=dict(missing),quality_flags=dict(flag_counts),model_accuracy='UNMEASURED - no independent validation sample or error metric',price_basis='scenario model, not observed average',model_sha256=sha((config_dir/'model.json').read_bytes()),works_sha256=sha((config_dir/'works.json').read_bytes()),files={p.name:sha(p.read_bytes()) for p in sorted(output.iterdir()) if p.is_file() and p.name!='build_manifest.json'})
    (output/'build_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    return manifest


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--input',type=Path,required=True,help='GitHub ZIP uploaded by the user')
    p.add_argument('--output',type=Path,default=ROOT/'data')
    p.add_argument('--config',type=Path,default=ROOT/'config')
    a=p.parse_args()
    try:
        m=build(a.input,a.output,a.config);print(json.dumps({k:m[k] for k in ['counts','accepted_makes','accepted_models','work_count','referenced_interval_rows']},indent=2))
        return 0
    except (OSError,ValueError,KeyError,zipfile.BadZipFile) as ex:print('Build failed: '+str(ex),file=sys.stderr);return 1
if __name__=='__main__':raise SystemExit(main())
