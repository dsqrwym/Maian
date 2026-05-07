package org.dsqrwym.shared.ui.components.product.detail

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined._1xMobiledata
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.domain.product.SharedProductDetailMedia
import org.dsqrwym.shared.domain.product.SharedProductDetailUiModel
import org.dsqrwym.shared.domain.product.SharedProductDetailVariant
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Box
import org.dsqrwym.shared.drawable.sharedicons.Package24
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.dialog.SharedMediaViewDialog
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.ui.media.SharedVideoPlayer
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTopAndButton
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.getWidthSizeClass
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun SharedProductDetailContent(
    product: SharedProductDetailUiModel?,
    isLoading: Boolean,
    selectedVariant: SharedProductDetailVariant?,
    padding: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    onVariantSelect: (SharedProductDetailVariant) -> Unit,
    onMediaClick: (SharedProductDetailMedia) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .paddingWithoutTopAndButton(padding)
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
    ) {
        item {
            Spacer(Modifier.height(padding.calculateTopPadding()))
        }
        item {
            FormCard {
                SharedProductMediaInfoLayout(
                    isLoading = isLoading,
                    productName = product?.name.orEmpty(),
                    categoryName = product?.categoryName,
                    productTitle = product?.title,
                    productCode = product?.productCode.orEmpty(),
                    taxRate = product?.iva.orEmpty(),
                    media = product?.media.orEmpty(),
                    onMediaClick = onMediaClick,
                )
            }
        }
        item {
            SharedProductVariantSelector(
                isLoading = isLoading,
                variants = product?.variants.orEmpty(),
                selectedVariant = selectedVariant,
                onVariantSelect = onVariantSelect,
            )
        }
        item {
            SharedProductDescriptionSection(
                productDescription = product?.description,
                isLoading = isLoading,
            )
        }
        item {
            Spacer(Modifier.height(padding.calculateBottomPadding()))
        }
    }
}

@Composable
fun SharedProductMediaPreviewDialog(
    media: SharedProductDetailMedia,
    productName: String?,
    onDismissRequest: () -> Unit,
) {
    when {
        media.isImage -> SharedMediaViewDialog(onDismissRequest = onDismissRequest) {
            SharedAsyncImage(
                modifier = Modifier.wrapContentSize(),
                model = media.model,
                contentDescription = productName ?: stringResource(SharedRes.string.image_content_description),
                imageName = productName,
            )
        }

        media.isVideo -> SharedMediaViewDialog(onDismissRequest = onDismissRequest) {
            SharedProductVideoPlayer(
                model = media.model,
                modifier = Modifier.fillMaxWidth(0.92f).aspectRatio(16f / 9f),
            )
        }
    }
}

@Composable
private fun SharedProductMediaInfoLayout(
    isLoading: Boolean,
    productName: String,
    categoryName: String?,
    productTitle: String?,
    productCode: String,
    taxRate: String,
    media: List<SharedProductDetailMedia>,
    onMediaClick: (SharedProductDetailMedia) -> Unit,
) {
    BoxWithConstraints (modifier = Modifier.fillMaxSize()){
        val useTwoColumns = getWidthSizeClass(constraints.maxWidth.dp) != WindowWidthSizeClass.Compact

        if (useTwoColumns) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = SharedRowLayout.arrangement,
                verticalAlignment = Alignment.Top,
            ) {
                SharedProductMediaPager(
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 360.dp)
                        .placeholderWithShimmer(isLoading),
                    productName = productName,
                    media = media,
                    onMediaClick = onMediaClick,
                )

                SharedProductInfoSection(
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
                SharedProductMediaPager(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .placeholderWithShimmer(isLoading),
                    productName = productName,
                    media = media,
                    onMediaClick = onMediaClick,
                )

                SharedProductInfoSection(
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
}

@Composable
private fun SharedProductMediaPager(
    modifier: Modifier = Modifier,
    productName: String = "",
    media: List<SharedProductDetailMedia> = emptyList(),
    onMediaClick: (SharedProductDetailMedia) -> Unit = {},
) {
    val sortedMedia = remember(media) { media.sortedBy { it.sort } }
    if (sortedMedia.isEmpty()) {
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

    val pagerState = rememberPagerState(pageCount = { sortedMedia.size })
    var autoPlay by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var resumeJob by remember { mutableStateOf<Job?>(null) }
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
                    if (sortedMedia.size > 1) {
                        val next = (pagerState.currentPage + 1) % sortedMedia.size
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
                val item = sortedMedia[page]
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
                            model = item.model,
                            contentDescription = stringResource(
                                SharedRes.string.product_image_with_name,
                                productName,
                            ),
                            contentScale = ContentScale.Fit,
                            zoomable = false,
                            enableContextMenu = false,
                        )

                        item.isVideo -> SharedProductVideoPlayer(
                            model = item.model,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (sortedMedia.size > 1) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .size(36.dp),
                    enabled = pagerState.currentPage > 0,
                    onClick = {
                        onUserInteraction()
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
                    enabled = pagerState.currentPage < sortedMedia.lastIndex,
                    onClick = {
                        onUserInteraction()
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

        if (sortedMedia.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(sortedMedia.size) { index ->
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
                                onUserInteraction()
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
private fun SharedProductInfoSection(
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
private fun SharedProductVariantSelector(
    isLoading: Boolean,
    variants: List<SharedProductDetailVariant>,
    selectedVariant: SharedProductDetailVariant?,
    onVariantSelect: (SharedProductDetailVariant) -> Unit,
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
            items(variants.sortedBy { it.sort }) { variant ->
                SharedProductVariantCard(
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
private fun SharedProductVariantCard(
    isLoading: Boolean,
    variant: SharedProductDetailVariant,
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
private fun SharedProductDescriptionSection(productDescription: String?, isLoading: Boolean = false) {
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
private fun SharedProductVideoPlayer(
    model: Any?,
    modifier: Modifier = Modifier,
) {
    when (model) {
        is PlatformFile -> SharedVideoPlayer(
            file = model,
            modifier = modifier,
            enableContextMenu = false,
        )

        is String -> SharedVideoPlayer(
            url = model,
            modifier = modifier,
            enableContextMenu = false,
        )
    }
}
