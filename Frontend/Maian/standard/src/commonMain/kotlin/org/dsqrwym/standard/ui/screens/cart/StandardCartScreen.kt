package org.dsqrwym.standard.ui.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_empty
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTopAndButton
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartWholesaler
import org.dsqrwym.standard.ui.component.ExitWholesalerModeButton
import org.dsqrwym.standard.ui.component.cart.*
import org.dsqrwym.standard.ui.viewmodels.cart.StandardCartUiState
import org.dsqrwym.standard.ui.viewmodels.cart.StandardCartViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class CartImagePreview(
    val url: String,
    val title: String?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardCartScreen(
    onNavigateBack: (() -> Unit)? = null,
    onProductDetailClick: (String) -> Unit = {},
    onExitWholesalerScope: () -> Unit = {},
    viewModel: StandardCartViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    val state = viewModel.uiState
    var imagePreview by remember { mutableStateOf<CartImagePreview?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<CartItem?>(null) }
    var pendingClearGroup by remember { mutableStateOf<CartGroup?>(null) }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = imagePreview != null || pendingDeleteItem != null || pendingClearGroup != null,
        overlayContent = {
            imagePreview?.let { preview ->
                SharedImageViewDialog(
                    model = preview.url,
                    imageName = preview.title,
                    onDismissRequest = { imagePreview = null },
                )
            }
            pendingDeleteItem?.let { item ->
                SharedConfirmDeleteDialog(
                    onDismissRequest = { pendingDeleteItem = null },
                    onConfirm = { viewModel.deleteCartItem(item.cartDetailId) },
                )
            }
            pendingClearGroup?.let { group ->
                SharedConfirmDeleteDialog(
                    onDismissRequest = { pendingClearGroup = null },
                    onConfirm = { viewModel.deleteWholesalerCart(group.wholesaler.id) },
                )
            }
        },
        title = {
            CartTopBarTitle(
                cart = state.cart,
                activeWholesalerName = state.activeWholesalerName,
                isWholesalerScoped = state.isWholesalerScoped,
                isRefreshing = state.isRefreshing,
                isLoading = state.isContentRefreshing,
                onRefresh = viewModel::refreshCart,
            )
        },
        actions = {
            if (state.isWholesalerScoped) {
                ExitWholesalerModeButton(modifier = Modifier, onExit = onExitWholesalerScope)
            }
        },
        bottomBar = {
            val cart = state.cart
            if (cart != null && !cart.isEmpty) {
                CartSummaryBottomBar(summary = cart.summary, isLoading = state.isContentRefreshing)
            }
        },
    ) { padding, scrollBehavior ->
        CartContent(
            state = state,
            padding = padding,
            scrollBehavior = scrollBehavior,
            onRefresh = viewModel::refreshCart,
            onRetry = { viewModel.loadCart(forceRefresh = true) },
            onWholesalerImageClick = { wholesaler ->
                wholesaler.imageUrl?.let { url ->
                    imagePreview = CartImagePreview(url, wholesaler.displayLabel)
                }
            },
            onProductImageClick = { item ->
                item.mainImage?.url(item.productId)?.let { url ->
                    imagePreview = CartImagePreview(url, item.productName)
                }
            },
            onQuantityChange = viewModel::updateItemQuantity,
            onDeleteItem = { pendingDeleteItem = it },
            onClearWholesalerCart = { pendingClearGroup = it },
            onCreateOrder = {},
            onProductDetailClick = onProductDetailClick,
            onWholesalerScopeClick = viewModel::enterWholesalerScope,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartContent(
    state: StandardCartUiState,
    padding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onWholesalerImageClick: (CartWholesaler) -> Unit,
    onProductImageClick: (CartItem) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    onClearWholesalerCart: (CartGroup) -> Unit,
    onCreateOrder: (CartGroup) -> Unit,
    onProductDetailClick: (String) -> Unit,
    onWholesalerScopeClick: (CartWholesaler) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .align(Alignment.TopCenter),
                isRefreshing = state.isRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        when {
            state.isLoading && state.cart == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SharedLoadingDotsIndicator()
                }
            }

            state.loadState == UiState.Error && state.cart == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SharedRetryButton(onRetry)
                }
            }

            state.isEmpty -> {
                SharedNotFoundPlaceholder(stringResource(StandardRes.string.cart_empty))
            }

            else -> {
                val gridState = rememberLazyStaggeredGridState()
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 399.dp),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .paddingWithoutTopAndButton(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
                    horizontalArrangement = SharedLazyGridLayout.arrangement,
                    verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(padding.calculateTopPadding()))
                    }
                    state.cart?.takeIf { it.hasCartProblems() }?.let { cart ->
                        item(span = StaggeredGridItemSpan.FullLine, key = "cart-global-alert") {
                            GlobalAlertPanel(
                                modifier = Modifier.animateItem(),
                                groups = cart.groups,
                                updatingCartDetailId = state.updatingCartDetailId,
                                deletingCartDetailId = state.deletingCartDetailId,
                                isLoading = state.isContentRefreshing,
                                onQuantityChange = onQuantityChange,
                                onDeleteItem = onDeleteItem,
                            )
                        }
                    }
                    val groups = state.cart?.groups.orEmpty()
                    val useSingleWholesalerLayout = groups.size == 1
                    if (useSingleWholesalerLayout) {
                        val group = groups.first()
                        item(
                            key = "cart-group-${group.wholesaler.id}-header",
                            span = StaggeredGridItemSpan.FullLine,
                        ) {
                            CartSingleWholesalerHeaderCard(
                                modifier = Modifier.animateItem(),
                                group = group,
                                selectingWholesalerId = state.selectingWholesalerId,
                                isLoading = state.isContentRefreshing,
                                isWholesalerScoped = state.isWholesalerScoped,
                                onWholesalerImageClick = onWholesalerImageClick,
                                onWholesalerScopeClick = onWholesalerScopeClick,
                            )
                        }
                        items(
                            group.items,
                            key = { item -> "cart-item-${item.cartDetailId}" },
                        ) { item ->
                            CartSingleWholesalerItemCard(
                                modifier = Modifier.animateItem(),
                                item = item,
                                updatingCartDetailId = state.updatingCartDetailId,
                                deletingCartDetailId = state.deletingCartDetailId,
                                isLoading = state.isContentRefreshing,
                                onProductImageClick = onProductImageClick,
                                onProductDetailClick = onProductDetailClick,
                                onQuantityChange = onQuantityChange,
                                onDeleteItem = onDeleteItem,
                            )
                        }
                        item(
                            key = "cart-group-${group.wholesaler.id}-footer",
                            span = StaggeredGridItemSpan.FullLine,
                        ) {
                            CartSingleWholesalerFooterCard(
                                modifier = Modifier.animateItem(),
                                group = group,
                                updatingCartDetailId = state.updatingCartDetailId,
                                deletingCartDetailId = state.deletingCartDetailId,
                                deletingWholesalerId = state.deletingWholesalerId,
                                selectingWholesalerId = state.selectingWholesalerId,
                                isLoading = state.isContentRefreshing,
                                onClearWholesalerCart = onClearWholesalerCart,
                                onCreateOrder = onCreateOrder,
                            )
                        }
                    } else {
                        items(
                            groups,
                            key = { group -> "cart-group-${group.wholesaler.id}" },
                        ) { group ->
                            CartGroupCard(
                                modifier = Modifier.animateItem(),
                                group = group,
                                updatingCartDetailId = state.updatingCartDetailId,
                                deletingCartDetailId = state.deletingCartDetailId,
                                deletingWholesalerId = state.deletingWholesalerId,
                                selectingWholesalerId = state.selectingWholesalerId,
                                isLoading = state.isContentRefreshing,
                                onWholesalerImageClick = onWholesalerImageClick,
                                onProductImageClick = onProductImageClick,
                                onQuantityChange = onQuantityChange,
                                onDeleteItem = onDeleteItem,
                                onClearWholesalerCart = onClearWholesalerCart,
                                onCreateOrder = onCreateOrder,
                                onProductDetailClick = onProductDetailClick,
                                onWholesalerScopeClick = onWholesalerScopeClick,
                                isWholesalerScoped = state.isWholesalerScoped,
                            )
                        }
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}
