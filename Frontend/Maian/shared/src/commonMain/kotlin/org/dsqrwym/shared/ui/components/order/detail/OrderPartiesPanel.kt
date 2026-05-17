package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.dto.SharedOrderPartnerSnapshot
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderPartiesPanel(
    order: SharedOrderDetail,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        stringResource(SharedRes.string.retailer_snapshot) to order.retailerSnapshot,
        stringResource(SharedRes.string.wholesaler_snapshot) to order.wholesalerSnapshot,
    )
    val widthSizeClass = calculateWindowSizeClass().widthSizeClass
    val columnCount = if (widthSizeClass == WindowWidthSizeClass.Compact) 1 else 2

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columnCount),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(cards) { (title, snapshot) ->
            OrderPartnerSnapshotCard(
                title = title,
                snapshot = snapshot,
                isLoading = isLoading,
            )
        }
    }
}

@Composable
fun OrderPartnerSnapshotCard(
    title: String,
    snapshot: SharedOrderPartnerSnapshot?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    OrderDetailCard(
        title = title,
        icon = Icons.Outlined.Business,
        modifier = modifier,
    ) {
        OrderDetailFieldGrid(
            fields = listOfNotNull(
                orderDetailFieldOrNull(stringResource(SharedRes.string.company_name), snapshot?.companyName),
                orderDetailFieldOrNull(stringResource(SharedRes.string.display_name), snapshot?.displayName),
                orderDetailFieldOrNull(stringResource(SharedRes.string.company_type), snapshot?.companyType),
                orderDetailFieldOrNull(stringResource(SharedRes.string.contact_name), snapshot?.contactName),
                orderDetailFieldOrNull(stringResource(SharedRes.string.tax_id), snapshot?.taxId),
                orderDetailFieldOrNull(stringResource(SharedRes.string.email), snapshot?.email),
                orderDetailFieldOrNull(stringResource(SharedRes.string.telephone), snapshot?.telephone),
                orderDetailFieldOrNull(stringResource(SharedRes.string.partner_id), snapshot?.userId),
            ),
            isLoading = isLoading,
        )
    }
}
