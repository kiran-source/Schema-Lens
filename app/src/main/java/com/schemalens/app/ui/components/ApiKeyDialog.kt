package com.schemalens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.schemalens.app.data.AppZone
import com.schemalens.app.network.AiProvider
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary

@Composable
fun ApiKeyDialog(
    currentProvider: AiProvider,
    currentApiKey: String,
    currentEndpoint: String,
    onDismiss: () -> Unit,
    onSave: (provider: AiProvider, key: String, endpoint: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var keyInput by remember { mutableStateOf(currentApiKey) }
    var endpointInput by remember { mutableStateOf(currentEndpoint) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            color = PanelDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Assessment Engine",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ZoneBadge(zone = AppZone.GREEN_LIGHT)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select your reasoning provider for Green Light risk assessment:",
                    color = TextDim,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Selectors
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProviderOption(
                        title = "✨ SchemaLens Engine (Instant · Built-in)",
                        subtitle = "Runs automatically with zero API key or setup needed.",
                        isSelected = selectedProvider == AiProvider.SMART_LOCAL,
                        onClick = { selectedProvider = AiProvider.SMART_LOCAL }
                    )

                    ProviderOption(
                        title = "🌐 Google Gemini 1.5",
                        subtitle = "Connects to Gemini Flash via Google AI Studio API key.",
                        isSelected = selectedProvider == AiProvider.GEMINI,
                        onClick = { selectedProvider = AiProvider.GEMINI }
                    )

                    ProviderOption(
                        title = "⚡ Custom / OpenAI Endpoint",
                        subtitle = "Compatible with custom LLM servers and OpenAI proxies.",
                        isSelected = selectedProvider == AiProvider.CUSTOM_OPENAI,
                        onClick = { selectedProvider = AiProvider.CUSTOM_OPENAI }
                    )
                }

                if (selectedProvider != AiProvider.SMART_LOCAL) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text(if (selectedProvider == AiProvider.GEMINI) "Gemini API Key (AIzaSy...)" else "API Key (Bearer token)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = PanelNested,
                            unfocusedContainerColor = PanelNested
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedProvider == AiProvider.CUSTOM_OPENAI) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = endpointInput,
                            onValueChange = { endpointInput = it },
                            placeholder = { Text("https://api.openai.com/v1/chat/completions", color = TextFaint, fontSize = 11.sp) },
                            label = { Text("Custom Endpoint URL") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = PanelNested,
                                unfocusedContainerColor = PanelNested
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = TextDim)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(selectedProvider, keyInput.trim(), endpointInput.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text("Apply Settings", color = PanelDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PanelNested else Color.Transparent)
            .border(1.dp, if (isSelected) AccentTeal else BorderDark, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentTeal else BorderDark)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = if (isSelected) TextPrimary else TextDim,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = subtitle,
                    color = TextFaint,
                    fontSize = 10.sp
                )
            }
        }
    }
}
