#!/usr/bin/env python3
"""Materialize a complete AutoCare vehicle/work catalogue.

This script is intentionally an OFFLINE data builder. The GUI never calculates fallback
prices or fallback intervals. Every applicable VehicleVariant x WorkDefinition pair is
written with one concrete estimated price. Every applicable MAINTENANCE pair also gets
one concrete km/month interval. NOT_APPLICABLE pairs are not written.

Existing positive prices and existing concrete intervals are preserved unless a price is
an obvious low outlier against the same deterministic model (mainly old fixed-price rows
for premium/performance/exotic cars). Every correction is written to the audit file.
"""
from __future__ import annotations

import argparse
import csv
import gzip
import json
import re
import sys
from collections import Counter, defaultdict
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

REFERENCE_SCRIPTS = Path(__file__).resolve().parents[1] / "tools" / "reference-data" / "scripts"
if str(REFERENCE_SCRIPTS) not in sys.path:
    sys.path.insert(0, str(REFERENCE_SCRIPTS))

from build_af3 import decision, load_traits
from build_seed import estimate, price_factors

D = Decimal
OTHER_CODES = {"OTHER_MAINTENANCE", "OTHER_REPAIR"}

TIER_FIXED_FACTOR = {
    "ECONOMY": D("0.90"),
    "MAINSTREAM": D("1.00"),
    "PREMIUM": D("1.25"),
    "PERFORMANCE": D("1.65"),
    "EXOTIC": D("2.80"),
}

# Used only when a work has no useful parts/hours/consumables model.
# This is part of the offline calculation, not a runtime fallback.
FAMILY_BASE_PRICE = {
    ("MAINTENANCE", "fixed"): D("100"),
    ("MAINTENANCE", "fluid"): D("120"),
    ("MAINTENANCE", "gear"): D("180"),
    ("MAINTENANCE", "auto"): D("260"),
    ("MAINTENANCE", "cabin"): D("80"),
    ("MAINTENANCE", "chassis"): D("160"),
    ("MAINTENANCE", "brake"): D("120"),
    ("MAINTENANCE", "engine"): D("180"),
    ("MAINTENANCE", "coolant"): D("140"),
    ("REPAIR", "fixed"): D("600"),
    ("REPAIR", "engine"): D("1200"),
    ("REPAIR", "gear"): D("2500"),
    ("REPAIR", "auto"): D("2500"),
    ("REPAIR", "brake"): D("550"),
    ("REPAIR", "chassis"): D("700"),
    ("REPAIR", "cabin"): D("500"),
    ("REPAIR", "coolant"): D("650"),
    ("REPAIR", "timing"): D("1100"),
    ("REPAIR", "clutch"): D("1400"),
    ("REPAIR", "battery"): D("300"),
    ("REPAIR", "exhaust"): D("900"),
    ("REPAIR", "turbo"): D("1000"),
    ("REPAIR", "injector"): D("350"),
}

ENGINE_ONLY_CHECK_CODES = {
    "OIL_SERVICE",
    "AIR_FILTER",
    "FUEL_FILTER",
    "TIMING_BELT_PUMP",
    "SPARK_PLUGS",
    "AUX_BELT",
    "GLOW_PLUGS",
    "TURBO",
    "EGR_VALVE",
    "DPF_CLEAN",
    "DPF_REPLACEMENT",
}


def drivetrain(traits: dict) -> str:
    """Return an explicit drivetrain only when the catalogue text actually says it."""
    text = " ".join(
        str(traits.get(key, ""))
        for key in ("model", "generation", "engine_label")
    ).lower()
    if re.search(r"\b(awd|4wd|4x4)\b", text) or any(
        token in text for token in ("xdrive", "quattro", "4matic", "4motion", "all4", "sh-awd")
    ):
        return "AWD"
    if re.search(r"\brwd\b", text):
        return "RWD"
    if re.search(r"\bfwd\b", text):
        return "FWD"
    return "UNKNOWN"


def explicitly_mentions_fwd(traits: dict) -> bool:
    text = " ".join(
        str(traits.get(key, ""))
        for key in ("model", "generation", "engine_label")
    ).lower()
    return re.search(r"\bfwd\b", text) is not None


def final_applicability(traits: dict, work: dict) -> tuple[str, str]:
    """Apply obvious hardware exclusions on top of the base catalogue decision."""
    state, reason = decision(traits, work)
    if state == "NOT_APPLICABLE":
        return state, reason

    code = work["code"]
    fuel_class = traits.get("fuel_class", "")
    drive = drivetrain(traits)

    if fuel_class == "BEV" and code == "TRANSMISSION_REBUILD":
        return "NOT_APPLICABLE", "EV_USES_DRIVE_UNIT_NOT_CONVENTIONAL_TRANSMISSION_REBUILD"
    if code == "DIFFERENTIAL_OIL" and (drive == "FWD" or explicitly_mentions_fwd(traits)):
        return "NOT_APPLICABLE", "FWD_HAS_NO_SEPARATE_DIFFERENTIAL_SERVICE_IN_THIS_MODEL"
    if code == "TRANSFER_CASE_OIL" and drive in {"FWD", "RWD"}:
        return "NOT_APPLICABLE", "NO_TRANSFER_CASE_ON_EXPLICIT_TWO_WHEEL_DRIVE_VARIANT"

    return state, reason


def money(value: str | Decimal) -> Decimal:
    try:
        return D(str(value).strip() or "0")
    except Exception:
        return D("0")


def round_ten(value: Decimal) -> Decimal:
    if value <= 0:
        return D("10")
    return (value / D("10")).quantize(D("1"), rounding=ROUND_HALF_UP) * D("10")


def load_csv(path: Path) -> list[dict]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def load_existing(path: Path) -> dict[tuple[str, str], dict]:
    if not path.exists():
        return {}
    opener = gzip.open if path.suffix == ".gz" else open
    with opener(path, "rt", encoding="utf-8-sig", newline="") as handle:
        rows = {}
        for row in csv.DictReader(handle):
            rows[(row["variant_code"], row["work_code"])] = row
        return rows


def load_overrides(path: Path) -> dict[str, Decimal]:
    result = {}
    for row in load_csv(path):
        result[row["work_code"]] = money(row["mainstream_base_eur"])
    return result


def load_intervals(path: Path) -> dict[str, dict]:
    result = {}
    for row in load_csv(path):
        result[row["work_code"]] = row
    return result


def fixed_tier_factor(traits: dict) -> Decimal:
    return TIER_FIXED_FACTOR.get(traits.get("tier", "MAINSTREAM"), D("1.00"))


def power_factor(traits: dict) -> Decimal:
    hp = money(traits.get("power_hp", "0"))
    if hp >= 800:
        return D("1.20")
    if hp >= 500:
        return D("1.12")
    if hp >= 350:
        return D("1.07")
    return D("1.00")


def calculated_price(traits: dict, work: dict, model: dict, overrides: dict[str, Decimal]) -> Decimal:
    """Calculate one concrete price for an already-applicable pair."""
    normal = money(estimate(traits, work, model)["estimated_price"])

    # Existing model deliberately left fixed-family work identical for every make.
    # For the final student dataset we still want premium/exotic labour to look realistic.
    if normal > 0:
        if work.get("family") == "fixed":
            normal = normal * fixed_tier_factor(traits)
        return round_ten(normal)

    code = work["code"]
    base = overrides.get(code)
    if base is None or base <= 0:
        base = FAMILY_BASE_PRICE.get((work["category"], work.get("family", "")))
    if base is None or base <= 0:
        # Explicit final deterministic rule, still materialized offline.
        base = D("120") if work["category"] == "MAINTENANCE" else D("700")

    value = base * fixed_tier_factor(traits) * power_factor(traits)
    return round_ten(value)


def choose_price(existing_row: dict | None, candidate: Decimal, traits: dict, work: dict) -> tuple[Decimal, str]:
    if existing_row is None:
        return candidate, "FILLED_NEW_RULE"

    old = money(existing_row.get("estimated_price", ""))
    if old <= 0:
        return candidate, "FILLED_MISSING_PRICE"

    # Preserve existing good values. Correct only obviously too-low values.
    # Fixed work is where the old model most often ignored premium/exotic labour completely.
    if work.get("family") == "fixed" and fixed_tier_factor(traits) > D("1.00"):
        if old < candidate * D("0.75"):
            return candidate, "CORRECTED_TIER_PRICE"

    if candidate > 0 and old < candidate * D("0.35"):
        return candidate, "CORRECTED_LOW_OUTLIER"

    return old, "KEPT_EXISTING"


def rounded_km(value: int) -> int:
    return max(1000, int(round(value / 1000.0) * 1000))


def adjusted_interval(base_km: int | None, base_months: int | None, traits: dict) -> tuple[int | None, int | None]:
    km = base_km
    months = base_months

    # A conservative vehicle-specific materialization. Existing reviewed intervals win
    # before this function is called.
    km_factor = 1.0
    month_factor = 1.0
    tier = traits.get("tier", "MAINSTREAM")

    if tier == "PERFORMANCE":
        km_factor *= 0.90
        month_factor *= 0.95
    elif tier == "EXOTIC":
        km_factor *= 0.80
        month_factor *= 0.90

    hp = int(traits.get("power_hp") or 0)
    if hp >= 500:
        km_factor *= 0.90

    year_from = int(traits.get("year_from") or 2005)
    if year_from < 1995:
        km_factor *= 0.90

    if km is not None:
        km = rounded_km(max(1000, int(km * km_factor)))
    if months is not None:
        months = max(1, int(round(months * month_factor)))

    return km, months


def parse_positive_int(value: str) -> int | None:
    text = str(value or "").strip()
    if not text:
        return None
    number = int(text)
    return number if number > 0 else None


def choose_interval(existing_row: dict | None, policy: dict, traits: dict) -> tuple[int | None, int | None, str]:
    if existing_row is not None:
        old_km = parse_positive_int(existing_row.get("interval_km", ""))
        old_months = parse_positive_int(existing_row.get("interval_months", ""))
        if old_km is not None or old_months is not None:
            return old_km, old_months, "KEPT_EXISTING"

    km = parse_positive_int(policy.get("interval_km", ""))
    months = parse_positive_int(policy.get("interval_months", ""))
    km, months = adjusted_interval(km, months, traits)

    if km is None and months is None:
        raise ValueError("Maintenance policy produced an empty interval")
    return km, months, "FILLED_MODELED_INTERVAL"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference-root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--intervals", type=Path, required=True)
    parser.add_argument("--price-overrides", type=Path, required=True)
    parser.add_argument("--existing", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--audit", type=Path, required=True)
    parser.add_argument("--summary", type=Path, required=True)
    parser.add_argument("--removed-applicability", type=Path, required=True)
    args = parser.parse_args()

    root = args.reference_root
    works = json.loads((root / "config" / "works.json").read_text(encoding="utf-8"))
    model = json.loads((root / "config" / "model.json").read_text(encoding="utf-8"))
    interval_policy = load_intervals(args.intervals)
    price_overrides = load_overrides(args.price_overrides)
    existing_path = args.existing or (root / "data" / "vehicle_work_rules.csv.gz")
    existing = load_existing(existing_path)

    works = [work for work in works if work["code"] not in OTHER_CODES]
    maintenance_codes = {work["code"] for work in works if work["category"] == "MAINTENANCE"}
    missing_interval_policy = sorted(maintenance_codes - set(interval_policy))
    extra_interval_policy = sorted(set(interval_policy) - maintenance_codes)
    if missing_interval_policy:
        raise ValueError("Missing explicit maintenance interval policy: " + ", ".join(missing_interval_policy))
    if extra_interval_policy:
        raise ValueError("Interval policy contains removed/unknown work codes: " + ", ".join(extra_interval_policy))

    trait_path = root / "base-input" / "vehicle_traits.csv"
    with trait_path.open(encoding="utf-8-sig", newline="") as handle:
        traits = [load_traits(row) for row in csv.DictReader(handle)]

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.audit.parent.mkdir(parents=True, exist_ok=True)
    args.removed_applicability.parent.mkdir(parents=True, exist_ok=True)
    counts = Counter()
    by_tier = defaultdict(Counter)
    rules_per_variant = Counter()
    final_removals_by_work = Counter()

    output_fields = ["variant_code", "work_code", "interval_km", "interval_months", "estimated_price"]
    audit_fields = [
        "variant_code", "work_code", "category", "tier", "state",
        "old_price", "new_price", "price_action",
        "old_interval_km", "new_interval_km",
        "old_interval_months", "new_interval_months", "interval_action",
    ]
    removed_fields = ["variant_code", "work_code", "reason"]

    with gzip.open(args.output, "wt", encoding="utf-8", newline="") as output_handle, \
         gzip.open(args.audit, "wt", encoding="utf-8", newline="") as audit_handle, \
         args.removed_applicability.open("w", encoding="utf-8", newline="") as removed_handle:
        output_writer = csv.DictWriter(output_handle, fieldnames=output_fields)
        audit_writer = csv.DictWriter(audit_handle, fieldnames=audit_fields)
        removed_writer = csv.DictWriter(removed_handle, fieldnames=removed_fields)
        output_writer.writeheader()
        audit_writer.writeheader()
        removed_writer.writeheader()

        for traits_row in traits:
            traits_row["_price_factors"] = price_factors(traits_row, model)
            variant_code = traits_row["variant_code"]

            for work in works:
                base_state, base_reason = decision(traits_row, work)
                state, reason = final_applicability(traits_row, work)
                counts["evaluated_pairs"] += 1
                if state == "NOT_APPLICABLE":
                    counts["not_applicable_pairs"] += 1
                    if base_state != "NOT_APPLICABLE":
                        removed_writer.writerow({
                            "variant_code": variant_code,
                            "work_code": work["code"],
                            "reason": reason,
                        })
                        counts["final_obvious_hardware_rules_removed"] += 1
                        final_removals_by_work[work["code"]] += 1
                    continue

                key = (variant_code, work["code"])
                old_row = existing.get(key)
                candidate = calculated_price(traits_row, work, model, price_overrides)
                price, price_action = choose_price(old_row, candidate, traits_row, work)
                if price <= 0:
                    raise ValueError(f"Non-positive price for {variant_code} / {work['code']}")

                interval_km = None
                interval_months = None
                interval_action = "REPAIR_NO_INTERVAL"
                if work["category"] == "MAINTENANCE":
                    interval_km, interval_months, interval_action = choose_interval(
                        old_row, interval_policy[work["code"]], traits_row)
                    if interval_km is None and interval_months is None:
                        raise ValueError(f"Empty maintenance interval for {variant_code} / {work['code']}")

                # Strong sanity check requested by the product decision.
                if traits_row.get("fuel_class") == "BEV" and work["code"] in ENGINE_ONLY_CHECK_CODES:
                    raise ValueError(f"BEV received combustion-only work: {variant_code} / {work['code']}")

                output_writer.writerow({
                    "variant_code": variant_code,
                    "work_code": work["code"],
                    "interval_km": interval_km or "",
                    "interval_months": interval_months or "",
                    "estimated_price": f"{price:.2f}",
                })

                audit_writer.writerow({
                    "variant_code": variant_code,
                    "work_code": work["code"],
                    "category": work["category"],
                    "tier": traits_row.get("tier", ""),
                    "state": state,
                    "old_price": (old_row or {}).get("estimated_price", ""),
                    "new_price": f"{price:.2f}",
                    "price_action": price_action,
                    "old_interval_km": (old_row or {}).get("interval_km", ""),
                    "new_interval_km": interval_km or "",
                    "old_interval_months": (old_row or {}).get("interval_months", ""),
                    "new_interval_months": interval_months or "",
                    "interval_action": interval_action,
                })

                counts["written_rules"] += 1
                counts["maintenance_rules"] += int(work["category"] == "MAINTENANCE")
                counts["repair_rules"] += int(work["category"] == "REPAIR")
                counts["price_" + price_action.lower()] += 1
                counts["interval_" + interval_action.lower()] += 1
                by_tier[traits_row.get("tier", "MAINSTREAM")]["rules"] += 1
                rules_per_variant[variant_code] += 1

    if len(traits) != 30366:
        raise ValueError(f"Expected 30366 variants, got {len(traits)}")
    if not rules_per_variant or min(rules_per_variant.values()) <= 0:
        raise ValueError("At least one variant has no applicable rule")

    summary = {
        "variant_count": len(traits),
        "work_count_after_other_removal": len(works),
        "maintenance_work_count": len(maintenance_codes),
        "counts": dict(counts),
        "rules_per_variant_min": min(rules_per_variant.values()),
        "rules_per_variant_max": max(rules_per_variant.values()),
        "tier_counts": {tier: dict(values) for tier, values in by_tier.items()},
        "policy": {
            "runtime_fallback": False,
            "other_works_removed": sorted(OTHER_CODES),
            "not_applicable_rows_stored": False,
            "existing_prices_preserved_unless_low_outlier": True,
            "existing_intervals_preserved": True,
            "obvious_drivetrain_conflicts_removed": True,
        },
        "final_hardware_removals_by_work": dict(final_removals_by_work),
    }
    args.summary.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
