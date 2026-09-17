#!/usr/bin/env python3
"""Validate the final materialized AutoCare vehicle/work catalogue."""
from __future__ import annotations

import argparse
import csv
import gzip
import json
import re
from collections import Counter, defaultdict
from decimal import Decimal
from pathlib import Path

COMBUSTION_ONLY = {
    "OIL_SERVICE",
    "AIR_FILTER",
    "FUEL_FILTER",
    "COOLANT",
    "TIMING_BELT_PUMP",
    "SPARK_PLUGS",
    "AUX_BELT",
    "GLOW_PLUGS",
    "TURBO",
    "EGR_VALVE",
    "DPF_CLEAN",
    "DPF_REPLACEMENT",
    "STARTER",
    "ALTERNATOR",
}


def drive_flags(row: dict) -> tuple[bool, bool, bool]:
    text = " ".join(row.get(field, "") for field in ("model", "generation", "engine_label")).lower()
    awd = bool(re.search(r"\b(awd|4wd|4x4)\b", text)) or any(
        token in text for token in ("xdrive", "quattro", "4matic", "4motion", "all4", "sh-awd")
    )
    rwd = bool(re.search(r"\brwd\b", text))
    fwd = bool(re.search(r"\bfwd\b", text))
    return awd, rwd, fwd


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--rules", type=Path, required=True)
    parser.add_argument("--works", type=Path, required=True)
    parser.add_argument("--traits", type=Path, required=True)
    parser.add_argument("--summary", type=Path)
    args = parser.parse_args()

    works = {}
    with args.works.open(encoding="utf-8") as handle:
        for work in json.load(handle):
            if work["code"] not in {"OTHER_MAINTENANCE", "OTHER_REPAIR"}:
                works[work["code"]] = work

    traits = {}
    with args.traits.open(encoding="utf-8-sig", newline="") as handle:
        for row in csv.DictReader(handle):
            awd, rwd, fwd = drive_flags(row)
            traits[row["variant_code"]] = (row.get("fuel_class", ""), awd, rwd, fwd)

    by_variant = defaultdict(int)
    counts = Counter()
    errors = []
    current_variant = None
    current_works = set()

    with gzip.open(args.rules, "rt", encoding="utf-8", newline="") as handle:
        for line_number, row in enumerate(csv.DictReader(handle), 2):
            variant_code = row["variant_code"]
            work_code = row["work_code"]
            key = (variant_code, work_code)

            if variant_code != current_variant:
                current_variant = variant_code
                current_works.clear()
            if work_code in current_works:
                errors.append(f"duplicate {key}")
                continue
            current_works.add(work_code)

            work = works.get(work_code)
            if work is None:
                errors.append(f"unknown/removed work {work_code} line {line_number}")
                continue
            variant_traits = traits.get(variant_code)
            if variant_traits is None:
                errors.append(f"unknown variant {variant_code} line {line_number}")
                continue

            try:
                price = Decimal(row["estimated_price"])
            except Exception:
                price = Decimal("0")
            if price <= 0:
                errors.append(f"non-positive price {key}")

            km = row.get("interval_km", "").strip()
            months = row.get("interval_months", "").strip()
            if work["category"] == "MAINTENANCE":
                counts["maintenance"] += 1
                if not km and not months:
                    errors.append(f"maintenance without interval {key}")
            else:
                counts["repair"] += 1
                if km or months:
                    errors.append(f"repair with interval {key}")

            fuel_class, awd, rwd, fwd = variant_traits
            if fuel_class == "BEV" and work_code in COMBUSTION_ONLY:
                errors.append(f"BEV combustion-only work {key}")
            if fuel_class == "BEV" and work_code == "TRANSMISSION_REBUILD":
                errors.append(f"BEV conventional transmission rebuild {key}")
            if fwd and work_code == "DIFFERENTIAL_OIL":
                errors.append(f"FWD separate differential oil {key}")
            if (fwd or rwd) and not awd and work_code == "TRANSFER_CASE_OIL":
                errors.append(f"2WD transfer case oil {key}")

            by_variant[variant_code] += 1
            counts["rules"] += 1

    if len(traits) != 30366:
        errors.append(f"expected 30366 variants, got {len(traits)}")
    missing_variants = [code for code in traits if by_variant[code] == 0]
    if missing_variants:
        errors.append(f"variants without rules: {len(missing_variants)}")

    result = {
        "variant_count": len(traits),
        "work_count": len(works),
        "rule_count": counts["rules"],
        "maintenance_rule_count": counts["maintenance"],
        "repair_rule_count": counts["repair"],
        "min_rules_per_variant": min(by_variant.values()) if by_variant else 0,
        "max_rules_per_variant": max(by_variant.values()) if by_variant else 0,
        "error_count": len(errors),
        "first_errors": errors[:100],
    }
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if args.summary:
        args.summary.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
