package org.dsqrwym.standard.domain.browse

import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.domain.category.CategoryTranslation
import org.dsqrwym.shared.localization.getLocalizedValue
import org.dsqrwym.shared.network.ApiConfig

enum class BrowseScope {
    GLOBAL,
    DISTRIBUTOR,
    CATEGORY
}

data class RetailProductImage(
    val id: Long,
    val mimeType: String,
) {
    fun url(productId: String): String =
        ApiConfig.FilePath.productFile(productId, id.toString())
}

data class RetailProductDetailMedia(
    val fileId: String,
    val mimeType: String,
    val sort: Int,
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isVideo: Boolean
        get() = mimeType.startsWith("video/")

    fun url(productId: String): String =
        ApiConfig.FilePath.productFile(productId, fileId)
}

data class RetailProduct(
    val id: String,
    val code: String,
    val name: String,
    val title: String?,
    val image: RetailProductImage?,
    val minPrice: String?,
    val minPriceIva: String?,
    val totalStock: Int,
    val minOrderQty: Int,
    val translations: List<SharedProductTranslation>,
) {
    fun localizedName(languageCode: String): String =
        translations.firstOrNull { it.langCode == languageCode }?.name ?: name

    fun localizedTitle(languageCode: String): String? =
        translations.firstOrNull { it.langCode == languageCode }?.title ?: title
}

data class RetailCategory(
    val id: String,
    val name: String,
    val level: Int,
    val ownerUserId: String?,
    val parentId: String?,
    val pathNames: List<String> = listOf(name),
    val translations: List<CategoryTranslation>,
) {
    val isPrivate: Boolean
        get() = ownerUserId != null

    fun localizedName(languageCode: String): String =
        translations.firstOrNull { it.langCode == languageCode }?.name ?: name

    fun localizedPathNames(languageCode: String): List<String> =
        if (pathNames.isEmpty()) listOf(localizedName(languageCode))
        else pathNames.dropLast(1) + localizedName(languageCode)
}

data class RetailProductDetailCategory(
    val id: String,
    val name: String,
    val iva: String?,
    val isPrimary: Boolean,
    val translations: List<CategoryTranslation>,
) {
    fun localizedName(languageCode: String): String =
        getLocalizedValue(
            langCode = languageCode,
            translations = translations,
            translationLangCode = { it.langCode },
            translatedValue = { it.name },
            fallback = name,
        ) ?: name
}

data class RetailProductDetailTranslation(
    val langCode: String,
    val name: String,
    val title: String?,
    val description: String?,
)

data class RetailProductVariant(
    val id: String,
    val productCode: String,
    val typeSale: SharedProductSaleVariant,
    val price: String,
    val priceIva: String,
    val availableStock: Int,
    val saleUnitQty: Int,
    val sort: Int,
    val minOrderQty: Int,
) {
    val maxPurchasableUnits: Int
        get() = availableStock / saleUnitQty.coerceAtLeast(1)

    val isPurchasable: Boolean
        get() = availableStock > 0 && maxPurchasableUnits >= minOrderQty
}

data class RetailProductDetail(
    val id: String,
    val name: String,
    val title: String?,
    val description: String?,
    val iva: String,
    val productCode: String,
    val media: List<RetailProductDetailMedia>,
    val categories: List<RetailProductDetailCategory>,
    val translations: List<RetailProductDetailTranslation>,
    val variants: List<RetailProductVariant>,
) {
    val mainCategory: RetailProductDetailCategory?
        get() = categories.firstOrNull { it.isPrimary } ?: categories.firstOrNull()

    fun localizedName(languageCode: String): String =
        getLocalizedValue(
            langCode = languageCode,
            translations = translations,
            translationLangCode = { it.langCode },
            translatedValue = { it.name },
            fallback = name,
        ) ?: name

    fun localizedTitle(languageCode: String): String? =
        getLocalizedValue(
            langCode = languageCode,
            translations = translations,
            translationLangCode = { it.langCode },
            translatedValue = { it.title },
            fallback = title,
        )

    fun localizedDescription(languageCode: String): String? =
        getLocalizedValue(
            langCode = languageCode,
            translations = translations,
            translationLangCode = { it.langCode },
            translatedValue = { it.description },
            fallback = description,
        )
}

data class RetailDistributor(
    val id: String,
    val userId: String?,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val telephone: String?,
    val cif: String?,
    val companyName: String?,
) {
    val displayName: String
        get() = companyName
            ?: listOfNotNull(firstName, lastName).joinToString(" ").takeIf { it.isNotBlank() }
            ?: username
            ?: email
            ?: userId
            ?: id

    val secondaryText: String
        get() = listOfNotNull(cif, telephone, email).firstOrNull { it.isNotBlank() } ?: ""
}
