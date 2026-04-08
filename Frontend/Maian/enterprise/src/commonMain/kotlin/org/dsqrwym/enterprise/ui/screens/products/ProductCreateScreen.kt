package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.add_language_translation
import maian.business.generated.resources.parent_category_selected
import maian.business.generated.resources.translations_count
import maian.shared.generated.resources.*
import org.dsqrwym.business.drawable.sharedicons.Barcode
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.richtext.BusinessRichTextEditor
import org.dsqrwym.business.ui.components.richtext.RichTextStyleButton
import org.dsqrwym.business.ui.components.row.BusinessLabelValueRow
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.business.ui.media.model.MediaType
import org.dsqrwym.business.ui.media.model.UploadMediaItem
import org.dsqrwym.business.ui.media.model.UploadState
import org.dsqrwym.business.ui.workspace.BusinessAuxiliaryHost
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface
import org.dsqrwym.business.ui.workspace.rememberBusinessAuxiliaryWorkspaceState
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.ui.components.containers.ReorderableContentBox
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductCreateViewModel
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Box
import org.dsqrwym.shared.drawable.sharedicons.InProgress
import org.dsqrwym.shared.drawable.sharedicons.Package24
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.cards.StatusIcon
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedDoubleField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedIntegerField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.ui.media.SharedVideoPlayer
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProductCreateViewModel = koinViewModel()
) {
    val workspaceState = rememberBusinessAuxiliaryWorkspaceState()

    DisposableEffect(Unit) {
        onDispose { workspaceState.close() }
    }

    BusinessAuxiliaryHost(
        workspaceState = workspaceState,
        mainContent = {
            ProductCreateScreenContent(
                onNavigateBack = onNavigateBack,
                viewModel = viewModel,
                isAuxiliaryOpen = workspaceState.isOpen,
                onToggleRichTextEditor = {
                    workspaceState.toggle(BusinessAuxiliarySurface.Editor)
                },
            )
        },
        auxiliaryContent = { surface ->
            ProductCreateAuxiliaryPane(
                viewModel = viewModel,
                surface = surface,
                onClose = workspaceState::close,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun ProductCreateScreenContent(
    onNavigateBack: () -> Unit,
    viewModel: ProductCreateViewModel,
    isAuxiliaryOpen: Boolean,
    onToggleRichTextEditor: () -> Unit,
) {
    val mediaPicker = viewModel.mediaPicker

    val translationTabs = viewModel.translationTabs
    val selectedIndex = viewModel.selectedTranslationIndex
    val selectedTranslationNameError = viewModel.selectedTranslationNameError
    val canAddTranslation = viewModel.canAddTranslation

    val productCode = viewModel.productCode
    val productCodeError = viewModel.productCodeError
    val productIva = viewModel.productIva
    val productStatus = viewModel.productStatus
    val selectedCategory = viewModel.filterCategory
    val categoryError = viewModel.productCategoryError

    val skuTabs = viewModel.productVariants
    val canAddSku = viewModel.canAddSku
    val productVariantsProductCodesErrors = viewModel.productVariantsProductCodesErrors

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = viewModel.showAddLanguageDialog,
        overlayContent = {
            AddLanguageDialog(
                availableLanguages = viewModel.getAvailableLanguages(),
                onDismiss = { viewModel.showAddLanguageDialog(false) },
                onAdd = { langCode, _ ->
                    viewModel.upsertTranslation(langCode, "", "")
                    viewModel.showAddLanguageDialog(false)
                }
            )
        },
        title = {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ShoppingBag, "产品")
                Text(stringResource(SharedRes.string.create))
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            viewModel.createFormUiState,
            viewModel.createButtonEnabled,
            viewModel::createProduct,
            stringResource(SharedRes.string.create),
            Icons.Outlined.Add,
            stringResource(SharedRes.string.create)
        )
    ) { padding, scrollBehavior ->
        val maxSize by derivedStateOf { mediaPicker.maxItemSize.div(1024 * 1024) }
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = StaggeredGridCells.Adaptive(minSize = 399.9.dp),
            contentPadding = PaddingValues(SharedLazyGridLayout.Padding),
            horizontalArrangement = SharedLazyGridLayout.arrangement,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(padding.calculateTopPadding()))
            }
            item {
                FormCard(
                    title = "媒体文件",
                    subtitle = "长按拖拽排序，首张图片为主图。单个文件不超过 ${maxSize}MB",
                    uiState = mediaPicker.mediaPickerUiState
                ) {
                    ProductMediaUploader(mediaPicker)
                }
            }
            item {
                FormCard(
                    title = "产品信息（多语言）",
                    subtitle = "添加不同语言的名称和描述。主语言为默认展示语言，其它语言用于多语言展示。至少需要填写一种语言。",
                    uiState = viewModel.productTranslationUiState
                ) {
                    ProductTranslationTabs(
                        translationTabs = translationTabs,
                        currentProductNameError = selectedTranslationNameError,
                        currentLanguageIndex = selectedIndex,
                        changeLanguageIndex = viewModel::changeLanguageIndex,
                        upsertTranslation = viewModel::upsertTranslation,
                        removeTranslation = viewModel::removeTranslation,
                        showAddLanguageDialog = viewModel::showAddLanguageDialog,
                        isAuxiliaryOpen = isAuxiliaryOpen,
                        canAddTranslation = canAddTranslation,
                        onToggleRichTextEditor = onToggleRichTextEditor,
                    )
                }
            }
            item {
                FormCard(
                    title = "产品属性",
                    subtitle = "设置产品的基础属性，包括分类、税率和状态信息。",
                    uiState = viewModel.productMetaDataUiState
                ) {
                    ProductMetaFields(
                        selectedCategory = selectedCategory,
                        onSelectedCategoryChange = viewModel::updateFilterCategory,
                        onSearchCategory = viewModel::findCategories,
                        onRemoveCategory = viewModel::removeFilterCategory,
                        categoryError = categoryError,
                        productCode = productCode,
                        productCodeError = productCodeError,
                        onProductCodeChange = viewModel::updateProductCode,
                        productIva = productIva,
                        onIvaChange = viewModel::updateProductIva,
                        productStatus = productStatus,
                        onProductStatusChange = viewModel::updateProductStatus,
                    )
                }
            }
            item(span = if (skuTabs.size > 1) StaggeredGridItemSpan.FullLine else null) {
                FormCard(
                    title = "SKU 列表",
                    subtitle = "每个产品至少需要一个销售变体（SKU）。你可以为不同包装、规格或价格创建多个变体。最多支持 50 个变体。",
                    uiState = viewModel.productVariantUiState
                ) {
                    ProductSKUFields(
                        skuTabs = skuTabs,
                        onReorder = viewModel::reorder,
                        onDelete = viewModel::deleteVariant,
                        onAddClick = {
                            viewModel.upsertProductVariant(
                                id = Uuid.generateV4().toString(),
                                typeSale = SharedProductSaleVariant.BOX,
                                availableStock = 100,
                            )
                        },
                        canAdd = canAddSku,
                        onUpdate = {
                            viewModel.upsertProductVariant(
                                id = it.id ?: Uuid.generateV4().toString(),
                                sort = it.sort,
                                price = it.price,
                                priceIva = it.priceIva,
                                typeSale = it.typeSale,
                                productCode = it.productCode,
                                availableStock = it.availableStock,
                                saleUnitQty = it.saleUnitQty,
                                minOrderQty = it.minOrderQty,
                                lowStockThreshold = it.lowStockThreshold,
                            )
                        },
                        productVariantsProductCodesErrors = productVariantsProductCodesErrors,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductSKUFields(
    skuTabs: List<ProductVariantDto>,
    productVariantsProductCodesErrors: Map<String, StringResource?>,
    isLoading: Boolean = false,
    onReorder: (Int, Int) -> Unit,
    onUpdate: (ProductVariantDto) -> Unit,
    onDelete: (String?) -> Unit,
    onAddClick: () -> Unit,
    canAdd: Boolean,
) {
    val gridState = rememberLazyStaggeredGridState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableState = rememberReorderableLazyStaggeredGridState(
        lazyStaggeredGridState = gridState,
        scrollThresholdPadding = PaddingValues(SharedLazyGridLayout.Padding)
    ) { from, to ->
        onReorder(from.index - 1, to.index - 1)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    val currentSKUId by remember { mutableStateOf(skuTabs.first().id) }
    val isisAnyItemDragging = reorderableState.isAnyItemDragging

    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 800.dp),
        state = gridState,
        columns = StaggeredGridCells.Adaptive(minSize = 338.dp),
        verticalItemSpacing = SharedLazyGridLayout.verticalItemSpacing,
        horizontalArrangement = SharedLazyGridLayout.arrangement,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                BusinessSelectedInfoCard(
                    modifier = Modifier.weight(1f, false).widthIn(max = 336.dp),
                    visible = skuTabs.isNotEmpty(),
                    description = "已添加${skuTabs.size}个变体。",
                    icon = Icons.Outlined.Info,
                    enabled = false,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    enabled = canAdd,
                ) {
                    Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
                    Spacer(Modifier.width(8.dp))
                    Text("添加产品变体")
                }
            }
        }
        itemsIndexed(skuTabs, key = { _, item -> item.id ?: "" }) { index, item ->
            ReorderableItem(
                state = reorderableState,
                key = item.id ?: "",
            ) { isSelfDragging ->
                SkuItemCard(
                    modifier = Modifier
                        .draggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            },
                        )
                        .animateContentSize(),
                    item = item,
                    index = index,
                    currentSKUid = currentSKUId,
                    isAnyItemDragging = isisAnyItemDragging,
                    isSelfDragging = isSelfDragging,
                    canDelete = skuTabs.size > 1,
                    onDelete = onDelete,
                    onUpdate = onUpdate,
                    productCodeError = item.id?.let { productVariantsProductCodesErrors[it] }
                )
            }
        }
    }
}

@Composable
fun SkuItemCard(
    index: Int,
    item: ProductVariantDto,
    isLoading: Boolean = false,
    isAnyItemDragging: Boolean = false,
    isSelfDragging: Boolean = false,
    canDelete: Boolean = true,
    currentSKUid: String? = null,
    productCodeError: StringResource?,
    onUpdate: (ProductVariantDto) -> Unit,
    onDelete: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(currentSKUid == item.id) }
    var tempExpanded by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(isAnyItemDragging) {
        if (isAnyItemDragging && !isSelfDragging) {
            tempExpanded = expanded
            expanded = false
        } else if (!isAnyItemDragging) {
            tempExpanded?.let { expanded = it }
            tempExpanded = null
        }
    }
    LaunchedEffect(productCodeError) {
        if (productCodeError != null) {
            expanded = true
        }
    }
    ReorderableContentBox(
        modifier = modifier,
        isDragging = isSelfDragging,
        index = index,
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(SharedColumnLayout.padding),
                verticalArrangement = SharedColumnLayout.arrangement
            ) {
                AnimatedVisibility(productCodeError != null) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatusIcon(uiState = UiState.Error)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f).placeholderWithShimmer(isLoading),
                        horizontalArrangement = SharedColumnLayout.arrangement,
                        verticalArrangement = Arrangement.Center,
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.productCode,
                            style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    item.typeSale.name + ": ${item.saleUnitQty}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                val icon = when (item.typeSale) {
                                    SharedProductSaleVariant.BOX -> SharedIcons.Box
                                    SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                                    SharedProductSaleVariant.PACK -> SharedIcons.Package24
                                }
                                Icon(icon, item.typeSale.name)
                            }
                        )
                    }
                    AnimatedVisibility(canDelete && !isAnyItemDragging) {
                        BusinessDeleteIconButton(onDelete = {
                            onDelete(item.id)
                        })
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f).placeholderWithShimmer(isLoading),

                        horizontalArrangement = SharedRowLayout.arrangement,
                    ) {
                        BusinessLabelValueRow("含税价: ", "${item.priceIva ?: 0}")
                        BusinessLabelValueRow("价格: ", "${item.price ?: 0}")
                        BusinessLabelValueRow("最小起订量: ", "${item.minOrderQty}")
                        BusinessLabelValueRow("库存: ", "${item.availableStock}")
                    }

                    val degrees by animateFloatAsState(if (expanded) 0f else 180f)
                    IconButton(
                        enabled = productCodeError == null,
                        onClick = {
                            expanded = !expanded
                        }, modifier = Modifier.size(32.dp)
                    ) {
                        val icon = Icons.Outlined.ExpandLess
                        Icon(modifier = Modifier.rotate(degrees), imageVector = icon, contentDescription = icon.name)
                    }
                }

                AnimatedVisibility(expanded) {
                    ProductSkuFields(
                        selectedSaleVariant = item.typeSale,
                        onSelectedSaleVariantChange = {
                            onUpdate(item.copy(typeSale = it))
                        },
                        productCode = item.productCode,
                        onProductCodeChange = {
                            onUpdate(item.copy(productCode = it))
                        },
                        saleUnitQty = item.saleUnitQty,
                        onSaleUnitQtyChange = {
                            it?.let { onUpdate(item.copy(saleUnitQty = it)) }
                        },
                        price = item.price,
                        priceIva = item.priceIva,
                        onPriceChange = { price, priceIva ->
                            onUpdate(item.copy(price = price, priceIva = priceIva))
                        },
                        availableStock = item.availableStock,
                        onAvailableStockChange = { stock ->
                            stock?.let { onUpdate(item.copy(availableStock = it)) }
                        },
                        minOrderQty = item.minOrderQty,
                        onMinOrderQtyChange = { min ->
                            min?.let { onUpdate(item.copy(minOrderQty = it)) }
                        },
                        lowStockThreshold = item.lowStockThreshold,
                        onLowStockThresholdChange = { onUpdate(item.copy(lowStockThreshold = it)) },
                        productCodeError = productCodeError
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSkuFields(
    modifier: Modifier = Modifier,
    selectedSaleVariant: SharedProductSaleVariant,
    onSelectedSaleVariantChange: (SharedProductSaleVariant) -> Unit,
    productCode: String,
    onProductCodeChange: (String) -> Unit,
    productCodeError: StringResource?,
    saleUnitQty: Int = 0,
    onSaleUnitQtyChange: (Int?) -> Unit,
    price: String?,
    priceIva: String?,
    onPriceChange: (String?, String?) -> Unit,
    availableStock: Int,
    onAvailableStockChange: (Int?) -> Unit,
    minOrderQty: Int,
    onMinOrderQtyChange: (Int?) -> Unit,
    lowStockThreshold: Int?,
    onLowStockThresholdChange: (Int?) -> Unit,
) {
    val focus = LocalFocusManager.current
    var saleUnitEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(selectedSaleVariant) {
        when (selectedSaleVariant) {
            SharedProductSaleVariant.UNIT -> {
                onSaleUnitQtyChange(1)
                saleUnitEnabled = false
            }

            else -> saleUnitEnabled = true
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = SharedColumnLayout.arrangement,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider()

        Text(
            text = "销售信息",
            style = MaterialTheme.typography.titleSmall,
        )

        MyOutlinedTextField(
            value = productCode,
            onValueChange = onProductCodeChange,
            leadingIcon = SharedIcons.Barcode,
            leadingIconContentDescription = "SKU编码",
            trailingIcon = {
                if (productCode.isBlank()) {
                    SharedScannerButton(onProductCodeChange)
                } else SharedCloseButton { onProductCodeChange("") }
            },
            labelText = "产品编码 (${stringResource(SharedRes.string.field_required)})",
            placeholderText = "请输入SKU编码",
            error = productCodeError.asString(),
            keyBordType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = SharedRowLayout.arrangement
        ) {
            Selector(
                items = SharedProductSaleVariant.entries,
                selectedItem = selectedSaleVariant,
                itemToString = { it.name },
                onItemSelected = {
                    it?.let { onSelectedSaleVariantChange(it) }
                },
                optionsLeadingIcon = {
                    return@Selector when (it) {
                        SharedProductSaleVariant.BOX -> SharedIcons.Box
                        SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                        SharedProductSaleVariant.PACK -> SharedIcons.Package24
                    }
                },
                config = SelectorConfig(
                    modifier = Modifier.weight(0.5f),
                    modifierFillMaxWidth = false,
                    label = "销售单位 (${stringResource(SharedRes.string.field_required)})",
                    leadingIcon = Icons.Outlined.Scale,
                    imeAction = ImeAction.Next,
                    onImeAction = { focus.moveFocus(FocusDirection.Next) }
                )
            )

            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                min = 1,
                enabled = saleUnitEnabled,
                value = saleUnitQty.toString(),
                onValueChange = onSaleUnitQtyChange,
                leadingIcon = Icons.Outlined.SwapHorizontalCircle,
                leadingIconContentDescription = "单位换算",
                trailingIcon = {
                    val icon = when (selectedSaleVariant) {
                        SharedProductSaleVariant.BOX -> SharedIcons.Box
                        SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                        SharedProductSaleVariant.PACK -> SharedIcons.Package24
                    }
                    Icon(icon, icon.name)

                },
                labelText = "单位换算 (${stringResource(SharedRes.string.field_required)})",
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
        }

        Text(
            text = "价格",
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = SharedRowLayout.arrangement
        ) {
            MyOutlinedDoubleField(
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                value = priceIva ?: "0.00",
                onValueChange = {
                    onPriceChange(price, it)
                },
                leadingIcon = Icons.Outlined.PriceCheck,
                leadingIconContentDescription = "含税价格",
                labelText = "含税价格 (${stringResource(SharedRes.string.field_required)})",
                trailingIcon = {
                    Icon(Icons.Outlined.EuroSymbol, contentDescription = Icons.Outlined.EuroSymbol.name)
                },
                max = 20000000.0,
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Right) }
            )

            MyOutlinedDoubleField(
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                value = price ?: "0.00",
                onValueChange = {
                    onPriceChange(it, priceIva)
                },
                leadingIcon = Icons.Outlined.AttachMoney,
                leadingIconContentDescription = "价格 ",
                labelText = "未含税价格 (${stringResource(SharedRes.string.field_required)})",
                placeholderText = "请输入产品未含税价格",
                trailingIcon = {
                    Icon(Icons.Outlined.EuroSymbol, contentDescription = Icons.Outlined.EuroSymbol.name)
                },
                max = 10000000.0,
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Down) }
            )
        }

        Text(
            text = "库存信息",
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                min = 1,
                value = minOrderQty.toString(),
                onValueChange = onMinOrderQtyChange,
                leadingIcon = Icons.Outlined.LooksOne,
                leadingIconContentDescription = "最小起订量",
                labelText = "最小起订量 (${stringResource(SharedRes.string.field_required)})",
                placeholderText = "请输入最小起订量",
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                min = 0,
                value = availableStock.toString(),
                onValueChange = onAvailableStockChange,
                leadingIcon = Icons.Outlined.Inventory2,
                leadingIconContentDescription = "库存数量",
                labelText = "库存数量 (${stringResource(SharedRes.string.field_required)})",
                placeholderText = "请输入当前库存数量",
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
        }
        MyOutlinedIntegerField(
            min = 0,
            value = lowStockThreshold?.toString() ?: "",
            onValueChange = onLowStockThresholdChange,
            leadingIcon = Icons.Outlined.NotificationImportant,
            leadingIconContentDescription = "低库存提醒",
            trailingIcon = {
                if (lowStockThreshold != null) {
                    SharedCloseButton {
                        onLowStockThresholdChange(null)
                    }
                }
            },
            labelText = "低库存提醒 (${stringResource(SharedRes.string.field_optional)})",
            placeholderText = "请输入低库存提醒数量",
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Next) }
        )
    }
}


@Composable
fun ProductMetaFields(
    selectedCategory: ReducedCategoryResponse?,
    onSelectedCategoryChange: (ReducedCategoryResponse?) -> Unit,
    onSearchCategory: suspend (String?, Int, Int) -> List<ReducedCategoryResponse>,
    onRemoveCategory: () -> Unit,
    categoryError: StringResource?,
    productCode: String = "",
    productCodeError: StringResource? = null,
    onProductCodeChange: (String) -> Unit = {},
    productIva: String = "",
    onIvaChange: (String?) -> Unit = {},
    productStatus: SharedProductStatus = SharedProductStatus.ACTIVE,
    onProductStatusChange: (SharedProductStatus) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        SearchableSelectorRemote(
            config = RemoteSearchableSelectorConfig(
                label = "选择产品主类别 (${stringResource(SharedRes.string.field_required)})",
                error = categoryError.asString(),
                leadingIcon = Icons.Outlined.Category,
                selectedItem = selectedCategory,
                onSelectedItemChange = onSelectedCategoryChange,
                pageSize = 100,
                itemToString = {
                    "${it.name}${it.translationString?.let { str -> " • $str" }.orEmpty()}"
                },
                onSearch = onSearchCategory,
            )
        )

        BusinessSelectedInfoCard(
            visible = selectedCategory != null,
            title = stringResource(BusinessRes.string.parent_category_selected),
            description = selectedCategory?.name ?: "",
            onClear = onRemoveCategory,
            enabled = true
        )


        MyOutlinedTextField(
            value = productCode,
            onValueChange = onProductCodeChange,
            leadingIcon = SharedIcons.Barcode,
            leadingIconContentDescription = "产品编码",
            trailingIcon = {
                if (productCode.isBlank()) {
                    SharedScannerButton(onProductCodeChange)
                } else SharedCloseButton { onProductCodeChange("") }
            },
            labelText = "产品编码 (${stringResource(SharedRes.string.field_required)})",
            placeholderText = "请输入产品编码",
            error = productCodeError.asString(),
            keyBordType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Row(
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyOutlinedDoubleField(
                value = productIva,
                modifier = Modifier.weight(0.5f),
                modifierFillMaxWidth = false,
                onValueChange = {
                    onIvaChange(it)
                },
                leadingIcon = Icons.Outlined.Percent,
                leadingIconContentDescription = stringResource(SharedRes.string.tax_rate),
                labelText = "${stringResource(SharedRes.string.tax_rate)}->IVA(%) (${stringResource(SharedRes.string.field_required)})",
                error = null,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) }
            )

            Selector(
                items = SharedProductStatus.entries,
                itemToString = { it.name },
                selectedItem = productStatus,
                onItemSelected = {
                    it?.let { onProductStatusChange(it) }
                },
                config = SelectorConfig(
                    modifier = Modifier.weight(0.5f),
                    label = "产品状态 (${stringResource(SharedRes.string.field_required)})",
                    leadingIcon = SharedIcons.InProgress,
                    modifierFillMaxWidth = false,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTranslationTabs(
    translationTabs: SnapshotStateList<Pair<SharedProductTranslation, RichTextState>>,
    currentProductNameError: StringResource?,
    currentLanguageIndex: Int,
    changeLanguageIndex: (Int) -> Unit,
    upsertTranslation: (
        String,
        String,
        String?,
        String?
    ) -> Unit,
    removeTranslation: (String) -> Unit,
    showAddLanguageDialog: (Boolean) -> Unit,
    canAddTranslation: Boolean,
    isAuxiliaryOpen: Boolean,
    onToggleRichTextEditor: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val selectedLanguageIndex =
        currentLanguageIndex.coerceIn(minimumValue = 0, maximumValue = translationTabs.lastIndex)
    val currentTranslation = translationTabs[selectedLanguageIndex].first
    val currentDescription = translationTabs[selectedLanguageIndex].second

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        BusinessSelectedInfoCard(
            visible = true,
            description = stringResource(BusinessRes.string.translations_count, translationTabs.size),
            icon = Icons.Outlined.Info,
            enabled = false,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TranslationTabRow(
            translationTabs = translationTabs,
            selectedLanguageIndex = selectedLanguageIndex,
            onSelect = changeLanguageIndex,
            onRemove = removeTranslation,
            onAddClick = { showAddLanguageDialog(true) },
            canAdd = canAddTranslation
        )

        MyOutlinedTextField(
            value = currentTranslation.name,
            onValueChange = {
                upsertTranslation(
                    currentTranslation.langCode,
                    it,
                    currentTranslation.title,
                    currentTranslation.description
                )
            },
            leadingIcon = Icons.AutoMirrored.Outlined.Label,
            leadingIconContentDescription = "",
            labelText = "产品名称 (${stringResource(SharedRes.string.field_required)})",
            placeholderText = "请输入产品名称",
            imeAction = ImeAction.Next,
            error = currentProductNameError.asString(),
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        MyOutlinedTextField(
            value = currentTranslation.title ?: "",
            onValueChange = {
                upsertTranslation(
                    currentTranslation.langCode,
                    currentTranslation.name,
                    it,
                    currentTranslation.description
                )
            },
            leadingIcon = Icons.AutoMirrored.Outlined.Article,
            leadingIconContentDescription = "",
            labelText = "产品标题 (${stringResource(SharedRes.string.field_optional)})",
            placeholderText = "请输入产品名称，用于进行简短的介绍",
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        BusinessRichTextEditor(
            label = "产品详情 (${stringResource(SharedRes.string.field_optional)})",
            placeholder = "请输入详细的产品介绍",
            state = currentDescription,
            toolbarItems = {
                item {
                    RichTextStyleButton(
                        onClick = onToggleRichTextEditor,
                        icon = Icons.Outlined.Expand,
                        isSelected = isAuxiliaryOpen,
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationTabRow(
    translationTabs: SnapshotStateList<Pair<SharedProductTranslation, RichTextState>>,
    selectedLanguageIndex: Int,
    onSelect: (Int) -> Unit,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit,
    canAdd: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = SharedLazyGridLayout.arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedLanguageIndex,
            modifier = Modifier.weight(1f),
            containerColor = Color.Transparent,
        ) {
            translationTabs.forEachIndexed { index, lang ->
                val language = LanguageManager.SupportedLanguages.fromCode(lang.first.langCode)
                val content = "${language.displayName} (${language.code})"
                LeadingIconTab(
                    selected = selectedLanguageIndex == index,
                    onClick = {
                        onSelect(index)
                    },
                    icon = {
                        Icon(Icons.Outlined.Language, content, modifier = Modifier.size(20.dp))
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(content, style = MaterialTheme.typography.labelLarge)
                            if (index > 0) {
                                BusinessDeleteIconButton { onRemove(lang.first.langCode) }
                            }
                        }
                    }
                )
            }
        }
        OutlinedButton(
            onClick = onAddClick,
            enabled = canAdd,
        ) {
            Icon(Icons.Outlined.Add, stringResource(SharedRes.string.add))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(BusinessRes.string.add_language_translation))
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun ProductMediaUploader(
    mediaPicker: MediaPickerViewModel,
    modifier: Modifier = Modifier
) {
    val mediaList = mediaPicker.mediaItems
    val hasVideo by derivedStateOf { mediaPicker.videoCount > 0 }
    val canAddMore = mediaPicker.canAddMore
    val fileKitMode by derivedStateOf {
        FileKitMode.Multiple(mediaPicker.remainingSlots)
    }
    val filekitType by derivedStateOf {
        if (mediaPicker.imageCount >= 9) {
            return@derivedStateOf FileKitType.Video
        }
        if (hasVideo) {
            return@derivedStateOf FileKitType.Image
        }
        FileKitType.ImageAndVideo
    }

    val launcher = rememberFilePickerLauncher(
        type = filekitType,
        mode = fileKitMode,
    ) { files ->
        files?.let(mediaPicker::addLocalFiles)
    }

    // Reorderable State
    val gridState = rememberLazyGridState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        scrollThresholdPadding = PaddingValues(SharedLazyGridLayout.Padding)
    ) { from, to ->
        // 处理数据重排
        mediaPicker.reorder(from.index - 1, to.index - 1)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    val isDragging = reorderableState.isAnyItemDragging

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 80.dp), // 自适应网格
        verticalArrangement = SharedLazyGridLayout.arrangement,
        horizontalArrangement = SharedLazyGridLayout.arrangement,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 500.dp) // 给一个高度限制，允许内部滚动
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            val deleteButtonWeight by animateFloatAsState(targetValue = if (isDragging) 1f else 0f)
            AnimatedVisibility(visible = isDragging || canAddMore) {
                Row(
                    modifier = Modifier.padding(top = 10.dp).height(80.dp),
                    horizontalArrangement = SharedLazyGridLayout.arrangement
                ) {
                    if (canAddMore) {
                        MediaAddGridItem(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { launcher.launch() }
                        )
                    }

                    // 只有当权重足够大（说明正在显示或正在进行动画）时才渲染组件
                    if (deleteButtonWeight > 0f) {
                        MediaRemoveGridItem(
                            modifier = Modifier
                                .alpha(deleteButtonWeight.coerceIn(0f, 1f))
                                .weight(deleteButtonWeight)
                                .fillMaxHeight()
                                .onGloballyPositioned {
                                    mediaPicker.updateDeleteZone(it.boundsInWindow())
                                },
                            isHovering = mediaPicker.isHoveringDeleteZone
                        )
                    }
                }
            }
        }

        // 3. 渲染媒体列表
        itemsIndexed(mediaList, key = { _, item -> item.localId }) { index, item ->
            ReorderableItem(
                state = reorderableState,
                key = item.localId
            ) { isDragging ->
                MediaGridItem(
                    item = item,
                    index = index,
                    isDragging = isDragging,
                    modifier = Modifier
                        .onGloballyPositioned { layoutCoordinates ->
                            // 实时上报位置给 Manager
                            if (isDragging) {
                                mediaPicker.onDragMove(layoutCoordinates.boundsInWindow())
                            }
                        }
                        .draggableHandle(
                            onDragStarted = {
                                mediaPicker.onDragStart(item.localId)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                            onDragStopped = {
                                mediaPicker.onDragEnd()
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            }),
                    retry = {
                        mediaPicker.retryUpload(item.localId)
                    }
                ) {}
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {

        }
    }
}

@Composable
fun MediaGridItem(
    index: Int,
    isDragging: Boolean,
    item: UploadMediaItem,
    modifier: Modifier = Modifier,
    retry: () -> Unit,
    onClick: () -> Unit
) {
    val hazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.glass()
    ReorderableContentBox(
        modifier = modifier.aspectRatio(1f),
        index = index,
        isDragging = isDragging,
        onClick = onClick,
    ) {
        // ===== 媒体内容 =====
        when (
            item.type) {
            MediaType.IMAGE -> {
                SharedAsyncImage(
                    model = item.file,
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                    zoomable = false,
                    enableContextMenu = false
                )
            }

            MediaType.VIDEO -> {
                SharedVideoPlayer(
                    item.file,
                    Modifier.hazeSource(hazeState),
                    showProgressBar = false,
                    enableContextMenu = false
                )
            }

            MediaType.DOCUMENT -> Unit
        }

        // ===== 上传状态遮罩 =====
        if (item.uploadState != UploadState.Success && item.uploadState != UploadState.Idle) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {} // 阻止点击穿透
                    .hazeEffect(hazeState, hazeStyle) {
                        progressive = HazeProgressive.RadialGradient(
                            radiusIntensity = 0.6f
                        )
                    }
            ) {
                when (item.uploadState) {
                    UploadState.Uploading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                text = "上传中：${(item.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    UploadState.Failed -> {
                        TextButton(retry, Modifier.align(Alignment.Center)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = stringResource(SharedRes.string.status_error_content_description),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "上传失败\n点击重试",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun MediaAddGridItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add Media",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "上传",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun MediaRemoveGridItem(
    modifier: Modifier = Modifier,
    isHovering: Boolean = false
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovering) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent
    )
    val borderColor by animateColorAsState(
        targetValue = if (isHovering) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
    )
    val scale by animateFloatAsState(
        targetValue = if (isHovering) 1.038f else 1f,
    )
    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remove Media",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isHovering) "松手删除" else "拖动到此删除",
                style = MaterialTheme.typography.labelSmall,
                color = if (isHovering) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            )
        }
    }
}
