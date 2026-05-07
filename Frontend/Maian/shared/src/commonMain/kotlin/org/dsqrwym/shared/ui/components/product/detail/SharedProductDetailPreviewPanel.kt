package org.dsqrwym.shared.ui.components.product.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import org.dsqrwym.shared.domain.product.SharedProductDetailMedia
import org.dsqrwym.shared.domain.product.SharedProductDetailUiModel

@Composable
fun SharedProductDetailPreviewPanel(
    product: SharedProductDetailUiModel,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    var previewMedia by remember { mutableStateOf<SharedProductDetailMedia?>(null) }
    var selectedVariant by remember(product) {
        mutableStateOf(product.variants.sortedBy { it.sort }.firstOrNull { it.isPurchasable })
    }
    var quantityText by remember(selectedVariant) {
        mutableStateOf(selectedVariant?.minOrderQty?.toString().orEmpty())
    }
    val canAddToCart = selectedVariant?.isPurchasable == true && quantityText.toIntOrNull() != null
    val emptyNestedScrollConnection = remember { object : NestedScrollConnection {} }

    previewMedia?.let { media ->
        SharedProductMediaPreviewDialog(
            media = media,
            productName = product.name,
            onDismissRequest = { previewMedia = null },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            SharedProductBottomCartBar(
                selectedVariant = selectedVariant,
                quantityText = quantityText,
                canAddToCart = canAddToCart,
                onQuantityChange = { value ->
                    selectedVariant?.let { variant ->
                        quantityText = value.filter { it.isDigit() }
                            .toIntOrNull()
                            ?.coerceIn(variant.minOrderQty, variant.maxPurchasableUnits)
                            ?.toString()
                            .orEmpty()
                    }
                },
                onQuantityStep = { delta ->
                    selectedVariant?.let { variant ->
                        val current = quantityText.toIntOrNull() ?: variant.minOrderQty
                        quantityText = (current + delta)
                            .coerceIn(variant.minOrderQty, variant.maxPurchasableUnits)
                            .toString()
                    }
                },
            )
        },
    ) { padding: PaddingValues ->
        SharedProductDetailContent(
            product = product,
            isLoading = isLoading,
            selectedVariant = selectedVariant,
            padding = padding,
            nestedScrollConnection = emptyNestedScrollConnection,
            onVariantSelect = { selectedVariant = it },
            onMediaClick = { previewMedia = it },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
