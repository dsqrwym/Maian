package org.dsqrwym.enterprise.ui.components.dashbord

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Legend
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_empty
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.amount_euro_value
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardRevenueTrendItem
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.dsqrwym.shared.util.formatter.toFixed
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun DashboardCartesianChart(
    modelProducer: CartesianChartModelProducer,
    chartKind: DashboardCartesianChartKind,
    xValueFormatter: CartesianValueFormatter,
    yValueFormatter: CartesianValueFormatter,
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter,
    xLabelSpacing: Int,
    legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
) {
    val marker = rememberDashboardCartesianMarker(markerValueFormatter)
    val colors = MaterialTheme.colorScheme
    val axisLabel = rememberAxisLabelComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant),
        lineCount = 1,
    )
    val axisLine = rememberAxisLineComponent(fill = Fill(colors.outlineVariant.copy(alpha = 0.5f)))
    val axisTick = rememberAxisTickComponent(fill = Fill(colors.outlineVariant.copy(alpha = 0.54f)))
    val axisGuideline = rememberAxisGuidelineComponent(fill = Fill(colors.outlineVariant.copy(alpha = 0.22f)))
    val markerController = CartesianMarkerController.rememberToggleOnTap()
    val fadingEdges = rememberFadingEdges(width = 20.dp)
    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
    )
    val zoomState = rememberVicoZoomState(
        initialZoom = remember { Zoom.max(Zoom.x(8.0), Zoom.Content) },
        minZoom = Zoom.Content,
        maxZoom = remember { Zoom.max(Zoom.fixed(12f), Zoom.Content) },
    )
    DashboardVicoTheme {
        CartesianChartHost(
            chart = rememberCartesianChart(
                when (chartKind) {
                    DashboardCartesianChartKind.Revenue -> rememberDashboardRevenueLineLayer()
                    DashboardCartesianChartKind.DailyOrders -> rememberDashboardDailyOrdersLayer()
                    DashboardCartesianChartKind.TopProducts -> rememberDashboardTopProductsLayer()
                },
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabel,
                    line = axisLine,
                    tick = axisTick,
                    guideline = axisGuideline,
                    valueFormatter = yValueFormatter,
                    itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 5 }) },
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabel,
                    line = axisLine,
                    tick = axisTick,
                    guideline = axisGuideline,
                    valueFormatter = xValueFormatter,
                    labelRotationDegrees = -35f,
                    itemPlacer = remember(xLabelSpacing) {
                        HorizontalAxis.ItemPlacer.aligned(spacing = { xLabelSpacing })
                    },
                ),
                marker = marker,
                legend = legend,
                fadingEdges = fadingEdges,
                getXStep = { 1.0 },
                markerController = markerController,
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            zoomState = zoomState,
        )
    }
}

@Composable
internal fun rememberDashboardDateFormatter(items: List<DashboardRevenueTrendItem>): CartesianValueFormatter =
    remember(items) {
        CartesianValueFormatter { _, value, _ ->
            items.getOrNull(value.roundToInt())?.date?.toDisplayDate() ?: value.roundToInt().toString()
        }
    }

@Composable
internal fun rememberDashboardRevenueValueFormatter(): CartesianValueFormatter {
    val amountTemplate = stringResource(SharedRes.string.amount_euro_value, DASHBOARD_AMOUNT_PLACEHOLDER)
    return remember(amountTemplate) {
        CartesianValueFormatter { _, value, _ ->
            amountTemplate.replace(DASHBOARD_AMOUNT_PLACEHOLDER, value.toFixed(0))
        }
    }
}

@Composable
internal fun rememberDashboardCartesianMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter,
): DefaultCartesianMarker {
    val colors = MaterialTheme.colorScheme
    val labelBackground = rememberShapeComponent(
        fill = Fill(colors.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(6.dp),
        strokeFill = Fill(colors.outlineVariant.copy(alpha = 0.62f)),
        strokeThickness = 1.dp,
    )
    val label = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface),
        lineCount = 3,
        padding = Insets(horizontal = 8.dp, vertical = 4.dp),
        background = labelBackground,
    )
    val guideline = rememberLineComponent(fill = Fill(colors.outlineVariant), thickness = 1.dp)
    val indicator = remember(colors.surface) {
        { color: Color ->
            ShapeComponent(
                fill = Fill(color),
                shape = CircleShape,
                strokeFill = Fill(colors.surface),
                strokeThickness = 1.dp,
            )
        }
    }
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint,
        indicator = indicator,
        indicatorSize = 9.dp,
        guideline = guideline,
    )
}

@Composable
internal fun DashboardDateRangeHint(items: List<DashboardRevenueTrendItem>) {
    Text(
        text = "${items.firstOrNull()?.date?.toDisplayDate().orEmpty()} - ${items.lastOrNull()?.date?.toDisplayDate().orEmpty()}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun DashboardNoChartData() {
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        SharedNotFoundPlaceholder(stringResource(EnterpriseRes.string.dashboard_empty))
    }
}

@Composable
internal fun DashboardVicoTheme(content: @Composable () -> Unit) {
    ProvideVicoTheme(theme = rememberM3VicoTheme(), content = content)
}

internal enum class DashboardCartesianChartKind {
    Revenue,
    DailyOrders,
    TopProducts,
}

internal object DashboardColumnRangeProvider : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        if (minY >= 0.0) 0.0 else minY

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
        val baseMaxY = when {
            minY == 0.0 && maxY == 0.0 -> 1.0
            maxY <= 0.0 -> 0.0
            else -> maxY
        }
        return baseMaxY + max(baseMaxY * DASHBOARD_COLUMN_TOP_SPACE_RATIO, 2.0)
    }
}

internal fun dashboardDateLabelSpacing(size: Int): Int =
    when {
        size <= 7 -> 1
        size <= 14 -> 2
        else -> (size / 7).coerceAtLeast(3)
    }

internal const val DASHBOARD_AMOUNT_PLACEHOLDER = "__DASHBOARD_AMOUNT__"
internal const val DASHBOARD_COLUMN_TOP_SPACE_RATIO = 0.28
