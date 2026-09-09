package com.schemalens.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.ui.components.CallSiteCard
import com.schemalens.app.ui.components.EmptyStateView
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskBreaking
import com.schemalens.app.ui.theme.RiskPending
import com.schemalens.app.ui.theme.RiskRisky
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextPrimary
import com.schemalens.app.ui.viewmodel.MainUiState
import com.schemalens.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TraceScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Filter call sites by search query and severity
    val filteredSites = uiState.callSites.filter { site ->
        val matchesQuery = uiState.callSiteSearchQuery.isBlank() ||
                site.lineText.contains(uiState.callSiteSearchQuery, ignoreCase = true) ||
                site.matchedIdentifier.contains(uiState.callSiteSearchQuery, ignoreCase = true) ||
                (site.file?.contains(uiState.callSiteSearchQuery, ignoreCase = true) == true)

        val matchesSeverity = uiState.selectedSeverityFilter == null ||
                (site.verdict?.sev ?: RiskSeverity.PENDING) == uiState.selectedSeverityFilter

        matchesQuery && matchesSeverity
    }

    val breakingCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.BREAKING }
    val riskyCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.RISKY }
    val safeCount = uiState.callSites.count { it.verdict?.sev == RiskSeverity.SAFE }
    val pendingCount = uiState.callSites.count { it.verdict == null || it.verdict.sev == RiskSeverity.PENDING }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Target Package & Code Buffer Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PanelDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TARGET PACKAGE TRACER",
                    color = AccentTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Package Name
                OutlinedTextField(
                    value = uiState.packageName,
                    onValueChange = { viewModel.updatePackageName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tracked Package / Module Name", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = PanelNested,
                        unfocusedContainerColor = PanelNested
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Code Buffer
                OutlinedTextField(
                    value = uiState.codeBuffer,
                    onValueChange = { viewModel.updateCodeBuffer(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    label = { Text("Application Source Buffer", fontSize = 12.sp) },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = PanelNested,
                        unfocusedContainerColor = PanelNested
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.performTrace() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        contentColor = BgDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Trace",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trace Call Sites (On-Device AST)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Call Sites Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PanelDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALL SITES DISCOVERED",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${filteredSites.size} of ${uiState.callSites.size}",
                        color = TextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = uiState.callSiteSearchQuery,
                    onValueChange = { viewModel.updateCallSiteSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = { Text("Search file, line, symbol...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextDim,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.callSiteSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateCallSiteSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextDim,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = PanelNested,
                        unfocusedContainerColor = PanelNested
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Severity Filter Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SeverityFilterChip(
                        label = "ALL",
                        count = uiState.callSites.size,
                        isSelected = uiState.selectedSeverityFilter == null,
                        color = AccentTeal,
                        onClick = { viewModel.setSelectedSeverityFilter(null) }
                    )

                    SeverityFilterChip(
                        label = "BREAKING",
                        count = breakingCount,
                        isSelected = uiState.selectedSeverityFilter == RiskSeverity.BREAKING,
                        color = RiskBreaking,
                        onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.BREAKING) }
                    )

                    SeverityFilterChip(
                        label = "RISKY",
                        count = riskyCount,
                        isSelected = uiState.selectedSeverityFilter == RiskSeverity.RISKY,
                        color = RiskRisky,
                        onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.RISKY) }
                    )

                    SeverityFilterChip(
                        label = "SAFE",
                        count = safeCount,
                        isSelected = uiState.selectedSeverityFilter == RiskSeverity.SAFE,
                        color = RiskSafe,
                        onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.SAFE) }
                    )

                    SeverityFilterChip(
                        label = "PENDING",
                        count = pendingCount,
                        isSelected = uiState.selectedSeverityFilter == RiskSeverity.PENDING,
                        color = RiskPending,
                        onClick = { viewModel.setSelectedSeverityFilter(RiskSeverity.PENDING) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (filteredSites.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Code,
                        title = if (uiState.callSites.isEmpty()) "No Call Sites Traced" else "No Matching Sites",
                        description = if (uiState.callSites.isEmpty())
                            "Tap 'Trace Call Sites' to scan source code for usage of '${uiState.packageName}'."
                        else
                            "No call sites match your filter criteria.",
                        actionLabel = if (uiState.callSites.isEmpty()) "Run Sample Trace" else "Clear Filters",
                        onActionClick = {
                            if (uiState.callSites.isEmpty()) viewModel.loadSamplePreset()
                            else {
                                viewModel.updateCallSiteSearchQuery("")
                                viewModel.setSelectedSeverityFilter(null)
                            }
                        }
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredSites.forEach { site ->
                            CallSiteCard(site = site)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SeverityFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else PanelNested)
            .border(
                1.dp,
                if (isSelected) color else BorderDark,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label ($count)",
            color = if (isSelected) color else TextDim,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
