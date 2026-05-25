package org.dsqrwym.enterprise.ui.components.dashbord

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_status_accepted
import maian.shared.generated.resources.order_status_cancelled
import maian.shared.generated.resources.order_status_pending
import maian.shared.generated.resources.order_status_rejected
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.shared.util.formatter.toFixed
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

internal data class DashboardStatusEntry(
    val label: String,
    val value: Int,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderStatusChart(data: DashboardResponse) {
    val colors = MaterialTheme.colorScheme
    val entries = listOf(
        DashboardStatusEntry(stringResource(SharedRes.string.order_status_pending), data.orderStatus.pending, colors.tertiary),
        DashboardStatusEntry(stringResource(SharedRes.string.order_status_accepted), data.orderStatus.accepted, colors.primary),
        DashboardStatusEntry(stringResource(SharedRes.string.order_status_rejected), data.orderStatus.rejected, colors.error),
        DashboardStatusEntry(stringResource(SharedRes.string.order_status_cancelled), data.orderStatus.cancelled, colors.outline),
    )
    if (entries.all { it.value == 0 }) {
        DashboardNoChartData()
        return
    }

    var selectedIndex by remember(entries) {
        mutableStateOf(entries.indexOfFirst { it.value > 0 }.takeIf { it >= 0 })
    }
    val selectedOffset by animateDpAsState(if (selectedIndex == null) 0.dp else 10.dp)
    val selectedStrokeThickness by animateDpAsState(if (selectedIndex == null) 1.dp else 8.dp)
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val selectedText = selectedIndex?.let { entries.getOrNull(it)?.let { entry -> dashboardPieSelectionText(entry, entries) } }
    val labelBackground = ShapeComponent(
        fill = Fill(colors.surface.copy(alpha = 0.88f)),
        strokeFill = Fill(colors.outlineVariant.copy(alpha = 0.55f)),
        strokeThickness = 1.dp,
    )
    val selectedLabel = PieChart.SliceLabel.Outside(
        textComponent = rememberTextComponent(
            style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface),
            lineCount = 2,
            padding = Insets(horizontal = 6.dp, vertical = 3.dp),
            background = labelBackground,
        ),
        lineColor = colors.outlineVariant,
        lineWidth = 1.dp,
        angledSegmentLength = 10.dp,
        horizontalSegmentLength = 14.dp,
        maxWidthToBoundsRatio = 0.44f,
    )
    val sliceProvider = remember(entries, selectedIndex, selectedOffset, selectedStrokeThickness, colors.surface, selectedLabel) {
        PieChart.SliceProvider.series(
            entries.mapIndexed { index, entry ->
                PieChart.Slice(
                    fill = Fill(entry.color.copy(alpha = if (selectedIndex == null || selectedIndex == index) 0.94f else 0.44f)),
                    strokeFill = Fill(
                        if (selectedIndex == index) entry.color.copy(alpha = 0.26f) else colors.surface.copy(alpha = 0.92f),
                    ),
                    strokeThickness = if (selectedIndex == index) selectedStrokeThickness else 1.dp,
                    offsetFromCenter = if (selectedIndex == index) selectedOffset else 0.dp,
                    label = if (selectedIndex == index) selectedLabel else null,
                )
            }
        )
    }
    val modelProducer = remember { PieChartModelProducer() }
    LaunchedEffect(entries) {
        modelProducer.runTransaction {
            pieSeries { series(entries.map { it.value }) }
        }
    }

    DashboardVicoTheme {
        TooltipBox(
            state = rememberTooltipState(),
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                selectedText?.let {
                    PlainTooltip {
                        Text(it)
                    }
                }
            },
        ) {
            PieChartHost(
                chart = rememberPieChart(
                    sliceProvider = sliceProvider,
                    spacing = 2.dp,
                    innerSize = PieSize.Inner.fixed(74.dp),
                    valueFormatter = remember(entries) {
                        PieValueFormatter { _, value, index ->
                            val entry = entries.getOrNull(index)
                            if (entry == null) {
                                value.toDouble().toFixed(0)
                            } else {
                                dashboardPieSelectionText(entry, entries)
                            }
                        }
                    },
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { chartSize = it }
                    .pointerInput(entries, chartSize, density) {
                        detectTapGestures { offset ->
                            selectedIndex = dashboardPieSliceIndexAt(
                                offset = offset,
                                size = chartSize,
                                values = entries.map { it.value },
                                innerRadiusPx = with(density) { 37.dp.toPx() },
                                extraOuterRadiusPx = with(density) { 16.dp.toPx() },
                            )
                        }
                    },
            )
        }
    }
    selectedText?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    StatusLegend(entries)
}

@Composable
internal fun StatusLegend(entries: List<DashboardStatusEntry>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        entries.forEach { entry ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(entry.color),
                )
                Text(
                    text = "${entry.label}: ${entry.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun dashboardPieSelectionText(
    entry: DashboardStatusEntry,
    entries: List<DashboardStatusEntry>,
): String {
    val total = entries.sumOf { it.value }.takeIf { it > 0 } ?: return "${entry.label}: ${entry.value}"
    val percent = entry.value.toDouble() / total * 100.0
    return "${entry.label}: ${entry.value} (${percent.toFixed(1)}%)"
}

internal fun dashboardPieSliceIndexAt(
    offset: Offset,
    size: IntSize,
    values: List<Int>,
    innerRadiusPx: Float,
    extraOuterRadiusPx: Float,
): Int? {
    if (size.width <= 0 || size.height <= 0) return null
    val total = values.sum().takeIf { it > 0 } ?: return null
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val distance = hypot(dx, dy)
    val outerRadius = minOf(size.width, size.height) / 2f + extraOuterRadiusPx
    if (distance !in innerRadiusPx..outerRadius) return null

    val angle = ((atan2(dy, dx) * 180f / PI.toFloat()) + 360f) % 360f
    val relativeAngle = (angle - DASHBOARD_PIE_START_ANGLE + 360f) % 360f
    var accumulated = 0f
    values.forEachIndexed { index, value ->
        val sweep = value.toFloat() / total * 360f
        if (sweep > 0f && relativeAngle in accumulated..(accumulated + sweep)) return index
        accumulated += sweep
    }
    return null
}

internal const val DASHBOARD_PIE_START_ANGLE = -90f
