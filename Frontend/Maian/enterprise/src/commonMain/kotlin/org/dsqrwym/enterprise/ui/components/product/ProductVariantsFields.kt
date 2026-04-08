package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.add
import maian.shared.generated.resources.field_optional
import maian.shared.generated.resources.field_required
import org.dsqrwym.business.drawable.sharedicons.Barcode
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.row.BusinessLabelValueRow
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.ui.components.containers.ReorderableContentBox
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Box
import org.dsqrwym.shared.drawable.sharedicons.Package24
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.cards.StatusIcon
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedDoubleField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedIntegerField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductVariantsFields(
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
                VariantCard(
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
fun VariantCard(
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
                    ProductVariantFields(
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
fun ProductVariantFields(
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
