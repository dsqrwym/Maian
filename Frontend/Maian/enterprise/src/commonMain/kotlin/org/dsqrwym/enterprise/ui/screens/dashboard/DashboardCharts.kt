package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_empty
import maian.enterprise.generated.resources.product_revenue_and_orders
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_status_accepted
import maian.shared.generated.resources.order_status_cancelled
import maian.shared.generated.resources.order_status_pending
import maian.shared.generated.resources.order_status_rejected
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardRevenueTrendItem
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardTopSellingProduct
import org.dsqrwym.shared.data.products.localizedProductName
import org.dsqrwym.shared.data.products.productNameTranslationText
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.dsqrwym.shared.util.formatter.toFixed
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

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
    val selectedOffset by animateDpAsState(if (selectedIndex == null) 0.dp else 12.dp)
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val selectedText = selectedIndex?.let { entries.getOrNull(it)?.let { entry -> dashboardPieSelectionText(entry, entries) } }
    val labelBackground = ShapeComponent(
        fill = Fill(colors.surface.copy(alpha = 0.88f)),
        strokeFill = Fill(colors.outlineVariant.copy(alpha = 0.55f)),
        strokeThickness = 1.dp,
    )
    val selectedLabel = com.patrykandpatrick.vico.compose.pie.PieChart.SliceLabel.Outside(
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
    val sliceProvider = remember(entries, selectedIndex, selectedOffset, colors.surface, selectedLabel) {
        com.patrykandpatrick.vico.compose.pie.PieChart.SliceProvider.series(
            entries.mapIndexed { index, entry ->
                com.patrykandpatrick.vico.compose.pie.PieChart.Slice(
                    fill = Fill(entry.color.copy(alpha = if (selectedIndex == null || selectedIndex == index) 0.94f else 0.44f)),
                    strokeFill = Fill(colors.surface.copy(alpha = 0.92f)),
                    strokeThickness = 1.dp,
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
                    .height(190.dp)
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

@Composable
internal fun RevenueTrendChart(items: List<DashboardRevenueTrendItem>) {
    if (items.isEmpty()) {
        DashboardNoChartData()
        return
    }
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
    DashboardCartesianChart(
        items = items,
        modelProducer = modelProducer,
        layer = DashboardCartesianLayer.Line,
    )
}

@Composable
internal fun DailyOrdersChart(items: List<DashboardRevenueTrendItem>) {
    if (items.isEmpty()) {
        DashboardNoChartData()
        return
    }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(items) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = items.indices.map { it },
                    y = items.map { it.orderCount },
                )
            }
        }
    }
    DashboardCartesianChart(
        items = items,
        modelProducer = modelProducer,
        layer = DashboardCartesianLayer.Column,
    )
}

@Composable
internal fun DashboardCartesianChart(
    items: List<DashboardRevenueTrendItem>,
    modelProducer: CartesianChartModelProducer,
    layer: DashboardCartesianLayer,
) {
    val marker = rememberDashboardCartesianMarker()
    val dateFormatter = rememberDashboardDateFormatter(items)
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
                when (layer) {
                    DashboardCartesianLayer.Line -> rememberLineCartesianLayer()
                    DashboardCartesianLayer.Column -> rememberColumnCartesianLayer()
                },
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = dateFormatter,
                    labelRotationDegrees = -35f,
                ),
                marker = marker,
                getXStep = { 1.0 },
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
    }
    DashboardDateRangeHint(items)
}

@Composable
internal fun rememberDashboardDateFormatter(items: List<DashboardRevenueTrendItem>): CartesianValueFormatter =
    remember(items) {
        CartesianValueFormatter { _, value, _ ->
            items.getOrNull(value.roundToInt())?.date?.toDisplayDate() ?: value.roundToInt().toString()
        }
    }

@Composable
internal fun rememberDashboardCartesianMarker(): DefaultCartesianMarker {
    val colors = MaterialTheme.colorScheme
    val label = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurface),
    )
    val guideline = rememberLineComponent(fill = Fill(colors.outlineVariant), thickness = 1.dp)
    val indicator = remember {
        { color: Color ->
            ShapeComponent(Fill(color), CircleShape)
        }
    }
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = remember { DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2) },
        labelPosition = DefaultCartesianMarker.LabelPosition.AbovePoint,
        indicator = indicator,
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
internal fun TopSellingProductsChart(products: List<DashboardTopSellingProduct>) {
    if (products.isEmpty()) {
        DashboardNoChartData()
        return
    }
    val languageCode = LanguageManager.getCurrent().code
    val maxQuantity = products.maxOfOrNull { it.soldQuantity }?.takeIf { it > 0 } ?: 1
    Column(verticalArrangement = SharedColumnLayout.arrangement) {
        TopProductsVicoChart(products, languageCode)
        products.forEach { product ->
            TopSellingProductBar(
                product = product,
                languageCode = languageCode,
                maxQuantity = maxQuantity,
            )
        }
    }
}

@Composable
internal fun TopProductsVicoChart(
    products: List<DashboardTopSellingProduct>,
    languageCode: String,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(products) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = products.indices.map { it },
                    y = products.map { it.soldQuantity },
                )
            }
        }
    }
    val productNameFormatter = remember(products, languageCode) {
        CartesianValueFormatter { _, value, _ ->
            val product = products.getOrNull(value.roundToInt())
            product?.productTranslation?.localizedProductName(languageCode, product.productName).orEmpty()
                .ifBlank { value.roundToInt().toString() }
        }
    }
    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.Start,
        autoScroll = Scroll.Absolute.Start,
        autoScrollCondition = AutoScrollCondition.Never,
    )
    val zoomState = rememberVicoZoomState(
        initialZoom = remember { Zoom.max(Zoom.x(6.0), Zoom.Content) },
        minZoom = Zoom.Content,
        maxZoom = remember { Zoom.max(Zoom.fixed(12f), Zoom.Content) },
    )
    DashboardVicoTheme {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = productNameFormatter,
                    labelRotationDegrees = -35f,
                ),
                marker = rememberDashboardCartesianMarker(),
                getXStep = { 1.0 },
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth().height(150.dp),
        )
    }
}

@Composable
internal fun TopSellingProductBar(
    product: DashboardTopSellingProduct,
    languageCode: String,
    maxQuantity: Int,
) {
    val ratio by animateFloatAsState(
        targetValue = (product.soldQuantity.toFloat() / maxQuantity).coerceIn(0f, 1f),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                ProductNameWithTranslations(
                    product = product,
                    languageCode = languageCode,
                )
            }
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = product.soldQuantity.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        ProductSalesHint(product)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductNameWithTranslations(
    product: DashboardTopSellingProduct,
    languageCode: String,
) {
    val name = product.productTranslation.localizedProductName(languageCode, product.productName)
    val translationText = product.productTranslation.productNameTranslationText()
    val content: @Composable () -> Unit = {
        SelectionContainer {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    if (translationText.isNullOrBlank()) {
        content()
    } else {
        TooltipBox(
            state = rememberTooltipState(),
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip {
                    SelectionContainer {
                        Text(translationText)
                    }
                }
            },
        ) {
            content()
        }
    }
}

@Composable
internal fun ProductSalesHint(product: DashboardTopSellingProduct) {
    Text(
        text = stringResource(
            EnterpriseRes.string.product_revenue_and_orders,
            product.revenue.asEuroAmount(),
            product.orderCount,
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun DashboardVicoTheme(content: @Composable () -> Unit) {
    ProvideVicoTheme(theme = rememberM3VicoTheme(), content = content)
}

internal enum class DashboardCartesianLayer {
    Line,
    Column,
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

private const val DASHBOARD_PIE_START_ANGLE = -90f
