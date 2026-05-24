package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardRevenueTrendItem
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.toDisplayDate
import kotlin.math.roundToInt

@Composable
internal fun RevenueTrendChart(items: List<DashboardRevenueTrendItem>) {
    if (items.isEmpty() || items.all { it.revenue.toDouble() == 0.0 }) {
        DashboardNoChartData()
        return
    }
    val revenueAmounts = items.map { it.revenue.asEuroAmount() }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(items) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = items.indices.map { it },
                    y = items.map { it.revenue.toDouble() },
                )
            }
        }
    }
    val markerValueFormatter = remember(items, revenueAmounts) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val index = targets.firstOrNull()?.x?.roundToInt() ?: return@ValueFormatter ""
            val item = items.getOrNull(index) ?: return@ValueFormatter ""
            "${item.date.toDisplayDate()}\n${revenueAmounts.getOrNull(index).orEmpty()}"
        }
    }
    DashboardCartesianChart(
        modelProducer = modelProducer,
        chartKind = DashboardCartesianChartKind.Revenue,
        xValueFormatter = rememberDashboardDateFormatter(items),
        yValueFormatter = rememberDashboardRevenueValueFormatter(),
        markerValueFormatter = markerValueFormatter,
        xLabelSpacing = dashboardDateLabelSpacing(items.size),
    )
    DashboardDateRangeHint(items)
}

@Composable
internal fun rememberDashboardRevenueLineLayer(): LineCartesianLayer {
    val colors = MaterialTheme.colorScheme
    val pointComponent = rememberShapeComponent(
        fill = Fill(colors.primary),
        shape = CircleShape,
        strokeFill = Fill(colors.surface),
        strokeThickness = 2.dp,
    )
    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(colors.primary)),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp, cap = StrokeCap.Round),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(
                        colors.primary.copy(alpha = 0.26f),
                        Color.Transparent,
                    ),
                ),
            ),
        ),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(pointComponent, size = 8.dp),
        ),
        interpolator = LineCartesianLayer.Interpolator.catmullRom(alpha = 0.2f),
    )
    return rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(line),
        pointSpacing = 34.dp,
    )
}
