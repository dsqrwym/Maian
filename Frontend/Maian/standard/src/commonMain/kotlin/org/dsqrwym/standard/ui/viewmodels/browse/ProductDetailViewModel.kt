package org.dsqrwym.standard.ui.viewmodels.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.RetailProductDetail
import org.dsqrwym.standard.domain.browse.RetailProductDetailMedia
import org.dsqrwym.standard.domain.browse.RetailProductVariant

class ProductDetailViewModel(
    private val repository: RetailBrowseRepository,
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

    private var productId: String? = null
    private var lastLoadedLanguageCode: String? = null

    val quantity: Int?
        get() = quantityText.toIntOrNull()

    val canAddToCart: Boolean
        get() = selectedVariant?.isPurchasable == true && quantity != null

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

    fun loadProduct(id: String) {
        if (productId == id && product != null && lastLoadedLanguageCode == languageCode) return
        productId = id
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.getProductDetail(id, languageCode)) {
                is SharedResponseResult.Success -> {
                    product = result.data
                    lastLoadedLanguageCode = languageCode
                    selectDefaultVariant(result.data)
                }

                is SharedResponseResult.Error -> {
                    lastLoadedLanguageCode = null
                    product = null
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                    emitNavigation(NavigationEvent.Back)
                }
            }
            isLoading = false
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

    private fun selectDefaultVariant(product: RetailProductDetail?) {
        val variant = product?.variants?.firstOrNull { it.isPurchasable }
        selectedVariant = variant
        quantityText = variant?.minOrderQty?.toString().orEmpty()
    }

}
