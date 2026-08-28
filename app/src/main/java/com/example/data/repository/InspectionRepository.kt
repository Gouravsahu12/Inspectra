package com.example.data.repository

import com.example.data.local.InspectionDao
import com.example.data.local.InspectionEntity
import com.example.model.ComplianceStatus
import com.example.model.ExtractedField
import com.example.model.FieldStatus
import com.example.model.InspectionRecord
import com.example.model.ProductCategory
import com.example.model.Severity
import com.example.model.Violation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class InspectionRepository(private val dao: InspectionDao) {

    val allInspections: Flow<List<InspectionRecord>> = dao.getAllInspections().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getInspectionById(id: String): InspectionRecord? {
        return dao.getInspectionById(id)?.toDomain()
    }

    suspend fun insertInspection(inspection: InspectionRecord) {
        dao.insertInspection(inspection.toEntity())
    }

    suspend fun initializePrepopulatedDataIfEmpty() {
        if (dao.getCount() == 0) {
            val initialList = getInitialSeedInspections()
            dao.insertAll(initialList.map { it.toEntity() })
        }
    }

    companion object {
        fun getInitialSeedInspections(): List<InspectionRecord> {
            val currentTime = System.currentTimeMillis()

            val demoDetergent = InspectionRecord(
                id = "INS-2026-000124",
                productName = "ABC Ultra Clean Laundry Detergent",
                brandManufacturer = "ABC Chemicals Pvt Ltd",
                category = ProductCategory.HOUSEHOLD_CHEMICALS,
                locationName = "Apex Mega Mart, Andheri West, Mumbai",
                facilityType = "Supermarket",
                storeName = "Apex Retail Outlets Ltd",
                inspectorId = "LM-OFFICER-8492",
                inspectorName = "Inspector Rajesh V. Sharma",
                timestamp = currentTime - (25 * 60 * 1000), // 25 mins ago
                complianceScore = 72,
                status = ComplianceStatus.NON_COMPLIANT,
                extractedFields = listOf(
                    ExtractedField(
                        id = "product_name",
                        fieldName = "Product Identity",
                        standardLabel = "Common / Generic Name",
                        extractedValue = "ABC Ultra Clean Detergent Powder",
                        confidence = 0.98f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(b)",
                        boundingBoxLabel = "Front Panel - Main Title"
                    ),
                    ExtractedField(
                        id = "net_quantity",
                        fieldName = "Net Quantity",
                        standardLabel = "Standard Metric Net Qty",
                        extractedValue = "1 kg (1000 g)",
                        confidence = 0.96f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(c)",
                        boundingBoxLabel = "Front Bottom Right"
                    ),
                    ExtractedField(
                        id = "mrp",
                        fieldName = "Maximum Retail Price (MRP)",
                        standardLabel = "MRP inclusive of all taxes",
                        extractedValue = "₹ 120.00 (Incl. of all taxes)",
                        confidence = 0.94f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(e)",
                        boundingBoxLabel = "Top Right Flap"
                    ),
                    ExtractedField(
                        id = "manufacturer_name",
                        fieldName = "Manufacturer Information",
                        standardLabel = "Name & Registered Address",
                        extractedValue = "ABC Chemicals Pvt Ltd, Plot 42, MIDC Industrial Area, Tarapur, Maharashtra - 401506",
                        confidence = 0.92f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(a)",
                        boundingBoxLabel = "Back Panel Lower Half"
                    ),
                    ExtractedField(
                        id = "consumer_care",
                        fieldName = "Consumer Care Details",
                        standardLabel = "Grievance Contact & Email",
                        extractedValue = "Not Detected / Missing",
                        confidence = 0.42f,
                        status = FieldStatus.VIOLATION,
                        ruleReference = "Rule 6(1)(f)",
                        boundingBoxLabel = "Back Panel Mandatory Region"
                    ),
                    ExtractedField(
                        id = "packed_date",
                        fieldName = "Packaging / Mfg Date",
                        standardLabel = "Month & Year of Packing",
                        extractedValue = "07/2026",
                        confidence = 0.91f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(d)",
                        boundingBoxLabel = "Batch Stamp Near Barcode"
                    ),
                    ExtractedField(
                        id = "country_origin",
                        fieldName = "Country of Origin",
                        standardLabel = "Country of Manufacture",
                        extractedValue = "India",
                        confidence = 0.89f,
                        status = FieldStatus.VERIFIED,
                        ruleReference = "Rule 6(1)(da)",
                        boundingBoxLabel = "Back Bottom Margin"
                    )
                ),
                violations = listOf(
                    Violation(
                        id = "VIO-00124-1",
                        ruleNumber = "Rule 6(1)(f)",
                        ruleTitle = "Consumer Care & Grievance Redressal Declaration Missing",
                        legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                        severity = Severity.HIGH,
                        observedValue = "Not Detected on any analyzed panel",
                        expectedRequirement = "Officer designation, telephone/toll-free number, and grievance email ID",
                        explanation = "Required statutory consumer care information was not identified in the package images scanned by the enforcement unit.",
                        ruleExcerpt = "Rule 6(1)(f): Every package shall bear the name, address, telephone number and e-mail address of the person who may be contacted by the consumer in case of a complaint.",
                        evidenceDrawableName = "img_detergent_package",
                        boundingBoxHighlight = "Back Panel - Grievance Block (Absence Confirmed)"
                    ),
                    Violation(
                        id = "VIO-00124-2",
                        ruleNumber = "Rule 7",
                        ruleTitle = "Font Size Proportion Verification Warning",
                        legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                        severity = Severity.MEDIUM,
                        observedValue = "Numeral height approx 2.1mm (Net Qty area 450cm²)",
                        expectedRequirement = "Minimum numeral height 4.0mm for principal display panel area > 200cm²",
                        explanation = "The font height of the net quantity declaration appears borderline lower than the mandatory statutory table under Rule 7.",
                        ruleExcerpt = "Rule 7(1): The height of any numeral and letter in the declaration on the principal display panel shall not be less than the minimum prescribed in Table 1.",
                        evidenceDrawableName = "img_detergent_package",
                        boundingBoxHighlight = "Front Panel Net Weight Label"
                    )
                ),
                imageDrawableNames = listOf("img_detergent_package"),
                officerNotes = "Noticed at shelf #4 during routine retail compliance sweep. Consumer care contact totally absent. Notice for explanation recommended.",
                manufacturerRiskLevel = "HIGH"
            )

            val demoEdibleOil = InspectionRecord(
                id = "INS-2026-000123",
                productName = "Golden Harvest Pure Sunflower Oil",
                brandManufacturer = "Kriti Agro Industries Ltd",
                category = ProductCategory.EDIBLE_OILS,
                locationName = "Reliance Smart Superstore, Malad West",
                facilityType = "Supermarket",
                storeName = "Reliance Retail Ventures",
                inspectorId = "LM-OFFICER-8492",
                inspectorName = "Inspector Rajesh V. Sharma",
                timestamp = currentTime - (75 * 60 * 1000), // 1.25 hrs ago
                complianceScore = 100,
                status = ComplianceStatus.COMPLIANT,
                extractedFields = listOf(
                    ExtractedField(id = "product_name", fieldName = "Product Identity", standardLabel = "Generic Name", extractedValue = "Refined Sunflower Oil", confidence = 0.99f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(b)"),
                    ExtractedField(id = "net_quantity", fieldName = "Net Quantity", standardLabel = "Volume / Weight", extractedValue = "1 Litre (Net Volume)", confidence = 0.98f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(c)"),
                    ExtractedField(id = "mrp", fieldName = "Maximum Retail Price", standardLabel = "MRP (All Taxes)", extractedValue = "₹ 165.00 (Incl. of all taxes) / ₹165/L", confidence = 0.97f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(e)"),
                    ExtractedField(id = "manufacturer_name", fieldName = "Manufacturer Details", standardLabel = "Name & Address", extractedValue = "Kriti Agro Industries Ltd, Plot 18-A, Industrial Growth Centre, Dewas, MP - 455001", confidence = 0.95f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(a)"),
                    ExtractedField(id = "consumer_care", fieldName = "Consumer Care", standardLabel = "Email & Helpline", extractedValue = "care@kritiagro.com | Toll-Free: 1800-209-4455 | Officer: Grievance Cell", confidence = 0.96f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(f)"),
                    ExtractedField(id = "packed_date", fieldName = "Date of Packing", standardLabel = "MM/YYYY", extractedValue = "08/2026", confidence = 0.94f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(d)"),
                    ExtractedField(id = "country_origin", fieldName = "Country of Origin", standardLabel = "Country", extractedValue = "India", confidence = 0.99f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(da)")
                ),
                violations = emptyList(),
                imageDrawableNames = listOf("img_edible_oil_package"),
                officerNotes = "All 7 statutory declarations fully verified. Font sizes and dual unit pricing fully compliant.",
                manufacturerRiskLevel = "LOW"
            )

            val demoBiscuits = InspectionRecord(
                id = "INS-2026-000122",
                productName = "Crunch Delight Butter Cookies",
                brandManufacturer = "Delight Bakers & Confectionery LLP",
                category = ProductCategory.FOOD_BEVERAGES,
                locationName = "Shree Krishna Provision Store, Borivali",
                facilityType = "Retail Grocery",
                storeName = "Shree Krishna General Trading",
                inspectorId = "LM-OFFICER-8492",
                inspectorName = "Inspector Rajesh V. Sharma",
                timestamp = currentTime - (3 * 3600 * 1000), // 3 hrs ago
                complianceScore = 84,
                status = ComplianceStatus.WARNING,
                extractedFields = listOf(
                    ExtractedField(id = "product_name", fieldName = "Product Identity", standardLabel = "Common Name", extractedValue = "Butter Cookies / Biscuits", confidence = 0.97f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(b)"),
                    ExtractedField(id = "net_quantity", fieldName = "Net Quantity", standardLabel = "Standard Units", extractedValue = "200 g", confidence = 0.96f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(c)"),
                    ExtractedField(id = "mrp", fieldName = "Maximum Retail Price", standardLabel = "MRP format", extractedValue = "₹ 45.00 (Incl. of all taxes)", confidence = 0.95f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(e)"),
                    ExtractedField(id = "manufacturer_name", fieldName = "Manufacturer", standardLabel = "Registered Address", extractedValue = "Delight Bakers LLP, GIDC Estate, Vapi, Gujarat", confidence = 0.88f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(a)"),
                    ExtractedField(id = "consumer_care", fieldName = "Consumer Care", standardLabel = "Care Contact", extractedValue = "Email: feedback@delightbakers.com", confidence = 0.72f, status = FieldStatus.WARNING, ruleReference = "Rule 6(1)(f)"),
                    ExtractedField(id = "packed_date", fieldName = "Date of Packing", standardLabel = "Date Code", extractedValue = "06/2026", confidence = 0.93f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(d)")
                ),
                violations = listOf(
                    Violation(
                        id = "VIO-00122-1",
                        ruleNumber = "Rule 6(1)(f)",
                        ruleTitle = "Incomplete Consumer Care Declaration (Phone Missing)",
                        legalActRef = "Legal Metrology (Packaged Commodities) Rules, 2011",
                        severity = Severity.LOW,
                        observedValue = "Email provided but telephone / helpline absent",
                        expectedRequirement = "Both telephone number and email address required",
                        explanation = "Rule 6(1)(f) mandates that both telephone contact and email address must be provided for consumer grievance.",
                        ruleExcerpt = "Rule 6(1)(f): Every package shall bear telephone number and e-mail address.",
                        evidenceDrawableName = "img_snack_package",
                        boundingBoxHighlight = "Back Panel - Contact Box"
                    )
                ),
                imageDrawableNames = listOf("img_snack_package"),
                officerNotes = "Minor non-conformance regarding phone helpline. Warning notice issued to packer.",
                manufacturerRiskLevel = "LOW"
            )

            val demoRice = InspectionRecord(
                id = "INS-2026-000121",
                productName = "Royal Heritage Basmati Rice 5kg",
                brandManufacturer = "Royal Grains International",
                category = ProductCategory.PACKAGED_GRAINS,
                locationName = "Central Agro Warehouse, Vashi APMC Market",
                facilityType = "Warehouse / Wholesale",
                storeName = "APMC Mandi Block-B",
                inspectorId = "LM-OFFICER-8492",
                inspectorName = "Inspector Rajesh V. Sharma",
                timestamp = currentTime - (24 * 3600 * 1000), // Yesterday
                complianceScore = 95,
                status = ComplianceStatus.COMPLIANT,
                extractedFields = listOf(
                    ExtractedField(id = "product_name", fieldName = "Product Identity", standardLabel = "Generic Name", extractedValue = "Traditional Aged Basmati Rice", confidence = 0.99f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(b)"),
                    ExtractedField(id = "net_quantity", fieldName = "Net Quantity", standardLabel = "Standard Units", extractedValue = "5 kg", confidence = 0.99f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(c)"),
                    ExtractedField(id = "mrp", fieldName = "Maximum Retail Price", standardLabel = "MRP (All Taxes)", extractedValue = "₹ 620.00 (Incl. of all taxes) ₹124/kg", confidence = 0.98f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(e)"),
                    ExtractedField(id = "manufacturer_name", fieldName = "Manufacturer", standardLabel = "Full Address", extractedValue = "Royal Grains Int., Taraori, Karnal, Haryana - 132116", confidence = 0.96f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(a)"),
                    ExtractedField(id = "consumer_care", fieldName = "Consumer Care", standardLabel = "Contact & Helpline", extractedValue = "Toll Free: 1800-419-7000, Email: customercare@royalgrains.in", confidence = 0.97f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(f)"),
                    ExtractedField(id = "packed_date", fieldName = "Date of Packing", standardLabel = "MM/YYYY", extractedValue = "05/2026", confidence = 0.95f, status = FieldStatus.VERIFIED, ruleReference = "Rule 6(1)(d)")
                ),
                violations = emptyList(),
                imageDrawableNames = listOf("img_snack_package"),
                officerNotes = "Verified 50 bags randomly in batch #RHB-2605. Standard weight & declaration compliant.",
                manufacturerRiskLevel = "LOW"
            )

            return listOf(demoDetergent, demoEdibleOil, demoBiscuits, demoRice)
        }
    }
}

// Helpers for JSON conversion
fun InspectionEntity.toDomain(): InspectionRecord {
    val fieldsList = mutableListOf<ExtractedField>()
    try {
        val array = JSONArray(extractedFieldsJson)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            fieldsList.add(
                ExtractedField(
                    id = obj.optString("id"),
                    fieldName = obj.optString("fieldName"),
                    standardLabel = obj.optString("standardLabel"),
                    extractedValue = obj.optString("extractedValue"),
                    confidence = obj.optDouble("confidence", 0.9).toFloat(),
                    status = FieldStatus.valueOf(obj.optString("status", FieldStatus.VERIFIED.name)),
                    ruleReference = obj.optString("ruleReference"),
                    isEditable = obj.optBoolean("isEditable", true),
                    boundingBoxLabel = obj.optString("boundingBoxLabel")
                )
            )
        }
    } catch (_: Exception) { }

    val violationsList = mutableListOf<Violation>()
    try {
        val array = JSONArray(violationsJson)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            violationsList.add(
                Violation(
                    id = obj.optString("id"),
                    ruleNumber = obj.optString("ruleNumber"),
                    ruleTitle = obj.optString("ruleTitle"),
                    legalActRef = obj.optString("legalActRef"),
                    severity = Severity.valueOf(obj.optString("severity", Severity.MEDIUM.name)),
                    observedValue = obj.optString("observedValue"),
                    expectedRequirement = obj.optString("expectedRequirement"),
                    explanation = obj.optString("explanation"),
                    ruleExcerpt = obj.optString("ruleExcerpt"),
                    evidenceDrawableName = obj.optString("evidenceDrawableName", "img_detergent_package"),
                    boundingBoxHighlight = obj.optString("boundingBoxHighlight")
                )
            )
        }
    } catch (_: Exception) { }

    val images = imageDrawableNames.split(",").filter { it.isNotBlank() }

    return InspectionRecord(
        id = id,
        productName = productName,
        brandManufacturer = brandManufacturer,
        category = try { ProductCategory.valueOf(category) } catch (_: Exception) { ProductCategory.HOUSEHOLD_CHEMICALS },
        locationName = locationName,
        facilityType = facilityType,
        storeName = storeName,
        inspectorId = inspectorId,
        inspectorName = inspectorName,
        timestamp = timestamp,
        complianceScore = complianceScore,
        status = try { ComplianceStatus.valueOf(status) } catch (_: Exception) { ComplianceStatus.NON_COMPLIANT },
        extractedFields = fieldsList,
        violations = violationsList,
        imageDrawableNames = if (images.isEmpty()) listOf("img_detergent_package") else images,
        officerNotes = officerNotes,
        isReportGenerated = isReportGenerated,
        manufacturerRiskLevel = manufacturerRiskLevel
    )
}

fun InspectionRecord.toEntity(): InspectionEntity {
    val fieldsArray = JSONArray()
    extractedFields.forEach { f ->
        val obj = JSONObject()
        obj.put("id", f.id)
        obj.put("fieldName", f.fieldName)
        obj.put("standardLabel", f.standardLabel)
        obj.put("extractedValue", f.extractedValue)
        obj.put("confidence", f.confidence)
        obj.put("status", f.status.name)
        obj.put("ruleReference", f.ruleReference)
        obj.put("isEditable", f.isEditable)
        obj.put("boundingBoxLabel", f.boundingBoxLabel)
        fieldsArray.put(obj)
    }

    val violationsArray = JSONArray()
    violations.forEach { v ->
        val obj = JSONObject()
        obj.put("id", v.id)
        obj.put("ruleNumber", v.ruleNumber)
        obj.put("ruleTitle", v.ruleTitle)
        obj.put("legalActRef", v.legalActRef)
        obj.put("severity", v.severity.name)
        obj.put("observedValue", v.observedValue)
        obj.put("expectedRequirement", v.expectedRequirement)
        obj.put("explanation", v.explanation)
        obj.put("ruleExcerpt", v.ruleExcerpt)
        obj.put("evidenceDrawableName", v.evidenceDrawableName)
        obj.put("boundingBoxHighlight", v.boundingBoxHighlight)
        violationsArray.put(obj)
    }

    return InspectionEntity(
        id = id,
        productName = productName,
        brandManufacturer = brandManufacturer,
        category = category.name,
        locationName = locationName,
        facilityType = facilityType,
        storeName = storeName,
        inspectorId = inspectorId,
        inspectorName = inspectorName,
        timestamp = timestamp,
        complianceScore = complianceScore,
        status = status.name,
        extractedFieldsJson = fieldsArray.toString(),
        violationsJson = violationsArray.toString(),
        imageDrawableNames = imageDrawableNames.joinToString(","),
        officerNotes = officerNotes,
        isReportGenerated = isReportGenerated,
        manufacturerRiskLevel = manufacturerRiskLevel
    )
}
