package org.dsqrwym.shared.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.load_failed
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SharedProductWaterfall(
    paginatedProducts: LazyPagingItems<T>,
    scrollBehavior: TopAppBarScrollBehavior,
    padding: PaddingValues,
    topContentHeight: Dp = 0.dp,
    isRefreshing: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    applyPaddingWithoutTop: Boolean = false,
    includeMenuTopPadding: Boolean = false,
    bottomSpacerHeight: Dp = 28.dp,
    key: (index: Int) -> Any = { index -> index },
    errorContent: @Composable BoxScope.() -> Unit = {
        SharedRetryButton { paginatedProducts.retry() }
    },
    itemContent: @Composable (T) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val windowSizeClass = LocalWindowSizeClass.current

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
        if (isError) {
            errorContent()
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(
                    minSize = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 166.dp else 200.dp,
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
                    Spacer(Modifier.height(topContentHeight))
                }

                items(
                    count = paginatedProducts.itemCount,
                    key = key,
                ) { index ->
                    paginatedProducts[index]?.let { itemContent(it) }
                }

                if (paginatedProducts.loadState.append is LoadState.Loading) {
                    appendLoadingIndicator()
                }

                if (paginatedProducts.loadState.append is LoadState.Error) {
                    appendErrorRetry { paginatedProducts.retry() }
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(bottomSpacerHeight))
                }
            }

            if (!isRefreshing &&
                !isError &&
                paginatedProducts.itemCount == 0 &&
                paginatedProducts.loadState.refresh !is LoadState.Loading
            ) {
                SharedPlainNotFoundPlaceholder()
            }
        }
    }
}

@Composable
fun SharedProductRefreshError(
    message: String = stringResource(SharedRes.string.load_failed),
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(12.dp))
        SharedRetryButton(retry = onRetry)
    }
}
