package com.schemalens.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextPrimary

import com.schemalens.app.network.AiProvider

@Composable
fun HardwareTelemetryHud(
    latencyMs: Long,
    provider: AiProvider = AiProvider.ON_DEVICE_SLM,
    modifier: Modifier = Modifier
) {
    // Dynamic runtime heap estimate
    val runtimeMemoryMb = remember {
        val runtime = Runtime.getRuntime()
        ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).coerceAtLeast(18)
    }

    val pulseTransition = rememberInfiniteTransition(label = "hudPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hudGlow"
    )

    val isAirGapped = provider == AiProvider.ON_DEVICE_SLM

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PanelDark,
                        if (isAirGapped) Color(0xFF102523) else Color(0xFF1E172E),
                        PanelDark
                    )
                )
            )
            .border(
                1.dp,
                if (isAirGapped) AccentTeal.copy(alpha = 0.4f * glowAlpha) else Color(0xFFD4A27F).copy(alpha = 0.5f * glowAlpha),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isAirGapped) RiskSafe.copy(alpha = glowAlpha) else Color(0xFFD4A27F).copy(alpha = glowAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAirGapped) "AIR-GAP HARDWARE TELEMETRY" else "CLAUDE NEURAL TELEMETRY",
                        color = if (isAirGapped) AccentTeal else Color(0xFFD4A27F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isAirGapped) "Zero Cloud Calls" else "Anthropic Claude API",
                    color = if (isAirGapped) RiskSafe else Color(0xFFD4A27F),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatItem(
                    icon = Icons.Default.Bolt,
                    label = if (isAirGapped) "SLM LATENCY" else "API LATENCY",
                    value = "${latencyMs}ms",
                    accentColor = if (isAirGapped) AccentTeal else Color(0xFFD4A27F)
                )

                TelemetryStatItem(
                    icon = Icons.Default.Memory,
                    label = "HEAP MEMORY",
                    value = "${runtimeMemoryMb} MB",
                    accentColor = Color(0xFF58A6FF)
                )

                TelemetryStatItem(
                    icon = Icons.Default.Shield,
                    label = "NET TRAFFIC",
                    value = if (isAirGapped) "0 KB" else "TLS / HTTPS",
                    accentColor = if (isAirGapped) RiskSafe else Color(0xFFD4A27F)
                )
            }
        }
    }
}

@Composable
private fun TelemetryStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PanelNested)
            .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    color = TextDim,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
