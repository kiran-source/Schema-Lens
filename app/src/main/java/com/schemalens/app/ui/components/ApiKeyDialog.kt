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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.schemalens.app.data.AppZone
import com.schemalens.app.network.AiProvider
import com.schemalens.app.ui.theme.AccentPurple
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.GradientEnd
import com.schemalens.app.ui.theme.GradientMid
import com.schemalens.app.ui.theme.GradientStart
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
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
                .clip(RoundedCornerShape(16.dp)),
            color = PanelDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with gradient accent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Assessment Engine",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ZoneBadge(zone = AppZone.GREEN_LIGHT)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select your reasoning provider for risk assessment:",
                    color = TextDim,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Provider Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Claude Opus — Recommended
                    ProviderOption(
                        title = "⚡ Claude Opus 4",
                        subtitle = "Anthropic's most powerful model with deep reasoning",
                        isSelected = selectedProvider == AiProvider.CLAUDE_OPUS,
                        isRecommended = true,
                        onClick = { selectedProvider = AiProvider.CLAUDE_OPUS }
                    )

                    ProviderOption(
                        title = "✨ SchemaLens / On-Device SLM",
                        subtitle = "100% on-device air-gapped, zero API key needed",
                        isSelected = selectedProvider == AiProvider.ON_DEVICE_SLM,
                        isRecommended = false,
                        onClick = { selectedProvider = AiProvider.ON_DEVICE_SLM }
                    )

                    ProviderOption(
                        title = "🌐 Custom / OpenAI Endpoint",
                        subtitle = "Compatible with any OpenAI-format API",
                        isSelected = selectedProvider == AiProvider.CUSTOM_OPENAI,
                        isRecommended = false,
                        onClick = { selectedProvider = AiProvider.CUSTOM_OPENAI }
                    )
                }

                // API Key input for cloud providers
                if (selectedProvider != AiProvider.ON_DEVICE_SLM) {
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = {
                            Text(
                                when (selectedProvider) {
                                    AiProvider.CLAUDE_OPUS -> "Anthropic API Key (sk-ant-...)"
                                    else -> "API Key (Bearer token)"
                                }
                            )
                        },
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

                Spacer(modifier = Modifier.height(20.dp))

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
                        Text("Apply Settings", color = BgDark, fontWeight = FontWeight.Bold)
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
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected && isRecommended -> AccentTeal
        isSelected -> AccentTeal
        else -> BorderDark
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PanelNested else Color.Transparent)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentTeal else BorderDark),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BgDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = if (isSelected) TextPrimary else TextDim,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isRecommended) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientStart, GradientMid, GradientEnd)
                                    )
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "BEST",
                                color = BgDark,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = TextFaint,
                    fontSize = 10.sp
                )
            }
        }
    }
}
