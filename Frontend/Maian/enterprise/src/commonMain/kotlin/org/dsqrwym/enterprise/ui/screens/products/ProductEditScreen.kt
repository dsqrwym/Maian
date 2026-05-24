package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update
import org.dsqrwym.business.ui.components.row.BusinessTitleIconRow
import org.dsqrwym.business.ui.workspace.BusinessAuxiliaryHost
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface
import org.dsqrwym.business.ui.workspace.rememberBusinessAuxiliaryWorkspaceState
import org.dsqrwym.enterprise.data.product.mapper.toDomain
import org.dsqrwym.enterprise.ui.components.product.ProductMediaUploader
import org.dsqrwym.enterprise.ui.components.product.ProductMetaFields
import org.dsqrwym.enterprise.ui.components.product.ProductSubcategorySelector
import org.dsqrwym.enterprise.ui.components.product.ProductTranslationTabs
import org.dsqrwym.enterprise.ui.components.product.ProductVariantsFields
import org.dsqrwym.enterprise.ui.screens.categories.AddLanguageDialog
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductEditViewModel
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    id: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ProductEditViewModel = koinViewModel()
) {
    val workspaceState = rememberBusinessAuxiliaryWorkspaceState()
    LaunchedEffect(id) {
        viewModel.initWithProduct(id)
        viewModel.navigateEvent.collect {
            onNavigateBack()
        }
    }
    DisposableEffect(Unit) {
        onDispose { workspaceState.close() }
    }

    BusinessAuxiliaryHost(
        workspaceState = workspaceState,
        mainContent = {
            ProductEditScreenContent(
                onNavigateBack = onNavigateBack,
                viewModel = viewModel,
                isAuxiliaryOpen = workspaceState.isOpen,
                onToggleRichTextEditor = {
                    workspaceState.toggle(BusinessAuxiliarySurface.Editor)
                },
                onTogglePreview = {
                    workspaceState.toggle(BusinessAuxiliarySurface.Preview)
                },
            )
        },
        auxiliaryContent = { surface ->
            ProductAuxiliaryPane(
                viewModel = viewModel,
                surface = surface,
                onClose = workspaceState::close,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun ProductEditScreenContent(
    onNavigateBack: () -> Unit,
    viewModel: ProductEditViewModel,
    isAuxiliaryOpen: Boolean,
    onToggleRichTextEditor: () -> Unit,
    onTogglePreview: () -> Unit,
) {
    val isLoading = viewModel.isLoading
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
    val subcategories = viewModel.productSubcategories

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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BusinessTitleIconRow(
                    if (translationTabs.isNotEmpty()) translationTabs[0].first.name else "",
                    Icons.Outlined.Inventory2,
                    stringResource(EnterpriseRes.string.product),
                    isLoading
                )

                IconButton(onClick = onTogglePreview) {
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = stringResource(EnterpriseRes.string.product_preview),
                    )
                }
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            viewModel.editFormUiState,
            viewModel.editButtonEnabled,
            viewModel::editProduct,
            stringResource(SharedRes.string.update),
            Icons.Outlined.Add,
            stringResource(SharedRes.string.update)
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
                    modifier = Modifier.animateItem(),
                    title = stringResource(EnterpriseRes.string.product_form_media_title),
                    subtitle = stringResource(EnterpriseRes.string.product_form_media_subtitle_mb, maxSize),
                    uiState = mediaPicker.mediaPickerUiState
                ) {
                    ProductMediaUploader(mediaPicker, modifier = Modifier.placeholderWithShimmer(isLoading))
                }
            }
            item {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(EnterpriseRes.string.product_form_translations_title),
                    subtitle = stringResource(EnterpriseRes.string.product_form_translations_subtitle),
                    uiState = viewModel.productTranslationUiState
                ) {
                    ProductTranslationTabs(
                        isLoading = isLoading,
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
                    modifier = Modifier.animateItem(),
                    title = stringResource(EnterpriseRes.string.product_form_attributes_title),
                    subtitle = stringResource(EnterpriseRes.string.product_form_attributes_subtitle),
                    uiState = viewModel.productMetaDataUiState
                ) {
                    ProductMetaFields(
                        isLoading = isLoading,
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
                    ProductSubcategorySelector(
                        isLoading = isLoading,
                        selectedCategories = subcategories,
                        canAddCategory = viewModel.canAddSubcategory,
                        maxCategories = viewModel.maxProductSubcategories,
                        onAddCategory = viewModel::addProductSubcategory,
                        onRemoveCategory = viewModel::removeProductSubcategory,
                        onSearchCategory = viewModel::findProductSubcategories,
                    )
                }
            }

            item(span = if (skuTabs.size > 1) StaggeredGridItemSpan.FullLine else null) {
                FormCard(
                    modifier = Modifier.animateItem(),
                    title = stringResource(EnterpriseRes.string.product_form_sku_title),
                    subtitle = stringResource(EnterpriseRes.string.product_form_sku_subtitle),
                    uiState = viewModel.productVariantUiState
                ) {
                    ProductVariantsFields(
                        isLoading = isLoading,
                        variants = skuTabs.map { it.toDomain() },
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
                                status = it.status,
                            )
                        },
                        productVariantsProductCodesErrors = productVariantsProductCodesErrors,
                    )
                }
            }
        }
    }
}

