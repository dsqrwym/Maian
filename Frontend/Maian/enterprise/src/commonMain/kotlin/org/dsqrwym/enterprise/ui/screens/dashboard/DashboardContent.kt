package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_accepted_orders
import maian.enterprise.generated.resources.dashboard_average_order_value
import maian.enterprise.generated.resources.dashboard_daily_orders
import maian.enterprise.generated.resources.dashboard_order_status
import maian.enterprise.generated.resources.dashboard_pending_orders
import maian.enterprise.generated.resources.dashboard_revenue_trend
import maian.enterprise.generated.resources.dashboard_top_selling_products
import maian.enterprise.generated.resources.dashboard_total_orders
import maian.enterprise.generated.resources.dashboard_total_revenue
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.enterprise.ui.components.dashbord.DailyOrdersChart
import org.dsqrwym.enterprise.ui.components.dashbord.OrderStatusChart
import org.dsqrwym.enterprise.ui.components.dashbord.RevenueTrendChart
import org.dsqrwym.enterprise.ui.components.dashbord.TopSellingProductsChart
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardContent(
    data: DashboardResponse,
    padding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    isRefreshing: Boolean,
) {
    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(horizontal = SharedLazyGridLayout.Padding),
        columns = StaggeredGridCells.Adaptive(minSize = 460.dp),
        horizontalArrangement = Arrangement.spacedBy(26.dp),
        verticalItemSpacing = 26.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Spacer(Modifier.height(padding.calculateTopPadding()))
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            DashboardSummaryCards(Modifier.animateItem(), data, isRefreshing)
        }
        item {
            DashboardChartCard(
                modifier = Modifier.animateItem(),
                title = stringResource(EnterpriseRes.string.dashboard_order_status),
                isRefreshing = isRefreshing,
            ) {
                OrderStatusChart(data)
            }
        }
        item {
            DashboardChartCard(
                modifier = Modifier.animateItem(),
                title = stringResource(EnterpriseRes.string.dashboard_revenue_trend),
                isRefreshing = isRefreshing,
            ) {
                RevenueTrendChart(data.revenueTrend)
            }
        }
        item {
            DashboardChartCard(
                modifier = Modifier.animateItem(),
                title = stringResource(EnterpriseRes.string.dashboard_daily_orders),
                isRefreshing = isRefreshing,
            ) {
                DailyOrdersChart(data.revenueTrend)
            }
        }
        item {
            DashboardChartCard(
                modifier = Modifier.animateItem(),
                title = stringResource(EnterpriseRes.string.dashboard_top_selling_products),
                isRefreshing = isRefreshing,
            ) {
                TopSellingProductsChart(data.topSellingProducts)
            }
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
internal fun DashboardSummaryCards(
    modifier: Modifier = Modifier,
    data: DashboardResponse,
    isRefreshing: Boolean
) {
    val items = listOf(
        stringResource(EnterpriseRes.string.dashboard_total_orders) to data.summary.totalOrders.toString(),
        stringResource(EnterpriseRes.string.dashboard_pending_orders) to data.summary.pendingOrders.toString(),
        stringResource(EnterpriseRes.string.dashboard_accepted_orders) to data.summary.acceptedOrders.toString(),
        stringResource(EnterpriseRes.string.dashboard_total_revenue) to data.summary.totalRevenue.asEuroAmount(),
        stringResource(EnterpriseRes.string.dashboard_average_order_value) to data.summary.averageOrderValue.asEuroAmount(),
    )
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = SharedRowLayout.arrangement,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        items.forEach { (label, value) ->
            DashboardSummaryCard(label = label, value = value, isRefreshing = isRefreshing)
        }
    }
}

@Composable
internal fun DashboardSummaryCard(
    label: String,
    value: String,
    isRefreshing: Boolean,
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(SharedColumnLayout.padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.placeholderWithShimmer(isRefreshing),
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun DashboardChartCard(
    title: String,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    content: @Composable () -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SharedColumnLayout.padding),
            verticalArrangement = SharedColumnLayout.arrangement,
        ) {
            Text(
                modifier = Modifier.placeholderWithShimmer(isRefreshing),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}
