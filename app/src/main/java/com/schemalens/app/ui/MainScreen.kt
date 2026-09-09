package com.schemalens.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.schemalens.app.data.AppTab
import com.schemalens.app.ui.components.ApiKeyDialog
import com.schemalens.app.ui.components.CameraCaptureDialog
import com.schemalens.app.ui.components.PhaseHeader
import com.schemalens.app.ui.components.QuickActionsFab
import com.schemalens.app.ui.navigation.BottomNavBar
import com.schemalens.app.ui.screens.AssessScreen
import com.schemalens.app.ui.screens.ExportScreen
import com.schemalens.app.ui.screens.SchemaScreen
import com.schemalens.app.ui.screens.TraceScreen
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCameraDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding(),
        topBar = {
            PhaseHeader(
                activeZone = uiState.activeZone,
                onZoneClick = { viewModel.setActiveZone(it) },
                onOpenSettings = { showApiKeyDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        floatingActionButton = {
            QuickActionsFab(
                onCameraClick = { showCameraDialog = true },
                onVoiceClick = { viewModel.toggleVoiceRecognition(context) },
                onTraceClick = {
                    viewModel.selectTab(AppTab.TRACE)
                    viewModel.performTrace()
                },
                onAssessClick = {
                    viewModel.selectTab(AppTab.ASSESS)
                    viewModel.performAssessment()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = BgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentTab,
                animationSpec = tween(250),
                label = "tabCrossfade"
            ) { tab ->
                when (tab) {
                    AppTab.SCHEMA -> SchemaScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenCamera = { showCameraDialog = true }
                    )
                    AppTab.TRACE -> TraceScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    AppTab.ASSESS -> AssessScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenSettings = { showApiKeyDialog = true }
                    )
                    AppTab.EXPORT -> ExportScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
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
