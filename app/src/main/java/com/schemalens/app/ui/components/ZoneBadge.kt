package com.schemalens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.data.AppZone
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.TextDim

@Composable
fun ZoneBadge(
    zone: AppZone,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, text) = when (zone) {
        AppZone.RED_LIGHT -> {
            Quad(
                Color(0xFF102A27),
                AccentTeal.copy(alpha = 0.4f),
                AccentTeal,
                "🔒 on-device · no network"
            )
        }
        AppZone.GREEN_LIGHT -> {
            Quad(
                Color(0xFF102A27),
                AccentTeal.copy(alpha = 0.4f),
                AccentTeal,
                "🔒 100% on-device SLM · air-gapped"
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
