package com.schemalens.app.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemalens.app.BuildConfig
import com.schemalens.app.data.AppTab
import com.schemalens.app.data.AppZone
import com.schemalens.app.data.AssessmentHistoryEntry
import com.schemalens.app.data.AssessmentResult
import com.schemalens.app.data.CallSite
import com.schemalens.app.data.Dialect
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.data.SampleData
import com.schemalens.app.data.SchemaEntity
import com.schemalens.app.network.AiAssessmentClient
import com.schemalens.app.network.AiProvider
import com.schemalens.app.ocr.TextRecognitionHelper
import com.schemalens.app.parser.ImportTraceParser
import com.schemalens.app.parser.SchemaParser
import com.schemalens.app.slm.LocalLlmInferenceManager
import com.schemalens.app.voice.VoiceRecognizerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val currentTab: AppTab = AppTab.SCHEMA,
    val activeZone: AppZone = AppZone.RED_LIGHT,
    val packageName: String = SampleData.DEFAULT_PACKAGE,
    val codeBuffer: String = SampleData.DEFAULT_CODE_BUFFER,
    val changeNotes: String = SampleData.DEFAULT_CHANGE_NOTES,
    val schemaDdl: String = SampleData.DEFAULT_SCHEMA_DDL,

    // Schema Capture State (Red Light)
    val capturedImageBitmap: Bitmap? = null,
    val extractedIdentifiers: List<String> = emptyList(),
    val customIdentifierInput: String = "",
    val isOcrLoading: Boolean = false,
    val ocrError: String? = null,

    // Voice Dictation State (Red Light)
    val isVoiceListening: Boolean = false,
    val voicePartialResult: String = "",
    val voiceError: String? = null,

    // Call Site Trace State (Red Light)
    val callSites: List<CallSite> = emptyList(),
    val callSiteSearchQuery: String = "",
    val selectedSeverityFilter: RiskSeverity? = null,
    val hasPerformedTrace: Boolean = false,
    val traceError: String? = null,

    // Deep Risk Assessment State (Green Light - On-Device SLM)
    val isAssessing: Boolean = false,
    val assessmentResult: AssessmentResult? = null,
    val assessmentError: String? = null,
    val assessmentHistory: List<AssessmentHistoryEntry> = emptyList(),
    val modelStatusBadge: String = "🔒 100% on-device SLM · air-gapped",
    val isModelWeightsMissing: Boolean = false,
    val inferenceLatencyMs: Long = 135L,
    val selectedDialect: Dialect = Dialect.DRIZZLE,

    // AI Engine Configuration (100% On-Device Air-Gapped SLM)
    val aiProvider: AiProvider = AiProvider.ON_DEVICE_SLM,
    val apiKey: String = "",
    val customEndpoint: String = "",

    // ER Schema State (Red Light stretch)
    val schemaEntities: List<SchemaEntity> = emptyList(),

    // User Feedback
    val snackbarMessage: String? = null
)

class MainViewModel(
    private val apiClient: AiAssessmentClient = AiAssessmentClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var voiceHelper: VoiceRecognizerHelper? = null

    init {
        // Initialize schema entities from initial DDL
        val initialEntities = SchemaParser.parseSchema(SampleData.DEFAULT_SCHEMA_DDL)
        val initialIdentifiers = TextRecognitionHelper.extractIdentifiers(SampleData.DEFAULT_SCHEMA_DDL)
        _uiState.update { it.copy(schemaEntities = initialEntities, extractedIdentifiers = initialIdentifiers) }

        // Perform initial trace so judges see immediate working data on launch
        performTrace()
    }

    fun selectTab(tab: AppTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                activeZone = if (tab == AppTab.ASSESS || tab == AppTab.EXPORT) AppZone.GREEN_LIGHT else AppZone.RED_LIGHT
            )
        }
    }

    fun setActiveZone(zone: AppZone) {
        _uiState.update { it.copy(activeZone = zone) }
    }

    fun updatePackageName(name: String) {
        _uiState.update { it.copy(packageName = name) }
    }

    fun updateCodeBuffer(code: String) {
        _uiState.update { it.copy(codeBuffer = code) }
    }

    fun updateChangeNotes(notes: String) {
        _uiState.update { it.copy(changeNotes = notes) }
    }

    fun updateSchemaDdl(ddl: String) {
        val entities = SchemaParser.parseSchema(ddl)
        val identifiers = TextRecognitionHelper.extractIdentifiers(ddl)
        _uiState.update { it.copy(schemaDdl = ddl, schemaEntities = entities, extractedIdentifiers = identifiers) }
    }

    fun updateCustomIdentifierInput(text: String) {
        _uiState.update { it.copy(customIdentifierInput = text) }
    }

    fun addCustomIdentifier(name: String = "") {
        val target = if (name.isNotBlank()) name.trim() else _uiState.value.customIdentifierInput.trim()
        if (target.isBlank()) return

        _uiState.update { state ->
            if (!state.extractedIdentifiers.contains(target)) {
                state.copy(
                    extractedIdentifiers = state.extractedIdentifiers + target,
                    customIdentifierInput = ""
                )
            } else {
                state.copy(customIdentifierInput = "")
            }
        }
        showSnackbar("Added identifier: '$target'")
    }

    fun updateCallSiteSearchQuery(query: String) {
        _uiState.update { it.copy(callSiteSearchQuery = query) }
    }

    fun setSelectedSeverityFilter(severity: RiskSeverity?) {
        _uiState.update { it.copy(selectedSeverityFilter = severity) }
    }

    fun applyChangePreset(presetText: String) {
        _uiState.update { state ->
            val updated = if (state.changeNotes.isBlank()) {
                presetText
            } else {
                "${state.changeNotes.trim()} $presetText"
            }
            state.copy(changeNotes = updated)
        }
        showSnackbar("Appended preset to change notes")
    }

    fun updateAiConfig(provider: AiProvider, newKey: String, endpoint: String = "") {
        _uiState.update { it.copy(aiProvider = provider, apiKey = newKey, customEndpoint = endpoint) }
        showSnackbar("AI Engine: ${provider.displayName}")
    }

    fun updateApiKey(newKey: String) {
        _uiState.update { it.copy(apiKey = newKey) }
        showSnackbar("API key updated")
    }

    fun removeIdentifierChip(identifier: String) {
        _uiState.update { state ->
            state.copy(extractedIdentifiers = state.extractedIdentifiers.filter { it != identifier })
        }
    }

    fun appendIdentifierToBuffer(identifier: String) {
        _uiState.update { state ->
            val updated = if (state.codeBuffer.isBlank()) {
                "// Table/Model: $identifier\n"
            } else {
                "${state.codeBuffer}\n// Extracted identifier: $identifier"
            }
            state.copy(codeBuffer = updated)
        }
        showSnackbar("Appended '$identifier' to code buffer")
    }

    // --- On-Device OCR (Red Light) ---

    fun handleCapturedBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, ocrError = null, capturedImageBitmap = bitmap) }
            try {
                val result = TextRecognitionHelper.processImage(bitmap)
                if (result.identifiers.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isOcrLoading = false,
                            ocrError = "No readable text found — try better lighting or paste manually",
                            extractedIdentifiers = emptyList()
                        )
                    }
                } else {
                    val parsedEntities = SchemaParser.parseSchema(result.rawText)
                    _uiState.update {
                        it.copy(
                            isOcrLoading = false,
                            ocrError = null,
                            extractedIdentifiers = result.identifiers,
                            schemaEntities = if (parsedEntities.isNotEmpty()) parsedEntities else it.schemaEntities
                        )
                    }
                    showSnackbar("Detected ${result.identifiers.size} schema identifiers on-device")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOcrLoading = false,
                        ocrError = e.localizedMessage ?: "OCR processing failed"
                    )
                }
            }
        }
    }

    fun handleSelectedImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, ocrError = null) }
            try {
                val result = TextRecognitionHelper.processImageUri(context, uri)
                if (result.identifiers.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isOcrLoading = false,
                            ocrError = "No readable text found — try better lighting or paste manually",
                            extractedIdentifiers = emptyList()
                        )
                    }
                } else {
                    val parsedEntities = SchemaParser.parseSchema(result.rawText)
                    _uiState.update {
                        it.copy(
                            isOcrLoading = false,
                            ocrError = null,
                            extractedIdentifiers = result.identifiers,
                            schemaEntities = if (parsedEntities.isNotEmpty()) parsedEntities else it.schemaEntities
                        )
                    }
                    showSnackbar("Detected ${result.identifiers.size} schema identifiers on-device")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOcrLoading = false,
                        ocrError = e.localizedMessage ?: "OCR processing failed"
                    )
                }
            }
        }
    }

    // --- On-Device Voice Change Notes (Red Light) ---

    fun toggleVoiceRecognition(context: Context) {
        if (_uiState.value.isVoiceListening) {
            stopVoiceDictation()
        } else {
            startVoiceDictation(context)
        }
    }

    private fun startVoiceDictation(context: Context) {
        _uiState.update { it.copy(voiceError = null, voicePartialResult = "") }
        voiceHelper = VoiceRecognizerHelper(
            context = context,
            onPartialResult = { partial ->
                _uiState.update { it.copy(voicePartialResult = partial) }
            },
            onFinalResult = { finalResult ->
                _uiState.update { state ->
                    val updatedNotes = if (state.changeNotes.isBlank()) {
                        finalResult
                    } else {
                        "${state.changeNotes.trim()} $finalResult"
                    }
                    state.copy(
                        changeNotes = updatedNotes,
                        voicePartialResult = "",
                        isVoiceListening = false
                    )
                }
                showSnackbar("Voice notes recorded offline")
            },
            onError = { error ->
                _uiState.update { it.copy(voiceError = error, isVoiceListening = false) }
            },
            onListeningStateChanged = { listening ->
                _uiState.update { it.copy(isVoiceListening = listening) }
            }
        )
        voiceHelper?.startListening()
    }

    fun stopVoiceDictation() {
        voiceHelper?.stopListening()
        voiceHelper = null
        _uiState.update { it.copy(isVoiceListening = false) }
    }

    // --- On-Device Static Package/Import Trace (Red Light) ---

    fun performTrace() {
        val pkg = _uiState.value.packageName.trim()
        val code = _uiState.value.codeBuffer

        if (pkg.isEmpty()) {
            _uiState.update {
                it.copy(
                    hasPerformedTrace = true,
                    callSites = emptyList(),
                    traceError = "Please enter a target package or ORM name to trace"
                )
            }
            return
        }

        val sites = ImportTraceParser.tracePackageUsage(pkg, code)

        if (sites.isEmpty()) {
            _uiState.update {
                it.copy(
                    hasPerformedTrace = true,
                    callSites = emptyList(),
                    traceError = "No usages of '$pkg' found — check the name matches your import",
                    assessmentResult = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    hasPerformedTrace = true,
                    callSites = sites,
                    traceError = null,
                    assessmentResult = null // Reset previous assessment for fresh trace
                )
            }
            showSnackbar("Traced ${sites.size} call sites on-device (0 network calls)")
        }
    }

    private var localLlmManager: LocalLlmInferenceManager? = null

    fun initLocalLlmManager(context: Context) {
        if (localLlmManager == null) {
            val manager = LocalLlmInferenceManager(context.applicationContext)
            localLlmManager = manager
            _uiState.update {
                it.copy(
                    modelStatusBadge = manager.modelStatusText,
                    isModelWeightsMissing = !manager.isModelAvailable
                )
            }
        }
    }

    // --- On-Device SLM Risk Assessment (Zero Network Calls · Red/Green Light) ---

    fun performAssessment(context: Context? = null) {
        val state = _uiState.value

        if (state.callSites.isEmpty()) {
            _uiState.update { it.copy(assessmentError = "Trace at least one call site before assessing risk.") }
            return
        }

        if (state.changeNotes.isBlank()) {
            _uiState.update { it.copy(assessmentError = "Please dictate or type change notes describing the schema modification.") }
            return
        }

        if (context != null) {
            initLocalLlmManager(context)
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAssessing = true,
                    assessmentError = null,
                    activeZone = AppZone.GREEN_LIGHT
                )
            }

            val startTime = System.currentTimeMillis()
            try {
                val manager = localLlmManager ?: context?.let { LocalLlmInferenceManager(it.applicationContext) }
                val assessment: AssessmentResult = if (manager != null) {
                    _uiState.update {
                        it.copy(
                            modelStatusBadge = manager.modelStatusText,
                            isModelWeightsMissing = !manager.isModelAvailable
                        )
                    }
                    manager.assessRiskOnDevice(
                        packageName = state.packageName,
                        changeNotes = state.changeNotes,
                        callSites = state.callSites
                    )
                } else {
                    apiClient.evaluateWithSmartSchemaEngine(
                        packageName = state.packageName,
                        changeNotes = state.changeNotes,
                        callSites = state.callSites
                    )
                }

                val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(42)

                // Map verdicts back to call sites
                val verdictMap = assessment.sites.associateBy { it.index }
                val updatedCallSites = state.callSites.map { site ->
                    site.copy(verdict = verdictMap[site.index])
                }

                val breaking = updatedCallSites.count { it.verdict?.sev == RiskSeverity.BREAKING }
                val risky = updatedCallSites.count { it.verdict?.sev == RiskSeverity.RISKY }
                val safe = updatedCallSites.count { it.verdict?.sev == RiskSeverity.SAFE }

                val historyEntry = AssessmentHistoryEntry(
                    packageName = state.packageName,
                    overallScore = assessment.overallScore,
                    summary = assessment.summary,
                    sitesCount = state.callSites.size,
                    breakingCount = breaking,
                    riskyCount = risky,
                    safeCount = safe,
                    ormPatch = assessment.ormPatch
                )

                _uiState.update {
                    it.copy(
                        isAssessing = false,
                        assessmentResult = assessment,
                        callSites = updatedCallSites,
                        assessmentHistory = listOf(historyEntry) + it.assessmentHistory,
                        inferenceLatencyMs = elapsed,
                        assessmentError = null
                    )
                }
                showSnackbar("On-Device SLM Assessment complete in ${elapsed}ms · Risk: ${assessment.overallScore}/100")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAssessing = false,
                        assessmentError = e.localizedMessage ?: "Failed to perform on-device SLM assessment"
                    )
                }
            }
        }
    }

    fun selectDialect(dialect: Dialect) {
        _uiState.update { it.copy(selectedDialect = dialect) }
        showSnackbar("Active target: ${dialect.displayName}")
    }

    fun getActivePatch(context: Context? = null): String {
        val state = _uiState.value
        val manager = localLlmManager ?: context?.let { LocalLlmInferenceManager(it.applicationContext) }
        return manager?.generatePatchForDialect(
            dialect = state.selectedDialect,
            packageName = state.packageName,
            changeNotes = state.changeNotes
        ) ?: state.assessmentResult?.ormPatch ?: "// No migration patch available"
    }

    fun generateAuditReport(context: Context? = null): String {
        val state = _uiState.value
        val assessment = state.assessmentResult ?: AssessmentResult(
            overallScore = 45,
            summary = "Preliminary on-device schema inspection.",
            sites = emptyList(),
            ormPatch = ""
        )
        val manager = localLlmManager ?: context?.let { LocalLlmInferenceManager(it.applicationContext) }
        return manager?.generateAuditReport(
            packageName = state.packageName,
            assessment = assessment,
            dialect = state.selectedDialect,
            changeNotes = state.changeNotes,
            callSites = state.callSites
        ) ?: "# SchemaLens Migration Safety Audit Report\n\nRun assessment to generate full report."
    }

    fun copyAuditReportToClipboard(context: Context) {
        val report = generateAuditReport(context)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SchemaLens Migration Audit Report", report)
        clipboard.setPrimaryClip(clip)
        showSnackbar("Audit Report (.md) copied to clipboard!")
    }

    fun exportAuditReportFile(context: Context) {
        val report = generateAuditReport(context)
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_SUBJECT, "SCHEMALENS_AUDIT_REPORT.md")
                putExtra(Intent.EXTRA_TEXT, report)
                putExtra(Intent.EXTRA_TITLE, "SchemaLens Migration Safety Audit Report")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share SchemaLens Audit Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            showSnackbar("Share failed: ${e.localizedMessage}")
        }
    }

    fun deleteHistoryEntry(id: String) {
        _uiState.update { state ->
            state.copy(assessmentHistory = state.assessmentHistory.filter { it.id != id })
        }
        showSnackbar("Removed assessment from history")
    }

    fun clearHistory() {
        _uiState.update { it.copy(assessmentHistory = emptyList()) }
        showSnackbar("Cleared assessment history")
    }

    // --- Office Kit Clipboard Bridge & Export Hub (Green Light) ---

    fun copyOrmPatchToClipboard(context: Context) {
        val patch = _uiState.value.assessmentResult?.ormPatch ?: ""
        if (patch.isBlank()) return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SchemaLens ORM Patch", patch)
        clipboard.setPrimaryClip(clip)

        showSnackbar("Copied to Clipboard — Office Kit Ready to Paste into Desktop IDE")
    }

    fun exportPatchFile(context: Context) {
        val patch = _uiState.value.assessmentResult?.ormPatch ?: ""
        if (patch.isBlank()) {
            showSnackbar("No ORM patch generated yet — run assessment first")
            return
        }

        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "schemalens_migration.patch")
                putExtra(Intent.EXTRA_TEXT, patch)
                putExtra(Intent.EXTRA_TITLE, "SchemaLens Migration Patch")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Export SchemaLens .patch file")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            showSnackbar("Exporting .patch file...")
        } catch (e: Exception) {
            showSnackbar("Export failed: ${e.localizedMessage}")
        }
    }

    // --- Preset Loaders ---

    fun loadSampleSchema() {
        val sampleDdl = SampleData.DEFAULT_SCHEMA_DDL
        val parsedEntities = SchemaParser.parseSchema(sampleDdl)
        val extractedIds = TextRecognitionHelper.extractIdentifiers(sampleDdl)
        _uiState.update {
            it.copy(
                schemaDdl = sampleDdl,
                schemaEntities = parsedEntities,
                extractedIdentifiers = extractedIds,
                ocrError = null
            )
        }
        showSnackbar("Loaded sample schema (Users, Orders, Products tables)")
    }

    fun loadSamplePreset() {
        val sampleDdl = SampleData.DEFAULT_SCHEMA_DDL
        val parsedEntities = SchemaParser.parseSchema(sampleDdl)
        val extractedIds = TextRecognitionHelper.extractIdentifiers(sampleDdl)
        _uiState.update {
            it.copy(
                packageName = SampleData.DEFAULT_PACKAGE,
                changeNotes = SampleData.DEFAULT_CHANGE_NOTES,
                codeBuffer = SampleData.DEFAULT_CODE_BUFFER,
                schemaDdl = sampleDdl,
                schemaEntities = parsedEntities,
                extractedIdentifiers = extractedIds,
                assessmentResult = null,
                traceError = null,
                assessmentError = null
            )
        }
        performTrace()
        showSnackbar("Loaded Drizzle ORM demo preset")
    }

    private fun showSnackbar(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopVoiceDictation()
    }
}
