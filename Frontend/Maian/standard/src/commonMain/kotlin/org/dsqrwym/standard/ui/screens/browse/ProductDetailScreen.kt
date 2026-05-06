package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Box
import org.dsqrwym.shared.drawable.sharedicons.Package24
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.dialog.SharedMediaViewDialog
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTopAndButton
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.dsqrwym.standard.domain.browse.RetailProductDetailMedia
import org.dsqrwym.standard.domain.browse.RetailProductVariant
import org.dsqrwym.standard.ui.viewmodels.browse.ProductDetailViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProductDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val isLoading = viewModel.isLoading

    val product = viewModel.product
    val languageCode = viewModel.languageCode
    val previewMedia = viewModel.previewMedia
    val selectedVariant = viewModel.selectedVariant
    val quantityText = viewModel.quantityText
    val addToCartEnabled = viewModel.canAddToCart


    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = previewMedia != null,
        overlayContent = {
            previewMedia?.let { media ->
                val url = media.url(productId)
                when {
                    media.isImage -> SharedImageViewDialog(
                        model = url,
                        imageName = product?.localizedName(languageCode),
                        onDismissRequest = viewModel::dismissMediaPreview,
                    )

                    media.isVideo -> SharedMediaViewDialog(onDismissRequest = viewModel::dismissMediaPreview) {
                        ProductRemoteVideoPlayer(
                            url = url,
                            modifier = Modifier.fillMaxWidth(0.92f).aspectRatio(16f / 9f),
                        )
                    }
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null)
                Text(
                    text = stringResource(SharedRes.string.product_detail),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        bottomBar = {
            ProductBottomCartBar(
                selectedVariant = selectedVariant,
                quantityText = quantityText,
                canAddToCart = addToCartEnabled,
                onQuantityChange = viewModel::updateQuantity,
                onQuantityStep = viewModel::updateStepQuantity,
            )
        }
    ) { padding, scrollBehavior ->
        if (product == null && !isLoading) {
            SharedPlainNotFoundPlaceholder()
            return@SharedTransparentScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTopAndButton(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
        ) {
            item {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }
            item {
                FormCard {
                    ProductMediaInfoLayout(
                        isLoading = isLoading,
                        productId = productId,
                        productName = product?.localizedName(languageCode) ?: "",
                        categoryName = product?.mainCategory?.localizedName(languageCode),
                        productTitle = product?.localizedTitle(languageCode),
                        productCode = product?.productCode ?: "",
                        taxRate = product?.iva ?: "",
                        media = product?.media ?: emptyList(),
                        onMediaClick = viewModel::showMediaPreview,
                    )
                }
            }
            item {
                ProductVariantSelector(
                    isLoading = isLoading,
                    variants = product?.variants ?: emptyList(),
                    selectedVariant = viewModel.selectedVariant,
                    onVariantSelect = viewModel::selectVariant,
                )
            }
            item {
                ProductDescriptionSection(
                    productDescription = product?.localizedDescription(languageCode),
                    isLoading = isLoading
                )
            }
            item {
                Spacer(Modifier.height(padding.calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun ProductMediaInfoLayout(
    isLoading: Boolean,
    productId: String,
    productName: String,
    categoryName: String?,
    productTitle: String?,
    productCode: String,
    taxRate: String,
    media: List<RetailProductDetailMedia>,
    onMediaClick: (RetailProductDetailMedia) -> Unit,
) {
    val useTwoColumns = calculateWindowSizeClass().widthSizeClass != WindowWidthSizeClass.Compact

    if (useTwoColumns) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalAlignment = Alignment.Top,
        ) {
            ProductMediaPager(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 360.dp)
                    .placeholderWithShimmer(isLoading),
                productId = productId,
                productName = productName,
                media = media,
                onMediaClick = onMediaClick,
            )

            ProductInfoSection(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 260.dp),
                isLoading = isLoading,
                categoryName = categoryName,
                productTitle = productTitle,
                productName = productName,
                productCode = productCode,
                taxRate = taxRate,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = SharedColumnLayout.arrangement,
        ) {
            ProductMediaPager(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .placeholderWithShimmer(isLoading),
                productId = productId,
                productName = productName,
                media = media,
                onMediaClick = onMediaClick,
            )

            ProductInfoSection(
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoading,
                categoryName = categoryName,
                productTitle = productTitle,
                productName = productName,
                productCode = productCode,
                taxRate = taxRate,
            )
        }
    }
}

@Composable
private fun ProductMediaPager(
    modifier: Modifier = Modifier,
    productId: String,
    productName: String = "",
    media: List<RetailProductDetailMedia> = emptyList(),
    onMediaClick: (RetailProductDetailMedia) -> Unit = {},
) {
    if (media.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = SharedColumnLayout.arrangement,
        ) {
            Icon(
                imageVector = Icons.Outlined.HideImage,
                contentDescription = productName,
                modifier = Modifier.size(68.dp),
            )
            Text(productName)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { media.size })
    var autoPlay by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var resumeJob by remember { mutableStateOf<Job?>(null) }

    // 用严格模式：每次操控取消上一个恢复任务，只保留最新一次的 30 秒倒计时
    val onUserInteraction by rememberUpdatedState {
        autoPlay = false
        resumeJob?.cancel()
        resumeJob = scope.launch {
            delay(30.seconds)
            autoPlay = true
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { autoPlay }
            .collectLatest { playing ->
                if (!playing) return@collectLatest
                while (true) {
                    delay(3.seconds)
                    if (media.size > 1) {
                        val next = (pagerState.currentPage + 1) % media.size
                        pagerState.animateScrollToPage(next)
                    }
                }
            }
    }

    Column(modifier = modifier, verticalArrangement = SharedColumnLayout.arrangement) {
        Box(
            modifier = Modifier
                .heightIn(min = 240.dp, max = 360.dp)
                .widthIn(min = 240.dp, max = 360.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        onUserInteraction()
                    }
                },
            ) { page ->
                val item = media[page]
                val url = item.url(productId)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMediaClick(item) },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        item.isImage -> SharedAsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = url,
                            contentDescription = stringResource(
                                SharedRes.string.product_image_with_name,
                                productName,
                            ),
                            contentScale = ContentScale.Fit,
                            zoomable = false,
                            enableContextMenu = false,
                        )

                        item.isVideo -> ProductRemoteVideoPlayer(
                            url = url,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (media.size > 1) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .size(36.dp),
                    enabled = pagerState.currentPage > 0,
                    onClick = {
                        onUserInteraction()  // ← 修复：通知自动轮播暂停
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = Icons.AutoMirrored.Outlined.ArrowBack.name,
                        modifier = Modifier.size(18.dp),
                    )
                }

                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(36.dp),
                    enabled = pagerState.currentPage < media.lastIndex,
                    onClick = {
                        onUserInteraction()  // ← 修复
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = Icons.AutoMirrored.Outlined.ArrowForward.name,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        if (media.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(media.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                            .clickable {
                                onUserInteraction()  // ← 修复
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductInfoSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    categoryName: String?,
    productTitle: String?,
    productName: String,
    productCode: String,
    taxRate: String,
) {
    SelectionContainer(modifier = modifier) {
        Column(
            verticalArrangement = SharedColumnLayout.arrangement
        ) {
            Text(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = productName,
                style = MaterialTheme.typography.headlineSmall,
            )
            productTitle?.let {
                Text(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
            FlowRow(
                horizontalArrangement = SharedRowLayout.arrangement,
                verticalArrangement = SharedColumnLayout.arrangement,
            ) {
                AssistChip(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    onClick = {},
                    label = { Text("${stringResource(SharedRes.string.product_code)}: $productCode") },
                )
                categoryName?.let {
                    AssistChip(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        onClick = {},
                        leadingIcon = { Icon(Icons.Outlined.Store, contentDescription = null) },
                        label = { Text(it) },
                    )
                }
                AssistChip(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    onClick = {},
                    label = {
                        Text(
                            text = "${stringResource(SharedRes.string.tax_rate)}: $taxRate %",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductVariantSelector(
    isLoading: Boolean,
    variants: List<RetailProductVariant>,
    selectedVariant: RetailProductVariant?,
    onVariantSelect: (RetailProductVariant) -> Unit,
) {
    FormCard(title = stringResource(SharedRes.string.variants)) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 800.dp),
            columns = StaggeredGridCells.Adaptive(minSize = 338.dp),
            verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
            horizontalArrangement = SharedLazyGridLayout.arrangement,
        ) {
            items(variants) { variant ->
                VariantCard(
                    isLoading = isLoading,
                    variant = variant,
                    selected = variant.id == selectedVariant?.id,
                    onSelect = { onVariantSelect(variant) },
                )
            }
        }
    }
}

@Composable
private fun VariantCard(
    isLoading: Boolean,
    variant: RetailProductVariant,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val enabled = variant.isPurchasable
    SelectionContainer {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onSelect),
            colors = CardDefaults.outlinedCardColors(
                containerColor = when {
                    selected -> MaterialTheme.colorScheme.secondaryContainer
                    !enabled -> Color.Unspecified
                    else -> Color.Transparent
                }
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = SharedRowLayout.arrangement,
                verticalAlignment = Alignment.Top,
            ) {
                RadioButton(
                    modifier = Modifier.fillMaxHeight(),
                    selected = selected,
                    onClick = onSelect,
                    enabled = enabled
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        text = variant.productCode,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                variant.typeSale.displayName() + ": ${variant.saleUnitQty}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            val icon = when (variant.typeSale) {
                                SharedProductSaleVariant.BOX -> SharedIcons.Box
                                SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                                SharedProductSaleVariant.PACK -> SharedIcons.Package24
                            }
                            Icon(icon, variant.typeSale.displayName())
                        }
                    )
                    Text(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        text = stringResource(SharedRes.string.product_price_with_vat_value, variant.priceIva),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        text = stringResource(SharedRes.string.product_price_without_vat_value, variant.price),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = SharedRowLayout.arrangement,
                        verticalArrangement = SharedColumnLayout.arrangement,
                    ) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = stringResource(
                                        SharedRes.string.available_stock_value,
                                        variant.availableStock
                                    )
                                )
                            })
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = stringResource(SharedRes.string.sale_unit_qty_value, variant.saleUnitQty)
                                )
                            })
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    "${stringResource(SharedRes.string.product_min_order_qty)}: ${variant.minOrderQty}",
                                    modifier = Modifier.placeholderWithShimmer(isLoading)
                                )
                            })
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text =
                                        stringResource(
                                            SharedRes.string.max_purchasable_qty_value,
                                            variant.maxPurchasableUnits
                                        )
                                )
                            })
                    }
                    if (!enabled) {
                        Text(
                            text = stringResource(SharedRes.string.not_purchasable),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalRichTextApi::class)
@Composable
private fun ProductDescriptionSection(productDescription: String?, isLoading: Boolean = false) {
    FormCard(title = stringResource(SharedRes.string.description)) {
        productDescription?.let {
            RichText(
                modifier = Modifier.fillMaxWidth().placeholderWithShimmer(isLoading),
                state = RichTextState().setHtml(it)
            )
        }
    }
}

@Composable
private fun ProductBottomCartBar(
    modifier: Modifier = Modifier,
    selectedVariant: RetailProductVariant?,
    quantityText: String,
    canAddToCart: Boolean,
    onQuantityChange: (String) -> Unit,
    onQuantityStep: (Int) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 260.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onQuantityStep(-1) },
                enabled = selectedVariant != null,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.outline,
                )
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = null)
            }

            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 72.dp, max = 150.dp),
                value = quantityText,
                onValueChange = onQuantityChange,
                enabled = selectedVariant != null,
                singleLine = true,
                label = { Text(stringResource(SharedRes.string.quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            IconButton(
                onClick = { onQuantityStep(1) },
                enabled = selectedVariant != null,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.outline,
                )
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }

        ElevatedButton(
            modifier = Modifier.widthIn(min = 150.dp),
            enabled = canAddToCart,
            onClick = {},
        ) {
            Icon(Icons.Outlined.AddShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(SharedRes.string.add_to_cart))
        }
    }
}

@Composable
private fun ProductRemoteVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
) {
    val playerState = rememberVideoPlayerState()
    LaunchedEffect(url) {
        playerState.openUri(url, InitialPlayerState.PAUSE)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black),
    ) {
        VideoPlayerSurface(
            playerState = playerState,
            modifier = Modifier.fillMaxSize(),
        )
        if (playerState.isLoading) {
            LinearProgressIndicator(Modifier.align(Alignment.TopCenter).fillMaxWidth())
        }
    }
}
