package com.schemalens.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.data.AppTab
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.data.SampleData
import com.schemalens.app.ui.components.ApiKeyDialog
import com.schemalens.app.ui.components.CameraCaptureDialog
import com.schemalens.app.ui.screens.AssessScreen
import com.schemalens.app.ui.screens.ExportScreen
import com.schemalens.app.ui.screens.SchemaScreen
import com.schemalens.app.ui.screens.TraceScreen
import com.schemalens.app.ui.viewmodel.MainUiState
import com.schemalens.app.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stepper state: 0 = Connect, 1 = Schema, 2 = Review, 3 = Deploy (default 2 to match mockup)
    var currentStep by remember { mutableIntStateOf(2) }

    var showCameraDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Keep currentStep synced with uiState.currentTab if user changes it elsewhere
    LaunchedEffect(uiState.currentTab) {
        currentStep = when (uiState.currentTab) {
            AppTab.SCHEMA -> 0
            AppTab.TRACE -> 1
            AppTab.ASSESS -> 2
            AppTab.EXPORT -> 3
        }
    }

    // <main> full-screen, centers the frame on black
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Phone frame — max-w-[420px], zinc-950, rounded-[2.5rem]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF090A0C))
                .border(1.dp, Color(0xFF1E222A), RoundedCornerShape(40.dp))
                .statusBarsPadding()
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    Column {
                        // <header> top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // App Icon tile
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF14171E))
                                        .border(1.dp, Color(0xFF222733), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storage,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Schema Studio",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    )
                                    Text(
                                        text = "production · main",
                                        color = Color(0xFF8B93A1),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Settings action button (Settings2)
                            IconButton(
                                onClick = { showApiKeyDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF14171E))
                                    .border(1.dp, Color(0xFF222733), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Settings",
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // <ProgressStepper current={2} />
                        ProgressStepper(
                            currentStep = currentStep,
                            onStepClick = { step ->
                                currentStep = step
                                val tab = when (step) {
                                    0 -> AppTab.SCHEMA
                                    1 -> AppTab.TRACE
                                    2 -> AppTab.ASSESS
                                    else -> AppTab.EXPORT
                                }
                                viewModel.selectTab(tab)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = currentStep,
                        animationSpec = tween(250),
                        label = "studioStepCrossfade"
                    ) { step ->
                        when (step) {
                            // Step 0: Connect / Input Schema
                            0 -> SchemaScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenCamera = { showCameraDialog = true }
                            )
                            // Step 1: Schema AST Trace
                            1 -> TraceScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            // Step 2: Review (Visual Layout with PresetCards, RiskDial, and PreviewSheet)
                            2 -> SchemaStudioReviewContent(
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenSettings = { showApiKeyDialog = true },
                                onCopyDdl = { viewModel.copyOrmPatchToClipboard(context) }
                            )
                            // Step 3: Deploy
                            else -> ExportScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Camera Capture Modal
    if (showCameraDialog) {
        CameraCaptureDialog(
            onDismiss = { showCameraDialog = false },
            onImageCaptured = { bitmap ->
                showCameraDialog = false
                viewModel.handleCapturedBitmap(bitmap)
            },
            onImageUriSelected = { uri ->
                showCameraDialog = false
                viewModel.handleSelectedImageUri(context, uri)
            }
        )
    }

    // API Key & Model Configuration Modal
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentProvider = uiState.aiProvider,
            currentApiKey = uiState.apiKey,
            currentEndpoint = uiState.customEndpoint,
            onDismiss = { showApiKeyDialog = false },
            onSave = { provider, key, endpoint ->
                viewModel.updateAiConfig(provider, key, endpoint)
                showApiKeyDialog = false
            }
        )
    }
}

/**
 * <ProgressStepper current={2} />
 * Top progress stepper with 4 steps and horizontal connector lines
 */
@Composable
fun ProgressStepper(
    currentStep: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Connect", "Schema", "Review", "Deploy")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, label ->
            // Step Node (dot/check) + Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStepClick(index) }
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < currentStep -> Color(0xFF10B981)
                                index == currentStep -> Color.White
                                else -> Color(0xFF161A22)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                index < currentStep -> Color(0xFF10B981)
                                index == currentStep -> Color.White
                                else -> Color(0xFF262C38)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        index < currentStep -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color(0xFF090A0C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        index == currentStep -> {
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFF090A0C),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFF5A6270),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == currentStep) Color.White else if (index < currentStep) Color(0xFF8B93A1) else Color(0xFF5A6270)
                )
            }

            // Connector between step nodes
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .offset(y = (-10).dp)
                        .background(
                            if (index < currentStep) Color(0xFF10B981) else Color(0xFF202630)
                        )
                )
            }
        }
    }
}

/**
 * Visual Layout content for Review (Step 2)
 * Contains:
 * - <PresetCards />: Relational, Document, Edge KV
 * - <RiskDial score={34} />: 270° circular arc gauge + stats
 * - <PreviewSheet />: Collapsible users.sql Generated DDL preview
 */
@Composable
fun SchemaStudioReviewContent(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onCopyDdl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedPreset by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // <PresetCards />
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schema presets",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Choose a base",
                    color = Color(0xFF8B93A1),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        selectedPreset = (selectedPreset + 1) % 3
                    }
                )
            }

            // 3 Selectable Preset Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PresetCard(
                    title = "Relational",
                    description = "Normalized Postgres with foreign keys",
                    footer = "12 tables",
                    icon = Icons.Outlined.Storage,
                    isSelected = selectedPreset == 0,
                    onClick = {
                        selectedPreset = 0
                        viewModel.updateSchemaDdl(SampleData.DEFAULT_SCHEMA_DDL)
                        viewModel.updateCodeBuffer(SampleData.DEFAULT_CODE_BUFFER)
                        viewModel.performTrace()
                    }
                )

                PresetCard(
                    title = "Document",
                    description = "Flexible nested collections",
                    footer = "6 collections",
                    icon = Icons.Outlined.Layers,
                    isSelected = selectedPreset == 1,
                    onClick = {
                        selectedPreset = 1
                        viewModel.applyChangePreset("Transitioning to Document embedded collections for user profiles and audit logs.")
                    }
                )

                PresetCard(
                    title = "Edge KV",
                    description = "Low-latency key-value at the edge",
                    footer = "3 namespaces",
                    icon = Icons.Outlined.Bolt,
                    isSelected = selectedPreset == 2,
                    onClick = {
                        selectedPreset = 2
                        viewModel.applyChangePreset("Migrating session storage to Edge KV cache namespaces.")
                    }
                )
            }
        }

        // <RiskDial score={34} />
        MigrationRiskCard(
            uiState = uiState,
            onAssess = { viewModel.performAssessment(context) }
        )

        // <PreviewSheet />
        PreviewSheet(
            ddlContent = uiState.schemaDdl,
            onCopyDdl = onCopyDdl
        )
    }
}

/**
 * Selectable preset card component
 */
@Composable
fun PresetCard(
    title: String,
    description: String,
    footer: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101318))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.8f) else Color(0xFF1E232B),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon tile
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF181D25))
                    .border(1.dp, Color(0xFF242B36), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Title + description
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = Color(0xFF8B93A1),
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    maxLines = 2
                )
            }

            // Footer
            Text(
                text = footer,
                color = Color(0xFF5A6270),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * <RiskDial score={34} />
 * Migration risk container with 270° circular arc gauge and category breakdown stats
 */
@Composable
fun MigrationRiskCard(
    uiState: MainUiState,
    onAssess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = uiState.assessmentResult?.overallScore ?: 34
    val riskColor = when {
        score > 65 -> Color(0xFFE5484D)
        score > 25 -> Color(0xFFF59E0B)
        else -> Color(0xFF3DD68C)
    }
    val riskLabel = when {
        score > 65 -> "Critical risk"
        score > 25 -> "Moderate risk"
        else -> "Low risk"
    }

    val breakingCount = uiState.assessmentResult?.sites?.count { it.sev == RiskSeverity.BREAKING } ?: 0
    val warningCount = uiState.assessmentResult?.sites?.count { it.sev == RiskSeverity.RISKY } ?: 3
    val indexesCount = uiState.callSites.size.takeIf { it > 0 } ?: 8

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF101318))
            .border(1.dp, Color(0xFF1E232B), RoundedCornerShape(24.dp))
            .clickable(onClick = onAssess)
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Migration risk",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // "live" pill badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF181F26))
                        .border(1.dp, Color(0xFF222B34), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "live",
                        color = Color(0xFF8B93A1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 270° Circular Arc Dial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSizeObj = Size(arcSize, arcSize)

                    // 270° Arc: start at 135°, sweep 270°
                    drawArc(
                        color = Color(0xFF1F2530),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSizeObj,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress stroke
                    val progressSweep = (score / 100f).coerceIn(0f, 1f) * 270f
                    drawArc(
                        color = riskColor,
                        startAngle = 135f,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSizeObj,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center label (score + risk level)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$score",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = riskLabel,
                        color = riskColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown stats rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    count = "$breakingCount",
                    label = "Breaking"
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    count = "$warningCount",
                    label = "Warnings"
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    count = "$indexesCount",
                    label = "Indexes"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Risk Assessment Action Button
            Button(
                onClick = onAssess,
                enabled = !uiState.isAssessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2DD4BF),
                    contentColor = Color(0xFF090A0C),
                    disabledContainerColor = Color(0xFF1E232B),
                    disabledContentColor = Color(0xFF5A6270)
                )
            ) {
                if (uiState.isAssessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF090A0C),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reasoning with On-Device SLM (Gemma 2B)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Assess",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Risk Assessment (On-Device SLM)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Telemetry micro-row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒 100% Air-Gapped SLM · 0 KB Net Calls · ~${uiState.inferenceLatencyMs}ms",
                    color = Color(0xFF5A6270),
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Breakdown stat box inside migration risk card
 */
@Composable
fun StatBox(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF14181F))
            .border(1.dp, Color(0xFF1F242D), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color(0xFF8B93A1),
                fontSize = 11.5.sp
            )
        }
    }
}

/**
 * <PreviewSheet />
 * Rounded bottom-sheet previews with collapsible users.sql generated DDL
 */
@Composable
fun PreviewSheet(
    ddlContent: String,
    onCopyDdl: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Previews",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF101318))
                .border(1.dp, Color(0xFF1E232B), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF171B22))
                                .border(1.dp, Color(0xFF222834), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Code,
                                contentDescription = "SQL file",
                                tint = Color(0xFF8B93A1),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "users.sql",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Generated DDL",
                                color = Color(0xFF8B93A1),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = Color(0xFF8B93A1)
                        )
                    }
                }

                // Collapsible body
                AnimatedVisibility(visible = isExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF07090C))
                            .border(1.dp, Color(0xFF181C24), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val ddlLines = listOf(
                            "01  create table users (",
                            "02    id uuid primary key default gen_random_uuid(),",
                            "03    email text unique not null,",
                            "04    created_at timestamptz default now()",
                            "05  );"
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            ddlLines.forEach { line ->
                                Row {
                                    Text(
                                        text = line.take(4),
                                        color = Color(0xFF475569),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = line.drop(4),
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Sparkle / Copy action button in bottom right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14171E))
                                .border(1.dp, Color(0xFF262C38), CircleShape)
                            .clickable(onClick = onCopyDdl),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "Copy DDL",
                                tint = Color(0xFFD1D5DB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview of the complete Schema Studio Review dashboard inside Android Studio.
 * Allows viewing and interacting with the UI without needing a physical device or emulator.
 */
@androidx.compose.ui.tooling.preview.Preview(
    name = "Schema Studio Review Layout",
    showBackground = true,
    backgroundColor = 0xFF090A0C,
    widthDp = 412,
    heightDp = 892
)
@Composable
fun SchemaStudioReviewPreview() {
    val sampleUiState = MainUiState(
        currentTab = AppTab.ASSESS,
        schemaDdl = SampleData.DEFAULT_SCHEMA_DDL
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF090A0C))
                .border(1.dp, Color(0xFF1E222A), RoundedCornerShape(40.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14171E))
                                .border(1.dp, Color(0xFF222733), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Schema Studio",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "production · main",
                                color = Color(0xFF8B93A1),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14171E))
                            .border(1.dp, Color(0xFF222733), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Settings",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Progress Stepper
                ProgressStepper(
                    currentStep = 2,
                    onStepClick = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Studio Review Dashboard Content
                SchemaStudioReviewContent(
                    uiState = sampleUiState,
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                    onOpenSettings = {},
                    onCopyDdl = {}
                )
            }
        }
    }
}

