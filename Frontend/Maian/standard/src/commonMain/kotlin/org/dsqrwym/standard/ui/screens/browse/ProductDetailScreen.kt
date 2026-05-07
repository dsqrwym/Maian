package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.product_detail
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.product.detail.SharedProductBottomCartBar
import org.dsqrwym.shared.ui.components.product.detail.SharedProductDetailContent
import org.dsqrwym.shared.ui.components.product.detail.SharedProductMediaPreviewDialog
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.standard.domain.browse.mapper.toSharedDetailUi
import org.dsqrwym.standard.domain.browse.mapper.toSharedMedia
import org.dsqrwym.standard.domain.browse.mapper.toSharedVariant
import org.dsqrwym.standard.ui.viewmodels.browse.ProductDetailViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProductDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val isLoading = viewModel.isLoading
    val product = viewModel.product
    val languageCode = viewModel.languageCode
    val previewMedia = viewModel.previewMedia
    val selectedVariant = viewModel.selectedVariant
    val quantityText = viewModel.quantityText
    val addToCartEnabled = viewModel.canAddToCart
    val productUi = product?.toSharedDetailUi(languageCode)

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = previewMedia != null,
        overlayContent = {
            previewMedia?.let { media ->
                SharedProductMediaPreviewDialog(
                    media = media.toSharedMedia(productId),
                    productName = product?.localizedName(languageCode),
                    onDismissRequest = viewModel::dismissMediaPreview,
                )
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null)
                Text(
                    text = stringResource(SharedRes.string.product_detail),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        bottomBar = {
            SharedProductBottomCartBar(
                selectedVariant = selectedVariant?.toSharedVariant(),
                quantityText = quantityText,
                canAddToCart = addToCartEnabled,
                onQuantityChange = viewModel::updateQuantity,
                onQuantityStep = viewModel::updateStepQuantity,
            )
        }
    ) { padding, scrollBehavior ->
        if (product == null && !isLoading) {
            SharedPlainNotFoundPlaceholder()
            return@SharedTransparentScaffold
        }

        SharedProductDetailContent(
            product = productUi,
            isLoading = isLoading,
            selectedVariant = selectedVariant?.toSharedVariant(),
            padding = padding,
            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
            onVariantSelect = { variant ->
                product?.variants
                    ?.firstOrNull { it.id == variant.id }
                    ?.let(viewModel::selectVariant)
            },
            onMediaClick = { media ->
                product?.media
                    ?.firstOrNull { it.sort == media.sort && it.mimeType == media.mimeType }
                    ?.let(viewModel::showMediaPreview)
            },
        )
    }
}
