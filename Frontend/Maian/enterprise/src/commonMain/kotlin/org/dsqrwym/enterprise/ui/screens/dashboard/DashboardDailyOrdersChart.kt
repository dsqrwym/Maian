package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_accepted_orders
import maian.enterprise.generated.resources.dashboard_total_orders
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardRevenueTrendItem
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun DailyOrdersChart(items: List<DashboardRevenueTrendItem>) {
    if (items.isEmpty() || items.all { it.orderCount == 0 && it.acceptedCount == 0 }) {
        DashboardNoChartData()
        return
    }
    val totalOrdersLabel = stringResource(EnterpriseRes.string.dashboard_total_orders)
    val acceptedOrdersLabel = stringResource(EnterpriseRes.string.dashboard_accepted_orders)
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(items) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = items.indices.map { it },
                    y = items.map { it.orderCount },
                )
                series(
                    x = items.indices.map { it },
                    y = items.map { it.acceptedCount },
                )
            }
        }
    }
    val markerValueFormatter = remember(items, totalOrdersLabel, acceptedOrdersLabel) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val index = targets.firstOrNull()?.x?.roundToInt() ?: return@ValueFormatter ""
            val item = items.getOrNull(index) ?: return@ValueFormatter ""
            "${item.date.toDisplayDate()}\n$totalOrdersLabel: ${item.orderCount}\n$acceptedOrdersLabel: ${item.acceptedCount}"
        }
    }
    val colors = MaterialTheme.colorScheme
    DashboardCartesianChart(
        modelProducer = modelProducer,
        chartKind = DashboardCartesianChartKind.DailyOrders,
        xValueFormatter = rememberDashboardDateFormatter(items),
        yValueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0) },
        markerValueFormatter = markerValueFormatter,
        xLabelSpacing = dashboardDateLabelSpacing(items.size),
        legend = rememberDashboardOrdersLegend(
            totalOrdersLabel = totalOrdersLabel,
            acceptedOrdersLabel = acceptedOrdersLabel,
            totalOrdersColor = colors.primary,
            acceptedOrdersColor = colors.tertiary,
        ),
    )
    DashboardDateRangeHint(items)
}

@Composable
internal fun rememberDashboardDailyOrdersLayer(): ColumnCartesianLayer {
    val colors = MaterialTheme.colorScheme
    val dataLabel = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant),
        padding = Insets(horizontal = 2.dp, vertical = 1.dp),
    )
    val totalOrdersColumn = rememberLineComponent(
        fill = Fill(
            Brush.verticalGradient(
                listOf(
                    colors.primary.copy(alpha = 0.96f),
                    colors.primary.copy(alpha = 0.54f),
                ),
            ),
        ),
        thickness = 14.dp,
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
    )
    val acceptedOrdersColumn = rememberLineComponent(
        fill = Fill(
            Brush.verticalGradient(
                listOf(
                    colors.tertiary.copy(alpha = 0.94f),
                    colors.tertiary.copy(alpha = 0.5f),
                ),
            ),
        ),
        thickness = 14.dp,
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
    )
    return rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(totalOrdersColumn, acceptedOrdersColumn),
        columnCollectionSpacing = 18.dp,
        mergeMode = { ColumnCartesianLayer.MergeMode.Grouped(columnSpacing = 4.dp) },
        dataLabel = dataLabel,
        dataLabelValueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0) },
        rangeProvider = remember { DashboardColumnRangeProvider },
    )
}

@Composable
internal fun rememberDashboardOrdersLegend(
    totalOrdersLabel: String,
    acceptedOrdersLabel: String,
    totalOrdersColor: Color,
    acceptedOrdersColor: Color,
): com.patrykandpatrick.vico.compose.common.Legend<CartesianMeasuringContext, CartesianDrawingContext> {
    val colors = MaterialTheme.colorScheme
    val labelComponent = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant),
    )
    val totalOrdersIcon = rememberShapeComponent(Fill(totalOrdersColor), RoundedCornerShape(2.dp))
    val acceptedOrdersIcon = rememberShapeComponent(Fill(acceptedOrdersColor), RoundedCornerShape(2.dp))
    return rememberHorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
        items = {
            add(LegendItem(totalOrdersIcon, labelComponent, totalOrdersLabel))
            add(LegendItem(acceptedOrdersIcon, labelComponent, acceptedOrdersLabel))
        },
        iconSize = 8.dp,
        iconLabelSpacing = 5.dp,
        columnSpacing = 12.dp,
        padding = Insets(top = 8.dp),
    )
}
