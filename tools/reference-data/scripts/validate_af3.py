#!/usr/bin/env python3
"""Full streaming audit; checks structural invariants, never establishes market accuracy."""
import csv,gzip,json
from collections import Counter
from decimal import Decimal
from pathlib import Path
P=Path(__file__).resolve().parents[1]

def read_rows(path):
 with path.open(encoding='utf-8',newline='') as f:
  yield from csv.DictReader(f)

works={r['code']:r for r in read_rows(P/'data/work_definitions.csv')}
traits={r['variant_code']:r for r in read_rows(P/'base-input/vehicle_traits.csv')}
counts=Counter();last=None;seen=set();ended=set()
with gzip.open(P/'data/all_pair_decisions.csv.gz','rt',encoding='utf-8',newline='') as f:
 for r in csv.DictReader(f):
  vc,wc=r['variant_code'],r['work_code'];assert vc in traits and wc in works
  if vc!=last:
   if last is not None:assert len(seen)==len(works);ended.add(last)
   assert vc not in ended;last=vc;seen=set()
  assert wc not in seen;seen.add(wc);state=r['state'];counts[state]+=1
  if r['estimated_price']:
   n=Decimal(r['estimated_price']);assert n>=0 and n%10==0
   assert state in {'IMPORTABLE','CONDITIONAL'}
  else:assert state!='IMPORTABLE'
  if state=='NOT_APPLICABLE':assert not r['estimated_price']
  if traits[vc]['fuel_class']=='BEV' and wc in {'OIL_SERVICE','GLOW_PLUGS','TIMING_BELT_PUMP'}:assert state=='NOT_APPLICABLE'
assert len(seen)==len(works);assert sum(counts.values())==len(works)*len(traits)
manifest=json.loads((P/'data/build_manifest.json').read_text(encoding='utf-8'))
for k,n in counts.items():assert n==manifest['counts'][k]
print(json.dumps({'status':'PASS_STRUCTURAL_ONLY','pair_counts':dict(counts),'total':sum(counts.values())},indent=2))
