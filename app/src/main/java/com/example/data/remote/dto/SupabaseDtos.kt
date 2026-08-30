package com.example.data.remote.dto

import com.example.model.ComplianceStatus
import com.example.model.ExtractedField
import com.example.model.FieldStatus
import com.example.model.InspectionRecord
import com.example.model.ProductCategory
import com.example.model.Severity
import com.example.model.Violation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class SupabaseInspectionDto(
    @Json(name = "id") val id: String,
    @Json(name = "client_uuid") val clientUuid: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "product_name") val productName: String,
    @Json(name = "brand_manufacturer") val brandManufacturer: String,
    @Json(name = "category") val category: String,
    @Json(name = "location_name") val locationName: String,
    @Json(name = "facility_type") val facilityType: String,
    @Json(name = "store_name") val storeName: String,
    @Json(name = "inspector_id") val inspectorId: String,
    @Json(name = "inspector_name") val inspectorName: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "compliance_score") val complianceScore: Int,
    @Json(name = "status") val status: String,
    @Json(name = "extracted_fields") val extractedFieldsJson: String? = null,
    @Json(name = "violations") val violationsJson: String? = null,
    @Json(name = "image_urls") val imageUrls: String? = null,
    @Json(name = "officer_notes") val officerNotes: String? = null,
    @Json(name = "is_report_generated") val isReportGenerated: Boolean = true,
    @Json(name = "report_url") val reportUrl: String? = null,
    @Json(name = "manufacturer_risk_level") val manufacturerRiskLevel: String = "LOW"
)

@JsonClass(generateAdapter = true)
data class SupabaseScanHistoryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "client_uuid") val clientUuid: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "product_name") val productName: String,
    @Json(name = "brand_manufacturer") val brandManufacturer: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "location_name") val locationName: String? = null,
    @Json(name = "facility_type") val facilityType: String? = null,
    @Json(name = "store_name") val storeName: String? = null,
    @Json(name = "inspector_id") val inspectorId: String? = null,
    @Json(name = "inspector_name") val inspectorName: String? = null,
    @Json(name = "compliance_score") val complianceScore: Int = 100,
    @Json(name = "status") val status: String = "COMPLIANT",
    @Json(name = "extracted_fields") val extractedFieldsJson: String? = null,
    @Json(name = "violations") val violationsJson: String? = null,
    @Json(name = "image_urls") val imageUrls: String? = null,
    @Json(name = "officer_notes") val officerNotes: String? = null,
    @Json(name = "is_report_generated") val isReportGenerated: Boolean = false,
    @Json(name = "report_url") val reportUrl: String? = null,
    @Json(name = "manufacturer_risk_level") val manufacturerRiskLevel: String = "LOW",
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseOfficerDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "badge_number") val badgeNumber: String,
    @Json(name = "designation") val designation: String,
    @Json(name = "department") val department: String,
    @Json(name = "jurisdiction") val jurisdiction: String,
    @Json(name = "zone") val zone: String? = null
)

// 1. AI/OCR Pipeline DTOs
@JsonClass(generateAdapter = true)
data class ProcessScanImageRequest(
    @Json(name = "scan_id") val scanId: String,
    @Json(name = "image_path") val imagePath: String,
    @Json(name = "user_id") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseExtractedFieldDto(
    @Json(name = "id") val id: String,
    @Json(name = "field_name") val fieldName: String? = null,
    @Json(name = "fieldName") val fieldNameAlt: String? = null,
    @Json(name = "standard_label") val standardLabel: String? = null,
    @Json(name = "standardLabel") val standardLabelAlt: String? = null,
    @Json(name = "extracted_value") val extractedValue: String? = null,
    @Json(name = "extractedValue") val extractedValueAlt: String? = null,
    @Json(name = "confidence") val confidence: Float = 0.9f,
    @Json(name = "status") val status: String? = "VERIFIED",
    @Json(name = "rule_reference") val ruleReference: String? = null,
    @Json(name = "ruleReference") val ruleReferenceAlt: String? = null,
    @Json(name = "bounding_box_label") val boundingBoxLabel: String? = null,
    @Json(name = "boundingBoxLabel") val boundingBoxLabelAlt: String? = null
)

@JsonClass(generateAdapter = true)
data class ProcessScanImageResponse(
    @Json(name = "scan_id") val scanId: String? = null,
    @Json(name = "extracted_fields") val extractedFieldsJson: String? = null,
    @Json(name = "fields") val fields: List<SupabaseExtractedFieldDto>? = null,
    @Json(name = "raw_text") val rawText: String? = null,
    @Json(name = "detected_languages") val detectedLanguages: List<String>? = null,
    @Json(name = "success") val success: Boolean = true
)

// 2. Compliance Engine DTOs
@JsonClass(generateAdapter = true)
data class CheckComplianceRequest(
    @Json(name = "scan_id") val scanId: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "category") val category: String,
    @Json(name = "extracted_fields") val extractedFieldsJson: String? = null,
    @Json(name = "fields") val fields: List<SupabaseExtractedFieldDto>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseViolationDto(
    @Json(name = "id") val id: String,
    @Json(name = "rule_number") val ruleNumber: String? = null,
    @Json(name = "ruleNumber") val ruleNumberAlt: String? = null,
    @Json(name = "rule_title") val ruleTitle: String? = null,
    @Json(name = "ruleTitle") val ruleTitleAlt: String? = null,
    @Json(name = "legal_act_ref") val legalActRef: String? = null,
    @Json(name = "legalActRef") val legalActRefAlt: String? = null,
    @Json(name = "severity") val severity: String? = "MEDIUM",
    @Json(name = "observed_value") val observedValue: String? = null,
    @Json(name = "observedValue") val observedValueAlt: String? = null,
    @Json(name = "expected_requirement") val expectedRequirement: String? = null,
    @Json(name = "expectedRequirement") val expectedRequirementAlt: String? = null,
    @Json(name = "explanation") val explanation: String? = null,
    @Json(name = "rule_excerpt") val ruleExcerpt: String? = null,
    @Json(name = "ruleExcerpt") val ruleExcerptAlt: String? = null,
    @Json(name = "evidence_drawable_name") val evidenceDrawableName: String? = null,
    @Json(name = "evidenceDrawableName") val evidenceDrawableNameAlt: String? = null,
    @Json(name = "bounding_box_highlight") val boundingBoxHighlight: String? = null,
    @Json(name = "boundingBoxHighlight") val boundingBoxHighlightAlt: String? = null
)

@JsonClass(generateAdapter = true)
data class CheckComplianceResponse(
    @Json(name = "scan_id") val scanId: String? = null,
    @Json(name = "compliance_score") val complianceScore: Int = 100,
    @Json(name = "status") val status: String = "COMPLIANT",
    @Json(name = "violations") val violationsJson: String? = null,
    @Json(name = "violation_list") val violationList: List<SupabaseViolationDto>? = null,
    @Json(name = "passed") val passed: Boolean = true
)

// 3. Analytics RPC DTOs
@JsonClass(generateAdapter = true)
data class AnalyticsRpcResponse(
    @Json(name = "total_scans") val totalScans: Int = 0,
    @Json(name = "compliant_scans") val compliantScans: Int = 0,
    @Json(name = "warning_scans") val warningScans: Int = 0,
    @Json(name = "violation_scans") val violationScans: Int = 0,
    @Json(name = "average_score") val averageScore: Double = 100.0,
    @Json(name = "scans_over_time") val scansOverTime: List<TimelinePointDto>? = null,
    @Json(name = "top_violations") val topViolations: List<ViolationSummaryDto>? = null
)

@JsonClass(generateAdapter = true)
data class TimelinePointDto(
    @Json(name = "date") val date: String,
    @Json(name = "count") val count: Int,
    @Json(name = "compliant_count") val compliantCount: Int = 0,
    @Json(name = "violation_count") val violationCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ViolationSummaryDto(
    @Json(name = "rule_title") val ruleTitle: String,
    @Json(name = "rule_number") val ruleNumber: String = "",
    @Json(name = "count") val count: Int,
    @Json(name = "severity") val severity: String = "MEDIUM",
    @Json(name = "percentage") val percentage: Int = 0
)

fun InspectionRecord.toScanHistoryDto(authUserId: String? = null): SupabaseScanHistoryDto {
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

    return SupabaseScanHistoryDto(
        id = id,
        clientUuid = clientUuid,
        userId = authUserId ?: userId.takeIf { it.isNotBlank() },
        productName = productName,
        brandManufacturer = brandManufacturer,
        category = category.name,
        locationName = locationName,
        facilityType = facilityType,
        storeName = storeName,
        inspectorId = inspectorId,
        inspectorName = inspectorName,
        complianceScore = complianceScore,
        status = status.name,
        extractedFieldsJson = fieldsArray.toString(),
        violationsJson = violationsArray.toString(),
        imageUrls = imageDrawableNames.joinToString(","),
        officerNotes = officerNotes,
        isReportGenerated = isReportGenerated,
        reportUrl = reportUrl,
        manufacturerRiskLevel = manufacturerRiskLevel,
        timestamp = timestamp
    )
}

fun SupabaseScanHistoryDto.toDomain(): InspectionRecord {
    val fieldsList = mutableListOf<ExtractedField>()
    if (!extractedFieldsJson.isNullOrBlank()) {
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
                        status = try { FieldStatus.valueOf(obj.optString("status", FieldStatus.VERIFIED.name)) } catch (_: Exception) { FieldStatus.VERIFIED },
                        ruleReference = obj.optString("ruleReference"),
                        isEditable = obj.optBoolean("isEditable", true),
                        boundingBoxLabel = obj.optString("boundingBoxLabel")
                    )
                )
            }
        } catch (_: Exception) { }
    }

    val violationsList = mutableListOf<Violation>()
    if (!violationsJson.isNullOrBlank()) {
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
                        severity = try { Severity.valueOf(obj.optString("severity", Severity.MEDIUM.name)) } catch (_: Exception) { Severity.MEDIUM },
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
    }

    val images = imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    val recordId = id ?: clientUuid ?: "INS-${System.currentTimeMillis()}"

    return InspectionRecord(
        id = recordId,
        clientUuid = clientUuid ?: recordId,
        userId = userId ?: "",
        productName = productName,
        brandManufacturer = brandManufacturer ?: "Brand Manufacturer",
        category = try { ProductCategory.valueOf(category ?: "") } catch (_: Exception) { ProductCategory.HOUSEHOLD_PRODUCTS },
        locationName = locationName ?: "Retail Outlet",
        facilityType = facilityType ?: "Supermarket",
        storeName = storeName ?: "Retail Store",
        inspectorId = inspectorId ?: "LM-OFFICER",
        inspectorName = inspectorName ?: "Inspector",
        timestamp = timestamp ?: System.currentTimeMillis(),
        complianceScore = complianceScore,
        status = try { ComplianceStatus.valueOf(status) } catch (_: Exception) { ComplianceStatus.COMPLIANT },
        extractedFields = fieldsList,
        violations = violationsList,
        imageDrawableNames = if (images.isEmpty()) listOf("img_detergent_package") else images,
        officerNotes = officerNotes ?: "",
        isReportGenerated = isReportGenerated,
        reportUrl = reportUrl,
        manufacturerRiskLevel = manufacturerRiskLevel,
        isPendingSync = false
    )
}

fun InspectionRecord.toSupabaseDto(): SupabaseInspectionDto {
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

    return SupabaseInspectionDto(
        id = id,
        clientUuid = clientUuid,
        userId = userId,
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
        imageUrls = imageDrawableNames.joinToString(","),
        officerNotes = officerNotes,
        isReportGenerated = isReportGenerated,
        reportUrl = reportUrl,
        manufacturerRiskLevel = manufacturerRiskLevel
    )
}

fun SupabaseInspectionDto.toDomain(): InspectionRecord {
    val fieldsList = mutableListOf<ExtractedField>()
    if (!extractedFieldsJson.isNullOrBlank()) {
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
                        status = try { FieldStatus.valueOf(obj.optString("status", FieldStatus.VERIFIED.name)) } catch (_: Exception) { FieldStatus.VERIFIED },
                        ruleReference = obj.optString("ruleReference"),
                        isEditable = obj.optBoolean("isEditable", true),
                        boundingBoxLabel = obj.optString("boundingBoxLabel")
                    )
                )
            }
        } catch (_: Exception) { }
    }

    val violationsList = mutableListOf<Violation>()
    if (!violationsJson.isNullOrBlank()) {
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
                        severity = try { Severity.valueOf(obj.optString("severity", Severity.MEDIUM.name)) } catch (_: Exception) { Severity.MEDIUM },
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
    }

    val images = imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    return InspectionRecord(
        id = id,
        clientUuid = clientUuid ?: id,
        userId = userId ?: "",
        productName = productName,
        brandManufacturer = brandManufacturer,
        category = try { ProductCategory.valueOf(category) } catch (_: Exception) { ProductCategory.HOUSEHOLD_PRODUCTS },
        locationName = locationName,
        facilityType = facilityType,
        storeName = storeName,
        inspectorId = inspectorId,
        inspectorName = inspectorName,
        timestamp = timestamp,
        complianceScore = complianceScore,
        status = try { ComplianceStatus.valueOf(status) } catch (_: Exception) { ComplianceStatus.COMPLIANT },
        extractedFields = fieldsList,
        violations = violationsList,
        imageDrawableNames = if (images.isEmpty()) listOf("img_detergent_package") else images,
        officerNotes = officerNotes ?: "",
        isReportGenerated = isReportGenerated,
        reportUrl = reportUrl,
        manufacturerRiskLevel = manufacturerRiskLevel,
        isPendingSync = false
    )
}
