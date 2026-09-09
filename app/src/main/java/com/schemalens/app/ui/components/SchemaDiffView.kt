package com.schemalens.app.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.data.DiffChangeType
import com.schemalens.app.data.SchemaDiffEntry
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskBreaking
import com.schemalens.app.ui.theme.RiskBreakingBg
import com.schemalens.app.ui.theme.RiskRisky
import com.schemalens.app.ui.theme.RiskRiskyBg
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.RiskSafeBg
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary

@Composable
fun SchemaDiffView(
    diffEntries: List<SchemaDiffEntry>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEMA DIFF INSPECTOR",
                    color = AccentTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${diffEntries.size} changes",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (diffEntries.isEmpty()) {
                Text(
                    text = "No schema modifications detected.",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    diffEntries.forEach { entry ->
                        DiffEntryRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffEntryRow(entry: SchemaDiffEntry) {
    val (symbol, color, bg, label) = when (entry.changeType) {
        DiffChangeType.ADDED -> Quadruple("+", RiskSafe, RiskSafeBg, "ADDED")
        DiffChangeType.REMOVED -> Quadruple("-", RiskBreaking, RiskBreakingBg, "DROPPED")
        DiffChangeType.RENAMED -> Quadruple("~", RiskRisky, RiskRiskyBg, "RENAMED")
        DiffChangeType.TYPE_CHANGED -> Quadruple("Δ", RiskRisky, RiskRiskyBg, "TYPE ALTERED")
        DiffChangeType.UNCHANGED -> Quadruple("=", TextDim, PanelNested, "UNCHANGED")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = symbol,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.columnName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                if (entry.oldValue != null && entry.newValue != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${entry.oldValue} → ${entry.newValue})",
                        color = TextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = label,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
