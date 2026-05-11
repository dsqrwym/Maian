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
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.business.drawable.sharedicons.Barcode
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.category.BusinessSelectedInfoCard
import org.dsqrwym.business.ui.components.row.BusinessLabelValueRow
import org.dsqrwym.enterprise.domain.product.ProductVariant
import org.dsqrwym.enterprise.ui.components.containers.ReorderableContentBox
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.data.products.toStringResource
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Box
import org.dsqrwym.shared.drawable.sharedicons.InProgress
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
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ProductVariantsFields(
    variants: List<ProductVariant>,
    productVariantsProductCodesErrors: Map<String, StringResource?>,
    isLoading: Boolean = false,
    onReorder: (Int, Int) -> Unit,
    onUpdate: (ProductVariant) -> Unit,
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
    val currentSKUId by remember { mutableStateOf(variants.firstOrNull()?.id) }
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
                modifier = Modifier.animateItem().fillMaxWidth(),
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                BusinessSelectedInfoCard(
                    modifier = Modifier.weight(1f, false).widthIn(max = 336.dp).placeholderWithShimmer(isLoading),
                    visible = variants.isNotEmpty(),
                    description = stringResource(EnterpriseRes.string.products_added_variants_count, variants.size),
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
                    Text(stringResource(EnterpriseRes.string.add_product_variant))
                }
            }
        }
        itemsIndexed(variants, key = { _, item -> item.id ?: Uuid.generateV4() }) { index, item ->
            ReorderableItem(
                modifier = Modifier.animateItem(),
                state = reorderableState,
                key = item.id ?: "",
            ) { isSelfDragging ->
                VariantCard(
                    isLoading = isLoading,
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
                    canDelete = variants.size > 1,
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
    item: ProductVariant,
    isLoading: Boolean = false,
    isAnyItemDragging: Boolean = false,
    isSelfDragging: Boolean = false,
    canDelete: Boolean = true,
    currentSKUid: String? = null,
    productCodeError: StringResource?,
    onUpdate: (ProductVariant) -> Unit,
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
                                    item.typeSale.displayName() + ": ${item.saleUnitQty}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                val icon = when (item.typeSale) {
                                    SharedProductSaleVariant.BOX -> SharedIcons.Box
                                    SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                                    SharedProductSaleVariant.PACK -> SharedIcons.Package24
                                }
                                Icon(icon, item.typeSale.displayName())
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
                        BusinessLabelValueRow(
                            stringResource(SharedRes.string.product_price_with_vat) + ": ",
                            "${item.priceIva ?: 0}"
                        )
                        BusinessLabelValueRow(
                            stringResource(SharedRes.string.product_price) + ": ",
                            "${item.price ?: 0}"
                        )
                        BusinessLabelValueRow(
                            stringResource(SharedRes.string.product_min_order_qty) + ": ",
                            "${item.minOrderQty}"
                        )
                        BusinessLabelValueRow(
                            stringResource(EnterpriseRes.string.available_stock) + ": ",
                            "${item.availableStock}"
                        )
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
                        isLoading = isLoading,
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
                        productCodeError = productCodeError,
                        status = item.status,
                        onStatusChange = { onUpdate(item.copy(status = it)) },
                        statusEnabled = index != 0
                    )
                }
            }
        }
    }
}

@Composable
fun ProductVariantFields(
    isLoading: Boolean = false,
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
    lowStockThreshold: Int,
    onLowStockThresholdChange: (Int) -> Unit,
    statusEnabled: Boolean = true,
    status: SharedProductStatus = SharedProductStatus.ACTIVE,
    onStatusChange: (SharedProductStatus) -> Unit = {},
) {
    val focus = LocalFocusManager.current
    val saleVariantLabels = SharedProductSaleVariant.entries.associateWith { stringResource(it.toStringResource()) }
    val productStatusLabels = SharedProductStatus.entries.associateWith { stringResource(it.toStringResource()) }
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
            text = stringResource(EnterpriseRes.string.sales_information),
            style = MaterialTheme.typography.titleSmall,
        )

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            value = productCode,
            onValueChange = onProductCodeChange,
            leadingIcon = SharedIcons.Barcode,
            leadingIconContentDescription = stringResource(SharedRes.string.product_code),
            trailingIcon = {
                if (productCode.isBlank()) {
                    SharedScannerButton(onProductCodeChange)
                } else SharedCloseButton { onProductCodeChange("") }
            },
            labelText = "${stringResource(SharedRes.string.product_code)} (${stringResource(SharedRes.string.field_required)})",
            placeholderText = stringResource(EnterpriseRes.string.enter_product_code),
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
                itemToString = { saleVariantLabels[it].orEmpty() },
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
                    modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                    modifierFillMaxWidth = false,
                    label = "${stringResource(EnterpriseRes.string.sale_unit)} (${stringResource(SharedRes.string.field_required)})",
                    leadingIcon = Icons.Outlined.Scale,
                    imeAction = ImeAction.Next,
                    onImeAction = { focus.moveFocus(FocusDirection.Next) }
                )
            )

            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                min = 1,
                enabled = saleUnitEnabled,
                value = saleUnitQty.toString(),
                onValueChange = onSaleUnitQtyChange,
                leadingIcon = Icons.Outlined.SwapHorizontalCircle,
                leadingIconContentDescription = stringResource(EnterpriseRes.string.sale_unit_conversion),
                trailingIcon = {
                    val icon = when (selectedSaleVariant) {
                        SharedProductSaleVariant.BOX -> SharedIcons.Box
                        SharedProductSaleVariant.UNIT -> Icons.Outlined._1xMobiledata
                        SharedProductSaleVariant.PACK -> SharedIcons.Package24
                    }
                    Icon(icon, icon.name)

                },
                labelText = "${stringResource(EnterpriseRes.string.sale_unit_conversion)} (${stringResource(SharedRes.string.field_required)})",
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
        }

        Text(
            text = stringResource(EnterpriseRes.string.price_information),
            style = MaterialTheme.typography.titleSmall,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = SharedRowLayout.arrangement
        ) {
            MyOutlinedDoubleField(
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                value = priceIva ?: "0.00",
                onValueChange = {
                    onPriceChange(price, it)
                },
                leadingIcon = Icons.Outlined.PriceCheck,
                leadingIconContentDescription = stringResource(SharedRes.string.product_price_with_vat),
                labelText = "${stringResource(SharedRes.string.product_price_with_vat)} (${stringResource(SharedRes.string.field_required)})",
                trailingIcon = {
                    Icon(Icons.Outlined.EuroSymbol, contentDescription = Icons.Outlined.EuroSymbol.name)
                },
                max = 20000000.0,
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Right) }
            )

            MyOutlinedDoubleField(
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                value = price ?: "0.00",
                onValueChange = {
                    onPriceChange(it, priceIva)
                },
                leadingIcon = Icons.Outlined.AttachMoney,
                leadingIconContentDescription = stringResource(SharedRes.string.product_price),
                labelText = "${stringResource(SharedRes.string.product_price_without_vat)} (${stringResource(SharedRes.string.field_required)})",
                placeholderText = stringResource(SharedRes.string.product_price_without_vat),
                trailingIcon = {
                    Icon(Icons.Outlined.EuroSymbol, contentDescription = Icons.Outlined.EuroSymbol.name)
                },
                max = 10000000.0,
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Down) }
            )
        }

        Text(
            text = stringResource(EnterpriseRes.string.inventory_information),
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SharedRowLayout.arrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                min = 1,
                value = minOrderQty.toString(),
                onValueChange = onMinOrderQtyChange,
                leadingIcon = Icons.Outlined.LooksOne,
                leadingIconContentDescription = stringResource(SharedRes.string.product_min_order_qty),
                labelText = "${stringResource(SharedRes.string.product_min_order_qty)} (${stringResource(SharedRes.string.field_required)})",
                placeholderText = stringResource(SharedRes.string.product_min_order_qty),
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
            MyOutlinedIntegerField(
                modifier = Modifier.weight(0.5f).placeholderWithShimmer(isLoading),
                modifierFillMaxWidth = false,
                min = 0,
                value = availableStock.toString(),
                onValueChange = onAvailableStockChange,
                leadingIcon = Icons.Outlined.Inventory2,
                leadingIconContentDescription = stringResource(EnterpriseRes.string.available_stock),
                labelText = "${stringResource(EnterpriseRes.string.available_stock)} (${stringResource(SharedRes.string.field_required)})",
                placeholderText = stringResource(EnterpriseRes.string.enter_available_stock),
                imeAction = ImeAction.Next,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
        }
        MyOutlinedIntegerField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            min = 0,
            value = if (lowStockThreshold == 0) "" else lowStockThreshold.toString(),
            onValueChange = { it?.let { onLowStockThresholdChange(it) } },
            leadingIcon = Icons.Outlined.NotificationImportant,
            leadingIconContentDescription = stringResource(EnterpriseRes.string.low_stock_threshold),
            trailingIcon = {
                if (lowStockThreshold != 0) {
                    SharedCloseButton {
                        onLowStockThresholdChange(0)
                    }
                }
            },
            labelText = "${stringResource(EnterpriseRes.string.low_stock_threshold)} (${stringResource(SharedRes.string.field_optional)})",
            placeholderText = stringResource(EnterpriseRes.string.enter_low_stock_threshold),
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Next) }
        )

        Selector(
            items = SharedProductStatus.entries,
            itemToString = { productStatusLabels[it].orEmpty() },
            selectedItem = status,
            onItemSelected = {
                it?.let { onStatusChange(it) }
            },
            config = SelectorConfig(
                enabled = statusEnabled,
                modifier = Modifier.placeholderWithShimmer(isLoading),
                label = "${stringResource(SharedRes.string.status)} (${stringResource(SharedRes.string.field_required)})",
                leadingIcon = SharedIcons.InProgress,
                onImeAction = { focus.moveFocus(FocusDirection.Next) }
            )
        )
    }
}



