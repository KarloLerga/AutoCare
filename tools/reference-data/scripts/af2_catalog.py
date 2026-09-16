#!/usr/bin/env python3
"""Pin, inspect, normalize and report the public engine CSV. No database credentials.
Python is a developer data tool only; the shipped Java desktop has no HTTP dependency.
Raw and generated database files are ODbL-derived data, not MIT application code.
"""
from __future__ import annotations
import hashlib,json
from decimal import Decimal,InvalidOperation
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
