package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.translation_deleted
import maian.shared.generated.resources.undo
import maian.shared.generated.resources.variant_deleted
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.data.product.dto.ProductFileDto
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.ui.viewmodels.categories.BaseCategoryFilterViewmodel
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.getOrElse
import org.dsqrwym.shared.serialization.getValOrNull
import org.dsqrwym.shared.serialization.toOptionalField
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.dsqrwym.shared.util.validation.sanitizeProductCode
import org.dsqrwym.shared.util.validation.sanitizeProductPricesInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

abstract class BaseProductFormViewModel(
    private val uploadRepository: SharedUploadRepository,
    protected val productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    protected val snackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    snackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    companion object {
        const val MAX_PRODUCT_SUBCATEGORIES = 10
    }

    // 产品媒体
    val mediaPicker = MediaPickerViewModel(
        uploadRepository = uploadRepository,
        coroutineScope = viewModelScope,
        snackbarViewModel = snackbarViewModel
    )

    val maxProductSubcategories: Int = MAX_PRODUCT_SUBCATEGORIES

    var productTranslationUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

    var productVariantUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

    var productMetaDataUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

    // 产品信息 (多语言)
    var selectedTranslationIndex by mutableIntStateOf(0)
        private set

    var translationTabs = mutableStateListOf(
        SharedProductTranslation("", "", "") to RichTextState()
    )
        protected set

    var selectedTranslationNameError: StringResource? by mutableStateOf(null)
        protected set

    var showAddLanguageDialog by mutableStateOf(false)
        private set

    // 产品基础属性
    var productIva by mutableStateOf("21.00")
        protected set

    var productCategoryError: StringResource? by mutableStateOf(null)
        protected set

    // 用户输入则使用用户输入否则根据选择类别
    var isIvaManuallyEdited = false
        protected set

    var productCode by mutableStateOf("")
        protected set

    var productCodeError: StringResource? by mutableStateOf(null)
        protected set

    var productStatus by mutableStateOf(SharedProductStatus.ACTIVE)
        protected set

    var productSubcategories = mutableStateListOf<CategorySummary>()
        protected set

    var productVariants = mutableStateListOf<ProductVariantDto>()
        protected set

    var productVariantsProductCodesErrors = mutableStateMapOf<String, StringResource?>()
        protected set

    // 记录用户是否手动编辑过某个 SKU 的 saleUnitQty
    protected val variantSaleUnitQtyManuallyEdited = mutableStateMapOf<String, Boolean>()

    val canAddTranslation by derivedStateOf {
        getAvailableLanguages().size > 1 &&
                productTranslationUiState == UiState.Loading
    }

    val canAddSku by derivedStateOf {
        productVariants.size < 50 &&
                productVariantsProductCodesErrors.values.none { it != null }
    }

    val canAddSubcategory by derivedStateOf {
        productSubcategories.size < MAX_PRODUCT_SUBCATEGORIES
    }

    protected fun checkProductMeta() {
        productMetaDataUiState = if (productCode.isNotBlank() && filterCategory != null) {
            UiState.Loading
        } else {
            UiState.Error
        }
    }

    protected fun checkProductTranslation() {
        productTranslationUiState = if (translationTabs.any { it.first.name.isBlank() }) {
            UiState.Error
        } else {
            UiState.Loading
        }

        selectedTranslationNameError =
            if (
                translationTabs.isNotEmpty() &&
                translationTabs[selectedTranslationIndex.coerceIn(0, translationTabs.lastIndex)]
                    .first.name.isBlank()
            ) {
                SharedRes.string.field_cannot_be_empty
            } else {
                null
            }
    }

    protected fun checkProductVariant() {
        var uiState = UiState.Loading

        val existingIds = productVariants.mapNotNull { it.id.getValOrNull() }.toSet()
        productVariantsProductCodesErrors.keys.retainAll { it in existingIds }

        productVariants.forEach { variant ->
            val id = variant.id.getValOrNull()

            if (variant.productCode.getOrElse { "" }.isBlank()) {
                uiState = UiState.Error
                id?.let {
                    productVariantsProductCodesErrors[it] = SharedRes.string.field_cannot_be_empty
                }
            } else {
                id?.let {
                    productVariantsProductCodesErrors[it] = null
                }
            }
        }

        productVariantUiState = uiState
    }

    // 根据销售单位返回推荐的 saleUnitQty
    protected fun getRecommendedSaleUnitQty(typeSale: SharedProductSaleVariant): Int {
        return when (typeSale) {
            SharedProductSaleVariant.UNIT -> 1
            SharedProductSaleVariant.PACK -> 12
            SharedProductSaleVariant.BOX -> 24
        }
    }

    fun changeLanguageIndex(languageIndex: Int) {
        selectedTranslationIndex = languageIndex
    }

    fun showAddLanguageDialog(value: Boolean) {
        showAddLanguageDialog = value
    }

    fun upsertTranslation(
        langCode: String,
        name: String,
        title: String?,
        description: String? = null
    ) {
        val index = translationTabs.indexOfFirst { it.first.langCode == langCode }

        if (index != -1) {
            translationTabs[index] = translationTabs[index].copy(
                first = SharedProductTranslation(
                    langCode = langCode,
                    name = name.take(50),
                    title = title?.take(100),
                    description = description
                )
            )
        } else {
            translationTabs.add(
                SharedProductTranslation(
                    langCode = langCode,
                    name = name.take(50),
                    title = title?.take(100),
                    description = description
                ) to RichTextState()
            )
            selectedTranslationIndex = translationTabs.lastIndex
        }

        checkProductTranslation()
    }

    fun removeTranslation(langCode: String) {
        val index = translationTabs.indexOfFirst { it.first.langCode == langCode }
        if (index == -1) return
        val item = translationTabs.removeAt(index)

        selectedTranslationIndex = selectedTranslationIndex.coerceIn(
            0,
            translationTabs.lastIndex.coerceAtLeast(0)
        )

        checkProductTranslation()

        viewModelScope.launch {
            val language = LanguageManager.SupportedLanguages.fromCode(item.first.langCode)
            snackbarViewModel.showUndo(
                message = "${getString(SharedRes.string.translation_deleted)} (${language.displayName})",
                actionLabel = getString(SharedRes.string.undo),
            ) {
                if (translationTabs.none { it.first.langCode == item.first.langCode }) {
                    translationTabs.add(index.coerceIn(0, translationTabs.size), item)
                    selectedTranslationIndex = index.coerceIn(0, translationTabs.lastIndex)
                    checkProductTranslation()
                }
            }
        }
    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { lang ->
                lang.code !in translationTabs.map { it.first.langCode }
            }
    }

    fun updateProductCode(code: String) {
        productCode = sanitizeProductCode(code)
        productCodeError = if (productCode.isBlank()) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }
        checkProductMeta()
    }

    fun updateProductIva(iva: String?) {
        val result = iva?.let { sanitizeIvaInput(it) } ?: return
        productIva = result
        isIvaManuallyEdited = iva.isNotBlank()

        calculatePrices()
    }

    protected fun calculatePrices() {
        productVariants.forEachIndexed { index, dto ->
            val (price, priceIva) = sanitizeProductPricesInput(
                priceIva = dto.priceIva.getValOrNull(),
                iva = productIva
            )

            productVariants[index] = dto.copy(
                price = price.toOptionalField(),
                priceIva = priceIva.toOptionalField()
            )
        }
    }

    fun updateProductStatus(state: SharedProductStatus) {
        productStatus = state
    }

    fun addProductSubcategory(category: CategorySummary) {
        if (category.id == filterCategory?.id) return
        if (productSubcategories.any { it.id == category.id }) return
        if (productSubcategories.size >= MAX_PRODUCT_SUBCATEGORIES) return
        productSubcategories.add(category)
    }

    fun removeProductSubcategory(categoryId: String) {
        productSubcategories.removeAll { it.id == categoryId }
    }

    fun upsertProductVariant(
        id: String?,
        typeSale: SharedProductSaleVariant,
        sort: Int = 0,
        price: String? = null,
        priceIva: String? = null,
        productCode: String = "",
        availableStock: Int,
        saleUnitQty: Int = 1,
        minOrderQty: Int = 1,
        lowStockThreshold: Int = 0,
        status: SharedProductStatus = SharedProductStatus.ACTIVE
    ) {
        val sanitizedProductCode = sanitizeProductCode(productCode)
        val index = productVariants.indexOfFirst { it.id.getValOrNull() == id }
        // 最少一个 variant 是开启的
        val hasStatusActives =
            productVariants.filter { it.status.getValOrNull() == SharedProductStatus.ACTIVE }

        if (index != -1) {
            val existingSku = productVariants[index]
            val effectiveIva = productIva.ifBlank { "0.00" }

            var finalPrice = price
            var finalPriceIva = priceIva

            finalPrice?.let {
                if (it != existingSku.price.getValOrNull()) {
                    sanitizeProductPricesInput(
                        price = it,
                        iva = effectiveIva
                    ).let { (price, priceIva) ->
                        finalPrice = price
                        finalPriceIva = priceIva
                    }
                }
            }

            finalPriceIva?.let {
                if (it != existingSku.priceIva.getValOrNull()) {
                    sanitizeProductPricesInput(
                        priceIva = it,
                        iva = effectiveIva
                    ).let { (price, priceIva) ->
                        finalPriceIva = priceIva
                        finalPrice = price
                    }
                }
            }

            var finalSaleUnitQty = saleUnitQty
            val variantId = existingSku.id.getValOrNull()
            val saleUnitQtyManuallyEdited =
                variantId?.let { variantSaleUnitQtyManuallyEdited[it] } == true

            if (saleUnitQty != existingSku.saleUnitQty.getValOrNull() && variantId != null) {
                variantSaleUnitQtyManuallyEdited[variantId] = true
            }

            if (!saleUnitQtyManuallyEdited) {
                finalSaleUnitQty = getRecommendedSaleUnitQty(typeSale)
            }

            productVariants[index] = existingSku.copy(
                typeSale = OptionalField.Value(typeSale),
                sort = sort.toOptionalField(),
                price = finalPrice.toOptionalField(),
                priceIva = finalPriceIva.toOptionalField(),
                productCode = sanitizedProductCode.toOptionalField(),
                availableStock = availableStock.toOptionalField(),
                saleUnitQty = finalSaleUnitQty.toOptionalField(),
                minOrderQty = minOrderQty.toOptionalField(),
                lowStockThreshold = lowStockThreshold.toOptionalField(),
                status = OptionalField.Value(if (hasStatusActives.size > 1) status else SharedProductStatus.ACTIVE)
            )
        } else {
            productVariants.add(
                ProductVariantDto(
                    id = id.toOptionalField(),
                    typeSale = OptionalField.Value(typeSale),
                    sort = sort.toOptionalField(),
                    price = price.toOptionalField(),
                    priceIva = priceIva.toOptionalField(),
                    productCode = sanitizedProductCode.toOptionalField(),
                    availableStock = availableStock.toOptionalField(),
                    saleUnitQty = getRecommendedSaleUnitQty(typeSale).toOptionalField(),
                    minOrderQty = minOrderQty.toOptionalField(),
                    lowStockThreshold = lowStockThreshold.toOptionalField(),
                    status = OptionalField.Value(if (hasStatusActives.isNotEmpty()) status else SharedProductStatus.ACTIVE)
                )
            )
        }

        checkProductVariant()
    }

    fun deleteVariant(skuId: String?) {
        val index = productVariants.indexOfFirst { it.id.getValOrNull() == skuId }
        if (index == -1) return
        val variantCode = productVariants[index].productCode.getOrElse { "" }
        val previousVariants = productVariants.toList()
        val previousErrors = productVariantsProductCodesErrors.toMap()
        val previousSaleUnitQtyEdited = variantSaleUnitQtyManuallyEdited.toMap()
        // 最少一个变体
        if (productVariants.size < 2) return
        productVariants.removeAt(index)
        skuId?.let {
            productVariantsProductCodesErrors.remove(it)
            variantSaleUnitQtyManuallyEdited.remove(it)
        }
        // 最少一个开启，之前的判断保证最少一个
        if (productVariants.size < 2) {
            productVariants[0] =
                productVariants[0].copy(status = OptionalField.Value(SharedProductStatus.ACTIVE))
        }
        checkProductVariant()

        viewModelScope.launch {
            val message = getString(SharedRes.string.variant_deleted).let {
                if (variantCode.isBlank()) it else "$it ($variantCode)"
            }
            snackbarViewModel.showUndo(
                message = message,
                actionLabel = getString(SharedRes.string.undo),
            ) {
                productVariants.clear()
                productVariants.addAll(previousVariants)
                productVariantsProductCodesErrors.clear()
                productVariantsProductCodesErrors.putAll(previousErrors)
                variantSaleUnitQtyManuallyEdited.clear()
                variantSaleUnitQtyManuallyEdited.putAll(previousSaleUnitQtyEdited)
                checkProductVariant()
            }
        }
    }

    fun reorder(from: Int, to: Int) {
        if (from == to) return
        if (from !in productVariants.indices || to !in productVariants.indices) return
        productVariants.add(to, productVariants.removeAt(from))
        
        // 更新所有variant的sort值以反映新的顺序
        productVariants.forEachIndexed { index, variant ->
            productVariants[index] = variant.copy(sort = index.toOptionalField())
        }
    }

    override suspend fun findCategories(
        query: String?, page: Int, limit: Int,
        excludedIds: List<String>?
    ): List<CategorySummary> {
        when (val result = categoryRepository.getCategoriesByLevel(
            query,
            page,
            limit,
            true,
            excludedIds = excludedIds
        )) {
            is SharedResponseResult.Success -> {
                return result.data?.items ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
            }
        }
        return emptyList()
    }

    suspend fun findProductSubcategories(
        query: String?,
        page: Int,
        limit: Int
    ): List<CategorySummary> {
        val primaryCategoryId = filterCategory?.id
        val selectedCategoryIds = productSubcategories.map { it.id }.toSet()
        return findCategories(
            query,
            page,
            limit,
            excludedIds = buildSet {
                primaryCategoryId?.let(::add)
                addAll(selectedCategoryIds)
            }.toList()
        )
    }

    override fun updateFilterCategory(category: CategorySummary?) {
        super.updateFilterCategory(category)
        category?.id?.let { primaryCategoryId ->
            productSubcategories.removeAll { it.id == primaryCategoryId }
        }
        productCategoryError = if (category == null) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }
        checkProductMeta()
        val iva = category?.iva ?: return

        // 没有被用户改过自动填充
        if (!isIvaManuallyEdited) {
            productIva = iva
            calculatePrices()
        }
    }

    override fun removeFilterCategory() {
        super.removeFilterCategory()
        checkProductMeta()
    }

    protected fun buildFiles(): List<ProductFileDto> {
        return mediaPicker.mediaItems.mapIndexedNotNull { index, item ->
            item.serverId?.let { serverId ->
                ProductFileDto(
                    fileId = serverId,
                    sort = index
                )
            }
        }
    }

    protected fun validateForm(): Boolean {
        checkProductMeta()
        checkProductVariant()
        checkProductTranslation()

        return productTranslationUiState == UiState.Loading &&
                productMetaDataUiState == UiState.Loading &&
                productVariantUiState == UiState.Loading &&
                productSubcategories.size <= MAX_PRODUCT_SUBCATEGORIES &&
                (
                        mediaPicker.mediaPickerUiState == UiState.Success ||
                                mediaPicker.mediaPickerUiState == UiState.Idle
                        )
    }

    protected fun resetForm() {
        productCode = ""
        productCodeError = null
        productIva = "21.00"
        productCategoryError = null
        productStatus = SharedProductStatus.ACTIVE
        productSubcategories.clear()
        productMetaDataUiState = UiState.Idle
        selectedTranslationIndex = 0
        selectedTranslationNameError = null
        productTranslationUiState = UiState.Idle
        productVariantUiState = UiState.Idle
        translationTabs.clear()
        translationTabs.add(SharedProductTranslation("", "", "") to RichTextState())
        productVariants.clear()
        productVariantsProductCodesErrors.clear()
        variantSaleUnitQtyManuallyEdited.clear()
        mediaPicker.clear()
        isIvaManuallyEdited = false
        showAddLanguageDialog(false)
        removeFilterCategory()
    }
}
