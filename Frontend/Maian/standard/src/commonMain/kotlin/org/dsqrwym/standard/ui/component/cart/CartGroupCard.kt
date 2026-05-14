package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartWholesaler

@Composable
internal fun CartGroupCard(
    group: CartGroup,
    updatingCartDetailId: String?,
    deletingCartDetailId: String?,
    deletingWholesalerId: String?,
    selectingWholesalerId: String?,
    isLoading: Boolean,
    isWholesalerScoped: Boolean,
    onWholesalerImageClick: (CartWholesaler) -> Unit,
    onProductImageClick: (CartItem) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    onClearWholesalerCart: (CartGroup) -> Unit,
    onCreateOrder: (CartGroup) -> Unit,
    onProductDetailClick: (String) -> Unit,
    onWholesalerScopeClick: (CartWholesaler) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClearingWholesaler = deletingWholesalerId == group.wholesaler.id
    val isSelectingScope = selectingWholesalerId == group.wholesaler.id
    val isGroupItemMutating = group.items.any {
        updatingCartDetailId == it.cartDetailId || deletingCartDetailId == it.cartDetailId
    }
    val isGroupMutating = isClearingWholesaler || isSelectingScope || isGroupItemMutating
    val borderColor = cartGroupBorderColor(group.status)

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = if (group.status == CartGroupStatus.AVAILABLE) 0.5.dp else 1.dp,
            color = borderColor.copy(alpha = 0.70f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CartWholesalerHeader(
                group = group,
                onImageClick = { onWholesalerImageClick(group.wholesaler) },
                showStoreButton = !isWholesalerScoped,
                isSelectingScope = isSelectingScope,
                isLoading = isLoading,
                onStoreClick = { onWholesalerScopeClick(group.wholesaler) },
            )

            if (group.status != CartGroupStatus.AVAILABLE) {
                GroupWarningBanner(group = group, isLoading = isLoading)
            }

            group.items.forEach { item ->
                CartItemRow(
                    item = item,
                    isUpdating = updatingCartDetailId == item.cartDetailId,
                    isDeleting = deletingCartDetailId == item.cartDetailId,
                    isLoading = isLoading,
                    onImageClick = { onProductImageClick(item) },
                    onProductDetailClick = { onProductDetailClick(item.productId) },
                    onQuantityChange = { quantity -> onQuantityChange(item, quantity) },
                    onDelete = { onDeleteItem(item) },
                )
            }

            CartGroupTotals(group = group, isLoading = isLoading)

            CartGroupActionRow(
                modifier = Modifier.align(Alignment.End),
                group = group,
                isClearingWholesaler = isClearingWholesaler,
                isGroupMutating = isGroupMutating,
                onClearWholesalerCart = onClearWholesalerCart,
                onCreateOrder = onCreateOrder,
            )
        }
    }
}

@Composable
internal fun CartSingleWholesalerHeaderCard(
    group: CartGroup,
    selectingWholesalerId: String?,
    isLoading: Boolean,
    isWholesalerScoped: Boolean,
    onWholesalerImageClick: (CartWholesaler) -> Unit,
    onWholesalerScopeClick: (CartWholesaler) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = if (group.status == CartGroupStatus.AVAILABLE) 0.5.dp else 1.dp,
            color = cartGroupBorderColor(group.status).copy(alpha = 0.70f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CartWholesalerHeader(
                group = group,
                onImageClick = { onWholesalerImageClick(group.wholesaler) },
                showStoreButton = !isWholesalerScoped,
                isSelectingScope = selectingWholesalerId == group.wholesaler.id,
                isLoading = isLoading,
                onStoreClick = { onWholesalerScopeClick(group.wholesaler) },
            )

            if (group.status != CartGroupStatus.AVAILABLE) {
                GroupWarningBanner(group = group, isLoading = isLoading)
            }
        }
    }
}

@Composable
internal fun CartSingleWholesalerItemCard(
    item: CartItem,
    updatingCartDetailId: String?,
    deletingCartDetailId: String?,
    isLoading: Boolean,
    onProductImageClick: (CartItem) -> Unit,
    onProductDetailClick: (String) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    CartItemRow(
        modifier = modifier,
        item = item,
        isUpdating = updatingCartDetailId == item.cartDetailId,
        isDeleting = deletingCartDetailId == item.cartDetailId,
        isLoading = isLoading,
        onImageClick = { onProductImageClick(item) },
        onProductDetailClick = { onProductDetailClick(item.productId) },
        onQuantityChange = { quantity -> onQuantityChange(item, quantity) },
        onDelete = { onDeleteItem(item) },
    )
}

@Composable
internal fun CartSingleWholesalerFooterCard(
    group: CartGroup,
    updatingCartDetailId: String?,
    deletingCartDetailId: String?,
    deletingWholesalerId: String?,
    selectingWholesalerId: String?,
    isLoading: Boolean,
    onClearWholesalerCart: (CartGroup) -> Unit,
    onCreateOrder: (CartGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClearingWholesaler = deletingWholesalerId == group.wholesaler.id
    val isSelectingScope = selectingWholesalerId == group.wholesaler.id
    val isGroupItemMutating = group.items.any {
        updatingCartDetailId == it.cartDetailId || deletingCartDetailId == it.cartDetailId
    }
    val isGroupMutating = isClearingWholesaler || isSelectingScope || isGroupItemMutating

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, cartGroupBorderColor(group.status).copy(alpha = 0.70f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CartGroupTotals(group = group, isLoading = isLoading)
            CartGroupActionRow(
                modifier = Modifier.align(Alignment.End),
                group = group,
                isClearingWholesaler = isClearingWholesaler,
                isGroupMutating = isGroupMutating,
                onClearWholesalerCart = onClearWholesalerCart,
                onCreateOrder = onCreateOrder,
            )
        }
    }
}
