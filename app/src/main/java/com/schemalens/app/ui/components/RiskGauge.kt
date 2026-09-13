package com.schemalens.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskBreaking
import com.schemalens.app.ui.theme.RiskRisky
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary

@Composable
fun RiskGauge(
    score: Int,
    summary: String,
    breakingCount: Int = 0,
    riskyCount: Int = 0,
    safeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val clampedScore = score.coerceIn(0, 100)

    val (gaugeColor, riskLabel) = when {
        clampedScore >= 66 -> Pair(RiskBreaking, "HIGH RISK")
        clampedScore >= 33 -> Pair(RiskRisky, "MODERATE")
        else -> Pair(RiskSafe, "LOW RISK")
    }

    val animatedProgress by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Semi-circular speedometer gauge
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2

                    // Background track (semi-circle)
                    drawArc(
                        color = Color(0xFF21262D),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Gradient progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to RiskSafe,
                            0.3f to RiskRisky,
                            0.6f to RiskBreaking,
                            1f to RiskBreaking
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Needle endpoint dot
                    val needleAngle = Math.toRadians((180.0 + 180.0 * animatedProgress))
                    val needleX = center.x + radius * kotlin.math.cos(needleAngle).toFloat()
                    val needleY = center.y + radius * kotlin.math.sin(needleAngle).toFloat()
                    drawCircle(
                        color = gaugeColor,
                        radius = 6.dp.toPx(),
                        center = Offset(needleX, needleY)
                    )
                    drawCircle(
                        color = Color(0xFF0D1117),
                        radius = 3.dp.toPx(),
                        center = Offset(needleX, needleY)
                    )
                }

                // Center score label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "$clampedScore",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = riskLabel,
                        color = gaugeColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary
            Text(
                text = summary,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Risk breakdown bar
            val total = (breakingCount + riskyCount + safeCount).coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PanelNested)
            ) {
                if (breakingCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(breakingCount.toFloat() / total)
                            .height(6.dp)
                            .background(RiskBreaking)
                    )
                }
                if (riskyCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(riskyCount.toFloat() / total)
                            .height(6.dp)
                            .background(RiskRisky)
                    )
                }
                if (safeCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(safeCount.toFloat() / total)
                            .height(6.dp)
                            .background(RiskSafe)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskLegendChip(count = breakingCount, label = "Breaking", color = RiskBreaking)
                RiskLegendChip(count = riskyCount, label = "Risky", color = RiskRisky)
                RiskLegendChip(count = safeCount, label = "Safe", color = RiskSafe)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Powered by On-Device SLM · Gemma 2B Air-Gapped Engine",
                color = TextFaint,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RiskLegendChip(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$count $label",
            color = TextDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
