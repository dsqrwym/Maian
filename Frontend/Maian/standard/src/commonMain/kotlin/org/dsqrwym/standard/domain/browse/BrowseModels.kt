package org.dsqrwym.standard.domain.browse

import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.domain.category.CategoryTranslation
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
