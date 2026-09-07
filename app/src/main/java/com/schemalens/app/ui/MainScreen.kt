package com.schemalens.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.schemalens.app.data.AppZone
import com.schemalens.app.data.CallSite
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.ui.components.ApiKeyDialog
import com.schemalens.app.ui.components.CallSiteCard
import com.schemalens.app.ui.components.CameraCaptureDialog
import com.schemalens.app.ui.components.ErGraphView
import com.schemalens.app.ui.components.PhaseHeader
import com.schemalens.app.ui.components.RiskGauge
import com.schemalens.app.ui.components.ZoneBadge
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskBreaking
import com.schemalens.app.ui.theme.RiskBreakingBg
import com.schemalens.app.ui.theme.RiskPending
import com.schemalens.app.ui.theme.RiskPendingBg
import com.schemalens.app.ui.theme.RiskRisky
import com.schemalens.app.ui.theme.RiskRiskyBg
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.RiskSafeBg
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary
import com.schemalens.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCameraDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceRecognition(context)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    // Filter call sites based on search query and selected severity
    val filteredCallSites: List<CallSite> = uiState.callSites.filter { site ->
        val matchesQuery = if (uiState.callSiteSearchQuery.isBlank()) {
            true
        } else {
            val q = uiState.callSiteSearchQuery.trim().lowercase()
            (site.file?.lowercase()?.contains(q) == true) ||
                    site.lineText.lowercase().contains(q) ||
                    site.matchedIdentifier.lowercase().contains(q) ||
                    (site.verdict?.note?.lowercase()?.contains(q) == true)
        }

        val matchesSeverity = if (uiState.selectedSeverityFilter == null) {
            true
        } else {
            val siteSev = site.verdict?.sev ?: RiskSeverity.PENDING
            siteSev == uiState.selectedSeverityFilter
        }

        matchesQuery && matchesSeverity
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PhaseHeader(
                activeZone = uiState.activeZone,
                onZoneClick = { zone -> viewModel.setActiveZone(zone) },
                onOpenSettings = { showApiKeyDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 420.dp) // Phone-first constrained width
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ----------------------------------------------------
                // CARD 1: SCHEMA IDENTIFIER EXTRACTOR & REFINEMENT (Red Light)
                // ----------------------------------------------------
                SectionCard(
                    title = "1. Schema Capture & Extractor",
                    zone = AppZone.RED_LIGHT
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.packageName,
                                onValueChange = { viewModel.updatePackageName(it) },
                                label = { Text("Target Package / ORM", color = TextDim, fontSize = 12.sp) },
                                singleLine = true,
                                colors = inputFieldColors(),
                                modifier = Modifier.weight(1f)
                            )

                            // Camera Scanner Button
                            Button(
                                onClick = { showCameraDialog = true },
                                modifier = Modifier
                                    .height(54.dp)
                                    .widthIn(min = 52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PanelNested)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Schema OCR",
                                    tint = AccentTeal
                                )
                            }
                        }

                        // Load Sample Schema buttons for judges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "On-device ML Kit OCR (`[A-Za-z_][A-Za-z0-9_]{2,}`)",
                                color = TextFaint,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Load Sample Schema",
                                    color = AccentTeal,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PanelNested)
                                        .clickable { viewModel.loadSampleSchema() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // OCR Status & Progress
                        if (uiState.isOcrLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PanelNested)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Processing image on-device (NPU OCR)...",
                                    color = TextDim,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        uiState.ocrError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RiskBreakingBg)
                                    .border(1.dp, RiskBreaking.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RiskBreaking, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = err, color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }

                        // Captured image thumbnail
                        uiState.capturedImageBitmap?.let { bmp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PanelNested)
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Captured Schema",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Captured Whiteboard Frame · 100% On-Device",
                                    color = TextDim,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Extracted Identifier Chips
                        if (uiState.extractedIdentifiers.isNotEmpty()) {
                            Column {
                                Text(
                                    text = "Extracted Schema Identifiers (Tap chip to append to code, 'x' to dismiss):",
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    uiState.extractedIdentifiers.forEach { id ->
                                        IdentifierChip(
                                            name = id,
                                            onTap = { viewModel.appendIdentifierToBuffer(id) },
                                            onRemove = { viewModel.removeIdentifierChip(id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Add Custom Identifier Input Box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.customIdentifierInput,
                                onValueChange = { viewModel.updateCustomIdentifierInput(it) },
                                placeholder = { Text("Add custom identifier (e.g. user_id, sku)...", color = TextFaint, fontSize = 11.sp) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                ),
                                colors = inputFieldColors(),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { viewModel.addCustomIdentifier() },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PanelNested)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add identifier", tint = AccentTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // CARD 2: VOICE CHANGE NOTES & QUICK-PRESETS (Red Light)
                // ----------------------------------------------------
                SectionCard(
                    title = "2. Voice Change Notes & Templates",
                    zone = AppZone.RED_LIGHT
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.changeNotes,
                                onValueChange = { viewModel.updateChangeNotes(it) },
                                label = { Text("What's changing in schema / migration?", color = TextDim, fontSize = 12.sp) },
                                placeholder = { Text("e.g. Renaming users.hashed_password to password_hash...", color = TextFaint, fontSize = 12.sp) },
                                minLines = 3,
                                maxLines = 5,
                                colors = inputFieldColors(),
                                modifier = Modifier.weight(1f)
                            )

                            // Mic Button with pulsing animation when recording
                            val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (uiState.isVoiceListening) 1.15f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "micScale"
                            )

                            Button(
                                onClick = {
                                    val hasMicPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasMicPermission) {
                                        viewModel.toggleVoiceRecognition(context)
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .height(54.dp)
                                    .widthIn(min = 52.dp)
                                    .scale(pulseScale),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isVoiceListening) RiskBreaking else PanelNested
                                )
                            ) {
                                Icon(
                                    imageVector = if (uiState.isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Voice Dictate",
                                    tint = if (uiState.isVoiceListening) BgDark else AccentTeal
                                )
                            }
                        }

                        // Live partial transcription feedback
                        if (uiState.isVoiceListening) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PanelNested)
                                    .border(1.dp, AccentTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (uiState.voicePartialResult.isNotBlank()) "Listening: \"${uiState.voicePartialResult}\"" else "Listening (offline speech engine)...",
                                    color = AccentTeal,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        uiState.voiceError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RiskBreakingBg)
                                    .padding(8.dp)
                            ) {
                                Text(text = err, color = RiskBreaking, fontSize = 11.sp)
                            }
                        }

                        // Quick-Preset Template Chips
                        Text(
                            text = "Quick-Preset Templates (Tap to populate change field):",
                            color = TextDim,
                            fontSize = 11.sp
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetTemplateChip(
                                label = "+ Rename Column",
                                onClick = { viewModel.applyChangePreset("Rename `users.hashed_password` to `users.password_hash`") }
                            )
                            PresetTemplateChip(
                                label = "+ Drop Table",
                                onClick = { viewModel.applyChangePreset("Drop deprecated `role` column in favor of new `roles` permission table") }
                            )
                            PresetTemplateChip(
                                label = "+ Type Change (Int -> UUID)",
                                onClick = { viewModel.applyChangePreset("Change column `orders.id` type from INT to UUID") }
                            )
                            PresetTemplateChip(
                                label = "+ Add Nullable",
                                onClick = { viewModel.applyChangePreset("Alter column `users.phone_number` to NULLABLE") }
                            )
                        }
                    }
                }

                // ----------------------------------------------------
                // CARD 3: CODE BUFFER & MULTI-FILE BUFFER (Red Light)
                // ----------------------------------------------------
                SectionCard(
                    title = "3. Code Buffer (Multi-File)",
                    zone = AppZone.RED_LIGHT
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Split files with: // === filename ===",
                                color = TextFaint,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()
                                        if (!clip.isNullOrBlank()) {
                                            viewModel.updateCodeBuffer(clip.text)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from Clipboard",
                                        tint = AccentTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.codeBuffer,
                            onValueChange = { viewModel.updateCodeBuffer(it) },
                            minLines = 6,
                            maxLines = 10,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextPrimary
                            ),
                            colors = inputFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ----------------------------------------------------
                // CARD 4: STATIC CALL-SITE TRACER & SEARCH / FILTER (Red Light)
                // ----------------------------------------------------
                SectionCard(
                    title = "4. Static Package Call Sites",
                    zone = AppZone.RED_LIGHT
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.performTrace() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = BgDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trace \"${uiState.packageName.ifBlank { "package" }}\" (Offline AST)",
                                color = BgDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        uiState.traceError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PanelNested)
                                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = err,
                                    color = TextDim,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (uiState.callSites.isNotEmpty()) {
                            // Search & Filter Bar
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Search Input Box
                                OutlinedTextField(
                                    value = uiState.callSiteSearchQuery,
                                    onValueChange = { viewModel.updateCallSiteSearchQuery(it) },
                                    placeholder = { Text("Filter call sites by file or code...", color = TextFaint, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = TextDim, modifier = Modifier.size(16.dp))
                                    },
                                    trailingIcon = {
                                        if (uiState.callSiteSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.updateCallSiteSearchQuery("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextDim, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = inputFieldColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Severity Filter Chips Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(filterScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val totalCount = uiState.callSites.size
                                    val breakingCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.BREAKING }
                                    val riskyCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.RISKY }
                                    val safeCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.SAFE }
                                    val pendingCount = uiState.callSites.count { it.verdict?.sev == null || it.verdict.sev == RiskSeverity.PENDING }

                                    SeverityFilterChip(
                                        label = "All ($totalCount)",
                                        isSelected = uiState.selectedSeverityFilter == null,
                                        color = AccentTeal,
                                        onClick = { viewModel.setSelectedSeverityFilter(null) }
                                    )
                                    if (breakingCount > 0) {
                                        SeverityFilterChip(
                                            label = "Breaking ($breakingCount)",
                                            isSelected = uiState.selectedSeverityFilter == RiskSeverity.BREAKING,
                                            color = RiskBreaking,
                                            onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.BREAKING) }
                                        )
                                    }
                                    if (riskyCount > 0) {
                                        SeverityFilterChip(
                                            label = "Risky ($riskyCount)",
                                            isSelected = uiState.selectedSeverityFilter == RiskSeverity.RISKY,
                                            color = RiskRisky,
                                            onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.RISKY) }
                                        )
                                    }
                                    if (safeCount > 0) {
                                        SeverityFilterChip(
                                            label = "Safe ($safeCount)",
                                            isSelected = uiState.selectedSeverityFilter == RiskSeverity.SAFE,
                                            color = RiskSafe,
                                            onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.SAFE) }
                                        )
                                    }
                                    if (pendingCount > 0 && breakingCount == 0 && riskyCount == 0 && safeCount == 0) {
                                        SeverityFilterChip(
                                            label = "Pending ($pendingCount)",
                                            isSelected = uiState.selectedSeverityFilter == RiskSeverity.PENDING,
                                            color = RiskPending,
                                            onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.PENDING) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Displaying ${filteredCallSites.size} of ${uiState.callSites.size} detected call sites:",
                                color = TextDim,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            if (filteredCallSites.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PanelNested)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "No call sites match current filter.",
                                        color = TextFaint,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    filteredCallSites.forEach { site ->
                                        CallSiteCard(site = site)
                                    }
                                }
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // CARD 5: INTERACTIVE ER RELATIONSHIP GRAPH (Red Light stretch)
                // ----------------------------------------------------
                if (uiState.schemaEntities.isNotEmpty()) {
                    ErGraphView(entities = uiState.schemaEntities)
                }

                // ----------------------------------------------------
                // CARD 6: DEEP RISK ASSESSMENT (Green Light)
                // ----------------------------------------------------
                SectionCard(
                    title = "5. Deep Risk Assessment",
                    zone = AppZone.GREEN_LIGHT
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val isAssessEnabled = uiState.callSites.isNotEmpty() &&
                                uiState.changeNotes.isNotBlank() &&
                                !uiState.isAssessing

                        Button(
                            onClick = { viewModel.performAssessment() },
                            enabled = isAssessEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RiskSafe,
                                disabledContainerColor = PanelNested
                            )
                        ) {
                            if (uiState.isAssessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = BgDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing Call Sites with AI...",
                                    color = BgDark,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if (isAssessEnabled) BgDark else TextFaint)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Run Deep Risk Assessment",
                                    color = if (isAssessEnabled) BgDark else TextFaint,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (!isAssessEnabled && !uiState.isAssessing) {
                            Text(
                                text = "• Requires at least 1 traced call site\n• Requires non-empty change notes description",
                                color = TextFaint,
                                fontSize = 11.sp
                            )
                        }

                        // Assessment Failure Retry UI
                        uiState.assessmentError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RiskBreakingBg)
                                    .border(1.dp, RiskBreaking.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = RiskBreaking, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Assessment Failed",
                                            color = RiskBreaking,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = err, color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { viewModel.performAssessment() }
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Retry Assessment", color = TextPrimary, fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { showApiKeyDialog = true }
                                        ) {
                                            Text("Configure AI Engine", color = AccentTeal, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Circular Risk Gauge + Summary
                        uiState.assessmentResult?.let { result ->
                            RiskGauge(score = result.overallScore, summary = result.summary)
                        }
                    }
                }

                // ----------------------------------------------------
                // CARD 7: ORM PATCH & OFFICE KIT EXPORT HUB (Green Light)
                // ----------------------------------------------------
                uiState.assessmentResult?.let { result ->
                    SectionCard(
                        title = "6. iQOO Office Kit Bridge & Export Hub",
                        zone = AppZone.GREEN_LIGHT
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Optimized ORM migration patch ready for desktop IDE handoff:",
                                color = TextDim,
                                fontSize = 11.sp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PanelNested)
                                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = result.ormPatch,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.copyOrmPatchToClipboard(context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy to Office Kit",
                                        color = BgDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.exportPatchFile(context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Export .patch",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Camera Capture & Gallery Dialog
    if (showCameraDialog) {
        CameraCaptureDialog(
            onDismiss = { showCameraDialog = false },
            onImageCaptured = { bitmap ->
                viewModel.handleCapturedBitmap(bitmap)
            },
            onImageUriSelected = { uri ->
                viewModel.handleSelectedImageUri(context, uri)
            }
        )
    }

    // AI Engine Configuration Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentProvider = uiState.aiProvider,
            currentApiKey = uiState.apiKey,
            currentEndpoint = uiState.customEndpoint,
            onDismiss = { showApiKeyDialog = false },
            onSave = { provider, newKey, endpoint ->
                viewModel.updateAiConfig(provider, newKey, endpoint)
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    zone: AppZone,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                ZoneBadge(zone = zone)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun IdentifierChip(
    name: String,
    onTap: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PanelNested)
            .border(0.8.dp, AccentTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                color = AccentTeal,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove chip",
                tint = TextDim,
                modifier = Modifier
                    .size(12.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@Composable
private fun PresetTemplateChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PanelNested)
            .border(0.8.dp, BorderDark, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = AccentTeal,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SeverityFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else PanelNested)
            .border(
                width = if (isSelected) 1.2.dp else 0.8.dp,
                color = if (isSelected) color else BorderDark,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = if (isSelected) color else TextDim,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun inputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentTeal,
    unfocusedBorderColor = BorderDark,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = PanelNested,
    unfocusedContainerColor = PanelNested
)
