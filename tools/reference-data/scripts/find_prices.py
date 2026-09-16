#!/usr/bin/env python3
"""Find source variants and their point estimates without opening a million-row CSV."""
import argparse,csv,sys
from pathlib import Path
from seed_io import rows
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--data',type=Path,default=Path(__file__).resolve().parents[1]/'data')
p.add_argument('--make',default='');p.add_argument('--model',default='');p.add_argument('--engine',default='');p.add_argument('--year',type=int)
p.add_argument('--limit',type=int,default=20);p.add_argument('--output',type=Path)
a=p.parse_args();selected={}
if not 1<=a.limit<=1000:raise SystemExit('limit must be 1..1000')
for v in rows(a.data/'vehicle_variants.csv'):
    if a.make.casefold() not in v['make'].casefold() or a.model.casefold() not in v['model'].casefold() or a.engine.casefold() not in v['engine_label'].casefold():continue
    if a.year and (a.year<int(v['year_from']) or (v['year_to'] and a.year>int(v['year_to']))):continue
    selected[v['code']]=v
    if len(selected)>=a.limit:break
if not selected:raise SystemExit('No matching source variants.')
names={w['code']:w['name'] for w in rows(a.data/'work_definitions.csv')}
fields=['variant_code','make','model','generation','engine_label','work_code','work_name','estimated_price_eur','state','reason']
out=a.output.open('w',encoding='utf-8-sig',newline='') if a.output else sys.stdout
try:
    w=csv.DictWriter(out,fieldnames=fields,lineterminator='\n');w.writeheader()
    for r in rows(a.data/'all_pair_decisions.csv.gz'):
        if r['variant_code'] not in selected:continue
        v=selected[r['variant_code']]
        w.writerow(dict(variant_code=v['code'],make=v['make'],model=v['model'],generation=v['generation'],engine_label=v['engine_label'],work_code=r['work_code'],work_name=names[r['work_code']],estimated_price_eur=r['estimated_price'],state=r['state'],reason=r['reason']))
finally:
    if a.output:out.close()
