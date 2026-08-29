"""
app_demo.py
------------
Minimal Streamlit app showing how to wire the backend/ modules together:
login -> enter/label data (or plug in your OCR output) -> compliance check
-> PDF download.

Run with:  streamlit run app_demo.py

Drop your OCR pipeline's output straight into `label_data` below instead of
the manual form, once it's ready — everything downstream (rules_engine,
pdf_report) already expects that dict shape.
"""

import streamlit as st

from backend import database, auth
from backend.rules_engine import run_compliance_check
from backend.pdf_report import generate_pdf_report

database.init_db()
st.set_page_config(page_title="Legal Metrology Compliance Checker", layout="centered")


def login_screen():
    st.title("Legal Metrology Compliance Checker")
    tab_login, tab_signup = st.tabs(["Log in", "Sign up"])

    with tab_login:
        username = st.text_input("Username", key="login_user")
        password = st.text_input("Password", type="password", key="login_pass")
        if st.button("Log in"):
            ok, result = auth.login(username, password)
            if ok:
                st.session_state["user"] = result
                st.rerun()
            else:
                st.error(result)

    with tab_signup:
        new_username = st.text_input("Choose a username", key="signup_user")
        new_password = st.text_input("Choose a password", type="password", key="signup_pass")
        if st.button("Create account"):
            ok, msg = auth.signup(new_username, new_password)
            if ok:
                st.success(msg)
            else:
                st.error(msg)


def main_app():
    user = st.session_state["user"]
    st.sidebar.write(f"Logged in as **{user['username']}**")
    if st.sidebar.button("Log out"):
        del st.session_state["user"]
        st.rerun()

    st.title("Check a Product Label")
    st.caption("Fill this in manually for now — swap for your OCR pipeline's output later.")

    with st.form("label_form"):
        commodity_name = st.text_input("Commodity / product name")
        commodity_category = st.selectbox(
            "Category (for standard-size check, optional)",
            ["", "biscuits", "tea", "coffee", "edible_oil", "salt", "cement",
             "mineral_water", "toilet_soap", "atta_flour_suji"],
        )
        manufacturer_name = st.text_input("Manufacturer / packer / importer name")
        manufacturer_address = st.text_area("Manufacturer / packer / importer address")

        col1, col2 = st.columns(2)
        with col1:
            net_quantity_value = st.number_input("Net quantity value", min_value=0.0, step=0.1)
        with col2:
            net_quantity_unit = st.selectbox("Unit", ["g", "kg", "ml", "l", "cm", "m", "count"])

        mrp_text = st.text_input("MRP text (as printed on label)")
        mfg_month_year = st.text_input("Month/Year of manufacture (e.g. 07/2026)")

        st.markdown("**Consumer care details**")
        care_name = st.text_input("Consumer care name")
        care_address = st.text_input("Consumer care address")
        care_phone = st.text_input("Consumer care phone")
        care_email = st.text_input("Consumer care email (optional)")

        declared_numeral_height_mm = st.number_input(
            "Numeral height on label, in mm (measured)", min_value=0.0, step=0.1,
        )
        panel_area_cm2 = st.number_input(
            "Principal display panel area, in cm² (only if declared by length/area/number)",
            min_value=0.0, step=1.0,
        )
        is_blown_or_molded = st.checkbox("Numerals are blown/molded/embossed on the container")

        submitted = st.form_submit_button("Run compliance check")

    if submitted:
        label_data = {
            "commodity_name": commodity_name,
            "commodity_category": commodity_category or None,
            "manufacturer_name": manufacturer_name,
            "manufacturer_address": manufacturer_address,
            "net_quantity_value": net_quantity_value,
            "net_quantity_unit": net_quantity_unit,
            "mrp_text": mrp_text,
            "mfg_month_year": mfg_month_year,
            "consumer_care": {
                "name": care_name, "address": care_address,
                "phone": care_phone, "email": care_email,
            },
            "declared_numeral_height_mm": declared_numeral_height_mm or None,
            "principal_display_panel_area_cm2": panel_area_cm2 or None,
            "is_blown_or_molded": is_blown_or_molded,
        }

        report = run_compliance_check(label_data)
        database.save_report(user["id"], report.product_name, report.passed, {
            "checks": report.checks,
            "issues": [i.__dict__ for i in report.issues],
        })

        if report.passed:
            st.success("COMPLIANT — no blocking issues found.")
        else:
            st.error("NON-COMPLIANT — see issues below.")

        for issue in report.issues:
            if issue.severity == "fail":
                st.write(f"❌ **[{issue.rule_ref}]** {issue.message}")
            else:
                st.write(f"⚠️ **[{issue.rule_ref}]** {issue.message}")

        pdf_path = generate_pdf_report(report, label_data, output_path="latest_report.pdf")
        with open(pdf_path, "rb") as f:
            st.download_button(
                "Download PDF report", f, file_name=f"{commodity_name or 'report'}_compliance.pdf",
                mime="application/pdf",
            )

    st.divider()
    st.subheader("Past reports")
    past = database.get_reports_for_user(user["id"])
    if not past:
        st.caption("No reports yet.")
    for r in past:
        status = "✅ Compliant" if r["passed"] else "❌ Non-compliant"
        st.write(f"{status} — **{r['product_name']}** — {r['created_at'][:16].replace('T', ' ')}")


if "user" not in st.session_state:
    login_screen()
else:
    main_app()
