package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.edit
import maian.shared.generated.resources.product_preview
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.tooltip.PermissionTooltip
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.ui.components.product.SharedProductWaterfall
import org.dsqrwym.shared.ui.components.product.SharedReadOnlyProductCard
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductWaterfallView(
    paginatedProducts: LazyPagingItems<Product>,
    scrollBehavior: TopAppBarScrollBehavior,
    updateCurrentProduct: (Product) -> Unit,
    onPreview: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    noPermissionText: String,
    padding: PaddingValues,
    isRefreshing: Boolean,
) {
    SharedProductWaterfall(
        paginatedProducts = paginatedProducts,
        scrollBehavior = scrollBehavior,
        padding = padding,
        applyPaddingWithoutTop = true,
        includeMenuTopPadding = true,
        key =  paginatedProducts.itemKey { it.id } ,
    ) { product ->
        ProductGridItem(
            modifier = Modifier.animateItem(),
            product = product,
            isRefreshing = isRefreshing,
            onImageClick = { updateCurrentProduct(product) },
            onPreview = { onPreview(product) },
            onEdit = { onEdit(product) },
            onDelete = { onDelete(product) },
            canEdit = canEdit,
            canDelete = canDelete,
            noPermissionText = noPermissionText,
        )
    }
}

@Composable
fun ProductGridItem(
    modifier: Modifier = Modifier,
    product: Product,
    isRefreshing: Boolean,
    onImageClick: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean = true,
    canDelete: Boolean = true,
    noPermissionText: String = "",
) {
    SharedReadOnlyProductCard(
        modifier = modifier,
        name = product.name,
        title = product.title,
        code = product.code,
        imageUrl = product.mainImage?.url(product.id),
        minPrice = product.minPrice,
        minPriceIva = product.minPriceIva,
        totalStock = product.totalStock,
        minOrderQty = product.minOrderQty,
        categoryName = product.mainCategory.name,
        categoryTranslation = product.mainCategory.nameTranslation,
        nameTranslation = product.nameTranslationsText,
        titleTranslation = product.titleTranslationsText,
        statusText = product.status.displayName(),
        isLoading = isRefreshing,
        onImageClick = onImageClick,
        showActions = true,
    ) {
        IconButton(onClick = onPreview) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = stringResource(SharedRes.string.product_preview),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
        PermissionTooltip(canEdit, noPermissionText) {
            IconButton(onClick = onEdit, enabled = canEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(SharedRes.string.edit),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        PermissionTooltip(canDelete, noPermissionText) {
            BusinessOutlinedDeleteButton(
                enabled = canDelete,
                onDelete = onDelete,
                iconSize = 20.dp
            )
        }
    }
}
