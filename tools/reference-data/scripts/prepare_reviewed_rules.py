#!/usr/bin/env python3
"""Promote SMALL individually reviewed selections to the existing AF3 Java import format.
This never marks all conditional estimates as approved. A named human reviewer and an
applicability source are mandatory. Price remains MODELLED unless explicitly replaced.
"""
import argparse,csv,datetime,sys
from pathlib import Path
from decimal import Decimal
from seed_io import rows,money,integer
from build_seed import round_ten,RULE_FIELDS
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--review',type=Path,required=True)
p.add_argument('--data',type=Path,default=ROOT/'data')
p.add_argument('--output',type=Path,required=True)
a=p.parse_args()
try:
    wanted={}
    for r in rows(a.review):
        key=r['variant_code'],r['work_code']
        if key in wanted:raise ValueError('Duplicate review pair')
        if r['applicability_confirmed']!='YES' or not r['reviewed_by'].strip() or not r['applicability_source'].strip():raise ValueError('Every row requires YES, a human reviewer and a specific applicability source.')
        date=datetime.date.fromisoformat(r['reviewed_on'])
        if date>datetime.date.today():raise ValueError('Review date is in the future')
        wanted[key]=r
    if not wanted:raise ValueError('Review contains no records')
    if len(wanted)>500:raise ValueError('This tool is for deliberate small reviewed batches (maximum 500)')
    decisions={}
    for r in rows(a.data/'all_pair_decisions.csv.gz'):
        key=r['variant_code'],r['work_code']
        if key in wanted:decisions[key]=r
    work_categories={r['code']:r['category'] for r in rows(a.data/'work_definitions.csv')}
    prepared=[]
    for key,review in wanted.items():
        if key not in decisions:raise ValueError('Unknown variant/work pair')
        result=decisions[key]
        if result['state']=='NOT_APPLICABLE':raise ValueError('Source says this work is not applicable. Correct source/traits and rebuild first; do not bypass it here.')
        supplied=money(review['estimated_price'])
        price=supplied if supplied is not None else money(result['estimated_price'])
        if price is None:raise ValueError('Quote-required row needs a reviewed point price with its price source')
        if supplied is not None and not review['price_source'].strip():raise ValueError('A reviewer-supplied price needs a price source or a named manual-estimate basis')
        km=integer(review['interval_km']);months=integer(review['interval_months'])
        if (km or months) and (work_categories[key[1]]!='MAINTENANCE' or not review['interval_source'].strip()):raise ValueError('A maintenance interval requires its own evidence; applicability/price approval is insufficient')
        if km is not None and not 1<=km<=1000000 or months is not None and not 1<=months<=1200:raise ValueError('Invalid interval')
        note=('AC-REVIEWED-1.0 | Applicability checked by '+review['reviewed_by']+' on '+review['reviewed_on']+'. '+review['applicability_source']+'. Price: '+('reviewer point estimate; '+review['price_source'] if supplied is not None else 'AC-MODEL-2.0-SQL scenario, NOT an observed market average')+'. Gross parts+labour; nearest 10 EUR.')
        if len(note)>1000 or len(review['interval_source'])>1000:raise ValueError('Provenance exceeds current DB capacity')
        prepared.append(dict(variant_code=key[0],work_code=key[1],interval_km=km or '',interval_months=months or '',estimated_price=f'{round_ten(price):.2f}',interval_source=review['interval_source'],estimate_note=note,price_basis='REVIEWED_POINT_ESTIMATE' if supplied is not None else 'MODELLED_APPLICABILITY_REVIEWED',applicability='REVIEWED',review_status='APPROVED'))
    a.output.parent.mkdir(parents=True,exist_ok=True)
    with a.output.open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=RULE_FIELDS,lineterminator='\n');w.writeheader();w.writerows(prepared)
    print(f'Prepared {len(prepared)} individually reviewed rows. Import through the setup JAR DatabaseTool import-rules command. Never use --replace-existing without checking every existing interval and price.')
except (OSError,ValueError,KeyError) as ex:print('Review preparation stopped: '+str(ex),file=sys.stderr);raise SystemExit(1)
