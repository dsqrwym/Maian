package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create
import org.dsqrwym.business.ui.workspace.BusinessAuxiliaryHost
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface
import org.dsqrwym.business.ui.workspace.rememberBusinessAuxiliaryWorkspaceState
import org.dsqrwym.enterprise.ui.components.product.ProductMediaUploader
import org.dsqrwym.enterprise.ui.components.product.ProductMetaFields
import org.dsqrwym.enterprise.ui.components.product.ProductTranslationTabs
import org.dsqrwym.enterprise.ui.components.product.ProductVariantsFields
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductCreateViewModel
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
                    ProductVariantsFields(
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

