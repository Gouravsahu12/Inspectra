package com.example.model

enum class ComplianceStatus {
    COMPLIANT,
    WARNING,
    NON_COMPLIANT
}

enum class Severity {
    HIGH,
    MEDIUM,
    LOW
}

enum class FieldStatus {
    VERIFIED,
    WARNING,
    VIOLATION,
    NOT_DETECTED,
    CONFLICT
}

enum class ProductCategory(val displayName: String, val code: String) {
    FOOD_BEVERAGES("Food & Beverages", "CAT-FNB"),
    COSMETICS("Cosmetics", "CAT-COS"),
    HOUSEHOLD_PRODUCTS("Household Products", "CAT-HHP"),
    PERSONAL_CARE("Personal Care", "CAT-PC"),
    ELECTRICAL_CONSUMER_GOODS("Electrical / Consumer Goods", "CAT-ECG"),
    OTHER("Other", "CAT-OTH"),
    // Backward compatibility aliases
    HOUSEHOLD_CHEMICALS("Household Products", "CAT-HHP"),
    EDIBLE_OILS("Food & Beverages", "CAT-FNB"),
    COSMETICS_PERSONAL_CARE("Personal Care", "CAT-PC"),
    PACKAGED_GRAINS("Food & Beverages", "CAT-FNB"),
    GENERAL_COMMODITY("Other", "CAT-OTH")
}

enum class PackageSide(val label: String, val shortName: String, val description: String) {
    FRONT("Front Panel", "Front", "Principal Display Panel, Generic Name, Brand"),
    BACK("Back Panel", "Back", "Statutory Declarations, MRP, Net Qty, Dates"),
    SIDE("Side Panel", "Side", "Manufacturer Address, Ingredients, Batch No"),
    TOP("Top Panel", "Top", "MRP / Packaging Date Flap, Barcode"),
    BOTTOM("Bottom Panel", "Bottom", "Base, Barcode, Container markings"),
    OTHER("Other Angle", "Other", "Additional Inset or Supplemental View")
}

data class CapturedProductImage(
    val id: String,
    val uri: String,
    val side: PackageSide,
    val capturedAt: Long = System.currentTimeMillis(),
    val isFromGallery: Boolean = false,
    val resolution: String = "1920x1080",
    val fileSizeKb: Long = 245L,
    val isSampleAsset: Boolean = false
)

data class ExtractedField(
    val id: String,
    val fieldName: String,
    val standardLabel: String,
    val extractedValue: String,
    val aiExtractedValue: String = extractedValue,
    val inspectorValue: String = extractedValue,
    val confidence: Float = 0.9f, // 0.0 to 1.0
    val status: FieldStatus = FieldStatus.VERIFIED,
    val ruleReference: String = "",
    val isEditable: Boolean = true,
    val boundingBoxLabel: String = "",
    val source: FieldSource = FieldSource.AI_EXTRACTED,
    val boundingBox: BoundingBoxRect? = null,
    val detectedSide: PackageSide? = null,
    val detectedImageUri: String? = null,
    val hasConflict: Boolean = false,
    val conflictingValues: List<String> = emptyList()
)

data class Violation(
    val id: String,
    val ruleNumber: String,
    val ruleTitle: String,
    val legalActRef: String,
    val severity: Severity,
    val observedValue: String,
    val expectedRequirement: String,
    val explanation: String,
    val ruleExcerpt: String,
    val evidenceDrawableName: String = "img_detergent_package",
    val boundingBoxHighlight: String = "Back Panel - Consumer Grievance Zone"
)

data class InspectionRecord(
    val id: String, // e.g. INS-2026-000124
    val productName: String,
    val brandManufacturer: String,
    val category: ProductCategory,
    val locationName: String,
    val facilityType: String,
    val storeName: String,
    val inspectorId: String,
    val inspectorName: String,
    val timestamp: Long,
    val complianceScore: Int, // 0 to 100
    val status: ComplianceStatus,
    val extractedFields: List<ExtractedField>,
    val violations: List<Violation>,
    val imageDrawableNames: List<String>,
    val officerNotes: String = "",
    val isReportGenerated: Boolean = true,
    val manufacturerRiskLevel: String = "MEDIUM"
)

data class InspectorProfile(
    val id: String = "LM-OFFICER-8492",
    val name: String = "Inspector Rajesh V. Sharma",
    val badgeNumber: String = "GOI-LM-WZ-2026",
    val designation: String = "Senior Legal Metrology Officer",
    val department: String = "Department of Consumer Affairs, Legal Metrology Wing",
    val jurisdiction: String = "Western Zone / Mumbai Metro Division-4",
    val totalInspectionsToday: Int = 24,
    val compliantToday: Int = 18,
    val violationsToday: Int = 6,
    val highRiskToday: Int = 2
)

data class ImageQualityMetric(
    val name: String,
    val isPassed: Boolean,
    val message: String
)
