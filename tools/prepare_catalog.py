#!/usr/bin/env python3
"""Pin, inspect, normalize and report the public engine CSV. No database credentials.
Python is a developer data tool only; the shipped Java desktop has no HTTP dependency.
Raw and generated database files are ODbL-derived data, not MIT application code.
"""
from __future__ import annotations
import argparse,csv,hashlib,json,sys,urllib.request
from collections import Counter
from datetime import datetime,timezone
from decimal import Decimal,InvalidOperation
from pathlib import Path
COMMIT="b4965631140fa82404359167621274601da69a5b"
URL=f"https://raw.githubusercontent.com/gor3a/vehicle-makes-models/{COMMIT}/data/csv/engines.csv"
REQUIRED="make_group make model generation gen_year_start gen_year_end body_type engine_label fuel_type cylinders displacement_cc power_hp torque_nm transmission drivetrain zero_to_100_s top_speed_kmh fuel_economy_combined_l100 length_mm width_mm height_mm wheelbase_mm curb_weight_kg".split()
OUT="code make model generation engine_label body_type fuel_type power_hp transmission year_from year_to image_path".split()
LIMITS={"make":100,"model":150,"generation":200,"engine_label":240,"body_type":100,"fuel_type":80,"transmission":120}
def clean(value: str|None)->str:
    value=" ".join((value or "").split())
    return "" if value.lower() in {"null","none","n/a","unknown","-"} else value
def integer(value: str,field: str,optional: bool=False)->int|None:
    if not value:
        if optional:return None
        raise ValueError(f"missing {field}")
    try:
        number=Decimal(value)
        if not number.is_finite() or number!=number.to_integral_value():raise ValueError(f"non-integer {field}: {value}")
        return int(number)
    except InvalidOperation as exc:raise ValueError(f"invalid {field}: {value}") from exc
def normalize(row: dict[str,str])->dict[str,str|int|None]:
    r={key:clean(row.get(key)) for key in REQUIRED}
    for name in ("make","model","generation","engine_label"):
        if not r[name]:raise ValueError(f"missing {name}")
    for name,limit in LIMITS.items():
        if len(r[name])>limit:raise ValueError(f"{name} exceeds {limit} characters")
    start=integer(r["gen_year_start"],"gen_year_start")
    end=integer(r["gen_year_end"],"gen_year_end",True)
    if not 1886<=start<=2100 or (end is not None and not start<=end<=2100):raise ValueError("invalid year range")
    hp=integer(r["power_hp"],"power_hp",True)
    if hp is not None and not 1<=hp<=10000:raise ValueError("invalid power_hp")
    # All 23 source values preserve distinct engine variants. Do NOT merge solely by a display label.
    # On a later upstream snapshot a spec change can produce a new code: explicit review, no blind remap.
    identity=json.dumps(r,ensure_ascii=False,sort_keys=True,separators=(",",":"))
    code="vmm-"+hashlib.sha256(identity.encode("utf-8")).hexdigest()
    return dict(code=code,make=r["make"],model=r["model"],generation=r["generation"],engine_label=r["engine_label"],body_type=r["body_type"],fuel_type=r["fuel_type"],power_hp=hp,transmission=r["transmission"],year_from=start,year_to=end,image_path="")
def prepare(source: Path,target: Path)->dict:
    target.mkdir(parents=True,exist_ok=True)
    counts=Counter();seen=set();makes=set();models=set();generations=set();missing=Counter();duplicates=[]
    with source.open("r",encoding="utf-8-sig",newline="") as raw,(target/"normalized.csv").open("w",encoding="utf-8",newline="") as good,(target/"rejected.csv").open("w",encoding="utf-8",newline="") as bad:
        reader=csv.DictReader(raw)
        if reader.fieldnames is None or len(reader.fieldnames)!=len(set(reader.fieldnames)):raise ValueError("CSV header missing or duplicated")
        absent=set(REQUIRED)-set(reader.fieldnames)
        if absent:raise ValueError(f"Unexpected source schema; missing columns: {sorted(absent)}")
        writer=csv.DictWriter(good,fieldnames=OUT);writer.writeheader()
        rejects=csv.writer(bad);rejects.writerow(["source_row","reason","raw_json"])
        for number,row in enumerate(reader,2):
            counts["input_rows"]+=1
            for key in REQUIRED:
                if not clean(row.get(key)):missing[key]+=1
            try:
                if None in row:raise ValueError("extra unnamed columns")
                normalized=normalize(row)
                if normalized["code"] in seen:
                    counts["exact_duplicates"]+=1;duplicates.append(number);continue
                seen.add(normalized["code"]);writer.writerow(normalized);counts["accepted_rows"]+=1
                makes.add(normalized["make"]);models.add((normalized["make"],normalized["model"]))
                generations.add((normalized["make"],normalized["model"],normalized["generation"],normalized["year_from"],normalized["year_to"]))
            except (ValueError,TypeError) as exc:
                counts["rejected_rows"]+=1;rejects.writerow([number,str(exc),json.dumps(row,ensure_ascii=False)])
    manifest={"source_url":URL,"source_commit":COMMIT,"source_file_sha256":hashlib.sha256(source.read_bytes()).hexdigest(),"normalized_sha256":hashlib.sha256((target/"normalized.csv").read_bytes()).hexdigest(),"generated_utc":datetime.now(timezone.utc).isoformat(),"counts":dict(counts),"accepted_makes":len(makes),"accepted_make_model_pairs":len(models),"accepted_generation_ranges":len(generations),"missing_input_values":dict(missing),"duplicate_source_rows":duplicates,"license":"ODbL-1.0","attribution":["gor3a/vehicle-makes-models","autoevolution.com"],"limitations":["No maintenance intervals, prices, labour times or VIN-level compatibility in this input.","Null end year is preserved; it does not certify current production.","Source snapshot is immutable; upgrades require deliberate comparison.","Rejections must be reviewed, not hidden."]}
    (target/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    (target/"ATTRIBUTION.txt").write_text(f"Derived from vehicle-makes-models by gor3a, commit {COMMIT}.\nhttps://github.com/gor3a/vehicle-makes-models\nUpstream factual source: autoevolution.com.\nData license: ODbL 1.0: https://opendatacommons.org/licenses/odbl/1-0/\nChanges: selected fields, normalized whitespace/nulls, validated rows, deduplicated exact variants, assigned hashed source codes.\nApplication code is separate from this adapted catalogue.\n",encoding="utf-8")
    return manifest
def main()->int:
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input",type=Path,default=Path("data/raw/engines.csv"))
    parser.add_argument("--output",type=Path,default=Path("data/generated"))
    parser.add_argument("--download",action="store_true")
    args=parser.parse_args()
    try:
        if args.download:
            args.input.parent.mkdir(parents=True,exist_ok=True)
            temporary=args.input.with_suffix(".download")
            with urllib.request.urlopen(URL,timeout=60) as response,temporary.open("wb") as dest:
                while chunk:=response.read(1024*1024):dest.write(chunk)
            temporary.replace(args.input)
        manifest=prepare(args.input,args.output)
        print(json.dumps(manifest["counts"],indent=2));print(f"Review {args.output/'manifest.json'} and rejected.csv before import.")
        return 0
    except (OSError,ValueError) as exc:
        print(f"Catalogue preparation failed: {exc}",file=sys.stderr);return 1
if __name__=="__main__":raise SystemExit(main())
