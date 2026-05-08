package org.dsqrwym.shared.paging

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/**
 * 是否正在远程加载
 * 用于：
 * - shimmer
 * - skeleton
 * 不包含 append/prepend
 */
val <T : Any> LazyPagingItems<T>.isRefreshing: Boolean
    get() = loadState.refresh is LoadState.Loading

/**
 * 是否正在加载更多
 * 用于：
 * - 底部 loading
 * - 顶部 loading
 */
val <T : Any> LazyPagingItems<T>.isAppendingOrPrepending: Boolean
    get() = loadState.append is LoadState.Loading ||
            loadState.prepend is LoadState.Loading

/**
 * 是否任意加载失败
 * 用于：
 * - 通用错误 UI
 */
val <T : Any> LazyPagingItems<T>.hasLoadError: Boolean
    get() = loadState.refresh is LoadState.Error ||
            loadState.append is LoadState.Error ||
            loadState.prepend is LoadState.Error

/**
 * 是否为空结果
 * 用于：
 * - EmptyContent
 */
val <T : Any> LazyPagingItems<T>.isEmptyResult: Boolean
    get() = loadState.refresh is LoadState.NotLoading &&
            itemCount == 0

/**
 * 是否为首次空数据加载
 * 用于：
 * - 首屏 skeleton
 * - shimmer
 */
val <T : Any> LazyPagingItems<T>.isInitialLoading: Boolean
    get() = loadState.refresh is LoadState.Loading &&
            itemCount == 0