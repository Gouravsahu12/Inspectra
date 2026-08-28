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
    @Json(name = "manufacturer_risk_level") val manufacturerRiskLevel: String = "LOW"
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
        manufacturerRiskLevel = manufacturerRiskLevel
    )
}
