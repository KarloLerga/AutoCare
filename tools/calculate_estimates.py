#!/usr/bin/env python3
"""Offline arithmetic only. Never infer parts or labour time from make, horsepower or age.
All input amounts/rates MUST include VAT. DRAFT data stays DRAFT after calculation.
Merge with a complete reviewed_rules.csv to preserve already verified intervals.
"""
from __future__ import annotations
import argparse,csv,json,sys
from decimal import Decimal,InvalidOperation,ROUND_HALF_UP
from pathlib import Path
FIELDS="variant_code work_code interval_km interval_months estimated_price interval_source estimate_note review_status".split()
INPUT_FIELDS="variant_code work_code parts_low parts_high labour_hours_low labour_hours_high hourly_rate_gross consumables_low consumables_high vat_basis source_urls checked_on reviewed_by review_status".split()
def number(row,key):
    try:value=Decimal(row[key])
    except (KeyError,InvalidOperation) as exc:raise ValueError(f"Invalid decimal: {key}") from exc
    if not value.is_finite() or value<0:raise ValueError(f"Invalid nonnegative number: {key}")
    return value
def estimate(row):
    if row.get("vat_basis")!="INCLUDED":raise ValueError("Inputs must all include VAT; do not mix net/gross prices.")
    p0,p1=number(row,"parts_low"),number(row,"parts_high")
    h0,h1=number(row,"labour_hours_low"),number(row,"labour_hours_high")
    c0,c1=number(row,"consumables_low"),number(row,"consumables_high")
    rate=number(row,"hourly_rate_gross")
    if p0>p1 or h0>h1 or c0>c1:raise ValueError("Lower bound exceeds upper bound.")
    low=(p0+h0*rate+c0).quantize(Decimal("0.01"),rounding=ROUND_HALF_UP)
    high=(p1+h1*rate+c1).quantize(Decimal("0.01"),rounding=ROUND_HALF_UP)
    mid=((low+high)/2).quantize(Decimal("0.01"),rounding=ROUND_HALF_UP)
    if high>Decimal("9999999.99"):raise ValueError("Estimate exceeds schema precision.")
    return low,high,mid
def approval(row, existing):
    cost_approved=(row.get("review_status")=="APPROVED" and bool(row.get("reviewed_by", "").strip())
                   and bool(row.get("source_urls", "").strip()) and bool(row.get("checked_on", "").strip()))
    # Approving a cost must not silently approve an older unreviewed interval.
    return cost_approved and (existing is None or existing.get("review_status")=="APPROVED")
def read(path):
    with path.open(encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))
def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument("--input",type=Path,required=True);p.add_argument("--rules",type=Path,required=True);p.add_argument("--output",type=Path,required=True);a=p.parse_args()
    try:
        original=read(a.rules);base={(r["variant_code"],r["work_code"]):r for r in original}
        if len(base)!=len(original):raise ValueError("Duplicate variant/work in base rules.")
        seen=set();calculations=[]
        for row in read(a.input):
            key=(row["variant_code"],row["work_code"])
            if key in seen:raise ValueError("Duplicate cost input pair.")
            seen.add(key);low,high,mid=estimate(row)
            approved=approval(row,base.get(key))
            # Approval is user-supplied provenance, NOT verification performed by this script or an LLM.
            note=f"MODELIRANA PROCJENA: {low}-{high} EUR s PDV-om; spremljena sredina {mid}. Izvori: {row.get('source_urls','NEMA')}; provjereno: {row.get('checked_on','NE')}; pregledao: {row.get('reviewed_by','NE')}. Nije ponuda servisa."
            if len(note)>1000:raise ValueError("Provenance note exceeds 1000 characters; shorten without losing source scope.")
            merged=dict(base.get(key,dict.fromkeys(FIELDS,"")))
            merged.update(variant_code=key[0],work_code=key[1],estimated_price=str(mid),estimate_note=note,review_status="APPROVED" if approved else "DRAFT")
            base[key]=merged;calculations.append(dict(variant_code=key[0],work_code=key[1],low=str(low),high=str(high),midpoint=str(mid),review_status=merged["review_status"]))
        a.output.parent.mkdir(parents=True,exist_ok=True)
        with a.output.open("w",encoding="utf-8",newline="") as f:
            writer=csv.DictWriter(f,fieldnames=FIELDS,extrasaction="ignore");writer.writeheader();writer.writerows(base.values())
        a.output.with_suffix(".calculations.json").write_text(json.dumps(calculations,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
        print(f"Calculated {len(calculations)} explicitly supplied variant/work pairs. Review full merged rules before import.");return 0
    except (OSError,ValueError,KeyError) as exc:print(f"Estimate preparation failed: {exc}",file=sys.stderr);return 1
if __name__=="__main__":raise SystemExit(main())
