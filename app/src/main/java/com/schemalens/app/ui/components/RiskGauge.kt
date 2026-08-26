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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.ui.theme.BorderDark
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
    modifier: Modifier = Modifier
) {
    val clampedScore = score.coerceIn(0, 100)

    val (gaugeColor, riskLabel) = when {
        clampedScore >= 66 -> Pair(RiskBreaking, "HIGH RISK / BREAKING")
        clampedScore >= 33 -> Pair(RiskRisky, "MODERATE RISK")
        else -> Pair(RiskSafe, "LOW RISK / SAFE")
    }

    val animatedProgress by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelNested)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Gauge
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val strokeWidth = 8.dp.toPx()

                    // Background track
                    drawCircle(
                        color = Color(0xFF28303C),
                        radius = (size.minDimension - strokeWidth) / 2,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active risk arc
                    drawArc(
                        color = gaugeColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$clampedScore",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "/100",
                        color = TextFaint,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Score explanation and summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = riskLabel,
                    color = gaugeColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Evaluated against AST traced call sites via Claude Sonnet 4.6",
                    color = TextDim,
                    fontSize = 10.sp
                )
            }
        }
    }
}
