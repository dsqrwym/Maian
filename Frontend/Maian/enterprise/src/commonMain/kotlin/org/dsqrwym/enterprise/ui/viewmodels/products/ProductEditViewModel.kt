package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.create_failed
import maian.shared.generated.resources.create_success
import maian.shared.generated.resources.field_cannot_be_empty
import org.dsqrwym.business.ui.media.MediaPickerViewModel
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.data.product.dto.ProductFileDto
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.ui.viewmodels.categories.BaseCategoryFilterViewmodel
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.file.SharedUploadRepository
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.sanitizeIvaInput
import org.dsqrwym.shared.util.validation.sanitizeProductCode
import org.dsqrwym.shared.util.validation.sanitizeProductPricesInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(FlowPreview::class)
class ProductCreateViewModel(
    private val uploadRepository: SharedUploadRepository,
    private val productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    private val snackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    snackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {
    val createFormUiState: UiState by mutableStateOf(UiState.Idle)
    val createButtonEnabled: Boolean by derivedStateOf {
        (mediaPicker.mediaPickerUiState == UiState.Success || mediaPicker.mediaPickerUiState == UiState.Idle)
                && productTranslationUiState == UiState.Loading
                && productMetaDataUiState == UiState.Loading
                && productVariantUiState == UiState.Loading
    }
    var productTranslationUiState: UiState by mutableStateOf(UiState.Idle)
        private set
    var productVariantUiState: UiState by mutableStateOf(UiState.Idle)
        private set
    var productMetaDataUiState: UiState by mutableStateOf(UiState.Idle)
        private set

    private fun checkProductMeta() {
        productMetaDataUiState = if (productCode.isNotBlank() && filterCategory != null) {
            UiState.Loading
        } else {
            UiState.Error
        }
    }

    private fun checkProductTranslation() {
        productTranslationUiState = if (translationTabs.any { it.first.name.isBlank() }) {
            UiState.Error
        } else {
            UiState.Loading
        }
        selectedTranslationNameError =
            if (translationTabs[selectedTranslationIndex.coerceIn(0, translationTabs.size)].first.name.isBlank()) {
                SharedRes.string.field_cannot_be_empty
            } else {
                null
            }
    }

    private fun checkProductVariant() {
        var uiState = UiState.Loading
        val existingIds = productVariants.mapNotNull { it.id }.toSet()
        productVariantsProductCodesErrors.keys.retainAll { it in existingIds }
        productVariants.forEach {
            if (it.productCode.isBlank()) {
                uiState = UiState.Error
                it.id?.let { id ->
                    productVariantsProductCodesErrors[id] = SharedRes.string.field_cannot_be_empty
                }
            } else {
                it.id?.let { id ->
                    productVariantsProductCodesErrors[id] = null
                }
            }
        }

        productVariantUiState = uiState
    }

    // 产品媒体
    val mediaPicker = MediaPickerViewModel(
        uploadRepository = uploadRepository,
        coroutineScope = viewModelScope,
        snackbarViewModel = snackbarViewModel
    )

    // 产品信息 (多语言)
    var selectedTranslationIndex by mutableIntStateOf(0)
        private set
    var translationTabs = mutableStateListOf(
        Pair(
            SharedProductTranslation(
                LanguageManager.getCurrent().code,
                "",
                ""
            ),
            RichTextState()
        )
    )
        private set
    var selectedTranslationNameError: StringResource? by mutableStateOf(null)

    var showAddLanguageDialog by mutableStateOf(false)
        private set
    val canAddTranslation by derivedStateOf {
        getAvailableLanguages().isNotEmpty()
                && productTranslationUiState == UiState.Loading
    }

    // 产品基础属性
    var productIva by mutableStateOf("21.00")
        private set
    var productCategoryError: StringResource? by mutableStateOf(null)

    // 用户输入则使用用户输入否则根据选择类别
    var isIvaManuallyEdited = false
        private set

    var productCode by mutableStateOf("")
        private set
    var productCodeError: StringResource? by mutableStateOf(null)

    var productStatus by mutableStateOf(SharedProductStatus.ACTIVE)
        private set

    // SKU
    @OptIn(ExperimentalUuidApi::class)
    var productVariants = mutableStateListOf(
        ProductVariantDto(
            id = Uuid.generateV4().toString(),
            typeSale = SharedProductSaleVariant.BOX,
            price = "0.00",
            priceIva = "0.00",
            productCode = "",
            availableStock = 100,
            saleUnitQty = getRecommendedSaleUnitQty(SharedProductSaleVariant.BOX)
        )
    )
        private set
    var productVariantsProductCodesErrors = mutableStateMapOf<String, StringResource?>()
    val canAddSku: Boolean by derivedStateOf {
        productVariants.size < 50 &&
                if (productVariantsProductCodesErrors.isNotEmpty()) productVariantsProductCodesErrors.values.none {
                    it?.let { true } ?: false
                } else true
    }

    // 记录用户是否手动编辑过某个 SKU 的 saleUnitQty
    private val variantSaleUnitQtyManuallyEdited = mutableStateMapOf<String, Boolean>()

    // 根据销售单位返回推荐的 saleUnitQty
    private fun getRecommendedSaleUnitQty(typeSale: SharedProductSaleVariant): Int {
        return when (typeSale) {
            SharedProductSaleVariant.UNIT -> 1
            SharedProductSaleVariant.PACK -> 12
            SharedProductSaleVariant.BOX -> 24
        }
    }


    init {
        // 监听语言
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest { _ ->
                val currentCode = LanguageManager.getCurrent().code
                val existingIndex = translationTabs.indexOfFirst { it.first.langCode == currentCode }

                if (existingIndex == -1) {
                    // 如果不存在，添加并放在第一位
                    translationTabs.add(
                        0,
                        Pair(
                            SharedProductTranslation(currentCode, "", ""),
                            RichTextState()
                        )
                    )
                } else if (existingIndex != 0) {
                    // 如果存在但不在第一位，移动到第一位
                    val existing = translationTabs.removeAt(existingIndex)
                    translationTabs.add(0, existing)
                }
                // 自动选中当前语言（第一位）
                selectedTranslationIndex = 0
            }
        }

        productVariants.firstOrNull()?.id?.let { firstId ->
            variantSaleUnitQtyManuallyEdited[firstId] = false
        }
    }

    fun showAddLanguageDialog(value: Boolean) {
        showAddLanguageDialog = value
    }

    fun changeLanguageIndex(languageIndex: Int) {
        selectedTranslationIndex = languageIndex
    }

    fun upsertTranslation(langCode: String, name: String, title: String?, description: String? = null) {
        translationTabs.indexOfFirst { it.first.langCode == langCode }.let {
            if (it != -1) {
                translationTabs[it] =
                    translationTabs[it].copy(
                        first = SharedProductTranslation(
                            langCode,
                            name = name.take(50),
                            title = title?.take(100),
                            description = description
                        )
                    )
            } else {
                translationTabs.add(
                    Pair(
                        SharedProductTranslation(
                            langCode = langCode,
                            name = name.take(50),
                            title = title?.take(100),
                            description = description,
                        ),
                        RichTextState()
                    )
                )
            }
        }
        checkProductTranslation()
    }

    fun removeTranslation(langCode: String) {
        if (translationTabs.isEmpty()) return
        translationTabs.find { it.first.langCode == langCode }?.let {
            translationTabs.remove(it)
        }
        checkProductTranslation()
    }

    @OptIn(ExperimentalUuidApi::class)
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
        lowStockThreshold: Int? = null
    ) {
        val sanitizedProductCode = sanitizeProductCode(productCode)

        val index = productVariants.indexOfFirst { it.id == id }
        if (index != -1) {
            val existingSku = productVariants[index]
            val effectiveIva = productIva.ifBlank { "0.00" }
            var finalPrice = price
            var finalPriceIva = priceIva

            finalPrice?.let {
                if (it != existingSku.price) {
                    sanitizeProductPricesInput(price = it, iva = effectiveIva).let { (price, priceIva) ->
                        finalPrice = price
                        finalPriceIva = priceIva
                    }
                }
            }

            finalPriceIva?.let {
                if (it != existingSku.priceIva) {
                    sanitizeProductPricesInput(priceIva = it, iva = effectiveIva).let { (price, priceIva) ->
                        finalPriceIva = priceIva
                        finalPrice = price
                    }
                }
            }

            var finalSaleUnitQty = saleUnitQty
            val saleUnitQtyManuallyEdited = existingSku.id?.let { variantSaleUnitQtyManuallyEdited[it] } == true

            if (saleUnitQty != existingSku.saleUnitQty) {
                existingSku.id?.let { variantSaleUnitQtyManuallyEdited[it] = true }
            }
            if (!saleUnitQtyManuallyEdited) {
                finalSaleUnitQty = getRecommendedSaleUnitQty(typeSale)
            }

            productVariants[index] = existingSku.copy(
                typeSale = typeSale,
                sort = sort,
                price = finalPrice,
                priceIva = finalPriceIva,
                productCode = sanitizedProductCode,
                availableStock = availableStock,
                saleUnitQty = finalSaleUnitQty,
                minOrderQty = minOrderQty,
                lowStockThreshold = lowStockThreshold
            )
        } else {
            productVariants.add(
                ProductVariantDto(
                    id = id,
                    typeSale = typeSale,
                    price = price,
                    priceIva = priceIva,
                    productCode = sanitizedProductCode,
                    availableStock = availableStock,
                    saleUnitQty = getRecommendedSaleUnitQty(typeSale),
                    minOrderQty = minOrderQty,
                    lowStockThreshold = lowStockThreshold
                )
            )
        }

        checkProductVariant()
    }

    fun deleteVariant(skuId: String?) {
        if (productVariants.isEmpty()) return
        productVariants.find { it.id == skuId }?.let {
            productVariants.remove(it)
            it.id?.let { id -> productVariantsProductCodesErrors.remove(id) }
        }
        checkProductVariant()
    }

    fun reorder(from: Int, to: Int) {
        if (from == to) return
        if (from !in productVariants.indices || to !in productVariants.indices) return
        productVariants.add(to, productVariants.removeAt(from))
    }

    fun getAvailableLanguages(): List<LanguageManager.SupportedLanguages> {
        return LanguageManager.SupportedLanguages.entries
            .filter { it.code !in translationTabs.map { translation -> translation.first.langCode } }
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

    private fun calculatePrices() {
        productVariants.forEachIndexed { index, dto ->
            val (price, priceIva) = sanitizeProductPricesInput(
                priceIva = dto.priceIva,
                iva = productIva
            )
            productVariants[index] = dto.copy(
                price = price,
                priceIva = priceIva
            )
        }
    }

    fun updateProductStatus(state: SharedProductStatus) {
        productStatus = state
    }

    fun createProduct() {
        checkProductMeta()
        checkProductVariant()
        checkProductTranslation()
        if (!createButtonEnabled) return
        val primaryCategory = filterCategory ?: return
        val primaryTranslation = translationTabs.first().first
        val restTranslations = translationTabs.drop(1).map { it.first }
        productVariants.forEachIndexed { index, _ ->
            productVariants[index] = productVariants[index].copy(sort = index)
        }
        val files = mediaPicker.mediaItems.mapIndexed { index, item ->
            item.serverId?.let { serverId ->
                ProductFileDto(
                    fileId = serverId,
                    sort = index
                )
            }
        }.filterNotNull()
        viewModelScope.launch {
            when (val result = productRepository.createProduct(
                name = primaryTranslation.name,
                title = primaryTranslation.title,
                description = primaryTranslation.description,
                translations = restTranslations,
                iva = productIva,
                productCode = productCode,
                primaryCategoryId = primaryCategory.id.toString(),
                variants = productVariants,
                files = files
            )) {
                is SharedResponseResult.Success -> {
                    val message = getString(SharedRes.string.create_success)
                    mySnackbarViewModel.showSuccess(
                        message = message
                    )
                }

                is SharedResponseResult.Error -> {
                    if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.create_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
        }
    }

    override suspend fun findCategories(query: String?, page: Int, limit: Int): List<ReducedCategoryResponse> {
        when (val result = categoryRepository.getCategoriesByLevel(query, page, limit, true)) {
            is SharedResponseResult.Success -> {
                return result.data?.items ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
            }
        }
        return emptyList()
    }

    override fun updateFilterCategory(category: ReducedCategoryResponse?) {
        super.updateFilterCategory(category)
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
}
