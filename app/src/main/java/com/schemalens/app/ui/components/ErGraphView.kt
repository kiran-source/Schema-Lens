package com.schemalens.app.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemalens.app.data.SchemaEntity
import com.schemalens.app.ui.theme.AccentTeal
import com.schemalens.app.ui.theme.BgDark
import com.schemalens.app.ui.theme.BorderDark
import com.schemalens.app.ui.theme.PanelDark
import com.schemalens.app.ui.theme.PanelNested
import com.schemalens.app.ui.theme.RiskSafe
import com.schemalens.app.ui.theme.TextDim
import com.schemalens.app.ui.theme.TextFaint
import com.schemalens.app.ui.theme.TextPrimary

@Composable
fun ErGraphView(
    entities: List<SchemaEntity>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
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
                            .background(AccentTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SCHEMA ER RELATIONSHIP GRAPH",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${entities.size} tables detected",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Relationship Diagram
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgDark)
                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid points
                    val node1Center = Offset(w * 0.22f, h * 0.5f)
                    val node2Center = Offset(w * 0.78f, h * 0.28f)
                    val node3Center = Offset(w * 0.78f, h * 0.75f)

                    // Draw connecting relationship curves
                    val path1 = Path().apply {
                        moveTo(node1Center.x + 35.dp.toPx(), node1Center.y - 10.dp.toPx())
                        cubicTo(
                            node1Center.x + (node2Center.x - node1Center.x) * 0.5f, node1Center.y - 30.dp.toPx(),
                            node1Center.x + (node2Center.x - node1Center.x) * 0.5f, node2Center.y,
                            node2Center.x - 45.dp.toPx(), node2Center.y
                        )
                    }

                    val path2 = Path().apply {
                        moveTo(node1Center.x + 35.dp.toPx(), node1Center.y + 10.dp.toPx())
                        cubicTo(
                            node1Center.x + (node3Center.x - node1Center.x) * 0.5f, node1Center.y + 30.dp.toPx(),
                            node1Center.x + (node3Center.x - node1Center.x) * 0.5f, node3Center.y,
                            node3Center.x - 45.dp.toPx(), node3Center.y
                        )
                    }

                    drawPath(
                        path = path1,
                        color = AccentTeal.copy(alpha = 0.7f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = path2,
                        color = RiskSafe.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Entity Cards positioned inside
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val firstEntity = entities.getOrNull(0)
                    val secondEntity = entities.getOrNull(1)
                    val thirdEntity = entities.getOrNull(2)

                    // Primary Entity Card (e.g. Users)
                    if (firstEntity != null) {
                        EntityCard(
                            entity = firstEntity,
                            headerColor = AccentTeal,
                            modifier = Modifier.width(135.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (secondEntity != null) {
                            EntityCard(
                                entity = secondEntity,
                                headerColor = RiskSafe,
                                modifier = Modifier.width(135.dp)
                            )
                        }
                        if (thirdEntity != null) {
                            EntityCard(
                                entity = thirdEntity,
                                headerColor = Color(0xFF60A5FA),
                                modifier = Modifier.width(135.dp)
                            )
                        }
                    }
                }
            }

            // Relationship Legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegendItem(color = AccentTeal, label = "FK: orders.user_id → users.id")
                LegendItem(color = RiskSafe, label = "FK: audit_logs.user_id → users.id")
            }
        }
    }
}

@Composable
private fun EntityCard(
    entity: SchemaEntity,
    headerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PanelNested)
            .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
            .padding(6.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(headerColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = entity.tableName,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            entity.columns.take(3).forEach { col ->
                Text(
                    text = "• $col",
                    color = if (col.contains("id", ignoreCase = true)) AccentTeal else TextDim,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = TextFaint,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
