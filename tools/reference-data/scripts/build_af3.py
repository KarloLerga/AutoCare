#!/usr/bin/env python3
"""Reproducible AF3 enrichment. Offline model estimates, explicit unknowns, source-scoped schedules."""
from __future__ import annotations
import argparse,csv,hashlib,json,shutil
from pathlib import Path
from contextlib import ExitStack
from collections import Counter,defaultdict
from decimal import Decimal as D
from build_seed import applicability as legacy_applicability,estimate,price_factors,writer
ROOT=Path(__file__).resolve().parents[1]
FALLBACK='/images/vehicles/fallback-car.jpg'
EMPTY_PRICE=dict.fromkeys(['estimated_price','parts_model_eur','hours_model','consumables_model_eur','raw_total_model_eur'],'')
AUDIT=['variant_code','work_code','state','reason','estimated_price','schedule_kind','parts_model_eur','hours_model','consumables_model_eur','raw_total_model_eur','price_basis']
RULE=['variant_code','work_code','interval_km','interval_months','estimated_price','interval_source','estimate_note','schedule_kind','price_basis','applicability','review_status']
INTERVAL=['variant_code','work_code','interval_km','interval_months','interval_source']
TESLA3='https://www.tesla.com/ownersmanual/model3/en_eu/GUID-E95DAAD9-646E-4249-9930-B109ED7B1D91.html'
TESLAY='https://www.tesla.com/ownersmanual/2020_2024_modely/en_eu/GUID-E95DAAD9-646E-4249-9930-B109ED7B1D91.html'
KIA='https://eu-www.kia.com/content/dam/kwcms/kme/de/de/assets/contents/service/service-wartung/Kia_Wartungsintervalle-Fahrzeuge_Stand-2025-09.pdf'
TOYOTA='https://www.toyotaadria.com/hr/pdf/cjenici_servis/2021/plan-servis-{}-CRO-2021.pdf'

def load_traits(row):
    t=dict(row);t['is_hybrid']=str(t['is_hybrid']).lower() in {'true','1'}
    for k in ['year_from','year_to','displacement_cc','cylinders','power_hp']:t[k]=int(t[k]) if t[k] else ''
    return t

def decision(t,w):
    group=w.get('applicability_group')
    if group is None:return legacy_applicability(t,w)
    fc=t['fuel_class'];ice=fc in {'PETROL','DIESEL','COMBUSTION_UNSPECIFIED'}
    bev=fc=='BEV';hy=t['is_hybrid'];label=t['engine_label'].lower()
    mild='mhev' in label or 'mild' in label
    plugin=bev or (hy and ('phev' in label or 'plug-in' in label or 'plug in' in label))
    no=lambda why:('NOT_APPLICABLE',why)
    cond=lambda why:('CONDITIONAL',why)
    if group=='MANUAL_ONLY':return no('MANUAL_HISTORY_FALLBACK_NOT_AUTOMATIC_RECOMMENDATION')
    if group.startswith('ICE') and not ice:return no('NO_CONFIRMED_COMBUSTION_ENGINE')
    if group.startswith('PETROL') and fc!='PETROL':return no('NO_CONFIRMED_PETROL_ENGINE')
    if group.startswith('DIESEL') and fc!='DIESEL':return no('NO_CONFIRMED_DIESEL_ENGINE')
    if group in {'BEV','EV_CONDITIONAL'} and not bev:return no('NOT_BATTERY_ELECTRIC')
    if group=='ELECTRIFIED' and not(bev or hy):return no('NO_TRACTION_BATTERY_CONFIRMED')
    if group=='ELECTRIFIED' and mild:return cond('MILD_HYBRID_VOLTAGE_AND_ASSEMBLY_NOT_CONFIRMED')
    if group=='PLUG_IN' and not plugin:return cond('HYBRID_NOT_CONFIRMED_PLUG_IN') if hy else no('NO_PLUG_IN_CHARGING')
    if group=='HYBRID_CONDITIONAL' and not hy:return no('NO_HYBRID_BATTERY_AIR_FILTER')
    if group=='TURBO_CONDITIONAL' and (not ice or t['turbo']=='NO'):return no('NO_CONFIRMED_TURBO')
    if group=='FRONT_DISC':
        if t['front_brake']=='DRUM':return no('FRONT_BRAKE_IS_DRUM')
        if t['front_brake']!='DISC':return cond('STANDARD_FRONT_DISC_CALIPER_NOT_CONFIRMED')
    if group=='REAR_DISC_CONDITIONAL' and t['rear_brake']=='DRUM':return no('REAR_BRAKE_IS_DRUM')
    if group=='PASSIVE_SUSPENSION' and t['tier'] in {'PREMIUM','PERFORMANCE','EXOTIC'}:return cond('PASSIVE_SPRING_LAYOUT_NOT_CONFIRMED')
    if group=='ICE_LIQUID' and 'COOLING_ARCHITECTURE_UNCONFIRMED' in t['flags']:return cond('LIQUID_COOLING_NOT_CONFIRMED')
    if group.endswith('CONDITIONAL'):return cond('EXACT_HARDWARE_OR_SERVICEABILITY_REQUIRES_REVIEW')
    if w.get('quote_only'):return 'QUOTE_REQUIRED','APPLICABLE_SCOPE_BUT_PART_ID_OR_SCOPE_PRICE_MISSING'
    if any(flag in t['flags'] for flag in ['LEGACY_QUOTE_REQUIRED','EXOTIC_QUOTE_REQUIRED']):return 'QUOTE_REQUIRED','OUTSIDE_MODEL_PRICING_SCOPE'
    if group.startswith('ICE') and any(flag in t['flags'] for flag in ['POWER_LABEL_CONFLICT','DISPLACEMENT_LABEL_CONFLICT']):return 'QUOTE_REQUIRED','CONFLICTING_ENGINE_DATA'
    return 'IMPORTABLE','MODELLED_SCOPE_ASSUMPTION_NOT_VIN_VERIFICATION'

def schedule_kind(t,w):
    if w['category']=='REPAIR':return 'CONDITION_BASED'
    if t['make']=='Tesla' and w['code'] in {'BRAKE_FLUID','HV_COOLANT'}:return 'CONDITION_BASED'
    if w['code'] in {'TYRE_PRESSURE_CHECK','TYRE_MOUNT_BALANCE','TYRE_REPLACE_SET','WHEEL_ALIGNMENT','AC_DISINFECTION','BATTERY_TEST','TRACTION_BATTERY_TEST','EV_DRIVE_FLUID','HV_COOLANT'}:return 'CONDITION_BASED'
    return 'UNKNOWN'

def referenced(t):
    out=[]
    def put(code,km,months,source):out.append(dict(variant_code=t['variant_code'],work_code=code,interval_km=km or '',interval_months=months or '',interval_source=source))
    toyota={('C-HR','C-HR (2016)'):'CHR',('C-HR','C-HR (2019)'):'CHR',('Yaris','Yaris 5 Doors (2019)'):'Yaris',('RAV4','RAV4 5 Doors (2018)'):'RAV4'}
    source=toyota.get((t['model'],t['generation'])) if t['make']=='Toyota' and t['is_hybrid'] and t['fuel_class'] in {'PETROL','COMBUSTION_UNSPECIFIED'} else None
    if source:
        if any(x in t['engine_label'].lower() for x in ['plug','phev','prime']):return out
        for code,km,mo in [('OIL_SERVICE',15000,12),('BRAKE_FLUID',30000,24),('AIR_FILTER',60000,48),('CABIN_FILTER',30000,24)]:
            put(code,km,mo,'OEM_MODEL_REFERENCE | HR HEV plan 2021; generation match is not a VIN confirmation. '+TOYOTA.format(source))
    if t['make']=='Tesla' and t['fuel_class']=='BEV':
        if (t['model'],t['generation'])==('Model 3','Model 3 (2023)'):
            for code,mo in [('CABIN_FILTER',24),('BRAKE_FLUID_CHECK',48),('WIPER_REPLACE',12)]:put(code,None,mo,'OEM_MODEL_REFERENCE | Model 3 current EU manual; normal use; brake CHECK, not replacement. '+TESLA3)
    return out

def market_candidates(t):
    if t['make']=='Tesla' and t['model']=='Model Y' and t['generation']=='Model Y (2019)':
        return [dict(variant_code=t['variant_code'],work_code=code,interval_km='',interval_months=mo,interval_source='OEM_MODEL_YEAR_REVIEW | 2020-2024 manual; source generation starts 2019 so verify actual year/VIN before activation. '+TESLAY,review_status='DRAFT_MARKET_AND_MODEL_YEAR_REVIEW',reviewer='',reviewed_on='') for code,mo in [('CABIN_FILTER',24),('BRAKE_FLUID_CHECK',48),('WIPER_REPLACE',12)]]
    if t['make']!='Kia':return []
    fc=t['fuel_class'];m=t['model'];y=t['year_from'];label=t['engine_label'].lower();out=[]
    def add(code,km,mo,scope):out.append(dict(variant_code=t['variant_code'],work_code=code,interval_km=km or '',interval_months=mo,interval_source='OEM_MARKET_REFERENCE_DE | '+scope+'; source model-year/VIN is not the dataset production-year. '+KIA,review_status='DRAFT_MARKET_AND_MODEL_YEAR_REVIEW',reviewer='',reviewed_on=''))
    add('BRAKE_FLUID',None,24,'German-registered Kia; all models in table; confirm HR/VIN plan')
    if m.startswith('Picanto') and fc=='PETROL' and 2011<=y<=2023:add('SCHEDULED_INSPECTION',15000,12,'Picanto TA/JA petrol; MY2012-2025')
    elif m in {'EV6','EV9'} and fc=='BEV':add('SCHEDULED_INSPECTION',30000,24,m+' electric CV/MV, DE schedule')
    elif m=='Niro' and t['is_hybrid'] and 2016<=y<=2019:add('SCHEDULED_INSPECTION',15000,12,'Niro DE HEV/PHEV MY2016-2022')
    elif m=='Niro' and fc=='BEV' and 2018<=y<=2020:add('SCHEDULED_INSPECTION',15000,12,'e-Niro DE EV MY2019-2022')
    elif m.startswith('Stonic') and fc=='PETROL':add('SCHEDULED_INSPECTION',15000,12,'Stonic YB CUV petrol MY2018-2025')
    elif m.startswith('Stonic') and fc=='DIESEL':add('SCHEDULED_INSPECTION',30000,24,'Stonic diesel MY2018-2020 only')
    elif m=='Ceed' and fc=='PETROL' and 2018<=y<=2021 and ('t-gdi' in label or 'tgdi' in label):add('SCHEDULED_INSPECTION',15000,12,'Ceed CD T-GDI MY2019-2025; match displacement/year')
    elif m=='Sportage' and fc=='PETROL' and 2015<=y<=2018 and ('t-gdi' in label or 'tgdi' in label):add('SCHEDULED_INSPECTION',15000,12,'Sportage QL 1.6 T-GDI MY2016-2021')
    elif m=='Sportage' and fc=='DIESEL' and 2015<=y<=2018:add('SCHEDULED_INSPECTION',30000,24,'Sportage QL diesel MY2016-2021')
    elif m=='Sportage' and fc=='PETROL' and 2021<=y<=2022:add('SCHEDULED_INSPECTION',15000,12,'Sportage NQ5 petrol/HEV MY2022-2025')
    elif m=='Sportage' and fc=='DIESEL' and 2021<=y<=2022:add('SCHEDULED_INSPECTION',20000,24,'Sportage NQ5 diesel MY2022-2024')
    return out

def digest(path):
    h=hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
    return h.hexdigest()

def checksums(path):
    names=['vehicle_variants.csv','work_definitions.csv','vehicle_work_rules.csv.gz','referenced_intervals.csv','diagnostic_rules.csv']
    (path/'checksums.sha256').write_text(''.join(digest(path/n)+'  '+n+'\n' for n in names),encoding='ascii')

def build(output):
    output.mkdir(parents=True,exist_ok=True)
    model=json.loads((ROOT/'config/model.json').read_text());works=json.loads((ROOT/'config/works.json').read_text())
    with (ROOT/'base-input/vehicle_variants.csv').open(encoding='utf-8',newline='') as f:variants=list(csv.DictReader(f));variant_fields=list(variants[0])
    with (ROOT/'base-input/vehicle_traits.csv').open(encoding='utf-8',newline='') as f:traits=[load_traits(x) for x in csv.DictReader(f)]
    assert len(traits)==len(variants) and all(a['code']==b['variant_code'] for a,b in zip(variants,traits))
    stats=Counter();bywork=defaultdict(Counter);bymake=defaultdict(Counter);introws=[];market=[];images={};exceptions=[];samplecodes=set()
    requests=[('Renault','Megane 5 Doors','DIESEL',None),('Toyota','C-HR',None,'C-HR (2019)'),('Toyota','Yaris',None,'Yaris 5 Doors (2019)'),('Tesla','Model 3','BEV','Model 3 (2023)'),('Tesla','Model Y','BEV',None),('Kia','Picanto','PETROL',None),('Kia','EV6','BEV',None),('Volkswagen','Golf','DIESEL',None),('BMW','3 Series','PETROL',None)]
    for make,modelname,fuel,generation in requests:
        hit=next((t for t in traits if t['make']==make and modelname.lower() in t['model'].lower() and (fuel is None or t['fuel_class']==fuel) and (generation is None or t['generation']==generation) and t['year_from']>=2015 and (make!='Toyota' or t['is_hybrid'])),None)
        if hit:samplecodes.add(hit['variant_code'])
    sample=output/'sample';sample.mkdir(exist_ok=True)
    with ExitStack() as stack:
        vw=writer(stack,output/'vehicle_variants.csv',variant_fields);sv=writer(stack,sample/'vehicle_variants.csv',variant_fields)
        rw=writer(stack,output/'vehicle_work_rules.csv.gz',RULE);sr=writer(stack,sample/'vehicle_work_rules.csv.gz',RULE)
        aw=writer(stack,output/'all_pair_decisions.csv.gz',AUDIT);cw=writer(stack,output/'conditional_estimates.csv.gz',AUDIT)
        wh=['variant_code','make','model','generation','engine_label','year_from','year_to','price_basis']+[w['code'] for w in works]
        wide=writer(stack,output/'prices_by_vehicle.csv',wh)
        for v,t in zip(variants,traits):
            v['image_path']=FALLBACK;vw.writerow(v)
            if v['code'] in samplecodes:sv.writerow(v)
            stats['variants']+=1;bymake[t['make']]['variants']+=1;t['_price_factors']=price_factors(t,model)
            values=dict.fromkeys(wh,'');values.update({k:t[k] for k in wh[:7]});values['price_basis']='MODELLED_NOT_OBSERVED; blank is not zero'
            reference=referenced(t);introws.extend(reference);market.extend(market_candidates(t))
            gkey=(v['make'],v['model'],v['generation'])
            group=images.setdefault(gkey,dict(group_id='img-'+hashlib.sha256('|'.join(gkey).encode()).hexdigest()[:20],make=v['make'],model=v['model'],generation=v['generation'],year_from=v['year_from'],year_to=v['year_to'],variant_codes=[]));group['variant_codes'].append(v['code'])
            available=0
            for w in works:
                state,reason=decision(t,w);kind=schedule_kind(t,w);stats[state]+=1;bywork[w['code']][state]+=1
                number=estimate(t,w,model) if state in {'IMPORTABLE','CONDITIONAL'} and not w.get('quote_only') else dict(EMPTY_PRICE)
                if state=='IMPORTABLE' and not number['estimated_price']:raise ValueError('IMPORTABLE without a numeric estimate')
                audit=dict(variant_code=v['code'],work_code=w['code'],state=state,reason=reason,schedule_kind=kind,price_basis='MODELLED_UNVALIDATED' if number['estimated_price'] else 'QUOTE_OR_NOT_APPLICABLE',**number);aw.writerow(audit)
                if state=='CONDITIONAL':cw.writerow(audit)
                quote_row=state=='QUOTE_REQUIRED' and w.get('applicability_group') is not None
                if state!='IMPORTABLE' and not quote_row:continue
                note=(f"AC-MODEL-2.0-SQL | 2026-09-16 | "+('MODELLED gross parts+labour, rounded 10 EUR; not a market mean or quote. ' if number['estimated_price'] else 'PRICE UNKNOWN; individual quote required. ')+f"Scope: {w['scope']}. Reason: {reason}. Numeric parts/hours/tier factors are assumptions; source/config in seed manifest.")
                if len(note)>1000:raise ValueError('Note too long')
                row=dict(variant_code=v['code'],work_code=w['code'],interval_km='',interval_months='',estimated_price=number['estimated_price'],interval_source='',estimate_note=note,schedule_kind=kind,price_basis='MODELLED' if number['estimated_price'] else 'QUOTE_REQUIRED',applicability=state,review_status='NOT_VIN_VERIFIED')
                rw.writerow(row);stats['seed_rules']+=1
                if v['code'] in samplecodes:sr.writerow(row);stats['sample_rules']+=1
                if number['estimated_price']:values[w['code']]=str(int(D(number['estimated_price'])));available+=1;stats['numeric_prices']+=1
            if available:stats['variants_with_price']+=1;bymake[t['make']]['variants_with_price']+=1
            wide.writerow(values)
            if t['make']=='Toyota' and reference:exceptions.append(dict(variant_code=v['code'],work_code='COOLANT',reason='First change shown at 150000 km/120 months; repeating interval not established by this 10-year table. Do not insert as a repeating plan.'))
            if t['make']=='Tesla' and t['model'] in {'Model Y','Model 3'}:exceptions.append(dict(variant_code=v['code'],work_code='TYRE_ROTATION',reason='Manual: 10000 km OR tread-depth difference >=1.5 mm. Staggered sizes/tyres need checking. Conditional, not silently a complete fixed-only rule.'))
    with ExitStack() as stack:
        wd=writer(stack,output/'work_definitions.csv',['code','name','category','default_estimated_price','estimate_note'])
        for w in works:wd.writerow(dict(code=w['code'],name=w['name'],category=w['category'],default_estimated_price='',estimate_note='AC-WORK-2.0 | '+w['scope']+' | No global vehicle-agnostic price.'))
        iw=writer(stack,output/'referenced_intervals.csv',INTERVAL);si=writer(stack,sample/'referenced_intervals.csv',INTERVAL)
        for row in introws:
            iw.writerow(row)
            if row['variant_code'] in samplecodes:si.writerow(row)
        mw=writer(stack,output/'market_review_intervals.csv',INTERVAL+['review_status','reviewer','reviewed_on'])
        for row in market:mw.writerow(row)
        ew=writer(stack,output/'schedule_exceptions.csv',['variant_code','work_code','reason'])
        for row in exceptions:ew.writerow(row)
        bw=writer(stack,output/'coverage_by_work.csv',['work_code','IMPORTABLE','CONDITIONAL','NOT_APPLICABLE','QUOTE_REQUIRED'])
        for code,counts in bywork.items():bw.writerow(dict(work_code=code,**{k:counts[k] for k in bw.fieldnames[1:]}))
        bm=writer(stack,output/'coverage_by_make.csv',['make','variants','variants_with_price'])
        for make,c in sorted(bymake.items()):bm.writerow(dict(make=make,variants=c['variants'],variants_with_price=c['variants_with_price']))
        ig=writer(stack,output/'image_groups.csv',['group_id','make','model','generation','year_from','year_to','representative_year','variant_count']);im=writer(stack,output/'image_group_variants.csv',['group_id','variant_code'])
        for g in images.values():
            end=int(g['year_to']) if g['year_to'] else 2026;start=int(g['year_from']);rep=min(2026,(start+max(start,end))//2)
            ig.writerow({**{k:g[k] for k in ['group_id','make','model','generation','year_from','year_to']},'representative_year':rep,'variant_count':len(g['variant_codes'])})
            for vc in g['variant_codes']:im.writerow(dict(group_id=g['group_id'],variant_code=vc))
    diagnostics=json.loads((ROOT/'config/diagnostics.json').read_text())
    extra={'HV_BATTERY_REPLACE':['pogonska baterija','neispravna visokonaponska baterija'],'ONBOARD_CHARGER':['ac punjenje ne radi','ugradjeni punjac'],'CHARGE_PORT':['ostecen prikljucak punjenja','konektor punjenja'],'WIPER_MOTOR':['brisaci se ne pokrecu','motor brisaca'],'BLOWER_MOTOR':['ventilator kabine ne radi','nema strujanja zraka'],'ENGINE_OIL_LEAK_TEST':['curenje ulja','trag ulja ispod motora'],'COOLING_PRESSURE_TEST':['gubi rashladnu tekucinu','curenje antifriza'],'FRONT_CALIPER':['prednja kocnica blokira','zagrijavanje prednjeg kotaca'],'CONTROL_ARM':['lupanje ovjesa','osteceno rame ovjesa'],'STEERING_RACK':['lupanje letve upravljaca','nepravilan rad upravljaca']}
    for work,phrases in extra.items():
        for index,phrase in enumerate(phrases,1):diagnostics.append(dict(code='ac-ds2-'+work.lower()+'-'+str(index),work_code=work,phrase=phrase,weight=3,active=1,basis='EDUCATIONAL_RULE_NOT_VALIDATED'))
    with ExitStack() as stack:
        dw=writer(stack,output/'diagnostic_rules.csv',['code','work_code','phrase','weight','active','basis'])
        for row in diagnostics:dw.writerow(row)
    for n in ['work_definitions.csv','diagnostic_rules.csv']:shutil.copy2(output/n,sample/n)
    checksums(sample);checksums(output)
    manifest={'version':'AF3-SQL-DATA-2.0','as_of':'2026-09-16','counts':dict(stats),'work_count':len(works),'reference_interval_count':len(introws),'market_review_interval_count':len(market),'image_group_count':len(images),'diagnostic_count':len(diagnostics),'sample_variant_count':len(samplecodes),'accuracy':'UNMEASURED model; not a national average, OEM prices or VIN-specific fitment','manufacturer_coverage':'Source-referenced model subset only. Never described as complete OEM coverage.','base_variant_sha256':digest(ROOT/'base-input/vehicle_variants.csv'),'base_traits_sha256':digest(ROOT/'base-input/vehicle_traits.csv'),'works_sha256':digest(ROOT/'config/works.json'),'model_sha256':digest(ROOT/'config/model.json'),'files':{p.name:digest(p) for p in output.iterdir() if p.is_file() and p.name!='build_manifest.json'}}
    (output/'build_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');return manifest
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',type=Path,default=ROOT/'data');a=p.parse_args();print(json.dumps(build(a.output),indent=2))
