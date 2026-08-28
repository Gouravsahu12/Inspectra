package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.ocr.ImagePreprocessor
import com.example.data.ocr.MlKitOCRService
import com.example.data.ocr.MultiImageAggregator
import com.example.data.ocr.OCRService
import com.example.data.repository.InspectionRepository
import com.example.engine.ComplianceEngine
import com.example.model.AnalysisProgressState
import com.example.model.AnalysisStep
import com.example.model.CapturedProductImage
import com.example.model.ComplianceStatus
import com.example.model.ExtractedField
import com.example.model.ExtractedPackageData
import com.example.model.FieldSource
import com.example.model.FieldStatus
import com.example.model.ImageQualityMetric
import com.example.model.InspectionRecord
import com.example.model.InspectorProfile
import com.example.model.OCRResult
import com.example.model.PackageSide
import com.example.model.ProductCategory
import com.example.model.Violation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InspectraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InspectionRepository
    private val ocrService: OCRService = MlKitOCRService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InspectionRepository(db.inspectionDao())
        viewModelScope.launch {
            repository.initializePrepopulatedDataIfEmpty()
        }
    }

    val inspections: StateFlow<List<InspectionRecord>> = repository.allInspections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InspectionRepository.getInitialSeedInspections()
        )

    // Navigation & Auth
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentNavIndex = MutableStateFlow(0) // 0: Home, 1: History, 2: Analytics, 3: Profile
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _inspectorProfile = MutableStateFlow(InspectorProfile())
    val inspectorProfile: StateFlow<InspectorProfile> = _inspectorProfile.asStateFlow()

    // History filter & search
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyStatusFilter = MutableStateFlow<ComplianceStatus?>(null)
    val historyStatusFilter: StateFlow<ComplianceStatus?> = _historyStatusFilter.asStateFlow()

    // Active Inspection Workflow State
    private val _isInspectionWizardOpen = MutableStateFlow(false)
    val isInspectionWizardOpen: StateFlow<Boolean> = _isInspectionWizardOpen.asStateFlow()

    private val _wizardStep = MutableStateFlow(1) // 1: Product, 2: Capture, 3: Analyze, 4: Extracted Data Review, 5: Result
    val wizardStep: StateFlow<Int> = _wizardStep.asStateFlow()

    // Wizard Form Fields
    private val _locationName = MutableStateFlow("Apex Hypermarket, Andheri West, Mumbai")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _facilityType = MutableStateFlow("Supermarket")
    val facilityType: StateFlow<String> = _facilityType.asStateFlow()

    private val _storeName = MutableStateFlow("Apex Retail Outlets Ltd")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProductCategory.HOUSEHOLD_PRODUCTS)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _selectedSampleType = MutableStateFlow("detergent") // "detergent", "oil", "biscuits"
    val selectedSampleType: StateFlow<String> = _selectedSampleType.asStateFlow()

    private val _capturedImages = MutableStateFlow(listOf("img_detergent_package"))
    val capturedImages: StateFlow<List<String>> = _capturedImages.asStateFlow()

    // Rich multi-view captured product images
    private val _productImages = MutableStateFlow<List<CapturedProductImage>>(
        listOf(
            CapturedProductImage(
                id = "init_front_01",
                uri = "img_detergent_package",
                side = PackageSide.FRONT,
                isSampleAsset = true,
                resolution = "1920x1080",
                fileSizeKb = 284L
            )
        )
    )
    val productImages: StateFlow<List<CapturedProductImage>> = _productImages.asStateFlow()

    // Camera & Review Sub-states
    private val _isCameraOpen = MutableStateFlow(false)
    val isCameraOpen: StateFlow<Boolean> = _isCameraOpen.asStateFlow()

    private val _cameraTargetSide = MutableStateFlow(PackageSide.FRONT)
    val cameraTargetSide: StateFlow<PackageSide> = _cameraTargetSide.asStateFlow()

    private val _reviewingImage = MutableStateFlow<CapturedProductImage?>(null)
    val reviewingImage: StateFlow<CapturedProductImage?> = _reviewingImage.asStateFlow()

    private val _viewingImageDetail = MutableStateFlow<CapturedProductImage?>(null)
    val viewingImageDetail: StateFlow<CapturedProductImage?> = _viewingImageDetail.asStateFlow()

    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog: StateFlow<Boolean> = _showDiscardDialog.asStateFlow()

    private val _inspectionCreatedAt = MutableStateFlow(System.currentTimeMillis())
    val inspectionCreatedAt: StateFlow<Long> = _inspectionCreatedAt.asStateFlow()

    private val _qualityMetrics = MutableStateFlow(
        listOf(
            ImageQualityMetric("Image Clarity & OCR Fidelity", true, "1920x1080 px (Optimal for statutory font OCR)"),
            ImageQualityMetric("Lighting & Glare Assessment", true, "Diffused packaging lighting (Luma: 142/255)"),
            ImageQualityMetric("Orientation & Aspect Normalization", true, "Corrected to standard portrait plane"),
            ImageQualityMetric("Panel Boundary & Framing", true, "Statutory declaration zones captured")
        )
    )
    val qualityMetrics: StateFlow<List<ImageQualityMetric>> = _qualityMetrics.asStateFlow()

    // Pipeline Progress & OCR States
    private val _analysisProgressState = MutableStateFlow(AnalysisProgressState())
    val analysisProgressState: StateFlow<AnalysisProgressState> = _analysisProgressState.asStateFlow()

    private val _extractedPackageData = MutableStateFlow<ExtractedPackageData?>(null)
    val extractedPackageData: StateFlow<ExtractedPackageData?> = _extractedPackageData.asStateFlow()

    private val _rawOcrResults = MutableStateFlow<List<OCRResult>>(emptyList())
    val rawOcrResults: StateFlow<List<OCRResult>> = _rawOcrResults.asStateFlow()

    private val _selectedOcrEvidenceField = MutableStateFlow<ExtractedField?>(null)
    val selectedOcrEvidenceField: StateFlow<ExtractedField?> = _selectedOcrEvidenceField.asStateFlow()

    private val _showRawOcrSheet = MutableStateFlow(false)
    val showRawOcrSheet: StateFlow<Boolean> = _showRawOcrSheet.asStateFlow()

    private val _showConflictDialogForField = MutableStateFlow<ExtractedField?>(null)
    val showConflictDialogForField: StateFlow<ExtractedField?> = _showConflictDialogForField.asStateFlow()

    // Extracted Fields for Review
    private val _extractedFields = MutableStateFlow<List<ExtractedField>>(emptyList())
    val extractedFields: StateFlow<List<ExtractedField>> = _extractedFields.asStateFlow()

    // Compliance Results (evaluated exclusively by Rules Engine)
    private val _complianceScore = MutableStateFlow(100)
    val complianceScore: StateFlow<Int> = _complianceScore.asStateFlow()

    private val _complianceStatus = MutableStateFlow(ComplianceStatus.COMPLIANT)
    val complianceStatus: StateFlow<ComplianceStatus> = _complianceStatus.asStateFlow()

    private val _violations = MutableStateFlow<List<Violation>>(emptyList())
    val violations: StateFlow<List<Violation>> = _violations.asStateFlow()

    private val _activeInspectionId = MutableStateFlow("INS-2026-000125")
    val activeInspectionId: StateFlow<String> = _activeInspectionId.asStateFlow()

    private val _selectedViolation = MutableStateFlow<Violation?>(null)
    val selectedViolation: StateFlow<Violation?> = _selectedViolation.asStateFlow()

    private val _showEvidenceViewer = MutableStateFlow(false)
    val showEvidenceViewer: StateFlow<Boolean> = _showEvidenceViewer.asStateFlow()

    private val _showReportScreen = MutableStateFlow(false)
    val showReportScreen: StateFlow<Boolean> = _showReportScreen.asStateFlow()

    private val _selectedHistoryItem = MutableStateFlow<InspectionRecord?>(null)
    val selectedHistoryItem: StateFlow<InspectionRecord?> = _selectedHistoryItem.asStateFlow()

    private val _reportNoticeIssued = MutableStateFlow(false)
    val reportNoticeIssued: StateFlow<Boolean> = _reportNoticeIssued.asStateFlow()

    fun startNewInspection() {
        val nextId = "INS-2026-000" + (125 + (0..99).random())
        _activeInspectionId.value = nextId
        _inspectionCreatedAt.value = System.currentTimeMillis()
        _wizardStep.value = 1
        _isInspectionWizardOpen.value = true
        _violations.value = emptyList()
        _complianceScore.value = 100
        _complianceStatus.value = ComplianceStatus.COMPLIANT
        _extractedFields.value = emptyList()
        _rawOcrResults.value = emptyList()
        _extractedPackageData.value = null
        _selectedOcrEvidenceField.value = null
        _reportNoticeIssued.value = false
        _showReportScreen.value = false
        _showEvidenceViewer.value = false
        _selectedViolation.value = null
        loadSampleImages("detergent")
    }

    fun closeInspectionWizard() {
        _isInspectionWizardOpen.value = false
        _wizardStep.value = 1
        _showDiscardDialog.value = false
    }

    fun promptDiscardInspection() {
        _showDiscardDialog.value = true
    }

    fun setShowDiscardDialog(show: Boolean) {
        _showDiscardDialog.value = show
    }

    fun dismissDiscardDialog() {
        _showDiscardDialog.value = false
    }

    fun setBottomNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    fun login(username: String = "LM-OFFICER-8492", password: String = "") {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun closeWizard() {
        closeInspectionWizard()
    }

    fun selectSamplePackage(sampleKey: String) {
        loadSampleImages(sampleKey)
    }

    fun updateMetadata(location: String, facility: String, store: String, category: ProductCategory) {
        _locationName.value = location
        _facilityType.value = facility
        _storeName.value = store
        _selectedCategory.value = category
    }

    fun setProductCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun promptConflictResolution(field: ExtractedField) {
        _showConflictDialogForField.value = field
    }

    fun openCamera(side: PackageSide) {
        _cameraTargetSide.value = side
        _isCameraOpen.value = true
    }

    fun closeCamera() {
        _isCameraOpen.value = false
    }

    fun onImageCaptured(uriString: String) {
        _isCameraOpen.value = false
        val newImage = CapturedProductImage(
            id = "img_${System.currentTimeMillis()}",
            uri = uriString,
            side = _cameraTargetSide.value,
            isFromGallery = false,
            resolution = "1920x1080",
            fileSizeKb = 340L
        )
        _reviewingImage.value = newImage
    }

    fun onPhotoCaptured(image: CapturedProductImage) {
        _isCameraOpen.value = false
        _reviewingImage.value = image
    }

    fun onGalleryImageSelected(uriString: String) {
        val newImage = CapturedProductImage(
            id = "img_gal_${System.currentTimeMillis()}",
            uri = uriString,
            side = _cameraTargetSide.value,
            isFromGallery = true,
            resolution = "1920x1080",
            fileSizeKb = 420L
        )
        _reviewingImage.value = newImage
    }

    fun acceptReviewImage(image: CapturedProductImage? = null) {
        val target = image ?: _reviewingImage.value ?: return
        val current = _productImages.value.toMutableList()
        // If image for this side already exists, replace it
        current.removeAll { it.side == target.side }
        current.add(target)
        _productImages.value = current

        val uriList = _capturedImages.value.toMutableList()
        if (!uriList.contains(target.uri)) {
            uriList.add(target.uri)
            _capturedImages.value = uriList
        }
        _reviewingImage.value = null
    }

    fun retakeReviewImage() {
        val targetSide = _reviewingImage.value?.side ?: PackageSide.FRONT
        _reviewingImage.value = null
        openCamera(targetSide)
    }

    fun rejectReviewImage() {
        _reviewingImage.value = null
    }

    fun discardReviewImage() {
        _reviewingImage.value = null
    }

    fun viewImageDetail(image: CapturedProductImage) {
        _viewingImageDetail.value = image
    }

    fun setViewingImageDetail(image: CapturedProductImage?) {
        _viewingImageDetail.value = image
    }

    fun dismissImageDetail() {
        _viewingImageDetail.value = null
    }

    fun removeProductImage(id: String) {
        _productImages.value = _productImages.value.filter { it.id != id }
        if (_viewingImageDetail.value?.id == id) {
            _viewingImageDetail.value = null
        }
    }

    fun deleteCapturedImage(image: CapturedProductImage) {
        _productImages.value = _productImages.value.filter { it.id != image.id }
        _capturedImages.value = _capturedImages.value.filter { it != image.uri }
        if (_viewingImageDetail.value?.id == image.id) {
            _viewingImageDetail.value = null
        }
    }

    fun addGalleryImages(newImages: List<CapturedProductImage>) {
        val current = _productImages.value.toMutableList()
        val currentUris = _capturedImages.value.toMutableList()
        for (img in newImages) {
            current.removeAll { it.side == img.side }
            current.add(img)
            if (!currentUris.contains(img.uri)) {
                currentUris.add(img.uri)
            }
        }
        _productImages.value = current
        _capturedImages.value = currentUris
    }

    fun loadSampleImages(sampleKey: String) {
        _selectedSampleType.value = sampleKey
        when (sampleKey) {
            "detergent" -> {
                _selectedCategory.value = ProductCategory.HOUSEHOLD_PRODUCTS
                _capturedImages.value = listOf("img_detergent_package")
                _productImages.value = listOf(
                    CapturedProductImage(
                        id = "pkg_sample_detergent",
                        uri = "img_detergent_package",
                        side = PackageSide.FRONT,
                        isSampleAsset = true,
                        resolution = "1920x1080",
                        fileSizeKb = 284L
                    ),
                    CapturedProductImage(
                        id = "pkg_sample_detergent_back",
                        uri = "img_detergent_package",
                        side = PackageSide.BACK,
                        isSampleAsset = true,
                        resolution = "1920x1080",
                        fileSizeKb = 276L
                    )
                )
            }
            "oil" -> {
                _selectedCategory.value = ProductCategory.FOOD_BEVERAGES
                _capturedImages.value = listOf("img_edible_oil_package")
                _productImages.value = listOf(
                    CapturedProductImage(
                        id = "pkg_sample_oil",
                        uri = "img_edible_oil_package",
                        side = PackageSide.FRONT,
                        isSampleAsset = true,
                        resolution = "1920x1080",
                        fileSizeKb = 312L
                    )
                )
            }
            "biscuits" -> {
                _selectedCategory.value = ProductCategory.FOOD_BEVERAGES
                _capturedImages.value = listOf("img_snack_package")
                _productImages.value = listOf(
                    CapturedProductImage(
                        id = "pkg_sample_biscuits",
                        uri = "img_snack_package",
                        side = PackageSide.FRONT,
                        isSampleAsset = true,
                        resolution = "1920x1080",
                        fileSizeKb = 245L
                    )
                )
            }
        }
    }

    fun goToWizardStep(step: Int) {
        _wizardStep.value = step
    }

    /**
     * Executes the end-to-end Product Image Analysis Pipeline:
     * Captured Images -> Upload/Storage Association -> Image Preprocessing ->
     * OCR Text Extraction -> Statutory Field Extraction -> Conflict Resolution ->
     * Structured Data ready for Inspector Review.
     */
    fun runAiAnalysis() {
        val currentImages = _productImages.value
        if (currentImages.isEmpty()) return

        _wizardStep.value = 3
        viewModelScope.launch {
            try {
                // Step 1: Uploading & Associating Images
                val completed = mutableSetOf<AnalysisStep>()
                _analysisProgressState.value = AnalysisProgressState(
                    currentStep = AnalysisStep.UPLOADING,
                    completedSteps = completed,
                    isProcessing = true,
                    totalImages = currentImages.size,
                    processedImages = 0
                )
                delay(400) // Brief UI stage progression
                completed.add(AnalysisStep.UPLOADING)

                // Step 2: Image Preprocessing (orientation, downscaling, contrast)
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    currentStep = AnalysisStep.PREPROCESSING,
                    completedSteps = completed
                )

                val preprocessedResults = currentImages.map { img ->
                    ImagePreprocessor.preprocessImage(getApplication(), img)
                }

                // Update quality metrics from real preprocessed image
                val aggMetrics = preprocessedResults.firstOrNull()?.qualityMetrics
                if (aggMetrics != null) {
                    _qualityMetrics.value = aggMetrics
                }
                delay(450)
                completed.add(AnalysisStep.PREPROCESSING)

                // Step 3: Text Extraction (ML Kit Latin + Devanagari OCR)
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    currentStep = AnalysisStep.OCR_EXTRACTION,
                    completedSteps = completed
                )

                val ocrResultsList = mutableListOf<OCRResult>()
                for ((idx, img) in currentImages.withIndex()) {
                    val preprocessed = preprocessedResults.getOrNull(idx)
                        ?: ImagePreprocessor.preprocessImage(getApplication(), img)
                    val ocrResult = ocrService.recognizeText(getApplication(), img, preprocessed)
                    ocrResultsList.add(ocrResult)
                    _analysisProgressState.value = _analysisProgressState.value.copy(
                        processedImages = idx + 1
                    )
                }
                _rawOcrResults.value = ocrResultsList
                delay(500)
                completed.add(AnalysisStep.OCR_EXTRACTION)

                // Step 4: Statutory Field Identification & NLP
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    currentStep = AnalysisStep.FIELD_IDENTIFICATION,
                    completedSteps = completed
                )
                delay(450)
                completed.add(AnalysisStep.FIELD_IDENTIFICATION)

                // Step 5: Multi-view Aggregation & Conflict Verification
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    currentStep = AnalysisStep.CONFLICT_RESOLUTION,
                    completedSteps = completed
                )

                val aggregatedData = MultiImageAggregator.aggregateMultiViewResults(
                    inspectionId = _activeInspectionId.value,
                    ocrResults = ocrResultsList
                )
                _extractedPackageData.value = aggregatedData
                _extractedFields.value = aggregatedData.fields
                _selectedCategory.value = aggregatedData.suggestedCategory

                delay(400)
                completed.add(AnalysisStep.CONFLICT_RESOLUTION)

                // Step 6: Ready for Inspector Review
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    currentStep = AnalysisStep.READY,
                    completedSteps = completed,
                    isProcessing = false,
                    detectedLanguages = aggregatedData.detectedLanguages
                )
                delay(300)

                // Navigate to Step 4 (Extracted Declarations Review Screen)
                _wizardStep.value = 4

            } catch (e: Exception) {
                _analysisProgressState.value = _analysisProgressState.value.copy(
                    isProcessing = false,
                    errorMessage = e.localizedMessage ?: "Unable to complete package analysis. Please retry."
                )
            }
        }
    }

    /**
     * Inspector manually updates or corrects any extracted field.
     * Records human-in-the-loop source and clears conflict flags.
     */
    fun updateExtractedField(id: String, newValue: String) {
        val currentList = _extractedFields.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }

        if (index != -1) {
            val oldField = currentList[index]
            val updated = oldField.copy(
                extractedValue = newValue,
                inspectorValue = newValue,
                source = FieldSource.INSPECTOR_EDITED,
                hasConflict = false,
                status = if (newValue.isNotBlank()) FieldStatus.VERIFIED else FieldStatus.NOT_DETECTED,
                confidence = 1.0f // Human confirmed
            )
            currentList[index] = updated
            _extractedFields.value = currentList
        }
    }

    /**
     * Resolves a conflict on a specific field by selecting one of the detected values
     * or accepting the inspector's confirmed value.
     */
    fun resolveConflict(fieldId: String, selectedValue: String) {
        updateExtractedField(fieldId, selectedValue)
        _showConflictDialogForField.value = null
    }

    fun openConflictDialog(field: ExtractedField) {
        _showConflictDialogForField.value = field
    }

    fun dismissConflictDialog() {
        _showConflictDialogForField.value = null
    }

    fun setSelectedCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun selectOcrEvidenceField(field: ExtractedField?) {
        _selectedOcrEvidenceField.value = field
    }

    fun toggleRawOcrSheet(show: Boolean) {
        _showRawOcrSheet.value = show
    }

    /**
     * Inspector completed review of Extracted Information and proceeds
     * to the deterministic Legal Metrology Compliance Engine check.
     */
    fun proceedToComplianceCheck() {
        val currentFields = _extractedFields.value
        val currentImg = _capturedImages.value.firstOrNull() ?: "img_detergent_package"

        val (score, detectedViolations) = ComplianceEngine.evaluateCompliance(
            productName = currentFields.find { it.id == "product_name" }?.extractedValue ?: "Packaged Commodity",
            category = _selectedCategory.value,
            fields = currentFields,
            evidenceImageName = currentImg
        )

        _complianceScore.value = score
        _violations.value = detectedViolations
        _complianceStatus.value = ComplianceEngine.determineStatus(score, detectedViolations)

        _wizardStep.value = 5 // Step 5: Compliance Result & Violations
    }

    fun saveInspectionToRegistry() {
        viewModelScope.launch {
            val record = InspectionRecord(
                id = _activeInspectionId.value,
                productName = _extractedFields.value.find { it.id == "product_name" }?.extractedValue ?: "Packaged Product",
                brandManufacturer = _extractedFields.value.find { it.id == "manufacturer_name" }?.extractedValue ?: "Unknown Manufacturer",
                category = _selectedCategory.value,
                locationName = _locationName.value,
                facilityType = _facilityType.value,
                storeName = _storeName.value,
                inspectorId = _inspectorProfile.value.id,
                inspectorName = _inspectorProfile.value.name,
                timestamp = System.currentTimeMillis(),
                complianceScore = _complianceScore.value,
                status = _complianceStatus.value,
                extractedFields = _extractedFields.value,
                violations = _violations.value,
                imageDrawableNames = _capturedImages.value,
                officerNotes = "Inspection executed via Inspectra AI Assistant. LMPC 2011 statutory validation completed.",
                isReportGenerated = true,
                manufacturerRiskLevel = if (_complianceStatus.value == ComplianceStatus.NON_COMPLIANT) "HIGH" else "LOW"
            )
            repository.insertInspection(record)
        }
    }

    fun selectViolationForDetail(violation: Violation?) {
        _selectedViolation.value = violation
    }

    fun toggleEvidenceViewer(show: Boolean) {
        _showEvidenceViewer.value = show
    }

    fun toggleReportScreen(show: Boolean) {
        _showReportScreen.value = show
    }

    fun selectHistoryItem(item: InspectionRecord?) {
        _selectedHistoryItem.value = item
    }

    fun issueEnforcementNotice() {
        _reportNoticeIssued.value = true
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryStatusFilter(status: ComplianceStatus?) {
        _historyStatusFilter.value = status
    }
}
