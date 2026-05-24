package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.product_revenue_and_orders
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardTopSellingProduct
import org.dsqrwym.shared.data.products.localizedProductName
import org.dsqrwym.shared.data.products.productNameTranslationText
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

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
    if (products.all { it.soldQuantity == 0 }) {
        DashboardNoChartData()
        return
    }
    val productNames = products.map { product ->
        product.productTranslation.localizedProductName(languageCode, product.productName)
    }
    val productSalesHints = products.map { product ->
        stringResource(
            EnterpriseRes.string.product_revenue_and_orders,
            product.revenue.asEuroAmount(),
            product.orderCount,
        )
    }
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
    val productNameFormatter = remember(productNames) {
        CartesianValueFormatter { _, value, _ ->
            productNames.getOrNull(value.roundToInt()).orEmpty().ifBlank { value.roundToInt().toString() }
        }
    }
    val markerValueFormatter = remember(products, productNames, productSalesHints) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val index = targets.firstOrNull()?.x?.roundToInt() ?: return@ValueFormatter ""
            val product = products.getOrNull(index) ?: return@ValueFormatter ""
            "${productNames.getOrNull(index).orEmpty()}: ${product.soldQuantity}\n${productSalesHints.getOrNull(index).orEmpty()}"
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
                rememberDashboardTopProductsLayer(),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0) },
                    itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 4 }) },
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = productNameFormatter,
                    labelRotationDegrees = -35f,
                ),
                marker = rememberDashboardCartesianMarker(markerValueFormatter),
                fadingEdges = rememberFadingEdges(width = 20.dp),
                getXStep = { 1.0 },
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            zoomState = zoomState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun rememberDashboardTopProductsLayer(): ColumnCartesianLayer {
    val colors = MaterialTheme.colorScheme
    val dataLabel = rememberTextComponent(
        style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant),
        padding = Insets(horizontal = 2.dp, vertical = 1.dp),
    )
    val column = rememberLineComponent(
        fill = Fill(
            Brush.verticalGradient(
                listOf(
                    colors.secondary.copy(alpha = 0.96f),
                    colors.primary.copy(alpha = 0.58f),
                ),
            ),
        ),
        thickness = 22.dp,
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp),
    )
    return rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(column),
        columnCollectionSpacing = 20.dp,
        dataLabel = dataLabel,
        dataLabelValueFormatter = remember { CartesianValueFormatter.decimal(decimalCount = 0) },
        rangeProvider = remember { DashboardColumnRangeProvider },
    )
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
