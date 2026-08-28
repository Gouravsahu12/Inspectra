package com.example.engine

import com.example.model.ComplianceStatus
import com.example.model.ExtractedField
import com.example.model.FieldStatus
import com.example.model.ProductCategory
import com.example.model.Severity
import com.example.model.Violation

object ComplianceEngine {

    /**
     * Evaluates extracted product declarations deterministically against the
     * Legal Metrology (Packaged Commodities) Rules, 2011 (LMPC Rules).
     */
    fun evaluateCompliance(
        productName: String,
        category: ProductCategory,
        fields: List<ExtractedField>,
        evidenceImageName: String
    ): Pair<Int, List<Violation>> {
        val violations = mutableListOf<Violation>()
        var scoreDeductions = 0

        // 1. Check Product Name / Common Generic Name - Rule 6(1)(b)
        val nameField = fields.find { it.id == "product_name" }
        if (nameField == null || nameField.extractedValue.isBlank() || nameField.status == FieldStatus.NOT_DETECTED) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-B",
                    ruleNumber = "Rule 6(1)(b)",
                    ruleTitle = "Common or Generic Name of Commodity Missing",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.HIGH,
                    observedValue = nameField?.extractedValue ?: "Not Detected",
                    expectedRequirement = "Clear generic/common name on principal display panel",
                    explanation = "Every package must bear the generic or common name of the commodity contained in the package.",
                    ruleExcerpt = "Rule 6(1)(b): The generic name of the commodity contained in the package and for packages containing more than one product, the name and number or quantity of each product shall be mentioned.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Principal Display Panel (Front)"
                )
            )
            scoreDeductions += 25
        }

        // 2. Check Net Quantity - Rule 6(1)(c) & Rule 7
        val netQtyField = fields.find { it.id == "net_quantity" }
        if (netQtyField == null || netQtyField.extractedValue.isBlank() || netQtyField.status == FieldStatus.NOT_DETECTED) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-C",
                    ruleNumber = "Rule 6(1)(c)",
                    ruleTitle = "Net Quantity Declaration Missing or Non-Standard",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.HIGH,
                    observedValue = netQtyField?.extractedValue ?: "Not Detected",
                    expectedRequirement = "Net quantity in standard metric units (kg, g, L, ml, N)",
                    explanation = "Net quantity was not declared or fails to comply with standard metric units of measurement.",
                    ruleExcerpt = "Rule 6(1)(c): The net quantity, in terms of the standard unit of weight or measure, of the commodity contained in the package shall be declared.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Front Bottom Area (Net Qty Area)"
                )
            )
            scoreDeductions += 30
        }

        // 3. Check MRP - Rule 6(1)(e)
        val mrpField = fields.find { it.id == "mrp" }
        if (mrpField == null || mrpField.extractedValue.isBlank() || mrpField.status == FieldStatus.NOT_DETECTED) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-E",
                    ruleNumber = "Rule 6(1)(e)",
                    ruleTitle = "Maximum Retail Price (MRP) Declaration Defect",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.HIGH,
                    observedValue = mrpField?.extractedValue ?: "Not Detected",
                    expectedRequirement = "Maximum Retail Price ₹ xx.xx (inclusive of all taxes) with unit sale price",
                    explanation = "MRP declaration is missing, illegible, or does not indicate tax inclusion status.",
                    ruleExcerpt = "Rule 6(1)(e): The retail sale price of the package shall be clearly indicated in the form of Maximum Retail Price (MRP) inclusive of all taxes.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Back Panel / Top Barcode Region"
                )
            )
            scoreDeductions += 25
        } else if (!mrpField.extractedValue.contains("₹") && !mrpField.extractedValue.contains("Rs", ignoreCase = true)) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-E-CURR",
                    ruleNumber = "Rule 6(1)(e)",
                    ruleTitle = "MRP Missing Standard Currency Symbol",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.LOW,
                    observedValue = mrpField.extractedValue,
                    expectedRequirement = "Price formatted with ₹ or Rs. symbol",
                    explanation = "The declared price lacks the standard Indian Rupee symbol format.",
                    ruleExcerpt = "Rule 6(1)(e): The retail sale price of the package must be prominently declared in INR.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "MRP Stamp Area"
                )
            )
            scoreDeductions += 10
        }

        // 4. Check Manufacturer / Packer Details - Rule 6(1)(a)
        val mfgField = fields.find { it.id == "manufacturer_name" }
        if (mfgField == null || mfgField.extractedValue.isBlank() || mfgField.status == FieldStatus.NOT_DETECTED) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-A",
                    ruleNumber = "Rule 6(1)(a)",
                    ruleTitle = "Manufacturer / Packer / Importer Information Missing",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.HIGH,
                    observedValue = mfgField?.extractedValue ?: "Not Detected",
                    expectedRequirement = "Complete registered corporate name and full physical address",
                    explanation = "The complete name and address of the manufacturer, packer, or importer was not identified.",
                    ruleExcerpt = "Rule 6(1)(a): The name and complete address of the manufacturer or where the manufacturer is not the packer, the name and address of the manufacturer and packer shall be mentioned.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Back Panel - Manufacturer Block"
                )
            )
            scoreDeductions += 25
        }

        // 5. Check Consumer Care Information - Rule 6(1)(f)
        val consumerCareField = fields.find { it.id == "consumer_care" }
        if (consumerCareField == null || consumerCareField.extractedValue.isBlank() || consumerCareField.status == FieldStatus.NOT_DETECTED || consumerCareField.status == FieldStatus.VIOLATION) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-F",
                    ruleNumber = "Rule 6(1)(f)",
                    ruleTitle = "Consumer Care & Grievance Redressal Declaration Missing",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.HIGH,
                    observedValue = consumerCareField?.extractedValue.takeIf { !it.isNullOrBlank() } ?: "Not Detected on Package",
                    expectedRequirement = "Officer designation, telephone/toll-free number, and email ID for consumer complaints",
                    explanation = "Required statutory consumer care information (name/designation, phone number, or email address) was not detected on any scanned panel of the package.",
                    ruleExcerpt = "Rule 6(1)(f): The name, address, telephone number and e-mail address of the person who can be contacted by the consumer in case of a complaint or query must be declared.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Back Panel - Grievance / Feedback Zone"
                )
            )
            scoreDeductions += 28
        }

        // 6. Check Date of Packing / Manufacture - Rule 6(1)(d)
        val dateField = fields.find { it.id == "packed_date" }
        if (dateField == null || dateField.extractedValue.isBlank() || dateField.status == FieldStatus.NOT_DETECTED) {
            violations.add(
                Violation(
                    id = "VIO-LMPC-6-1-D",
                    ruleNumber = "Rule 6(1)(d)",
                    ruleTitle = "Month and Year of Manufacture/Packing Missing",
                    legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                    severity = Severity.MEDIUM,
                    observedValue = dateField?.extractedValue ?: "Not Detected",
                    expectedRequirement = "Month and year of manufacture or packing in MM/YYYY format",
                    explanation = "The manufacturing or packaging month and year must be explicitly stated.",
                    ruleExcerpt = "Rule 6(1)(d): The month and the year in which the commodity is manufactured or packed or imported shall be declared.",
                    evidenceDrawableName = evidenceImageName,
                    boundingBoxHighlight = "Batch / Date Coding Area"
                )
            )
            scoreDeductions += 15
        }

        val calculatedScore = (100 - scoreDeductions).coerceIn(0, 100)
        return Pair(calculatedScore, violations)
    }

    fun determineStatus(score: Int, violations: List<Violation>): ComplianceStatus {
        val hasHighSeverity = violations.any { it.severity == Severity.HIGH }
        return when {
            score >= 90 && violations.isEmpty() -> ComplianceStatus.COMPLIANT
            score >= 75 && !hasHighSeverity -> ComplianceStatus.WARNING
            else -> ComplianceStatus.NON_COMPLIANT
        }
    }
}
