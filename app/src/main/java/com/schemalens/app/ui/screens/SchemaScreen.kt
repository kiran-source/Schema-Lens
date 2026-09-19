package com.schemalens.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.schemalens.app.data.SampleData
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.ui.components.EmptyStateView
import com.schemalens.app.ui.components.ErGraphView
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskBreaking
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary
import com.schemalens.app.ui.viewmodel.MainUiState
import com.schemalens.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchemaScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleSelectedImageUri(context, it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header Card: Vision OCR & Source
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
                    Column {
                        Text(
                            text = "SCHEMA INGESTION",
                            color = AccentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ML Kit OCR & DDL Parser",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onOpenCamera,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentTeal,
                                contentColor = BgDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Gallery",
                                tint = AccentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                    }
                }

                // OCR Progress or Error
                if (uiState.isOcrLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PanelNested)
                            .padding(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentTeal,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Running on-device ML Kit OCR...",
                            color = TextDim,
                            fontSize = 12.sp
                        )
                    }
                }

                if (uiState.ocrError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.ocrError,
                        color = RiskBreaking,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instant Schema Presets Row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "QUICK DEMO PRESETS",
                        color = TextDim,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loadCustomSchemaPreset(SampleData.DEFAULT_SCHEMA_DDL, "E-Commerce (Orders, Products)") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.4f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🛒 E-Comm", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadCustomSchemaPreset(SampleData.SAAS_AUTH_SCHEMA_DDL, "SaaS Auth (Orgs, Users)") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔐 Auth", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadCustomSchemaPreset(SampleData.FINTECH_LEDGER_SCHEMA_DDL, "FinTech (Ledgers, Accounts)") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💳 Ledger", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DDL Input Field with Action Toolbar
                val clipboardManager = LocalClipboardManager.current
                OutlinedTextField(
                    value = uiState.schemaDdl,
                    onValueChange = { viewModel.updateSchemaDdl(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    label = { Text("Schema DDL / SQL / Whiteboard OCR", fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updates AST & ER graph automatically",
                        color = TextDim,
                        fontSize = 11.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Quick Paste Button
                        OutlinedButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    viewModel.updateSchemaDdl(text)
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = AccentTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 11.sp)
                        }

                        // Clear Button
                        if (uiState.schemaDdl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { viewModel.updateSchemaDdl("") },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDim),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Clear", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Extracted Identifiers Card
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
                        text = "EXTRACTED IDENTIFIERS",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${uiState.extractedIdentifiers.size} symbols",
                        color = TextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.extractedIdentifiers.isEmpty()) {
                    Text(
                        text = "No identifiers extracted yet. Enter DDL or scan a schema above.",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.extractedIdentifiers.forEach { identifier ->
                            IdentifierChip(
                                name = identifier,
                                onRemove = { viewModel.removeIdentifierChip(identifier) },
                                onClick = { viewModel.appendIdentifierToBuffer(identifier) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Identifier Add Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.customIdentifierInput,
                        onValueChange = { viewModel.updateCustomIdentifierInput(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        placeholder = { Text("Add custom table/column...", fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.addCustomIdentifier() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentTeal)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add symbol",
                            tint = BgDark
                        )
                    }
                }
            }
        }

        // Entity Relationship (ER) Graph Preview
        if (uiState.schemaEntities.isNotEmpty()) {
            ErGraphView(
                entities = uiState.schemaEntities,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = "No Entity Relationships",
                description = "Input CREATE TABLE statements or scan a schema to render the interactive ER diagram.",
                actionLabel = "Load Sample DDL",
                onActionClick = { viewModel.loadSampleSchema() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun IdentifierChip(
    name: String,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PanelNested)
            .border(0.8.dp, AccentTeal.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = TextDim,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}
