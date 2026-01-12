package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.SharedOverlayContentBox
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.platform.Platform
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductWaterfallView(
    paginatedProducts: LazyPagingItems<ProductResponse>,
    fakeProducts: List<ProductResponse>,
    scrollBehavior: TopAppBarScrollBehavior,
    updateCurrentProduct: (ProductResponse) -> Unit,
    onPreview: (ProductResponse) -> Unit,
    onDelete: (ProductResponse) -> Unit,
    padding: PaddingValues,
    isRefreshing: Boolean,
    isError: Boolean,
) {
    // 状态管理：列表状态
    val platform = remember { getPlatform() }
    val state = rememberLazyStaggeredGridState()
    val windowSizeClass = LocalWindowSizeClass.current
    var filterFlowRowHeight by remember { mutableStateOf(0.dp) }
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .paddingWithoutTop(padding),
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
        }
    ) {
        if (isError) {
            SharedRetryButton {
                paginatedProducts.retry()
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 166.dp else 200.dp),
                state = state,
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = SharedLazyGridLayout.Padding)
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                    Spacer(Modifier.fillMaxWidth().height(filterFlowRowHeight).heightIn(min = 28.dp))
                }

                // Fake Data (骨架屏)
                if (isRefreshing) {
                    items(fakeProducts.size) {
                        ProductGridItem(
                            product = null, // null 触发内部 shimmer
                            isRefreshing = true,
                            platform = platform,
                            onClick = {},
                            onImageClick = {},
                            onPreview = {},
                            onDelete = {}
                        )
                    }
                } else {
                    // Paging 数据项
                    items(
                        count = paginatedProducts.itemCount,
                        key = { index ->
                            // 尝试获取唯一ID，如果获取不到使用index (防止crash)
                            paginatedProducts.peek(index)?.id ?: index
                        }
                    ) { index ->
                        val product = paginatedProducts[index]
                        if (product != null) {
                            ProductGridItem(
                                product = product,
                                isRefreshing = false,
                                platform = platform,
                                onImageClick = { updateCurrentProduct(product) },
                                onClick = { },
                                onPreview = { onPreview(product) },
                                onDelete = { onDelete(product) }
                            )
                        }
                    }

                    // 底部加载状态 (Paging Append Loading)
                    if (paginatedProducts.loadState.append is LoadState.Loading) {
                        appendLoadingIndicator()
                    }

                    // 底部错误重试 (Paging Append Error)
                    if (paginatedProducts.loadState.append is LoadState.Error) {
                        appendErrorRetry { paginatedProducts.retry() }
                    }
                }
                item {
                    Spacer(Modifier.height(28.dp))
                }
            }

            // 空状态提示
            if (!isRefreshing && !isError && paginatedProducts.itemCount == 0 && paginatedProducts.loadState.refresh !is LoadState.Loading) {
                SharedNotFoundPlaceholder()
            }
        }
    }
}

@Composable
fun ProductGridItem(
    product: ProductResponse?,
    isRefreshing: Boolean,
    platform: Platform,
    onClick: () -> Unit,
    onImageClick: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    // 如果 product 为空 (placeholder) 或者正在刷新，显示骨架屏
    val isLoading = product == null || isRefreshing

    OutlinedCard(
        onClick = { if (!isLoading) onClick() },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // --- 1. 图片区域 (带状态标签) ---
            SharedOverlayContentBox(
                isLoading,
                loadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp) // 骨架屏固定高度
                            .placeholderWithShimmer(true)
                    )
                },
                overlaySurfaceColor = product?.let {
                    if (it.totalStock > 0) MaterialTheme.colorScheme.primaryContainer else
                        MaterialTheme.colorScheme.errorContainer
                } ?: MaterialTheme.colorScheme.errorContainer,
                topEndOverlay = {
                    product?.let {
                        SelectionContainer {
                            Text(
                                text = if (it.totalStock > 0) "库存: ${it.totalStock}" else "缺货",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = if (it.totalStock > 0) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                },
            ) {
                product?.let {
                    SharedAsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable(onClick = onImageClick),
                        model = it.mainImage.getUrl(it.id),
                        contentDescription = it.name,
                        placeholder = rememberVectorPainter(SharedIcons.MaianLogo),
                        zoomable = false,
                        enableContextMenu = false,
                        contentScale = if (platform.type == PlatformType.Android) ContentScale.FillWidth else ContentScale.Fit,
                    )
                }
            }

            // --- 2. 内容详情区域 ---
            Column(modifier = Modifier.padding(SharedColumnLayout.padding)) {
                if (isLoading) {
                    // 骨架屏文本
                    Box(Modifier.fillMaxWidth(0.8f).height(16.dp).placeholderWithShimmer(true))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.5f).height(14.dp).placeholderWithShimmer(true))
                } else {
                    product.let { item ->
                        // 类别 & 编码 (辅助信息)
                        FlowRow(
                            verticalArrangement = Arrangement.Center,
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer {
                                Text(
                                    text = item.code,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    ).padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            SelectionContainer {
                                Text(
                                    text = item.mainCategory.localName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // 产品名称 (主标题)
                        SelectionContainer {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 副标题 (次级标题)
                        if (item.title.isNotEmpty() && item.title != item.name) {
                            SelectionContainer {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider()

                        Spacer(Modifier.height(8.dp))

                        // 价格 & 起订量 (关键数据)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            SelectionContainer {
                                Column {
                                    Text(
                                        text = "含税: ${item.minPrice}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "未税: ${item.minPriceIva}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            SelectionContainer {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "MOQ: ${item.minOrderQty}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = item.status.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. 操作栏 (针对管理员/批发商的快捷入口) ---
            if (!isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(SharedColumnLayout.padding),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onPreview) {
                        Icon(
                            imageVector = Icons.Default.Visibility, // 假设你有这个Icon
                            contentDescription = "预览",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    BusinessOutlinedDeleteButton(onDelete = onDelete)
                }
            }
        }
    }
}