package org.dsqrwym.standard.data.browse.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.data.products.dto.SharedProductFile
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailDistributor
import org.dsqrwym.standard.domain.browse.RetailProduct
import org.dsqrwym.standard.domain.browse.RetailProductImage
import org.dsqrwym.shared.domain.category.CategoryTranslation

@Serializable
data class RetailProductResponse(
    val id: String,
    @SerialName("product_code")
    val code: String,
    val name: String,
    val title: String? = null,
    @SerialName("main_image")
    val mainImage: SharedProductFile? = null,
    @SerialName("total_stock")
    val totalStock: Int = 0,
    @SerialName("min_order_qty")
    val minOrderQty: Int = 0,
    @SerialName("min_price")
    val minPrice: String? = null,
    @SerialName("min_price_iva")
    val minPriceIva: String? = null,
    @SerialName("product_translations")
    val translations: List<SharedProductTranslation> = emptyList(),
)

fun RetailProductResponse.toDomain(): RetailProduct =
    RetailProduct(
        id = id,
        code = code,
        name = name,
        title = title,
        image = mainImage?.let { RetailProductImage(id = it.id, mimeType = it.mimeType) },
        minPrice = minPrice,
        minPriceIva = minPriceIva,
        totalStock = totalStock,
        minOrderQty = minOrderQty,
        translations = translations,
    )

@Serializable
data class RetailCategoryResponse(
    val id: String,
    val name: String,
    val level: Int = 1,
    @SerialName("user_id")
    val userId: String? = null,
    val parent: RetailCategoryParentResponse? = null,
    @SerialName("category_translations")
    val translations: List<SharedCategoryTranslation> = emptyList(),
)

@Serializable
data class RetailCategoryParentResponse(
    val id: String,
    val name: String,
    val parent: RetailCategoryParentResponse? = null,
)

fun RetailCategoryResponse.toDomain(): RetailCategory =
    RetailCategory(
        id = id,
        name = name,
        level = level,
        ownerUserId = userId,
        parentId = parent?.id,
        pathNames = parent.pathNames() + name,
        translations = translations.map { CategoryTranslation(it.langCode, it.name) },
    )

private fun RetailCategoryParentResponse?.pathNames(): List<String> =
    generateSequence(this) { it.parent }
        .map { it.name }
        .toList()
        .asReversed()

@Serializable
data class RetailDistributorResponse(
    val id: String,
    @SerialName("user_id")
    val userId: String? = null,
    val username: String? = null,
    val email: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val telephone: String? = null,
    val cif: String? = null,
    val profile: JsonObject? = null,
)

fun RetailDistributorResponse.toDomain(): RetailDistributor =
    RetailDistributor(
        id = id,
        userId = userId,
        username = username,
        firstName = firstName,
        lastName = lastName,
        email = email,
        telephone = telephone,
        cif = cif,
        companyName = profile?.get("companyName")?.jsonPrimitive?.content
            ?: profile?.get("company_name")?.jsonPrimitive?.content
            ?: profile?.get("businessName")?.jsonPrimitive?.content,
    )
