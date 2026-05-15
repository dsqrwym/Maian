package org.dsqrwym.standard.ui.viewmodels.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import maian.standard.generated.resources.*
import org.dsqrwym.shared.data.profile.SharedWholesalerProfileRepository
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.standard.data.cart.StandardCartRepository
import org.dsqrwym.standard.data.order.StandardOrderRepository
import org.dsqrwym.standard.domain.cart.Cart
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartWholesaler
import org.dsqrwym.standard.domain.browse.toRetailWholesaler
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStore
import org.jetbrains.compose.resources.getString

data class StandardCartUiState(
    val loadState: UiState = UiState.Idle,
    val cart: Cart? = null,
    val isRefreshing: Boolean = false,
    val isBackgroundRefreshing: Boolean = false,
    val updatingCartDetailId: String? = null,
    val deletingCartDetailId: String? = null,
    val deletingWholesalerId: String? = null,
    val creatingOrderWholesalerId: String? = null,
    val selectingWholesalerId: String? = null,
    val activeWholesalerId: String? = null,
    val activeWholesalerName: String? = null,
) {
    val isLoading: Boolean
        get() = loadState == UiState.Loading

    val isContentRefreshing: Boolean
        get() = isRefreshing || isBackgroundRefreshing || (isLoading && cart != null)

    val isEmpty: Boolean
        get() = loadState == UiState.Success && (cart?.isEmpty ?: true)

    fun isMutating(cartDetailId: String): Boolean =
        updatingCartDetailId == cartDetailId || deletingCartDetailId == cartDetailId

    fun isDeletingWholesaler(wholesalerId: String): Boolean =
        deletingWholesalerId == wholesalerId

    val isWholesalerScoped: Boolean
        get() = activeWholesalerId != null
}

class StandardCartViewModel(
    private val repository: StandardCartRepository,
    private val orderRepository: StandardOrderRepository,
    private val wholesalerProfileRepository: SharedWholesalerProfileRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set

    var uiState by mutableStateOf(
        StandardCartUiState(
            activeWholesalerId = BrowseScopeStore.state.wholesalerId,
            activeWholesalerName = BrowseScopeStore.state.wholesalerName,
        )
    )
        private set

    private var lastLoadedLanguageCode: String? = null
    private var lastLoadedWholesalerId: String? = null

    init {
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest {
                val currentCode = LanguageManager.getCurrent().code
                if (languageCode == currentCode) return@collectLatest
                languageCode = currentCode
                if (uiState.cart != null || uiState.loadState != UiState.Idle) {
                    loadCart(forceRefresh = true)
                }
            }
        }
        viewModelScope.launch {
            snapshotFlow { BrowseScopeStore.state }.collectLatest { scopeState ->
                val activeWholesalerId = scopeState.wholesalerId
                val activeWholesalerName = scopeState.wholesalerName
                    ?: scopeState.wholesaler?.displayName
                    ?: scopeState.wholesaler?.companyName
                if (
                    uiState.activeWholesalerId == activeWholesalerId &&
                    uiState.activeWholesalerName == activeWholesalerName
                ) {
                    return@collectLatest
                }
                uiState = uiState.copy(
                    activeWholesalerId = activeWholesalerId,
                    activeWholesalerName = activeWholesalerName,
                )
                if (uiState.cart != null || uiState.loadState != UiState.Idle) {
                    loadCart(forceRefresh = true)
                }
            }
        }
        viewModelScope.launch {
            repository.updateEvents.collectLatest {
                if (uiState.cart == null && uiState.loadState == UiState.Idle) return@collectLatest
                if (uiState.isRefreshing) return@collectLatest
                loadCartInternal(
                    refreshing = false,
                    backgroundRefreshing = uiState.cart != null,
                )
            }
        }
    }

    fun loadCart(forceRefresh: Boolean = false) {
        if (!forceRefresh &&
            uiState.cart != null &&
            uiState.loadState == UiState.Success &&
            lastLoadedLanguageCode == languageCode &&
            lastLoadedWholesalerId == uiState.activeWholesalerId
        ) {
            return
        }
        if (uiState.isLoading || uiState.isRefreshing || uiState.isBackgroundRefreshing) return

        viewModelScope.launch {
            loadCartInternal(refreshing = false)
        }
    }

    fun refreshCart() {
        if (uiState.isLoading || uiState.isRefreshing || uiState.isBackgroundRefreshing) return
        viewModelScope.launch {
            loadCartInternal(refreshing = true)
        }
    }

    fun updateItemQuantity(item: CartItem, quantity: Int) {
        if (uiState.isMutating(item.cartDetailId)) return

        val boundedQuantity = quantity.coerceIn(
            item.minOrderQty,
            item.maxOrderQuantity.coerceAtLeast(item.minOrderQty),
        )
        if (boundedQuantity == item.quantity) return

        viewModelScope.launch {
            uiState = uiState.copy(updatingCartDetailId = item.cartDetailId)
            when (val result = repository.updateCartItemQuantity(item.cartDetailId, boundedQuantity)) {
                is SharedResponseResult.Success -> Unit
                is SharedResponseResult.Error -> {
                    mySnackbarViewModel.showError(
                        result.message ?: getString(SharedRes.string.update_failed),
                    )
                }
            }
            uiState = uiState.copy(updatingCartDetailId = null)
        }
    }

    fun deleteCartItem(cartDetailId: String) {
        if (uiState.isMutating(cartDetailId)) return

        viewModelScope.launch {
            uiState = uiState.copy(deletingCartDetailId = cartDetailId)
            when (val result = repository.deleteCartItem(cartDetailId)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.delete_success))
                }

                is SharedResponseResult.Error -> {
                    mySnackbarViewModel.showError(
                        result.message ?: getString(SharedRes.string.delete_failed),
                    )
                }
            }
            uiState = uiState.copy(deletingCartDetailId = null)
        }
    }

    fun deleteWholesalerCart(wholesalerId: String) {
        val normalizedWholesalerId = wholesalerId.trim()
        if (normalizedWholesalerId.isBlank() || uiState.isDeletingWholesaler(normalizedWholesalerId)) return

        viewModelScope.launch {
            uiState = uiState.copy(deletingWholesalerId = normalizedWholesalerId)
            when (val result = repository.deleteWholesalerCart(normalizedWholesalerId)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.delete_success))
                }

                is SharedResponseResult.Error -> {
                    mySnackbarViewModel.showError(
                        result.message ?: getString(SharedRes.string.delete_failed),
                    )
                }
            }
            uiState = uiState.copy(deletingWholesalerId = null)
        }
    }

    fun createOrderFromCart(group: CartGroup) {
        val wholesalerId = group.wholesaler.id.trim()
        if (wholesalerId.isBlank()) return
        if (group.status != CartGroupStatus.AVAILABLE) return
        if (uiState.isContentRefreshing || uiState.creatingOrderWholesalerId != null) return
        if (uiState.isDeletingWholesaler(wholesalerId) || uiState.selectingWholesalerId == wholesalerId) return
        if (group.items.any { uiState.isMutating(it.cartDetailId) }) return

        viewModelScope.launch {
            uiState = uiState.copy(creatingOrderWholesalerId = wholesalerId)
            when (val result = orderRepository.createOrderFromCart(wholesalerId)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(getString(StandardRes.string.create_order_success))
                    loadCartInternal(
                        refreshing = false,
                        backgroundRefreshing = uiState.cart != null,
                        showLoadError = false,
                    )
                }

                is SharedResponseResult.Error -> {
                    loadCartInternal(
                        refreshing = false,
                        backgroundRefreshing = uiState.cart != null,
                        showLoadError = false,
                    )
                    mySnackbarViewModel.showError(createOrderErrorMessage(result))
                }
            }
            uiState = uiState.copy(creatingOrderWholesalerId = null)
        }
    }

    fun enterWholesalerScope(wholesaler: CartWholesaler) {
        val wholesalerId = wholesaler.id.trim()
        if (wholesalerId.isBlank() || uiState.selectingWholesalerId == wholesalerId) return

        viewModelScope.launch {
            uiState = uiState.copy(selectingWholesalerId = wholesalerId)
            when (val result = wholesalerProfileRepository.getWholesalerProfile(wholesalerId)) {
                is SharedResponseResult.Success -> {
                    result.data?.let { profile ->
                        BrowseScopeStore.selectWholesaler(profile.toRetailWholesaler(wholesalerId))
                    } ?: mySnackbarViewModel.showError(getString(SharedRes.string.load_failed))
                }

                is SharedResponseResult.Error -> {
                    mySnackbarViewModel.showError(
                        result.message ?: getString(SharedRes.string.load_failed),
                    )
                }
            }
            uiState = uiState.copy(selectingWholesalerId = null)
        }
    }

    private suspend fun loadCartInternal(
        refreshing: Boolean,
        backgroundRefreshing: Boolean = false,
        showLoadError: Boolean = true,
    ) {
        uiState = uiState.copy(
            loadState = if ((refreshing || backgroundRefreshing) && uiState.cart != null) {
                uiState.loadState
            } else {
                UiState.Loading
            },
            isRefreshing = refreshing,
            isBackgroundRefreshing = backgroundRefreshing,
        )

        val wholesalerId = uiState.activeWholesalerId
        when (val result = repository.getMyCart(langCode = languageCode, wholesalerId = wholesalerId)) {
            is SharedResponseResult.Success -> {
                uiState = uiState.copy(
                    loadState = UiState.Success,
                    cart = result.data ?: Cart.Empty,
                    isRefreshing = false,
                    isBackgroundRefreshing = false,
                )
                lastLoadedLanguageCode = languageCode
                lastLoadedWholesalerId = wholesalerId
            }

            is SharedResponseResult.Error -> {
                val message = result.message ?: getString(SharedRes.string.load_failed)
                if (showLoadError) {
                    mySnackbarViewModel.showError(message)
                }
                uiState = uiState.copy(
                    loadState = if (uiState.cart == null) UiState.Error else uiState.loadState,
                    isRefreshing = false,
                    isBackgroundRefreshing = false,
                )
                lastLoadedLanguageCode = null
                lastLoadedWholesalerId = null
            }
        }
    }

    private suspend fun createOrderErrorMessage(result: SharedResponseResult.Error): String {
        if (SharedResponseResult.shouldShowToUser(result.type)) {
            return result.message ?: getString(StandardRes.string.create_order_failed)
        }

        return when (result.message?.trim()) {
            "CART_EMPTY" -> getString(StandardRes.string.create_order_error_cart_empty)
            "WHOLESALER_NOT_FOUND_OR_INVALID" -> getString(StandardRes.string.create_order_error_wholesaler_invalid)
            "SHIPPING_ADDRESS_NOT_FOUND" -> getString(StandardRes.string.create_order_error_shipping_address_missing)
            "PRODUCT_NOT_AVAILABLE" -> getString(StandardRes.string.create_order_error_product_unavailable)
            "VARIANT_NOT_AVAILABLE" -> getString(StandardRes.string.create_order_error_variant_unavailable)
            "QUANTITY_BELOW_MIN_ORDER" -> getString(StandardRes.string.create_order_error_quantity_below_min_order)
            "NOT_ENOUGH_STOCK" -> getString(StandardRes.string.create_order_error_not_enough_stock)
            else -> getString(StandardRes.string.create_order_failed)
        }
    }
}
