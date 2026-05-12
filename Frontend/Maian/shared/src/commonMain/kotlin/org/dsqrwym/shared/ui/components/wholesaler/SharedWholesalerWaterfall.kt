package org.dsqrwym.shared.ui.components.wholesaler

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
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SharedWholesalerWaterfall(
    paginatedItems: LazyPagingItems<T>,
    scrollBehavior: TopAppBarScrollBehavior,
    padding: PaddingValues,
    emptyText: String,
    modifier: Modifier = Modifier,
    minSize: Dp = 360.dp,
    key: (index: Int) -> Any = { index -> index },
    itemContent: @Composable LazyStaggeredGridItemScope.(T) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = paginatedItems.isRefreshing

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = { paginatedItems.refresh() },
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
        if (paginatedItems.hasLoadError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { SharedRetryButton { paginatedItems.retry() } }
        } else if (paginatedItems.isEmptyResult) {
            SharedNotFoundPlaceholder(emptyText)
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = minSize),
                state = state,
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = SharedLazyGridLayout.Padding),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                }

                items(
                    count = paginatedItems.itemCount,
                    key = key,
                ) { index ->
                    paginatedItems[index]?.let { itemContent(it) }
                }

                if (paginatedItems.isAppendingOrPrepending) {
                    appendLoadingIndicator()
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
    }
