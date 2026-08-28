package com.example.data.ocr

import com.example.model.ExtractedField
import com.example.model.ExtractedPackageData
import com.example.model.FieldSource
import com.example.model.FieldStatus
import com.example.model.OCRResult
import com.example.model.ProductCategory

object MultiImageAggregator {

    /**
     * Consolidates multi-view OCR results into unified structured package declarations,
     * detecting and flagging any conflicting values across panels.
     */
    fun aggregateMultiViewResults(
        inspectionId: String,
        ocrResults: List<OCRResult>
    ): ExtractedPackageData {
        if (ocrResults.isEmpty()) {
            return ExtractedPackageData(
                inspectionId = inspectionId,
                analysisStatus = "no_images",
                imagesAnalyzed = 0,
                fields = emptyList()
            )
        }

        // 1. Extract raw field list from each image side
        val perImageFields = ocrResults.map { ocr ->
            ProductFieldExtractor.extractFieldsFromOcrResult(ocr)
        }

        // 2. Collect all recognized field IDs
        val allFieldIds = listOf(
            "product_name",
            "net_quantity",
            "mrp",
            "manufacturer_name",
            "consumer_care",
            "packed_date",
            "country_origin",
            "batch_no"
        )

        val unifiedFields = mutableListOf<ExtractedField>()
        var conflictCounter = 0

        for (fieldId in allFieldIds) {
            // Find all non-empty detections for this field across images
            val detectedOccurrences = perImageFields.flatMap { it.filter { f -> f.id == fieldId && f.extractedValue.isNotBlank() } }

            if (detectedOccurrences.isEmpty()) {
                // Not detected in any image
                val sampleField = perImageFields.firstOrNull()?.find { it.id == fieldId }
                unifiedFields.add(
                    sampleField ?: ExtractedField(
                        id = fieldId,
                        fieldName = getFieldNameForId(fieldId),
                        standardLabel = getStandardLabelForId(fieldId),
                        extractedValue = "",
                        aiExtractedValue = "",
                        inspectorValue = "",
                        confidence = 0f,
                        status = FieldStatus.NOT_DETECTED,
                        ruleReference = getRuleReferenceForId(fieldId),
                        boundingBoxLabel = "Not detected on any scanned panel",
                        source = FieldSource.AI_EXTRACTED
                    )
                )
            } else if (detectedOccurrences.size == 1) {
                // Exactly one detection
                unifiedFields.add(detectedOccurrences.first())
            } else {
                // Multiple detections: check for conflicts
                val normalizedValues = detectedOccurrences.map { normalizeFieldValue(it.id, it.extractedValue) }.distinct()

                if (normalizedValues.size > 1) {
                    // CONFLICT DETECTED across images (e.g. MRP ₹120 vs ₹125)
                    conflictCounter++
                    val highestConf = detectedOccurrences.maxByOrNull { it.confidence } ?: detectedOccurrences.first()
                    val conflictList = detectedOccurrences.map { "${it.detectedSide?.shortName ?: "Angle"}: ${it.extractedValue}" }.distinct()

                    unifiedFields.add(
                        highestConf.copy(
                            status = FieldStatus.CONFLICT,
                            hasConflict = true,
                            conflictingValues = conflictList,
                            boundingBoxLabel = "Conflicting values detected across panels"
                        )
                    )
                } else {
                    // Values match across images -> merge with highest confidence
                    val bestOccurrence = detectedOccurrences.maxByOrNull { it.confidence } ?: detectedOccurrences.first()
                    val mergedConfidence = (bestOccurrence.confidence + 0.02f).coerceAtMost(0.99f)

                    unifiedFields.add(
                        bestOccurrence.copy(
                            confidence = mergedConfidence,
                            status = FieldStatus.VERIFIED,
                            boundingBoxLabel = "Confirmed across ${detectedOccurrences.size} panels"
                        )
                    )
                }
            }
        }

        // 3. Aggregate all OCR text for category classification and language detection
        val combinedText = ocrResults.joinToString("\n") { it.rawText }
        val suggestedCategory = ProductFieldExtractor.suggestProductCategory(combinedText)
        val allLanguages = ocrResults.flatMap { it.recognizedLanguages }.distinct()

        // 4. Calculate total average confidence
        val validConfs = unifiedFields.filter { it.status == FieldStatus.VERIFIED }.map { it.confidence }
        val avgConf = if (validConfs.isNotEmpty()) validConfs.average().toFloat() else 0.88f

        return ExtractedPackageData(
            inspectionId = inspectionId,
            analysisStatus = "completed",
            imagesAnalyzed = ocrResults.size,
            fields = unifiedFields,
            rawOcrResults = ocrResults,
            suggestedCategory = suggestedCategory,
            detectedLanguages = allLanguages,
            conflictCount = conflictCounter,
            totalConfidenceScore = avgConf
        )
    }

    private fun normalizeFieldValue(fieldId: String, value: String): String {
        val trimmed = value.trim().lowercase()
        return when (fieldId) {
            "mrp" -> {
                // Extract pure numeric digits
                trimmed.filter { it.isDigit() || it == '.' }
            }
            "net_quantity" -> {
                trimmed.replace(" ", "").replace("grams", "g").replace("gm", "g")
            }
            "country_origin" -> {
                if (trimmed.contains("india")) "india" else trimmed
            }
            else -> trimmed
        }
    }

    private fun getFieldNameForId(id: String): String = when (id) {
        "product_name" -> "Product Identity"
        "net_quantity" -> "Net Quantity"
        "mrp" -> "Maximum Retail Price (MRP)"
        "manufacturer_name" -> "Manufacturer / Packer Details"
        "consumer_care" -> "Consumer Care & Grievance"
        "packed_date" -> "Month & Year of Packing"
        "country_origin" -> "Country of Origin"
        "batch_no" -> "Batch / Lot Number"
        else -> id.replace("_", " ").capitalize()
    }

    private fun getStandardLabelForId(id: String): String = when (id) {
        "product_name" -> "Common / Generic Name"
        "net_quantity" -> "Standard Metric Units"
        "mrp" -> "MRP inclusive of all taxes"
        "manufacturer_name" -> "Name & Complete Address"
        "consumer_care" -> "Grievance Contact & Email"
        "packed_date" -> "Packaging Date MM/YYYY"
        "country_origin" -> "Manufacture / Origin Country"
        "batch_no" -> "Statutory Lot Identifier"
        else -> "Statutory Declaration"
    }

    private fun getRuleReferenceForId(id: String): String = when (id) {
        "product_name" -> "Rule 6(1)(b)"
        "net_quantity" -> "Rule 6(1)(c)"
        "mrp" -> "Rule 6(1)(e)"
        "manufacturer_name" -> "Rule 6(1)(a)"
        "consumer_care" -> "Rule 6(1)(f)"
        "packed_date" -> "Rule 6(1)(d)"
        "country_origin" -> "Rule 6(1)(da)"
        "batch_no" -> "Rule 6(1)(d)"
        else -> "LMPC 2011"
    }
}
