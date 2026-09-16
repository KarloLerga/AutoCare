"""Shared CSV readers and import validation; Python standard library only."""
from __future__ import annotations
import csv,gzip,hashlib,json
from decimal import Decimal,InvalidOperation
from pathlib import Path
from typing import Iterator


def rows(path: Path) -> Iterator[dict[str,str]]:
    opener=gzip.open if path.suffix=='.gz' else open
    with opener(path,'rt',encoding='utf-8-sig',newline='') as f:
        r=csv.DictReader(f)
        if not r.fieldnames or len(set(r.fieldnames))!=len(r.fieldnames):raise ValueError(f'Invalid header: {path.name}')
        for number,row in enumerate(r,2):
            if None in row or any(v is None for v in row.values()):raise ValueError(f'Invalid column count: {path.name}:{number}')
            yield row


def money(text: str) -> Decimal|None:
    if not text:return None
    try:n=Decimal(text)
    except InvalidOperation as ex:raise ValueError('Invalid decimal amount') from ex
    if not n.is_finite() or n<0 or n>Decimal('9999999.99') or n.as_tuple().exponent < -2:raise ValueError('Out-of-range or over-precise price')
    return n


def integer(text: str) -> int|None:
    if not text:return None
    n=int(text)
    if str(n)!=text:raise ValueError('Invalid canonical integer')
    return n


def digest(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        while data:=f.read(1<<20):h.update(data)
    return h.hexdigest()


def validate(folder: Path, full_audit: bool=True) -> dict:
    manifest=json.loads((folder/'build_manifest.json').read_text(encoding='utf-8'))
    for name,expected in manifest['files'].items():
        if Path(name).name!=name:raise ValueError('Unsafe manifest filename')
        if digest(folder/name)!=expected:raise ValueError('Data checksum mismatch: '+name)
    variants={};works={};counts={'variants':0,'works':0,'rules':0,'interval_rules':0,'diagnostic_rules':0}
    for v in rows(folder/'vehicle_variants.csv'):
        if v['code'] in variants:raise ValueError('Duplicate variant code')
        if not v['code'].startswith('vmm-') or len(v['code'])!=68:raise ValueError('Wrong variant code')
        for col,limit in {'make':100,'model':150,'generation':200,'engine_label':240,'body_type':100,'fuel_type':80,'transmission':120,'image_path':255}.items():
            if len(v[col])>limit:raise ValueError('Variant string too long: '+col)
        if any(not v[k] for k in ['make','model','generation','engine_label']):raise ValueError('Missing variant identity')
        start=integer(v['year_from']);end=integer(v['year_to']);hp=integer(v['power_hp'])
        if not start or not 1886<=start<=2100 or (end is not None and not start<=end<=2100):raise ValueError('Invalid years')
        if hp is not None and not 1<=hp<=10000:raise ValueError('Invalid power')
        if v['image_path'] and (not v['image_path'].startswith('/images/') or '..' in v['image_path']):raise ValueError('Invalid image path')
        variants[v['code']]=v;counts['variants']+=1
    for w in rows(folder/'work_definitions.csv'):
        if w['code'] in works:raise ValueError('Duplicate work code')
        if w['category'] not in {'MAINTENANCE','REPAIR'}:raise ValueError('Invalid category')
        if len(w['code'])>80 or len(w['name'])>160 or len(w['estimate_note'])>1000:raise ValueError('Work value too long')
        if w['default_estimated_price']:raise ValueError('Global prices must stay NULL in this package')
        works[w['code']]=w;counts['works']+=1
    previous=None;seen_variants=set();seen_works=set()
    for r in rows(folder/'vehicle_work_rules.csv.gz'):
        vc,wc=r['variant_code'],r['work_code']
        if vc not in variants or wc not in works:raise ValueError('Orphan rule reference')
        if vc!=previous:
            if vc in seen_variants:raise ValueError('Noncontiguous/duplicated variant block')
            seen_variants.add(vc);seen_works=set();previous=vc
        if wc in seen_works:raise ValueError('Duplicate variant/work pair')
        seen_works.add(wc)
        amount=money(r['estimated_price'])
        if amount is None or amount<=0 or amount%10:raise ValueError('Price must be positive and rounded to 10 EUR')
        if r['price_basis']!='MODELLED' or r['review_status']!='MODELLED_NOT_INDIVIDUALLY_VERIFIED' or r['applicability']!='IMPORTABLE':raise ValueError('Wrong trust/applicability status')
        if not r['estimate_note'].startswith('AC-MODEL-') or len(r['estimate_note'])>1000:raise ValueError('Missing or overlong model provenance')
        km=integer(r['interval_km']);months=integer(r['interval_months'])
        if km is not None and not 1<=km<=1_000_000:raise ValueError('Invalid km interval')
        if months is not None and not 1<=months<=1200:raise ValueError('Invalid month interval')
        if (km or months) and (works[wc]['category']!='MAINTENANCE' or not r['interval_source'].startswith('OEM_MODEL_PLAN |')):raise ValueError('Unreferenced or repair interval')
        if len(r['interval_source'])>1000:raise ValueError('Interval provenance too long')
        if km or months:counts['interval_rules']+=1
        counts['rules']+=1
    if counts['variants']!=manifest['counts']['accepted_variants'] or counts['rules']!=manifest['counts']['IMPORTABLE']:raise ValueError('Manifest row count mismatch')
    if counts['interval_rules']!=manifest['referenced_interval_rows']:raise ValueError('Interval count mismatch')
    if (folder/'diagnostic_rules.csv').exists():
        codes=set();phrases=set()
        for r in rows(folder/'diagnostic_rules.csv'):
            if r['code'] in codes or len(r['code'])>80:raise ValueError('Diagnostic code conflict')
            pair=(r['work_code'],r['phrase'].lower())
            if pair in phrases:raise ValueError('Duplicate phrase for same candidate')
            codes.add(r['code']);phrases.add(pair)
            if r['work_code'] not in works or works[r['work_code']]['category']!='REPAIR':raise ValueError('Invalid diagnostic candidate')
            if not 1<=integer(r['weight'])<=100 or not 1<=len(r['phrase'])<=160 or r['active'] not in {'0','1'}:raise ValueError('Invalid diagnostic value')
            counts['diagnostic_rules']+=1
    if full_audit:
        current=None;block=set();seen=set();decisions=0;importable=0
        for r in rows(folder/'all_pair_decisions.csv.gz'):
            vc=r['variant_code'];wc=r['work_code']
            if vc!=current:
                if current is not None and block!=set(works):raise ValueError('Incomplete decision block')
                if vc in seen or vc not in variants:raise ValueError('Invalid decision variant')
                seen.add(vc);current=vc;block=set()
            if wc in block or wc not in works:raise ValueError('Invalid decision work')
            block.add(wc);decisions+=1
            amount=money(r['estimated_price'])
            if r['state'] in {'IMPORTABLE','CONDITIONAL'}:
                if amount is None or amount%10:raise ValueError('Decision price not rounded')
                if abs(Decimal(r['raw_total_model_eur'])-amount)>Decimal('5.01'):raise ValueError('Rounding inconsistency')
            elif r['state'] in {'NOT_APPLICABLE','QUOTE_REQUIRED'}:
                if amount is not None:raise ValueError('Forbidden fabricated amount for an excluded row')
            else:raise ValueError('Unknown applicability state')
            if r['state']=='IMPORTABLE':importable+=1
        if block!=set(works) or seen!=set(variants) or decisions!=len(works)*len(variants) or importable!=counts['rules']:raise ValueError('Decision coverage mismatch')
        counts['all_pair_decisions']=decisions
    return counts
