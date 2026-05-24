package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.add_cart_error_cart_line_limit_exceeded
import maian.standard.generated.resources.add_cart_error_cart_wholesaler_limit_exceeded
import maian.standard.generated.resources.cart_item_added
import maian.standard.generated.resources.product_information_changed_retry
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.data.cart.StandardCartRepository
import org.dsqrwym.standard.domain.browse.RetailProductDetail
import org.dsqrwym.standard.domain.browse.RetailProductDetailMedia
import org.dsqrwym.standard.domain.browse.RetailProductVariant
import org.jetbrains.compose.resources.getString

class ProductDetailViewModel(
    private val repository: RetailBrowseRepository,
    private val cartRepository: StandardCartRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel(), SharedNavigable by SharedNavigableDelegate() {
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set
    var product by mutableStateOf<RetailProductDetail?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var selectedVariant by mutableStateOf<RetailProductVariant?>(null)
        private set
    var quantityText by mutableStateOf("")
        private set
    var previewMedia by mutableStateOf<RetailProductDetailMedia?>(null)
        private set
    var addToCartUiState by mutableStateOf(UiState.Idle)
        private set

    private var productId: String? = null
    private var lastLoadedLanguageCode: String? = null

    val quantity: Int?
        get() = quantityText.toIntOrNull()

    val canAddToCart: Boolean
        get() {
            val variant = selectedVariant ?: return false
            val currentQuantity = quantity ?: return false
            return addToCartUiState == UiState.Idle &&
                    variant.isPurchasable &&
                    currentQuantity in variant.minOrderQty..variant.maxPurchasableUnits
        }

    init {
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest {
                val currentCode = LanguageManager.getCurrent().code
                if (languageCode == currentCode) return@collectLatest
                languageCode = currentCode
                productId?.let { loadProduct(it) }
            }
        }
    }

    fun loadProduct(id: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && productId == id && product != null && lastLoadedLanguageCode == languageCode) return
        val preserveSelection = productId == id && product != null
        productId = id
        viewModelScope.launch {
            loadProductDetail(id, preserveSelection)
        }
    }

    fun selectVariant(variant: RetailProductVariant) {
        if (!variant.isPurchasable) return

        selectedVariant = variant
        quantityText = variant.minOrderQty
            .coerceAtMost(variant.maxPurchasableUnits)
            .toString()
    }

    fun updateQuantity(value: String) {
        val variant = selectedVariant ?: return

        val raw = value.filter { it.isDigit() }
        val parsed = raw.toIntOrNull() ?: variant.minOrderQty

        val coerced = parsed.coerceIn(
            variant.minOrderQty,
            variant.maxPurchasableUnits,
        )

        quantityText = coerced.toString()
    }

    fun updateStepQuantity(delta: Int) {
        val variant = selectedVariant ?: return
        val current = quantity ?: variant.minOrderQty

        val next = (current + delta).coerceIn(
            variant.minOrderQty,
            variant.maxPurchasableUnits,
        )

        quantityText = next.toString()
    }

    fun showMediaPreview(media: RetailProductDetailMedia) {
        previewMedia = media
    }

    fun dismissMediaPreview() {
        previewMedia = null
    }

    fun onAddToCartClick() {
        if (addToCartUiState == UiState.Loading) return

        val variant = selectedVariant ?: return
        val currentQuantity = quantity ?: return
        if (!variant.isPurchasable) return
        if (currentQuantity !in variant.minOrderQty..variant.maxPurchasableUnits) {
            quantityText = currentQuantity
                .coerceIn(variant.minOrderQty, variant.maxPurchasableUnits)
                .toString()
            return
        }

        viewModelScope.launch {
            addToCartUiState = UiState.Loading
            when (val result = cartRepository.addCartItem(variant.id, currentQuantity)) {
                is SharedResponseResult.Success -> {
                    addToCartUiState = UiState.Success
                    mySnackbarViewModel.showSuccess(getString(StandardRes.string.cart_item_added))
                    productId?.let { loadProductDetail(it) }
                }

                is SharedResponseResult.Error -> {
                    addToCartUiState = UiState.Error
                    handleAddToCartError(result)
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            addToCartUiState = UiState.Idle
        }
    }

    private suspend fun loadProductDetail(
        id: String,
        preserveSelection: Boolean = true,
    ) {
        val previousVariantId = if (preserveSelection) selectedVariant?.id else null
        val previousQuantity = if (preserveSelection) quantity else null

        isLoading = true
        when (val result = repository.getProductDetail(id, languageCode)) {
            is SharedResponseResult.Success -> {
                product = result.data
                lastLoadedLanguageCode = languageCode
                updateSelectedVariantAfterReload(result.data, previousVariantId, previousQuantity)
            }

            is SharedResponseResult.Error -> {
                lastLoadedLanguageCode = null
                product = null
                selectedVariant = null
                quantityText = ""
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
                emitNavigation(NavigationEvent.Back)
            }
        }
        isLoading = false
    }

    private suspend fun handleAddToCartError(result: SharedResponseResult.Error) {
        addCartLimitErrorMessage(result)?.let { message ->
            mySnackbarViewModel.showError(message)
            return
        }

        if (result.isProductDataChangedError()) {
            mySnackbarViewModel.showError(getString(StandardRes.string.product_information_changed_retry))
            productId?.let { loadProductDetail(it) }
            return
        }

        if (SharedResponseResult.shouldShowToUser(result.type)) {
            result.message?.let { mySnackbarViewModel.showError(it) }
        }
    }

    private fun SharedResponseResult.Error.isProductDataChangedError(): Boolean =
        message in cartProductDataChangedMessages

    private suspend fun addCartLimitErrorMessage(result: SharedResponseResult.Error): String? =
        when (result.message?.trim()) {
            "CART_WHOLESALER_LIMIT_EXCEEDED" ->
                getString(StandardRes.string.add_cart_error_cart_wholesaler_limit_exceeded)
            "CART_LINE_LIMIT_EXCEEDED" ->
                getString(StandardRes.string.add_cart_error_cart_line_limit_exceeded)
            else -> null
        }

    private fun updateSelectedVariantAfterReload(
        product: RetailProductDetail?,
        preferredVariantId: String?,
        preferredQuantity: Int?,
    ) {
        val variant = if (preferredVariantId == null) {
            product?.variants?.firstOrNull { it.isPurchasable }
        } else {
            product?.variants?.firstOrNull { it.id == preferredVariantId && it.isPurchasable }
        }

        selectedVariant = variant
        quantityText = variant
            ?.let {
                (preferredQuantity ?: it.minOrderQty)
                    .coerceIn(it.minOrderQty, it.maxPurchasableUnits)
                    .toString()
            }
            .orEmpty()
    }

    private companion object {
        val cartProductDataChangedMessages = setOf(
            "VARIANT_NOT_FOUND_OR_INVALID",
            "QUANTITY_BELOW_MIN_ORDER",
            "NOT_ENOUGH_STOCK",
        )
    }

}
