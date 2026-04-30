package org.dsqrwym.enterprise.ui.viewmodels.products

abstract class BaseProductFormViewModel(
    private val uploadRepository: SharedUploadRepository,
    protected val productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    protected val snackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    snackbarViewModel
), SharedNavigable by SharedNavigableDelegate() {

    val mediaPicker = MediaPickerViewModel(
        uploadRepository = uploadRepository,
        coroutineScope = viewModelScope,
        snackbarViewModel = snackbarViewModel
    )

    var productTranslationUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

    var productVariantUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

    var productMetaDataUiState: UiState by mutableStateOf(UiState.Idle)
        protected set

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

    var productIva by mutableStateOf("21.00")
        protected set

    var productCategoryError: StringResource? by mutableStateOf(null)
        protected set

    var isIvaManuallyEdited = false
        protected set

    var productCode by mutableStateOf("")
        protected set

    var productCodeError: StringResource? by mutableStateOf(null)
        protected set

    var productStatus by mutableStateOf(SharedProductStatus.ACTIVE)
        protected set

    var productVariants = mutableStateListOf<ProductVariantDto>()
        protected set

    var productVariantsProductCodesErrors = mutableStateMapOf<String, StringResource?>()
        protected set

    protected val variantSaleUnitQtyManuallyEdited = mutableStateMapOf<String, Boolean>()

    val canAddTranslation by derivedStateOf {
        getAvailableLanguages().isNotEmpty() &&
                productTranslationUiState == UiState.Loading
    }

    val canAddSku by derivedStateOf {
        productVariants.size < 50 &&
                productVariantsProductCodesErrors.values.none { it != null }
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

            if (variant.productCode.isBlank()) {
                uiState = UiState.Error
                if (id != null) {
                    productVariantsProductCodesErrors[id] = SharedRes.string.field_cannot_be_empty
                }
            } else {
                if (id != null) {
                    productVariantsProductCodesErrors[id] = null
                }
            }
        }

        productVariantUiState = uiState
    }

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
        val item = translationTabs.find { it.first.langCode == langCode } ?: return
        translationTabs.remove(item)

        selectedTranslationIndex = selectedTranslationIndex.coerceIn(
            0,
            translationTabs.lastIndex.coerceAtLeast(0)
        )

        checkProductTranslation()
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
        lowStockThreshold: Int = 0
    ) {
        val sanitizedProductCode = sanitizeProductCode(productCode)
        val index = productVariants.indexOfFirst { it.id.getValOrNull() == id }

        if (index != -1) {
            val existingSku = productVariants[index]
            val effectiveIva = productIva.ifBlank { "0.00" }

            var finalPrice = price
            var finalPriceIva = priceIva

            finalPrice?.let {
                if (it != existingSku.price) {
                    sanitizeProductPricesInput(price = it, iva = effectiveIva).let { result ->
                        finalPrice = result.first
                        finalPriceIva = result.second
                    }
                }
            }

            finalPriceIva?.let {
                if (it != existingSku.priceIva) {
                    sanitizeProductPricesInput(priceIva = it, iva = effectiveIva).let { result ->
                        finalPrice = result.first
                        finalPriceIva = result.second
                    }
                }
            }

            var finalSaleUnitQty = saleUnitQty
            val variantId = existingSku.id.getValOrNull()
            val saleUnitQtyManuallyEdited =
                variantId?.let { variantSaleUnitQtyManuallyEdited[it] } == true

            if (saleUnitQty != existingSku.saleUnitQty && variantId != null) {
                variantSaleUnitQtyManuallyEdited[variantId] = true
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
                    id = id.toOptionalField(),
                    typeSale = typeSale,
                    sort = sort,
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
        val item = productVariants.find { it.id.getValOrNull() == skuId } ?: return
        productVariants.remove(item)
        skuId?.let { productVariantsProductCodesErrors.remove(it) }
        checkProductVariant()
    }

    fun reorder(from: Int, to: Int) {
        if (from == to) return
        if (from !in productVariants.indices || to !in productVariants.indices) return

        productVariants.add(to, productVariants.removeAt(from))
    }

    override fun updateFilterCategory(category: CategorySummary?) {
        super.updateFilterCategory(category)

        productCategoryError = if (category == null) {
            SharedRes.string.field_cannot_be_empty
        } else {
            null
        }

        checkProductMeta()

        val iva = category?.iva ?: return

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