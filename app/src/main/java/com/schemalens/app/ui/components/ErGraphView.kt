package com.schemalens.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ErGraphView(
    entities: List<SchemaEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTable by remember { mutableStateOf<String?>(null) }
    val collapsedTables = remember { mutableStateMapOf<String, Boolean>() }

    // Draggable offsets for each table node
    val nodeOffsets = remember { mutableStateMapOf<String, Offset>() }

    // Color palette for table headers
    val tableColors = listOf(
        AccentTeal,
        RiskSafe,
        Color(0xFF60A5FA),
        Color(0xFFA78BFA),
        Color(0xFFFBBF24)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "4. INTERACTIVE ER SCHEMA GRAPH",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextDim,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Drag / Tap Node",
                        color = TextDim,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas relationship map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgDark)
                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
            ) {
                // Background relationship connector lines
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // User -> Orders FK curve
                    val userCenter = Offset(w * 0.25f, h * 0.45f)
                    val orderCenter = Offset(w * 0.72f, h * 0.25f)
                    val productCenter = Offset(w * 0.72f, h * 0.75f)

                    val pathUserOrders = Path().apply {
                        moveTo(userCenter.x + 40.dp.toPx(), userCenter.y - 10.dp.toPx())
                        cubicTo(
                            userCenter.x + (orderCenter.x - userCenter.x) * 0.5f, userCenter.y - 25.dp.toPx(),
                            userCenter.x + (orderCenter.x - userCenter.x) * 0.5f, orderCenter.y,
                            orderCenter.x - 45.dp.toPx(), orderCenter.y
                        )
                    }

                    val pathOrdersProduct = Path().apply {
                        moveTo(orderCenter.x - 10.dp.toPx(), orderCenter.y + 25.dp.toPx())
                        cubicTo(
                            orderCenter.x - 20.dp.toPx(), orderCenter.y + 45.dp.toPx(),
                            productCenter.x - 20.dp.toPx(), productCenter.y - 45.dp.toPx(),
                            productCenter.x - 10.dp.toPx(), productCenter.y - 20.dp.toPx()
                        )
                    }

                    drawPath(
                        path = pathUserOrders,
                        color = AccentTeal.copy(alpha = 0.8f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = pathOrdersProduct,
                        color = RiskSafe.copy(alpha = 0.7f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Interactive Table Nodes positioned inside
                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val firstEntity = entities.getOrNull(0)
                    val secondEntity = entities.getOrNull(1)
                    val thirdEntity = entities.getOrNull(2)

                    // Primary Node (e.g. Users)
                    if (firstEntity != null) {
                        val isCollapsed = collapsedTables[firstEntity.tableName] ?: false
                        val offset = nodeOffsets[firstEntity.tableName] ?: Offset.Zero

                        DraggableEntityCard(
                            entity = firstEntity,
                            headerColor = tableColors[0],
                            isSelected = selectedTable == firstEntity.tableName,
                            isCollapsed = isCollapsed,
                            offset = offset,
                            onDrag = { delta ->
                                val current = nodeOffsets[firstEntity.tableName] ?: Offset.Zero
                                nodeOffsets[firstEntity.tableName] = Offset(
                                    (current.x + delta.x).coerceIn(-20f, 30f),
                                    (current.y + delta.y).coerceIn(-30f, 30f)
                                )
                            },
                            onToggleCollapse = {
                                collapsedTables[firstEntity.tableName] = !isCollapsed
                            },
                            onSelect = {
                                selectedTable = if (selectedTable == firstEntity.tableName) null else firstEntity.tableName
                            },
                            modifier = Modifier.width(140.dp)
                        )
                    }

                    // Dependent Nodes Column (e.g. Orders, Products)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.width(140.dp)
                    ) {
                        if (secondEntity != null) {
                            val isCollapsed = collapsedTables[secondEntity.tableName] ?: false
                            val offset = nodeOffsets[secondEntity.tableName] ?: Offset.Zero

                            DraggableEntityCard(
                                entity = secondEntity,
                                headerColor = tableColors[1],
                                isSelected = selectedTable == secondEntity.tableName,
                                isCollapsed = isCollapsed,
                                offset = offset,
                                onDrag = { delta ->
                                    val current = nodeOffsets[secondEntity.tableName] ?: Offset.Zero
                                    nodeOffsets[secondEntity.tableName] = Offset(
                                        (current.x + delta.x).coerceIn(-30f, 20f),
                                        (current.y + delta.y).coerceIn(-20f, 20f)
                                    )
                                },
                                onToggleCollapse = {
                                    collapsedTables[secondEntity.tableName] = !isCollapsed
                                },
                                onSelect = {
                                    selectedTable = if (selectedTable == secondEntity.tableName) null else secondEntity.tableName
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (thirdEntity != null) {
                            val isCollapsed = collapsedTables[thirdEntity.tableName] ?: false
                            val offset = nodeOffsets[thirdEntity.tableName] ?: Offset.Zero

                            DraggableEntityCard(
                                entity = thirdEntity,
                                headerColor = tableColors[2],
                                isSelected = selectedTable == thirdEntity.tableName,
                                isCollapsed = isCollapsed,
                                offset = offset,
                                onDrag = { delta ->
                                    val current = nodeOffsets[thirdEntity.tableName] ?: Offset.Zero
                                    nodeOffsets[thirdEntity.tableName] = Offset(
                                        (current.x + delta.x).coerceIn(-30f, 20f),
                                        (current.y + delta.y).coerceIn(-20f, 20f)
                                    )
                                },
                                onToggleCollapse = {
                                    collapsedTables[thirdEntity.tableName] = !isCollapsed
                                },
                                onSelect = {
                                    selectedTable = if (selectedTable == thirdEntity.tableName) null else thirdEntity.tableName
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Relationship Legend
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RelationshipLegendChip(color = AccentTeal, label = "FK: orders.user_id → users.id")
                RelationshipLegendChip(color = RiskSafe, label = "FK: orders.product_id → products.id")
            }
        }
    }
}

@Composable
private fun DraggableEntityCard(
    entity: SchemaEntity,
    headerColor: Color,
    isSelected: Boolean,
    isCollapsed: Boolean,
    offset: Offset,
    onDrag: (Offset) -> Unit,
    onToggleCollapse: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .background(PanelNested)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) headerColor else BorderDark,
                shape = RoundedCornerShape(8.dp)
            )
            .animateContentSize()
            .padding(6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelect),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(headerColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = entity.tableName,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = "Toggle columns",
                    tint = TextDim,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onToggleCollapse)
                )
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    entity.columns.take(4).forEach { col ->
                        val isId = col.contains("id", ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isId) "🔑 " else "• ",
                                color = if (isId) AccentTeal else TextFaint,
                                fontSize = 8.sp
                            )
                            Text(
                                text = col,
                                color = if (isId) AccentTeal else TextDim,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationshipLegendChip(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PanelNested)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = TextDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

