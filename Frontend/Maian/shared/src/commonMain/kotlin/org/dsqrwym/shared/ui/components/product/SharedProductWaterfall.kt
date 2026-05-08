package org.dsqrwym.shared.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.no_products
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SharedProductWaterfall(
    paginatedProducts: LazyPagingItems<T>,
    scrollBehavior: TopAppBarScrollBehavior,
    padding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    applyPaddingWithoutTop: Boolean = false,
    includeMenuTopPadding: Boolean = false,
    minSize: Dp = 200.dp,
    key: (index: Int) -> Any = { index -> index },
    itemContent: @Composable LazyStaggeredGridItemScope.(T) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = paginatedProducts.isRefreshing

    PullToRefreshBox(
        modifier = modifier
            .fillMaxSize()
            .then(if (applyPaddingWithoutTop) Modifier.paddingWithoutTop(padding) else Modifier),
        isRefreshing = isRefreshing,
        onRefresh = { paginatedProducts.refresh() },
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        if (paginatedProducts.isEmptyResult) {
            SharedNotFoundPlaceholder(stringResource(SharedRes.string.no_products))
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(
                    minSize = minSize,
                ),
                state = state,
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = SharedLazyGridLayout.Padding),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    if (includeMenuTopPadding) {
                        Spacer(Modifier.paddingTopForMenu())
                    }
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                }

                if (paginatedProducts.hasLoadError) {
                    appendErrorRetry { paginatedProducts.retry() }
                } else {
                    items(
                        count = paginatedProducts.itemCount,
                        key = key,
                    ) { index ->
                        paginatedProducts[index]?.let { itemContent(it) }
                    }

                    if (paginatedProducts.isAppendingOrPrepending) {
                        appendLoadingIndicator()
                    }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}
