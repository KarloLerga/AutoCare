#!/usr/bin/env python3
"""Validate the final materialized AutoCare vehicle/work catalogue."""
from __future__ import annotations

import argparse
import csv
import gzip
import json
from collections import Counter, defaultdict
from decimal import Decimal
from pathlib import Path


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

    fuel = {}
    with args.traits.open(encoding="utf-8-sig", newline="") as handle:
        for row in csv.DictReader(handle):
            fuel[row["variant_code"]] = row.get("fuel_class", "")

    combustion_only = {"OIL_SERVICE", "SPARK_PLUGS", "TIMING_BELT_PUMP", "DPF_REPLACEMENT"}
    seen = set()
    by_variant = defaultdict(int)
    counts = Counter()
    errors = []

    with gzip.open(args.rules, "rt", encoding="utf-8", newline="") as handle:
        for line_number, row in enumerate(csv.DictReader(handle), 2):
            key = (row["variant_code"], row["work_code"])
            if key in seen:
                errors.append(f"duplicate {key}")
                continue
            seen.add(key)

            work = works.get(row["work_code"])
            if work is None:
                errors.append(f"unknown/removed work {row['work_code']} line {line_number}")
                continue
            if row["variant_code"] not in fuel:
                errors.append(f"unknown variant {row['variant_code']} line {line_number}")
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

            if fuel[row["variant_code"]] == "BEV" and row["work_code"] in combustion_only:
                errors.append(f"BEV combustion-only work {key}")

            by_variant[row["variant_code"]] += 1
            counts["rules"] += 1

    if len(fuel) != 30366:
        errors.append(f"expected 30366 variants, got {len(fuel)}")
    missing_variants = [code for code in fuel if by_variant[code] == 0]
    if missing_variants:
        errors.append(f"variants without rules: {len(missing_variants)}")

    result = {
        "variant_count": len(fuel),
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
