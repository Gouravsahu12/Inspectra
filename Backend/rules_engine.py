"""
rules_engine.py
----------------
Compliance engine for the Legal Metrology (Packaged Commodities) Rules, 2011.

This module takes structured data extracted from a product label (e.g. via your
OCR pipeline) and checks it against the mandatory declarations, Maximum
Permissible Error (MPE) tables, minimum font-size rules, and standard package
sizes defined in the Act.

Input contract
--------------
Your OCR/extraction layer should produce a dict like this and pass it to
`run_compliance_check()`:

    label_data = {
        "manufacturer_name": "ABC Foods Pvt Ltd",
        "manufacturer_address": "Plot 12, MIDC, Nagpur, Maharashtra - 440001",
        "commodity_name": "Refined Sunflower Oil",
        "net_quantity_value": 1.0,
        "net_quantity_unit": "l",          # g, kg, ml, l, cm, m, or "count"
        "mrp_text": "MRP Rs. 180 (incl. of all taxes)",
        "mfg_month_year": "07/2026",
        "consumer_care": {
            "name": "ABC Foods Customer Care",
            "address": "Plot 12, MIDC, Nagpur",
            "phone": "1800-123-4567",
            "email": "care@abcfoods.com",
        },
        "declared_numeral_height_mm": 2.0,   # measured/estimated from label image
        "is_blown_or_molded": False,          # True for embossed glass/plastic containers
        "principal_display_panel_area_cm2": 120.0,  # only needed for length/area/number goods
        "commodity_category": "edible_oil",   # optional, used for Second Schedule lookup
    }

Nothing here talks to a database or a UI — keep this module pure so it's easy
to unit test and reuse from both the Streamlit app and any batch/CLI job.
"""

from dataclasses import dataclass, field
from typing import Optional


# ---------------------------------------------------------------------------
# Rule 6 — Mandatory declarations
# ---------------------------------------------------------------------------

MANDATORY_DECLARATION_FIELDS = [
    ("manufacturer_name", "Name of manufacturer/packer/importer (Rule 6(1)(a))"),
    ("manufacturer_address", "Complete address of manufacturer/packer/importer (Rule 6(1)(a))"),
    ("commodity_name", "Common or generic name of the commodity (Rule 6(1)(b))"),
    ("net_quantity_value", "Net quantity declaration (Rule 6(1)(c))"),
    ("mrp_text", "Retail Sale Price / MRP declaration (Rule 6(1)(e))"),
    ("mfg_month_year", "Month and year of manufacture/pre-packing/import (Rule 6(1)(d))"),
]

# Commodities exempt from the month/year declaration (Rule 6, proviso A)
MONTH_YEAR_EXEMPT_KEYWORDS = ["bidi", "incense stick", "agarbatti", "lpg", "gas cylinder"]

# Commodities exempt from the MRP declaration (Rule 6, proviso C)
MRP_EXEMPT_KEYWORDS = ["bidi", "lpg", "domestic gas cylinder"]

CONSUMER_CARE_FIELDS = ["name", "address", "phone"]  # email is "if available" per Rule 6(2)


# ---------------------------------------------------------------------------
# First Schedule — Maximum Permissible Error (MPE)
# ---------------------------------------------------------------------------
# Table I: weight/volume. Each row is (min_g_ml, max_g_ml, mode, value)
# mode = "percent" -> value is % of declared quantity
# mode = "absolute" -> value is a flat error in g or ml
MPE_WEIGHT_VOLUME_TABLE = [
    (0, 50, "percent", 9.0),
    (50, 100, "absolute", 4.5),
    (100, 200, "percent", 4.5),
    (200, 300, "absolute", 9.0),
    (300, 500, "percent", 3.0),
    (500, 1000, "absolute", 15.0),
    (1000, 10000, "percent", 1.5),
    (10000, 15000, "absolute", 150.0),
    (15000, float("inf"), "percent", 1.0),
]

WEIGHT_UNITS = {"g", "kg"}
VOLUME_UNITS = {"ml", "l"}
LENGTH_UNITS = {"cm", "m"}
AREA_UNITS = {"cm2", "m2", "sqm", "sq_metre"}


def _to_base_weight_volume(value: float, unit: str) -> float:
    """Normalize weight to grams or volume to millilitres."""
    unit = unit.lower()
    if unit == "kg":
        return value * 1000
    if unit == "l":
        return value * 1000
    return value  # already g or ml


def calculate_mpe(declared_value: float, unit: str) -> Optional[dict]:
    """
    Returns the Maximum Permissible Error for a declared weight/volume quantity,
    per First Schedule, Table I. Returns None if the unit isn't weight/volume.
    """
    unit = unit.lower()
    if unit not in WEIGHT_UNITS | VOLUME_UNITS:
        return None

    base_qty = _to_base_weight_volume(declared_value, unit)
    base_unit = "g" if unit in WEIGHT_UNITS else "ml"

    for lo, hi, mode, val in MPE_WEIGHT_VOLUME_TABLE:
        if lo <= base_qty < hi:
            if mode == "percent":
                mpe_value = round(base_qty * val / 100, 1 if base_qty <= 1000 else 0)
                return {
                    "mode": "percent",
                    "rule_value": val,
                    "mpe_in_base_unit": mpe_value,
                    "base_unit": base_unit,
                    "base_qty": base_qty,
                }
            else:
                return {
                    "mode": "absolute",
                    "rule_value": val,
                    "mpe_in_base_unit": val,
                    "base_unit": base_unit,
                    "base_qty": base_qty,
                }
    return None


def calculate_mpe_length_area_number(declared_value: float, kind: str) -> Optional[dict]:
    """
    First Schedule, Table II — length, area, or number.
    kind: "length" | "area" | "number"
    """
    if kind == "length":
        pct = 2.0 if declared_value <= 10 else 1.0
    elif kind == "area":
        pct = 4.0 if declared_value <= 10 else 1.0
    elif kind == "number":
        pct = 2.0
    else:
        return None
    return {"mode": "percent", "rule_value": pct, "mpe_value": round(declared_value * pct / 100, 2)}


def check_actual_quantity(declared_value: float, unit: str, measured_value: float) -> dict:
    """
    Compares a measured/tested net quantity against the declared quantity and
    the applicable MPE, per Rule 19(6). Use this if your pipeline can measure
    the actual product (e.g. from a partner scale) rather than only reading
    the label.
    """
    mpe = calculate_mpe(declared_value, unit)
    if mpe is None:
        return {"checked": False, "reason": f"No MPE rule for unit '{unit}'"}

    base_declared = _to_base_weight_volume(declared_value, unit)
    base_measured = _to_base_weight_volume(measured_value, unit)
    deficiency = base_declared - base_measured

    within_limit = deficiency <= mpe["mpe_in_base_unit"]
    return {
        "checked": True,
        "declared_base": base_declared,
        "measured_base": base_measured,
        "deficiency": round(deficiency, 2),
        "allowed_mpe": mpe["mpe_in_base_unit"],
        "compliant": within_limit,
    }


# ---------------------------------------------------------------------------
# Rule 7 — Minimum numeral height on the principal display panel
# ---------------------------------------------------------------------------
# Table I: keyed by declared weight/volume (g or ml)
FONT_SIZE_TABLE_WEIGHT_VOLUME = [
    (0, 200, 1, 2),
    (200, 500, 2, 4),
    (500, float("inf"), 4, 6),
]

# Table II: keyed by principal display panel area (cm2)
FONT_SIZE_TABLE_PANEL_AREA = [
    (0, 100, 1, 2),
    (100, 500, 2, 4),
    (500, 2500, 4, 6),
    (2500, float("inf"), 6, 6),
]


def required_numeral_height(
    unit: str,
    declared_value: Optional[float] = None,
    panel_area_cm2: Optional[float] = None,
    blown_or_molded: bool = False,
) -> Optional[float]:
    """
    Returns the minimum numeral height (mm) required under Rule 7.
    Use Table I (declared weight/volume) for weight- or volume-declared goods;
    use Table II (panel area) for length/area/number-declared goods.
    """
    if unit.lower() in WEIGHT_UNITS | VOLUME_UNITS and declared_value is not None:
        base_qty = _to_base_weight_volume(declared_value, unit.lower())
        table = FONT_SIZE_TABLE_WEIGHT_VOLUME
    elif panel_area_cm2 is not None:
        base_qty = panel_area_cm2
        table = FONT_SIZE_TABLE_PANEL_AREA
    else:
        return None

    for lo, hi, normal, blown in table:
        if lo <= base_qty < hi:
            return blown if blown_or_molded else normal
    return None


# ---------------------------------------------------------------------------
# Second Schedule — Standard package sizes (subset; extend as needed)
# ---------------------------------------------------------------------------
# Each entry: allowed quantities in grams/millilitres for that commodity category.
SECOND_SCHEDULE_STANDARD_SIZES = {
    "biscuits": [25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 600, 700, 800, 900, 1000],
    "tea": [25, 50, 100, 125, 250, 500, 1000],
    "coffee": [25, 50, 100, 200, 250, 500, 1000],
    "edible_oil": [50, 100, 200, 500, 1000, 2000, 3000, 5000],
    "salt": [50, 100, 200, 500, 750, 1000, 2000, 5000],
    "cement": [1000, 2000, 5000, 10000, 20000, 25000, 40000, 50000],
    "mineral_water": [100, 150, 200, 250, 300, 500, 750, 1000, 1500, 2000, 3000, 4000, 5000],
    "toilet_soap": [25, 50, 75, 100, 125, 150],
    "atta_flour_suji": [100, 200, 500, 1000, 2000, 5000],
}


def check_standard_package_size(commodity_category: str, declared_value: float, unit: str) -> dict:
    """
    Rule 5 / Second Schedule — checks whether a declared quantity for a
    scheduled commodity matches one of the standard sizes. Non-standard sizes
    are legal only if labelled "Not a standard pack size under the Legal
    Metrology (Packaged Commodities) Rules, 2011".
    """
    category = commodity_category.lower().replace(" ", "_")
    if category not in SECOND_SCHEDULE_STANDARD_SIZES:
        return {"checked": False, "reason": "Commodity not in Second Schedule lookup table"}

    base_qty = _to_base_weight_volume(declared_value, unit.lower())
    allowed = SECOND_SCHEDULE_STANDARD_SIZES[category]
    is_standard = base_qty in allowed

    return {
        "checked": True,
        "declared_base_qty": base_qty,
        "is_standard_size": is_standard,
        "allowed_sizes": allowed,
        "note": None if is_standard else (
            "Non-standard pack size — must carry the label "
            "'Not a standard pack size under the Legal Metrology "
            "(Packaged Commodities) Rules, 2011' (Rule 5 proviso)."
        ),
    }


# ---------------------------------------------------------------------------
# Full compliance report
# ---------------------------------------------------------------------------

@dataclass
class ComplianceIssue:
    severity: str          # "fail" | "warning"
    rule_ref: str
    message: str


@dataclass
class ComplianceReport:
    product_name: str
    passed: bool
    issues: list = field(default_factory=list)
    checks: dict = field(default_factory=dict)


def run_compliance_check(label_data: dict) -> ComplianceReport:
    """
    Main entry point. Runs every applicable check against the extracted
    label data and returns a structured ComplianceReport.
    """
    issues = []
    checks = {}
    commodity_name = (label_data.get("commodity_name") or "").lower()

    # 1. Mandatory declarations present (Rule 6)
    for field_key, description in MANDATORY_DECLARATION_FIELDS:
        value = label_data.get(field_key)
        present = value not in (None, "", 0) or (field_key == "net_quantity_value" and value == 0)

        if field_key == "mfg_month_year" and not present:
            if any(k in commodity_name for k in MONTH_YEAR_EXEMPT_KEYWORDS):
                checks[field_key] = "exempt"
                continue
        if field_key == "mrp_text" and not present:
            if any(k in commodity_name for k in MRP_EXEMPT_KEYWORDS):
                checks[field_key] = "exempt"
                continue

        checks[field_key] = "present" if present else "missing"
        if not present:
            issues.append(ComplianceIssue("fail", "Rule 6(1)", f"Missing: {description}"))

    # 2. Consumer care details (Rule 6(2))
    care = label_data.get("consumer_care") or {}
    missing_care = [f for f in CONSUMER_CARE_FIELDS if not care.get(f)]
    if missing_care:
        issues.append(ComplianceIssue(
            "fail", "Rule 6(2)",
            f"Consumer-care declaration incomplete — missing: {', '.join(missing_care)}",
        ))
    checks["consumer_care"] = "complete" if not missing_care else f"missing {missing_care}"

    # 3. MPE check (First Schedule) — only informational unless a measured value is supplied
    unit = (label_data.get("net_quantity_unit") or "").lower()
    net_qty = label_data.get("net_quantity_value")
    if unit in WEIGHT_UNITS | VOLUME_UNITS and net_qty:
        mpe = calculate_mpe(net_qty, unit)
        checks["mpe"] = mpe
        measured = label_data.get("measured_net_quantity")
        if measured is not None:
            mpe_check = check_actual_quantity(net_qty, unit, measured)
            checks["mpe_actual_check"] = mpe_check
            if mpe_check.get("checked") and not mpe_check["compliant"]:
                issues.append(ComplianceIssue(
                    "fail", "Rule 19(6)/First Schedule",
                    f"Measured quantity deficiency ({mpe_check['deficiency']} "
                    f"{mpe_check.get('allowed_mpe') and mpe['base_unit']}) exceeds "
                    f"the maximum permissible error ({mpe_check['allowed_mpe']}).",
                ))

    # 4. Minimum numeral height (Rule 7)
    declared_height = label_data.get("declared_numeral_height_mm")
    if declared_height is not None:
        required_height = required_numeral_height(
            unit=unit,
            declared_value=net_qty,
            panel_area_cm2=label_data.get("principal_display_panel_area_cm2"),
            blown_or_molded=label_data.get("is_blown_or_molded", False),
        )
        checks["numeral_height"] = {"declared": declared_height, "required": required_height}
        if required_height is not None and declared_height < required_height:
            issues.append(ComplianceIssue(
                "fail", "Rule 7",
                f"Numeral height {declared_height}mm is below the required "
                f"{required_height}mm minimum for this quantity/panel size.",
            ))

    # 5. Standard package size (Second Schedule) — informational, not a hard fail
    category = label_data.get("commodity_category")
    if category and unit and net_qty:
        std_check = check_standard_package_size(category, net_qty, unit)
        checks["standard_size"] = std_check
        if std_check.get("checked") and not std_check["is_standard_size"]:
            issues.append(ComplianceIssue("warning", "Rule 5", std_check["note"]))

    # 6. MRP format sanity check (Rule 2(m)) — looks for "incl" / "inclusive of all taxes"
    mrp_text = (label_data.get("mrp_text") or "").lower()
    if mrp_text and "incl" not in mrp_text:
        issues.append(ComplianceIssue(
            "warning", "Rule 2(m)",
            "MRP declaration should state it is inclusive of all taxes "
            "(e.g. 'MRP Rs. XX incl. of all taxes').",
        ))

    passed = not any(i.severity == "fail" for i in issues)
    return ComplianceReport(
        product_name=label_data.get("commodity_name", "Unnamed product"),
        passed=passed,
        issues=issues,
        checks=checks,
    )
