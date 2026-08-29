"""
pdf_report.py
--------------
Turns a ComplianceReport (from rules_engine.run_compliance_check) into a
polished PDF, ready for download from the Streamlit UI.

Usage:
    from backend.rules_engine import run_compliance_check
    from backend.pdf_report import generate_pdf_report

    report = run_compliance_check(label_data)
    pdf_path = generate_pdf_report(report, label_data, output_path="report.pdf")
"""

from datetime import datetime
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable,
)


def _build_styles():
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle(
        name="ReportTitle", parent=styles["Title"], fontSize=18, spaceAfter=4,
    ))
    styles.add(ParagraphStyle(
        name="SubTitle", parent=styles["Normal"], fontSize=10, textColor=colors.grey,
    ))
    styles.add(ParagraphStyle(
        name="SectionHeading", parent=styles["Heading2"], spaceBefore=14, spaceAfter=6,
    ))
    styles.add(ParagraphStyle(
        name="IssueFail", parent=styles["Normal"], textColor=colors.HexColor("#B00020"),
        spaceAfter=4,
    ))
    styles.add(ParagraphStyle(
        name="IssueWarn", parent=styles["Normal"], textColor=colors.HexColor("#8A6D00"),
        spaceAfter=4,
    ))
    return styles


def generate_pdf_report(report, label_data: dict, output_path: str = "compliance_report.pdf") -> str:
    """
    report: a rules_engine.ComplianceReport instance
    label_data: the original dict passed into run_compliance_check (used to
                show what was read off the label)
    output_path: where to write the PDF

    Returns the output_path for convenience.
    """
    styles = _build_styles()
    doc = SimpleDocTemplate(
        output_path, pagesize=A4,
        topMargin=20 * mm, bottomMargin=20 * mm, leftMargin=18 * mm, rightMargin=18 * mm,
    )
    story = []

    # --- Header -----------------------------------------------------------
    story.append(Paragraph("Legal Metrology Compliance Report", styles["ReportTitle"]))
    story.append(Paragraph(
        "Legal Metrology (Packaged Commodities) Rules, 2011", styles["SubTitle"],
    ))
    story.append(Paragraph(
        f"Generated: {datetime.now().strftime('%d %b %Y, %H:%M')}", styles["SubTitle"],
    ))
    story.append(Spacer(1, 8))
    story.append(HRFlowable(width="100%", color=colors.HexColor("#CCCCCC")))
    story.append(Spacer(1, 10))

    # --- Verdict banner -----------------------------------------------------
    verdict_text = "COMPLIANT" if report.passed else "NON-COMPLIANT"
    verdict_color = colors.HexColor("#1B7A1B") if report.passed else colors.HexColor("#B00020")
    verdict_table = Table([[Paragraph(
        f"<b>Overall result: {verdict_text}</b>",
        ParagraphStyle(name="Verdict", parent=styles["Normal"], textColor=colors.white, fontSize=13),
    )]], colWidths=[170 * mm])
    verdict_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), verdict_color),
        ("TOPPADDING", (0, 0), (-1, -1), 8),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
    ]))
    story.append(verdict_table)
    story.append(Spacer(1, 14))

    # --- Product details ----------------------------------------------------
    story.append(Paragraph("Product Details (as read from label)", styles["SectionHeading"]))
    detail_rows = [
        ["Field", "Value"],
        ["Product / commodity name", str(label_data.get("commodity_name", "—"))],
        ["Manufacturer / packer", str(label_data.get("manufacturer_name", "—"))],
        ["Address", str(label_data.get("manufacturer_address", "—"))],
        ["Net quantity", f"{label_data.get('net_quantity_value', '—')} {label_data.get('net_quantity_unit', '')}"],
        ["MRP declaration", str(label_data.get("mrp_text", "—"))],
        ["Month/Year of manufacture", str(label_data.get("mfg_month_year", "—"))],
    ]
    detail_table = Table(detail_rows, colWidths=[55 * mm, 115 * mm])
    detail_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#EFEFEF")),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#DDDDDD")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("FONTSIZE", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    story.append(detail_table)
    story.append(Spacer(1, 14))

    # --- Declaration checklist ----------------------------------------------
    story.append(Paragraph("Mandatory Declaration Checklist (Rule 6)", styles["SectionHeading"]))
    check_rows = [["Declaration", "Status"]]
    status_labels = {
        "present": "OK",
        "missing": "MISSING",
        "exempt": "Exempt for this commodity",
    }
    field_labels = {
        "manufacturer_name": "Manufacturer / packer / importer name",
        "manufacturer_address": "Manufacturer / packer / importer address",
        "commodity_name": "Common / generic name",
        "net_quantity_value": "Net quantity",
        "mrp_text": "Retail sale price (MRP)",
        "mfg_month_year": "Month & year of manufacture",
        "consumer_care": "Consumer care details (name, address, phone)",
    }
    for key, label in field_labels.items():
        status = report.checks.get(key, "—")
        if isinstance(status, str):
            display_status = status_labels.get(status, status)
        else:
            display_status = str(status)
        check_rows.append([label, display_status])

    check_table = Table(check_rows, colWidths=[115 * mm, 55 * mm])
    check_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#EFEFEF")),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#DDDDDD")),
        ("FONTSIZE", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    story.append(check_table)
    story.append(Spacer(1, 14))

    # --- MPE detail, if applicable -------------------------------------------
    mpe = report.checks.get("mpe")
    if mpe:
        story.append(Paragraph("Maximum Permissible Error (First Schedule)", styles["SectionHeading"]))
        mpe_text = (
            f"Applicable rule: {mpe['rule_value']}"
            f"{'%' if mpe['mode'] == 'percent' else ' ' + mpe['base_unit']} "
            f"({mpe['mode']}) &rarr; allowed deficiency of "
            f"{mpe['mpe_in_base_unit']} {mpe['base_unit']} on a declared "
            f"quantity of {mpe['base_qty']} {mpe['base_unit']}."
        )
        story.append(Paragraph(mpe_text, styles["Normal"]))
        story.append(Spacer(1, 10))

    # --- Issues --------------------------------------------------------------
    story.append(Paragraph("Issues Found", styles["SectionHeading"]))
    if not report.issues:
        story.append(Paragraph("No issues found.", styles["Normal"]))
    else:
        for issue in report.issues:
            style = styles["IssueFail"] if issue.severity == "fail" else styles["IssueWarn"]
            tag = "FAIL" if issue.severity == "fail" else "WARNING"
            story.append(Paragraph(f"[{tag}] ({issue.rule_ref}) {issue.message}", style))

    story.append(Spacer(1, 16))
    story.append(HRFlowable(width="100%", color=colors.HexColor("#CCCCCC")))
    story.append(Spacer(1, 6))
    story.append(Paragraph(
        "This report is generated automatically against the Legal Metrology "
        "(Packaged Commodities) Rules, 2011 and is intended as a compliance "
        "aid, not a substitute for review by a Legal Metrology Officer.",
        styles["SubTitle"],
    ))

    doc.build(story)
    return output_path
