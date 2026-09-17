package com.schemalens.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
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
import com.schemalens.app.data.CallSite
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.haptic.HapticFeedbackManager
import com.schemalens.app.ui.theme.AccentTeal
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
import androidx.compose.ui.platform.LocalContext

@Composable
fun CallSiteCard(
    site: CallSite,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val severity = site.verdict?.sev ?: RiskSeverity.PENDING
    val context = LocalContext.current

    val (accentColor, chipBg, chipText) = when (severity) {
        RiskSeverity.BREAKING -> Triple(RiskBreaking, RiskBreakingBg, "BREAKING")
        RiskSeverity.RISKY -> Triple(RiskRisky, RiskRiskyBg, "RISKY")
        RiskSeverity.SAFE -> Triple(RiskSafe, RiskSafeBg, "SAFE")
        RiskSeverity.PENDING -> Triple(RiskPending, RiskPendingBg, "PENDING REVIEW")
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(400),
        label = "cardBorderAccent"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .clickable {
                isExpanded = !isExpanded
                // Haptic feedback on BREAKING card tap
                if (severity == RiskSeverity.BREAKING) {
                    HapticFeedbackManager.playRiskFeedback(context, severity)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Accent Border Pillar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(animatedBorderColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Header: File, Line, and Verdict Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = site.file ?: "inline",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ":${site.lineNumber}",
                            color = TextDim,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Verdict Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(chipBg)
                                .border(0.8.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chipText,
                                color = accentColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Code snippet line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PanelNested)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = site.lineText,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                // AI Risk Note (if available)
                if (site.verdict != null && site.verdict.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Impact:",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = site.verdict.note,
                            color = TextDim,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Matched identifier: ${site.matchedIdentifier}",
                        color = TextFaint,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Expandable Details
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderDark)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Site Index", "#${site.index}")
                        DetailRow("Target Symbol", site.matchedIdentifier)
                        DetailRow("Status", severity.name)
                        if (site.file != null) {
                            DetailRow("Source File", site.file)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = AccentTeal,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
