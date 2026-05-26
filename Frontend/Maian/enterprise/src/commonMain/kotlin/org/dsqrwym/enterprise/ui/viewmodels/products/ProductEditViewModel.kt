package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.stock_adjustment_cannot_be_negative
import maian.enterprise.generated.resources.stock_changed_refresh_adjustment
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.business.ui.media.model.UploadedProductFile
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.dto.ProductCategoryResponse
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.data.product.dto.ProductResponseForUpdate
import org.dsqrwym.enterprise.data.product.dto.ProductUpdateDto
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.shared.data.category.mapper.toDomain
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.getOrElse
import org.dsqrwym.shared.serialization.getValOrNull
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

@OptIn(FlowPreview::class)
class ProductEditViewModel(
    uploadRepository: SharedUploadRepository,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    snackbarViewModel: MySnackbarViewModel
) : BaseProductFormViewModel(
    uploadRepository,
    productRepository,
    categoryRepository,
    snackbarViewModel
) {

    var isLoading by mutableStateOf(true)
        private set

    // 当前编辑的产品 ID
    private var productId: String? = null

    var editFormUiState: UiState by mutableStateOf(UiState.Idle)
    val editButtonEnabled: Boolean by derivedStateOf {
        editFormUiState == UiState.Idle &&
                (mediaPicker.mediaPickerUiState == UiState.Success || mediaPicker.mediaPickerUiState == UiState.Idle)
                && productTranslationUiState == UiState.Loading
                && productMetaDataUiState == UiState.Loading
                && productVariantUiState == UiState.Loading
    }
    private var initialProduct: ProductResponseForUpdate? = null

    val initialVariantStocks: Map<String, Int>
        get() = initialProduct?.variant
            ?.mapNotNull { variant ->
                val id = variant.id.getValOrNull() ?: return@mapNotNull null
                id to variant.availableStock.getOrElse { 0 }
            }
            ?.toMap()
            .orEmpty()

    // 主分类是否改变
    private val isPrimaryCategoryChanged: Boolean
        get() {
            val initialPrimary = initialProduct?.categories?.find { it.isPrimary }
            return filterCategory?.id != initialPrimary?.id
        }

    private val isSubcategoriesChanged: Boolean
        get() {
            val initialIds = initialProduct?.categories
                ?.filter { !it.isPrimary }
                ?.map { it.id }
                ?.toSet()
                .orEmpty()
            val currentIds = productSubcategories.map { it.id }.toSet()
            return currentIds != initialIds
        }

    // 产品根字段 (name/title/description) 是否改变
    private val isNameChanged: Boolean
        get() {
            val initial = initialProduct ?: return true
            val current = translationTabs.first().first
            return current.name != initial.name
        }
    private val isTitleChanged: Boolean
        get() {
            val initial = initialProduct ?: return true
            val current = translationTabs.first().first
            return current.title != initial.title
        }
    private val isDescriptionChanged: Boolean
        get() {
            val initial = initialProduct ?: return true
            val current = translationTabs.first().second
            return current.toHtml() != initial.description
        }

    // 需要删除的语言代码（基于初始 translations 列表中被移除的项）
    private val translationsToDelete: List<String>
        get() {
            val initialCodes = initialProduct?.translations?.map { it.langCode } ?: emptyList()
            val currentCodes = translationTabs.drop(1).map { it.first.langCode }.toSet()
            return initialCodes.filter { it !in currentCodes }
        }

    // 其他翻译是否改变（排除第一个主语言后的翻译列表）
    private val isTranslationsChanged: Boolean
        get() {
            val initialTranslations = initialProduct?.translations ?: emptyList()
            // 当前 translations 列表：除了第一个（主语言）以外的翻译
            val currentOthers = translationTabs.drop(1).map { it.first }
            // 与初始的 translation 比较（忽略主语言占位，假设初始 translations 包含所有语言，但我们也需要排除主语言）
            // 简化：直接比较整个列表，但通常会保持同步更新
            return currentOthers.toSet() != initialTranslations.toSet()
        }

    // 文件是否改变
    private val isFilesChanged: Boolean
        get() {
            val initialFiles = initialProduct?.files?.map { it.copy(mimeType = null) } ?: emptyList()
            val currentFiles = buildFiles() // buildFiles 会忽略 mimetype
            return initialFiles != currentFiles
        }


    init {
        productVariants.firstOrNull()?.id?.getValOrNull()?.let { firstId ->
            variantSaleUnitQtyManuallyEdited[firstId] = false
        }
    }

    private fun buildToDeleteVariants(): List<String> {
        val currentVariantIds = productVariants
            .mapNotNull { it.id.getValOrNull() }
            .toSet()
        return initialProduct?.variant
            ?.mapNotNull { it.id.getValOrNull() }
            ?.filter { initialId -> initialId !in currentVariantIds }
            .orEmpty()
    }

    private fun buildCreateVariants(): List<ProductVariantDto> {
        val existingVariantsIds = initialProduct?.variant?.map { it.id }
            ?: return productVariants.map { it.copy(id = OptionalField.Undefined) }
        // 新增：没有 id 的变体
        return productVariants.filter { !existingVariantsIds.contains(it.id) }
            .map { it.copy(id = OptionalField.Undefined) }
    }

    private fun buildUpdateVariants(): List<ProductVariantDto> {
        return productVariants.mapNotNull { variant ->
            val existingVariant = initialProduct?.variant?.find { it.id.getValOrNull() != null && it.id == variant.id }
                ?: return@mapNotNull null
            if (existingVariant != variant) {
                return@mapNotNull existingVariant.copy(
                    typeSale = if (existingVariant.typeSale != variant.typeSale) variant.typeSale else OptionalField.Undefined,
                    status = if (existingVariant.status != variant.status) variant.status else OptionalField.Undefined,
                    sort = if (existingVariant.sort != variant.sort) variant.sort else OptionalField.Undefined,
                    productCode = if (existingVariant.productCode != variant.productCode) variant.productCode else OptionalField.Undefined,
                    price = if (existingVariant.price != variant.price) variant.price else OptionalField.Undefined,
                    priceIva = if (existingVariant.priceIva != variant.priceIva) variant.priceIva else OptionalField.Undefined,
                    availableStock = OptionalField.Undefined,
                    availableStockDelta = run {
                        val existingStock = existingVariant.availableStock.getOrElse { 0 }
                        val currentStock = variant.availableStock.getOrElse { existingStock }
                        val delta = currentStock - existingStock
                        if (delta != 0) OptionalField.Value(delta) else OptionalField.Undefined
                    },
                    lowStockThreshold = if (existingVariant.lowStockThreshold != variant.lowStockThreshold) variant.lowStockThreshold else OptionalField.Undefined,
                    saleUnitQty = if (existingVariant.saleUnitQty != variant.saleUnitQty) variant.saleUnitQty else OptionalField.Undefined,
                    minOrderQty = if (existingVariant.minOrderQty != variant.minOrderQty) variant.minOrderQty else OptionalField.Undefined
                )
            }
            return@mapNotNull null
        }
    }

    fun initWithProduct(productId: String) {
        resetForm()
        this@ProductEditViewModel.productId = productId
        isLoading = true
        viewModelScope.launch {
            when (val result = productRepository.getProductForUpdate(productId)) {
                is SharedResponseResult.Success -> {
                    result.data?.let { products ->
                        initialProduct = products // 保存快照
                        productCode = products.productCode
                        productIva = products.iva
                        productStatus = products.status
                        translationTabs[0] = translationTabs[0].copy(
                            first = SharedProductTranslation(
                                langCode = "",
                                name = products.name,
                                title = products.title,
                                description = products.description,
                            ),
                            second = RichTextState().setHtml(products.description ?: "")
                        )
                        translationTabs.addAll(products.translations.map {
                            Pair(it, RichTextState().setHtml(it.description ?: ""))
                        })
                        filterCategory = products.categories.find { it.isPrimary }?.toCategorySummary()
                        productSubcategories.addAll(
                            products.categories
                                .filter { !it.isPrimary }
                                .map { it.toCategorySummary() }
                        )
                        productVariants.addAll(products.variant)
                        mediaPicker.addUploadedProductFiles(
                            products.files.map { file ->
                                UploadedProductFile(
                                    fileId = file.fileId,
                                    sort = file.sort,
                                    url = ApiConfig.FilePath.productFile(products.id, file.fileId),
                                    mimeType = file.mimeType.getValOrNull()
                                )
                            }
                        )
                        validateForm()
                    }
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let {
                            mySnackbarViewModel.showError(it)
                        }
                    }
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }
            }
            isLoading = false
        }
    }

    fun editProduct() {
        if (!editButtonEnabled) return
        val id = productId ?: return
        // 重新校验，防止状态过期
        if (!validateForm()) return

        if (productVariants.any { it.availableStock.getOrElse { 0 } < 0 }) {
            viewModelScope.launch {
                mySnackbarViewModel.showError(getString(EnterpriseRes.string.stock_adjustment_cannot_be_negative))
            }
            return
        }

        viewModelScope.launch {
            editFormUiState = UiState.Loading
            val primaryTranslation = translationTabs.first().first.copy(
                description = translationTabs.first().second.toHtml()
            )

            val dto = ProductUpdateDto(
                name = if (isNameChanged) OptionalField.Value(primaryTranslation.name)
                else OptionalField.Undefined,
                title = if (isTitleChanged) {
                    primaryTranslation.title?.let { OptionalField.Value(it) }
                } else OptionalField.Undefined,
                description = if (isDescriptionChanged) {
                    primaryTranslation.description?.let { OptionalField.Value(it) }
                } else OptionalField.Undefined,
                iva = if (productIva != initialProduct?.iva) OptionalField.Value(productIva)
                else OptionalField.Undefined,
                productCode = if (productCode != initialProduct?.productCode) OptionalField.Value(productCode)
                else OptionalField.Undefined,
                status = if (productStatus != initialProduct?.status) OptionalField.Value(productStatus)
                else OptionalField.Undefined,
                primaryCategoryId = if (isPrimaryCategoryChanged) OptionalField.Value(filterCategory?.id ?: "")
                else OptionalField.Undefined,
                subCategoryIds = if (isSubcategoriesChanged) OptionalField.Value(productSubcategories.map { it.id })
                else OptionalField.Undefined,
                // 变体
                createVariants = buildCreateVariants().takeIf { it.isNotEmpty() }?.let { OptionalField.Value(it) }
                    ?: OptionalField.Undefined,
                updateVariants = buildUpdateVariants().takeIf { it.isNotEmpty() }?.let { OptionalField.Value(it) }
                    ?: OptionalField.Undefined,
                variantsToDelete = buildToDeleteVariants().takeIf { it.isNotEmpty() }?.let { OptionalField.Value(it) }
                    ?: OptionalField.Undefined,
                // 翻译
                translations = if (isTranslationsChanged) {
                    OptionalField.Value(translationTabs.drop(1).map { it.first })
                } else OptionalField.Undefined,
                translationsToDelete = translationsToDelete.takeIf { it.isNotEmpty() }?.let { OptionalField.Value(it) }
                    ?: OptionalField.Undefined,
                // 文件
                files = if (isFilesChanged) {
                    OptionalField.Value(buildFiles()) // 空列表表示清空，null 不行，这里用 Value
                } else OptionalField.Undefined
            )


            when (val result = productRepository.updateProduct(id, dto)) {
                is SharedResponseResult.Success -> {
                    editFormUiState = UiState.Success
                    val message = getString(SharedRes.string.update_success)
                    mySnackbarViewModel.showSuccess(
                        message = message
                    )
                    loadProduct(id, navigateOnError = false)
                }

                is SharedResponseResult.Error -> {
                    editFormUiState = UiState.Error
                    if (result.message?.trim() == "STOCK_CANNOT_BE_NEGATIVE") {
                        mySnackbarViewModel.showError(getString(EnterpriseRes.string.stock_changed_refresh_adjustment))
                        loadProduct(id, navigateOnError = false)
                    } else if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.update_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            delay(SharedUiTiming.formStateResetDelay)
            editFormUiState = UiState.Idle
        }
    }

    private suspend fun loadProduct(productId: String, navigateOnError: Boolean) {
        isLoading = true
        when (val result = productRepository.getProductForUpdate(productId)) {
            is SharedResponseResult.Success -> {
                result.data?.let { products ->
                    applyProduct(products)
                }
            }

            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let {
                        mySnackbarViewModel.showError(it)
                    }
                }
                if (navigateOnError) {
                    emitNavigation(NavigationEvent.ToRoute(Categories))
                }
            }
        }
        isLoading = false
    }

    private fun applyProduct(products: ProductResponseForUpdate) {
        resetForm()
        initialProduct = products
        productCode = products.productCode
        productIva = products.iva
        productStatus = products.status
        translationTabs[0] = translationTabs[0].copy(
            first = SharedProductTranslation(
                langCode = "",
                name = products.name,
                title = products.title,
                description = products.description,
            ),
            second = RichTextState().setHtml(products.description ?: "")
        )
        translationTabs.addAll(products.translations.map {
            Pair(it, RichTextState().setHtml(it.description ?: ""))
        })
        filterCategory = products.categories.find { it.isPrimary }?.toCategorySummary()
        productSubcategories.addAll(
            products.categories
                .filter { !it.isPrimary }
                .map { it.toCategorySummary() }
        )
        productVariants.addAll(products.variant)
        mediaPicker.addUploadedProductFiles(
            products.files.map { file ->
                UploadedProductFile(
                    fileId = file.fileId,
                    sort = file.sort,
                    url = ApiConfig.FilePath.productFile(products.id, file.fileId),
                    mimeType = file.mimeType.getValOrNull()
                )
            }
        )
        validateForm()
    }
}

private fun ProductCategoryResponse.toCategorySummary(): CategorySummary =
    CategorySummary(
        id = id,
        name = name,
        iva = iva,
        translations = translation.map { it.toDomain() }
    )
