package com.example.model

import com.example.model.PackageSide

enum class FieldSource {
    AI_EXTRACTED,
    INSPECTOR_EDITED,
    MANUALLY_ADDED
}

data class BoundingBoxRect(
    val left: Float, // Normalized 0.0 to 1.0
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
}

data class OCRLine(
    val text: String,
    val confidence: Float = 0.9f,
    val boundingBox: BoundingBoxRect? = null
)

data class OCRBlock(
    val text: String,
    val confidence: Float = 0.9f,
    val boundingBox: BoundingBoxRect? = null,
    val lines: List<OCRLine> = emptyList(),
    val side: PackageSide = PackageSide.FRONT
)

data class OCRResult(
    val imageUri: String,
    val side: PackageSide,
    val rawText: String,
    val blocks: List<OCRBlock>,
    val recognizedLanguages: List<String> = listOf("en"),
    val processingTimeMs: Long = 0L,
    val isRealOcr: Boolean = true
)

enum class AnalysisStep(val label: String, val subtitle: String) {
    UPLOADING("Uploading images", "Associating packaging images with inspection record"),
    PREPROCESSING("Image preprocessing", "Correcting orientation, optimizing resolution & contrast"),
    OCR_EXTRACTION("Text extraction", "Running multi-angle OCR on all captured package panels"),
    FIELD_IDENTIFICATION("Field identification", "Extracting statutory declarations & LMPC 2011 attributes"),
    CONFLICT_RESOLUTION("Conflict verification", "Cross-referencing declarations across panels"),
    READY("Analysis complete", "Ready for inspector review and validation")
}

data class AnalysisProgressState(
    val currentStep: AnalysisStep = AnalysisStep.UPLOADING,
    val completedSteps: Set<AnalysisStep> = emptySet(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val totalImages: Int = 1,
    val processedImages: Int = 0,
    val detectedLanguages: List<String> = listOf("English")
)

data class ExtractedPackageData(
    val inspectionId: String,
    val analysisStatus: String = "completed",
    val imagesAnalyzed: Int = 1,
    val fields: List<ExtractedField> = emptyList(),
    val rawOcrResults: List<OCRResult> = emptyList(),
    val suggestedCategory: ProductCategory = ProductCategory.HOUSEHOLD_PRODUCTS,
    val detectedLanguages: List<String> = listOf("English"),
    val conflictCount: Int = 0,
    val totalConfidenceScore: Float = 0.92f
)
