package com.example.data.ocr

import android.content.Context
import com.example.model.BoundingBoxRect
import com.example.model.CapturedProductImage
import com.example.model.OCRBlock
import com.example.model.OCRLine
import com.example.model.OCRResult
import com.example.model.PackageSide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

interface OCRService {
    suspend fun recognizeText(
        context: Context,
        image: CapturedProductImage,
        preprocessed: ImagePreprocessor.PreprocessedImageResult
    ): OCRResult
}

class MlKitOCRService : OCRService {

    // Primary Latin Recognizer & Multilingual Devanagari (Hindi) Recognizer
    private val latinRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val devanagariRecognizer by lazy {
        try {
            TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        } catch (_: Exception) {
            latinRecognizer
        }
    }

    override suspend fun recognizeText(
        context: Context,
        image: CapturedProductImage,
        preprocessed: ImagePreprocessor.PreprocessedImageResult
    ): OCRResult = withContext(Dispatchers.Default) {
        var rawTextResult = ""
        val ocrBlocks = mutableListOf<OCRBlock>()
        val detectedLanguages = mutableSetOf("English")
        var isReal = true

        val timeMs = measureTimeMillis {
            try {
                val inputImage = InputImage.fromBitmap(preprocessed.bitmap, 0)
                
                // Use Devanagari recognizer which recognizes both Latin and Devanagari scripts
                val textVision: Text = devanagariRecognizer.process(inputImage).await()
                rawTextResult = textVision.text

                val imgWidth = preprocessed.width.toFloat().coerceAtLeast(1f)
                val imgHeight = preprocessed.height.toFloat().coerceAtLeast(1f)

                // Check for Hindi/Devanagari Unicode script range (\u0900 - \u097F)
                val containsDevanagari = rawTextResult.any { it in '\u0900'..'\u097F' }
                if (containsDevanagari) {
                    detectedLanguages.add("Hindi (Devanagari)")
                }

                textVision.textBlocks.forEach { block ->
                    val blockBox = block.boundingBox?.let { box ->
                        BoundingBoxRect(
                            left = (box.left / imgWidth).coerceIn(0f, 1f),
                            top = (box.top / imgHeight).coerceIn(0f, 1f),
                            right = (box.right / imgWidth).coerceIn(0f, 1f),
                            bottom = (box.bottom / imgHeight).coerceIn(0f, 1f)
                        )
                    }

                    val lines = block.lines.map { line ->
                        val lineBox = line.boundingBox?.let { box ->
                            BoundingBoxRect(
                                left = (box.left / imgWidth).coerceIn(0f, 1f),
                                top = (box.top / imgHeight).coerceIn(0f, 1f),
                                right = (box.right / imgWidth).coerceIn(0f, 1f),
                                bottom = (box.bottom / imgHeight).coerceIn(0f, 1f)
                            )
                        }
                        val confidence = line.confidence ?: 0.88f
                        OCRLine(
                            text = line.text,
                            confidence = confidence,
                            boundingBox = lineBox
                        )
                    }

                    val avgConfidence = if (lines.isNotEmpty()) {
                        lines.map { it.confidence }.average().toFloat()
                    } else 0.85f

                    ocrBlocks.add(
                        OCRBlock(
                            text = block.text,
                            confidence = avgConfidence,
                            boundingBox = blockBox,
                            lines = lines,
                            side = image.side
                        )
                    )
                }

                // If real ML Kit returned empty on a sample asset (e.g. vector graphic or stub image), enrich with sample statutory text
                if (rawTextResult.isBlank() && image.isSampleAsset) {
                    val sampleData = getSamplePackageOcrData(image)
                    rawTextResult = sampleData.rawText
                    ocrBlocks.addAll(sampleData.blocks)
                    detectedLanguages.addAll(sampleData.recognizedLanguages)
                    isReal = false
                }

            } catch (e: Exception) {
                // Fallback for sample assets if ML Kit initialization encountered device limitation
                if (image.isSampleAsset) {
                    val sampleData = getSamplePackageOcrData(image)
                    rawTextResult = sampleData.rawText
                    ocrBlocks.addAll(sampleData.blocks)
                    detectedLanguages.addAll(sampleData.recognizedLanguages)
                    isReal = false
                } else {
                    rawTextResult = "OCR processing encountered error: ${e.localizedMessage ?: "Unknown error"}"
                }
            }
        }

        OCRResult(
            imageUri = image.uri,
            side = image.side,
            rawText = rawTextResult,
            blocks = ocrBlocks,
            recognizedLanguages = detectedLanguages.toList(),
            processingTimeMs = timeMs,
            isRealOcr = isReal
        )
    }

    private fun getSamplePackageOcrData(image: CapturedProductImage): OCRResult {
        return when {
            image.uri.contains("oil") -> {
                val text = """
                    GOLDEN HARVEST
                    Pure Refined Sunflower Oil
                    Net Quantity: 1 Litre (910 g)
                    MRP ₹ 165.00 (Incl. of all taxes)
                    Unit Sale Price: ₹ 165.00 / L
                    Manufactured & Packed by:
                    Kriti Agro Industries Ltd,
                    Industrial Area, Dewas, Madhya Pradesh - 455001
                    Customer Care: care@kritiagro.com | 1800-209-4455
                    Date of Packing: 08/2026
                    Batch No: GHO-2608
                    Country of Origin: India
                """.trimIndent()

                OCRResult(
                    imageUri = image.uri,
                    side = image.side,
                    rawText = text,
                    blocks = listOf(
                        OCRBlock(
                            text = "GOLDEN HARVEST Pure Refined Sunflower Oil",
                            confidence = 0.98f,
                            boundingBox = BoundingBoxRect(0.1f, 0.15f, 0.9f, 0.35f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Net Quantity: 1 Litre (910 g)",
                            confidence = 0.96f,
                            boundingBox = BoundingBoxRect(0.15f, 0.40f, 0.85f, 0.50f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "MRP ₹ 165.00 (Incl. of all taxes)\nUnit Sale Price: ₹ 165.00 / L",
                            confidence = 0.95f,
                            boundingBox = BoundingBoxRect(0.1f, 0.52f, 0.9f, 0.65f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Manufactured & Packed by: Kriti Agro Industries Ltd, Dewas MP - 455001",
                            confidence = 0.93f,
                            boundingBox = BoundingBoxRect(0.08f, 0.68f, 0.92f, 0.82f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Customer Care: care@kritiagro.com | 1800-209-4455\nDate of Packing: 08/2026\nCountry of Origin: India",
                            confidence = 0.94f,
                            boundingBox = BoundingBoxRect(0.08f, 0.84f, 0.92f, 0.95f),
                            side = image.side
                        )
                    ),
                    recognizedLanguages = listOf("English"),
                    processingTimeMs = 180L,
                    isRealOcr = false
                )
            }
            image.uri.contains("snack") || image.uri.contains("biscuit") -> {
                val text = """
                    CRUNCH DELIGHT
                    Butter Cookies
                    Net Qty: 200 g
                    Max Retail Price: ₹ 45.00 (Inclusive of all taxes)
                    Mfd by: Delight Bakers LLP, Plot 14, GIDC Vapi, Gujarat - 396195
                    For queries: feedback@delightbakers.com
                    Pkd: 06/2026
                    Batch: CD-9022
                    Made in India
                """.trimIndent()

                OCRResult(
                    imageUri = image.uri,
                    side = image.side,
                    rawText = text,
                    blocks = listOf(
                        OCRBlock(
                            text = "CRUNCH DELIGHT Butter Cookies",
                            confidence = 0.97f,
                            boundingBox = BoundingBoxRect(0.12f, 0.12f, 0.88f, 0.32f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Net Qty: 200 g",
                            confidence = 0.96f,
                            boundingBox = BoundingBoxRect(0.2f, 0.38f, 0.8f, 0.48f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Max Retail Price: ₹ 45.00 (Inclusive of all taxes)",
                            confidence = 0.94f,
                            boundingBox = BoundingBoxRect(0.1f, 0.50f, 0.9f, 0.62f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Mfd by: Delight Bakers LLP, Vapi, Gujarat - 396195\nFor queries: feedback@delightbakers.com",
                            confidence = 0.89f,
                            boundingBox = BoundingBoxRect(0.08f, 0.65f, 0.92f, 0.82f),
                            side = image.side
                        )
                    ),
                    recognizedLanguages = listOf("English"),
                    processingTimeMs = 160L,
                    isRealOcr = false
                )
            }
            else -> {
                // Detergent (Demonstrates Missing Consumer Care for testing)
                val text = """
                    ABC ULTRA CLEAN
                    Active Washing Powder / Laundry Detergent
                    Net Quantity: 1 kg
                    MRP: ₹ 120.00 (Incl. of all taxes)
                    Unit Sale Price: ₹ 0.12 / g
                    Manufactured by: ABC Chemicals Pvt Ltd,
                    Plot 42, MIDC Industrial Area, Tarapur, Maharashtra - 401506
                    Month & Year of Packing: 07/2026
                    Batch No: DET-7721
                    Country of Origin: India
                """.trimIndent()

                OCRResult(
                    imageUri = image.uri,
                    side = image.side,
                    rawText = text,
                    blocks = listOf(
                        OCRBlock(
                            text = "ABC ULTRA CLEAN Laundry Detergent",
                            confidence = 0.98f,
                            boundingBox = BoundingBoxRect(0.1f, 0.14f, 0.9f, 0.32f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Net Quantity: 1 kg",
                            confidence = 0.96f,
                            boundingBox = BoundingBoxRect(0.18f, 0.36f, 0.82f, 0.48f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "MRP: ₹ 120.00 (Incl. of all taxes)\nUnit Sale Price: ₹ 0.12 / g",
                            confidence = 0.94f,
                            boundingBox = BoundingBoxRect(0.12f, 0.50f, 0.88f, 0.64f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Manufactured by: ABC Chemicals Pvt Ltd, MIDC Tarapur - 401506",
                            confidence = 0.92f,
                            boundingBox = BoundingBoxRect(0.08f, 0.66f, 0.92f, 0.82f),
                            side = image.side
                        ),
                        OCRBlock(
                            text = "Month & Year of Packing: 07/2026\nBatch No: DET-7721\nCountry of Origin: India",
                            confidence = 0.91f,
                            boundingBox = BoundingBoxRect(0.1f, 0.84f, 0.9f, 0.95f),
                            side = image.side
                        )
                    ),
                    recognizedLanguages = listOf("English"),
                    processingTimeMs = 195L,
                    isRealOcr = false
                )
            }
        }
    }
}
