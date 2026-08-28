package com.example.data.ocr

import com.example.model.BoundingBoxRect
import com.example.model.ExtractedField
import com.example.model.FieldSource
import com.example.model.FieldStatus
import com.example.model.OCRBlock
import com.example.model.OCRResult
import com.example.model.PackageSide
import com.example.model.ProductCategory
import java.util.Locale
import java.util.regex.Pattern

object ProductFieldExtractor {

    // Regular Expression Patterns for Indian LMPC Statutory Declarations
    private val MRP_PATTERN = Pattern.compile(
        """(?i)(?:M\.?R\.?P\.?|Maximum\s+Retail\s+Price|Max\.?\s*Retail\s*Price|MRP\s*Rs\.?|MRP\s*₹)\s*[:=\-]?\s*(?:₹|Rs\.?|INR)?\s*([0-9]+(?:\.[0-9]{1,2})?)"""
    )
    private val STANDALONE_PRICE_PATTERN = Pattern.compile(
        """(?:₹|Rs\.?|INR)\s*([0-9]+(?:\.[0-9]{1,2})?)"""
    )

    private val NET_QTY_PATTERN = Pattern.compile(
        """(?i)(?:Net\s*(?:Quantity|Qty|Wt|Weight|Vol|Volume|Content)|Net\s*Q)\s*[:=\-]?\s*([0-9]+(?:\.[0-9]+)?\s*(?:kg|g|gm|gms|gram|grams|mg|l|ltr|litre|litres|ml|milli\s*litre|N|units|pcs|pieces|pack|sachets|tab|tablets|capsules|m|cm|mm))\b"""
    )
    private val STANDALONE_QTY_PATTERN = Pattern.compile(
        """\b([0-9]+(?:\.[0-9]+)?\s*(?:kg|g|gm|gms|mg|l|ltr|litre|litres|ml|N))\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val DATE_PATTERN = Pattern.compile(
        """(?i)(?:Mfg\.?\s*Date|Pkg\.?\s*Date|Date\s*of\s*(?:Mfg|Packing|Import)|Packed\s*on|Pkd\.?|Packed|Mfd\.?|Mfg|Use\s*before|Best\s*before|Expiry|EXP)\s*[:=\-]?\s*([0-9]{1,2}[\/\-][0-9]{2,4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*[\s\.\,\/\-]+(?:20)?[0-9]{2}|[0-9]{1,2}[\/\-][0-9]{1,2}[\/\-][0-9]{2,4})"""
    )

    private val BATCH_PATTERN = Pattern.compile(
        """(?i)(?:Batch\s*(?:No\.?|Number|Code)|Lot\s*(?:No\.?|Number)|B\.?\s*No\.?|Lot)\s*[:=\-]?\s*([A-Za-z0-9\-\/]+)"""
    )

    private val COUNTRY_PATTERN = Pattern.compile(
        """(?i)(?:Country\s*of\s*Origin|Made\s*in|Product\s*of)\s*[:=\-]?\s*([A-Za-z\s]+)"""
    )

    private val EMAIL_PATTERN = Pattern.compile(
        """\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b"""
    )
    private val TOLL_FREE_PATTERN = Pattern.compile(
        """\b(?:1800|1860)[\s\-]?[0-9]{3,4}[\s\-]?[0-9]{3,4}\b"""
    )
    private val PHONE_PATTERN = Pattern.compile(
        """\b(?:(?:\+91|0)?[\s\-]?[6-9][0-9]{9})\b"""
    )
    private val PIN_CODE_PATTERN = Pattern.compile(
        """\b[1-9][0-9]{2}\s?[0-9]{3}\b"""
    )

    private val MFG_KEYWORD_PATTERN = Pattern.compile(
        """(?i)(?:Manufactured\s*by|Mfd\s*by|Mfg\s*by|Packed\s*by|Pkg\s*by|Marketed\s*by|Imported\s*by|Corporate\s*Office)\s*[:=\-]?\s*"""
    )

    /**
     * Extracts structured statutory fields from a single OCR result.
     */
    fun extractFieldsFromOcrResult(ocrResult: OCRResult): List<ExtractedField> {
        val rawText = ocrResult.rawText
        val blocks = ocrResult.blocks
        val side = ocrResult.side
        val imageUri = ocrResult.imageUri

        val fields = mutableListOf<ExtractedField>()

        // 1. MRP Extraction
        val mrpField = extractMrp(rawText, blocks, side, imageUri)
        fields.add(mrpField)

        // 2. Net Quantity Extraction
        val netQtyField = extractNetQuantity(rawText, blocks, side, imageUri)
        fields.add(netQtyField)

        // 3. Product Name Extraction
        val productNameField = extractProductName(rawText, blocks, side, imageUri)
        fields.add(productNameField)

        // 4. Manufacturer & Packer Info Extraction
        val mfgField = extractManufacturer(rawText, blocks, side, imageUri)
        fields.add(mfgField)

        // 5. Consumer Care Info Extraction
        val consumerCareField = extractConsumerCare(rawText, blocks, side, imageUri)
        fields.add(consumerCareField)

        // 6. Date of Packing / Manufacturing Extraction
        val dateField = extractPackedDate(rawText, blocks, side, imageUri)
        fields.add(dateField)

        // 7. Country of Origin Extraction
        val originField = extractCountryOfOrigin(rawText, blocks, side, imageUri)
        fields.add(originField)

        // 8. Batch / Lot Number Extraction
        val batchField = extractBatchNumber(rawText, blocks, side, imageUri)
        fields.add(batchField)

        return fields
    }

    private fun extractMrp(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val matcher = MRP_PATTERN.matcher(rawText)
        if (matcher.find()) {
            val priceVal = matcher.group(1) ?: ""
            val matchedBlock = findMatchingBlock(blocks, priceVal, "MRP", "Price", "₹")
            val hasTaxMention = rawText.contains("tax", ignoreCase = true) || rawText.contains("incl", ignoreCase = true)
            val fullPriceStr = if (hasTaxMention) {
                "₹ $priceVal (Incl. of all taxes)"
            } else {
                "₹ $priceVal"
            }
            val confidence = (matchedBlock?.confidence ?: 0.92f) * 0.98f

            return ExtractedField(
                id = "mrp",
                fieldName = "Maximum Retail Price (MRP)",
                standardLabel = "MRP inclusive of all taxes",
                extractedValue = fullPriceStr,
                aiExtractedValue = fullPriceStr,
                inspectorValue = fullPriceStr,
                confidence = confidence.coerceIn(0.5f, 0.99f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(e)",
                boundingBoxLabel = "${side.shortName} Panel - MRP Stamp",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        // Fallback: standalone currency pattern if on top or back flap
        val priceMatcher = STANDALONE_PRICE_PATTERN.matcher(rawText)
        if (priceMatcher.find()) {
            val priceVal = priceMatcher.group(1) ?: ""
            val matchedBlock = findMatchingBlock(blocks, priceVal)
            val priceStr = "₹ $priceVal"
            val confidence = (matchedBlock?.confidence ?: 0.85f) * 0.88f

            return ExtractedField(
                id = "mrp",
                fieldName = "Maximum Retail Price (MRP)",
                standardLabel = "MRP inclusive of all taxes",
                extractedValue = priceStr,
                aiExtractedValue = priceStr,
                inspectorValue = priceStr,
                confidence = confidence.coerceIn(0.5f, 0.95f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(e)",
                boundingBoxLabel = "${side.shortName} Panel - Price Region",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "mrp",
            fieldName = "Maximum Retail Price (MRP)",
            standardLabel = "MRP inclusive of all taxes",
            ruleReference = "Rule 6(1)(e)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractNetQuantity(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val matcher = NET_QTY_PATTERN.matcher(rawText)
        if (matcher.find()) {
            val qtyVal = matcher.group(1)?.trim() ?: ""
            val matchedBlock = findMatchingBlock(blocks, qtyVal, "Net", "Quantity", "Weight")
            val confidence = (matchedBlock?.confidence ?: 0.93f) * 0.97f

            return ExtractedField(
                id = "net_quantity",
                fieldName = "Net Quantity",
                standardLabel = "Standard Metric Units",
                extractedValue = qtyVal,
                aiExtractedValue = qtyVal,
                inspectorValue = qtyVal,
                confidence = confidence.coerceIn(0.5f, 0.99f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(c)",
                boundingBoxLabel = "${side.shortName} Panel - Net Qty Zone",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        val standaloneMatcher = STANDALONE_QTY_PATTERN.matcher(rawText)
        if (standaloneMatcher.find()) {
            val qtyVal = standaloneMatcher.group(1)?.trim() ?: ""
            val matchedBlock = findMatchingBlock(blocks, qtyVal)
            val confidence = (matchedBlock?.confidence ?: 0.88f) * 0.90f

            return ExtractedField(
                id = "net_quantity",
                fieldName = "Net Quantity",
                standardLabel = "Standard Metric Units",
                extractedValue = qtyVal,
                aiExtractedValue = qtyVal,
                inspectorValue = qtyVal,
                confidence = confidence.coerceIn(0.5f, 0.95f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(c)",
                boundingBoxLabel = "${side.shortName} Panel - Quantity Region",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "net_quantity",
            fieldName = "Net Quantity",
            standardLabel = "Standard Metric Units",
            ruleReference = "Rule 6(1)(c)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractProductName(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        // Look for the top-most or most prominent header block on the Front panel
        val frontBlocks = blocks.filter { it.side == PackageSide.FRONT || side == PackageSide.FRONT }
        val candidateBlock = frontBlocks.firstOrNull { block ->
            val text = block.text.trim()
            text.length in 4..60 && !text.contains("MRP", ignoreCase = true) && !text.contains("Net", ignoreCase = true)
        } ?: blocks.firstOrNull { it.text.trim().length in 4..60 }

        if (candidateBlock != null) {
            val name = candidateBlock.text.lines().firstOrNull()?.trim() ?: candidateBlock.text.trim()
            val confidence = (candidateBlock.confidence * 0.95f).coerceIn(0.6f, 0.99f)

            return ExtractedField(
                id = "product_name",
                fieldName = "Product Identity",
                standardLabel = "Common / Generic Name",
                extractedValue = name,
                aiExtractedValue = name,
                inspectorValue = name,
                confidence = confidence,
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(b)",
                boundingBoxLabel = "${side.shortName} Panel - Principal Display",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = candidateBlock.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "product_name",
            fieldName = "Product Identity",
            standardLabel = "Common / Generic Name",
            ruleReference = "Rule 6(1)(b)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractManufacturer(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val mfgMatcher = MFG_KEYWORD_PATTERN.matcher(rawText)
        if (mfgMatcher.find()) {
            val startIndex = mfgMatcher.end()
            val remainingText = rawText.substring(startIndex)
            val lines = remainingText.lines().take(4).map { it.trim() }.filter { it.isNotBlank() }
            val mfgString = lines.joinToString(", ")

            val matchedBlock = findMatchingBlock(blocks, "Manufactured", "Mfd", "Packed by", "MIDC", "GIDC", "Plot")
            val confidence = (matchedBlock?.confidence ?: 0.90f) * 0.94f

            return ExtractedField(
                id = "manufacturer_name",
                fieldName = "Manufacturer / Packer Details",
                standardLabel = "Name & Complete Address",
                extractedValue = mfgString.take(120),
                aiExtractedValue = mfgString.take(120),
                inspectorValue = mfgString.take(120),
                confidence = confidence.coerceIn(0.6f, 0.98f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(a)",
                boundingBoxLabel = "${side.shortName} Panel - Manufacturer Block",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        // Check if any block has a 6-digit Indian PIN code and corporate keywords (Pvt Ltd, Ltd, LLP)
        val pinMatcher = PIN_CODE_PATTERN.matcher(rawText)
        if (pinMatcher.find()) {
            val pinCode = pinMatcher.group(0)
            val matchedBlock = blocks.find { it.text.contains(pinCode ?: "") }
            if (matchedBlock != null) {
                val value = matchedBlock.text.replace("\n", ", ").trim()
                return ExtractedField(
                    id = "manufacturer_name",
                    fieldName = "Manufacturer / Packer Details",
                    standardLabel = "Name & Complete Address",
                    extractedValue = value.take(120),
                    aiExtractedValue = value.take(120),
                    inspectorValue = value.take(120),
                    confidence = 0.86f,
                    status = FieldStatus.VERIFIED,
                    ruleReference = "Rule 6(1)(a)",
                    boundingBoxLabel = "${side.shortName} Panel - Address & PIN $pinCode",
                    source = FieldSource.AI_EXTRACTED,
                    boundingBox = matchedBlock.boundingBox,
                    detectedSide = side,
                    detectedImageUri = imageUri
                )
            }
        }

        return createNotDetectedField(
            id = "manufacturer_name",
            fieldName = "Manufacturer / Packer Details",
            standardLabel = "Name & Complete Address",
            ruleReference = "Rule 6(1)(a)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractConsumerCare(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val emails = mutableListOf<String>()
        val emailMatcher = EMAIL_PATTERN.matcher(rawText)
        while (emailMatcher.find()) {
            emailMatcher.group(0)?.let { emails.add(it) }
        }

        val tollFrees = mutableListOf<String>()
        val tfMatcher = TOLL_FREE_PATTERN.matcher(rawText)
        while (tfMatcher.find()) {
            tfMatcher.group(0)?.let { tollFrees.add(it) }
        }

        val phones = mutableListOf<String>()
        val phoneMatcher = PHONE_PATTERN.matcher(rawText)
        while (phoneMatcher.find()) {
            phoneMatcher.group(0)?.let { phones.add(it) }
        }

        val hasGrievanceKeyword = rawText.contains("consumer", ignoreCase = true) ||
                rawText.contains("customer", ignoreCase = true) ||
                rawText.contains("grievance", ignoreCase = true) ||
                rawText.contains("care", ignoreCase = true) ||
                rawText.contains("feedback", ignoreCase = true)

        if (emails.isNotEmpty() || tollFrees.isNotEmpty() || (hasGrievanceKeyword && phones.isNotEmpty())) {
            val parts = mutableListOf<String>()
            if (emails.isNotEmpty()) parts.add(emails.first())
            if (tollFrees.isNotEmpty()) parts.add("Toll-Free: ${tollFrees.first()}")
            else if (phones.isNotEmpty()) parts.add("Helpline: ${phones.first()}")

            val extractedStr = parts.joinToString(" | ")
            val matchedBlock = findMatchingBlock(blocks, "care", "helpline", "toll", "feedback", "@")
            val confidence = (matchedBlock?.confidence ?: 0.91f) * 0.96f

            return ExtractedField(
                id = "consumer_care",
                fieldName = "Consumer Care & Grievance",
                standardLabel = "Grievance Contact & Email",
                extractedValue = extractedStr,
                aiExtractedValue = extractedStr,
                inspectorValue = extractedStr,
                confidence = confidence.coerceIn(0.6f, 0.98f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(f)",
                boundingBoxLabel = "${side.shortName} Panel - Grievance Cell",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "consumer_care",
            fieldName = "Consumer Care & Grievance",
            standardLabel = "Grievance Contact & Email",
            ruleReference = "Rule 6(1)(f)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractPackedDate(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val matcher = DATE_PATTERN.matcher(rawText)
        if (matcher.find()) {
            val dateStr = matcher.group(1)?.trim() ?: ""
            val matchedBlock = findMatchingBlock(blocks, dateStr, "Mfg", "Pkg", "Date", "Packed")
            val confidence = (matchedBlock?.confidence ?: 0.90f) * 0.93f

            return ExtractedField(
                id = "packed_date",
                fieldName = "Month & Year of Packing",
                standardLabel = "Packaging Date MM/YYYY",
                extractedValue = dateStr,
                aiExtractedValue = dateStr,
                inspectorValue = dateStr,
                confidence = confidence.coerceIn(0.5f, 0.97f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(d)",
                boundingBoxLabel = "${side.shortName} Panel - Packaging Date Stamp",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "packed_date",
            fieldName = "Month & Year of Packing",
            standardLabel = "Packaging Date MM/YYYY",
            ruleReference = "Rule 6(1)(d)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractCountryOfOrigin(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val matcher = COUNTRY_PATTERN.matcher(rawText)
        if (matcher.find()) {
            val country = matcher.group(1)?.trim()?.lines()?.firstOrNull() ?: "India"
            val matchedBlock = findMatchingBlock(blocks, "Origin", "Made in", "Product of", country)
            val confidence = (matchedBlock?.confidence ?: 0.92f) * 0.95f

            return ExtractedField(
                id = "country_origin",
                fieldName = "Country of Origin",
                standardLabel = "Manufacture / Origin Country",
                extractedValue = country,
                aiExtractedValue = country,
                inspectorValue = country,
                confidence = confidence.coerceIn(0.6f, 0.99f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(da)",
                boundingBoxLabel = "${side.shortName} Panel - Origin Declaration",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        } else if (rawText.contains("India", ignoreCase = true)) {
            val matchedBlock = findMatchingBlock(blocks, "India")
            return ExtractedField(
                id = "country_origin",
                fieldName = "Country of Origin",
                standardLabel = "Manufacture / Origin Country",
                extractedValue = "India",
                aiExtractedValue = "India",
                inspectorValue = "India",
                confidence = 0.88f,
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(da)",
                boundingBoxLabel = "${side.shortName} Panel - Origin Tag",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "country_origin",
            fieldName = "Country of Origin",
            standardLabel = "Manufacture / Origin Country",
            ruleReference = "Rule 6(1)(da)",
            side = side,
            imageUri = imageUri
        )
    }

    private fun extractBatchNumber(
        rawText: String,
        blocks: List<OCRBlock>,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        val matcher = BATCH_PATTERN.matcher(rawText)
        if (matcher.find()) {
            val batch = matcher.group(1)?.trim() ?: ""
            val matchedBlock = findMatchingBlock(blocks, batch, "Batch", "Lot", "B.No")
            val confidence = (matchedBlock?.confidence ?: 0.88f) * 0.92f

            return ExtractedField(
                id = "batch_no",
                fieldName = "Batch / Lot Number",
                standardLabel = "Statutory Lot Identifier",
                extractedValue = batch,
                aiExtractedValue = batch,
                inspectorValue = batch,
                confidence = confidence.coerceIn(0.5f, 0.96f),
                status = FieldStatus.VERIFIED,
                ruleReference = "Rule 6(1)(d)",
                boundingBoxLabel = "${side.shortName} Panel - Batch Coding",
                source = FieldSource.AI_EXTRACTED,
                boundingBox = matchedBlock?.boundingBox,
                detectedSide = side,
                detectedImageUri = imageUri
            )
        }

        return createNotDetectedField(
            id = "batch_no",
            fieldName = "Batch / Lot Number",
            standardLabel = "Statutory Lot Identifier",
            ruleReference = "Rule 6(1)(d)",
            side = side,
            imageUri = imageUri
        )
    }

    /**
     * Suggests a broad product category based on extracted text tokens.
     */
    fun suggestProductCategory(allText: String): ProductCategory {
        val lower = allText.lowercase(Locale.getDefault())
        return when {
            lower.contains("oil") || lower.contains("food") || lower.contains("biscuit") ||
                    lower.contains("cookie") || lower.contains("snack") || lower.contains("rice") ||
                    lower.contains("flour") || lower.contains("tea") || lower.contains("coffee") ||
                    lower.contains("juice") || lower.contains("beverage") || lower.contains("grain") ->
                ProductCategory.FOOD_BEVERAGES

            lower.contains("detergent") || lower.contains("washing powder") || lower.contains("cleaner") ||
                    lower.contains("dishwash") || lower.contains("bleach") || lower.contains("disinfectant") ->
                ProductCategory.HOUSEHOLD_PRODUCTS

            lower.contains("cream") || lower.contains("lotion") || lower.contains("shampoo") ||
                    lower.contains("soap") || lower.contains("face wash") || lower.contains("cosmetic") ->
                ProductCategory.COSMETICS

            lower.contains("toothpaste") || lower.contains("sanitizer") || lower.contains("deodorant") ->
                ProductCategory.PERSONAL_CARE

            lower.contains("bulb") || lower.contains("led") || lower.contains("wire") ||
                    lower.contains("battery") || lower.contains("charger") || lower.contains("cable") ->
                ProductCategory.ELECTRICAL_CONSUMER_GOODS

            else -> ProductCategory.OTHER
        }
    }

    private fun findMatchingBlock(blocks: List<OCRBlock>, vararg keywords: String): OCRBlock? {
        return blocks.find { block ->
            keywords.any { kw -> block.text.contains(kw, ignoreCase = true) }
        }
    }

    private fun createNotDetectedField(
        id: String,
        fieldName: String,
        standardLabel: String,
        ruleReference: String,
        side: PackageSide,
        imageUri: String
    ): ExtractedField {
        return ExtractedField(
            id = id,
            fieldName = fieldName,
            standardLabel = standardLabel,
            extractedValue = "",
            aiExtractedValue = "",
            inspectorValue = "",
            confidence = 0.0f,
            status = FieldStatus.NOT_DETECTED,
            ruleReference = ruleReference,
            boundingBoxLabel = "Not detected on ${side.shortName} Panel",
            source = FieldSource.AI_EXTRACTED,
            boundingBox = null,
            detectedSide = side,
            detectedImageUri = imageUri
        )
    }
}
