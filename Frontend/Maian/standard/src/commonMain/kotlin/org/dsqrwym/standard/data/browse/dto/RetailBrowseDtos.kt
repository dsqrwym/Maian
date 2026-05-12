package org.dsqrwym.standard.data.browse.dto

/**
 * 零售浏览数据传输对象
 * 定义API响应的数据结构和到领域模型的转换函数
 * 提供数据层和领域层之间的映射
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.dto.SharedProductFile
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProductDetail
import org.dsqrwym.standard.domain.browse.RetailProductDetailCategory
import org.dsqrwym.standard.domain.browse.RetailProductDetailMedia
import org.dsqrwym.standard.domain.browse.RetailProductDetailTranslation
import org.dsqrwym.standard.domain.browse.RetailWholesaler
import org.dsqrwym.standard.domain.browse.RetailProduct
import org.dsqrwym.standard.domain.browse.RetailProductImage
import org.dsqrwym.standard.domain.browse.RetailProductVariant
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

@Serializable
data class RetailProductDetailResponse(
    val id: String,
    val name: String,
    val title: String? = null,
    val description: String? = null,
    val iva: String,
    @SerialName("product_code")
    val productCode: String,
    @SerialName("products_files")
    val productFiles: List<RetailProductDetailFileResponse> = emptyList(),
    @SerialName("product_categories")
    val productCategories: List<RetailProductDetailCategoryResponse> = emptyList(),
    @SerialName("product_translations")
    val productTranslations: List<RetailProductDetailTranslationResponse> = emptyList(),
    @SerialName("variant_products")
    val variantProducts: List<RetailProductVariantResponse> = emptyList(),
)

@Serializable
data class RetailProductDetailFileResponse(
    val sort: Int,
    @SerialName("file_id")
    val fileId: String,
    @SerialName("mime_type")
    val mimeType: String,
)

@Serializable
data class RetailProductDetailCategoryResponse(
    val id: String,
    val name: String,
    val iva: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    @SerialName("category_translations")
    val translations: List<SharedCategoryTranslation> = emptyList(),
)

@Serializable
data class RetailProductDetailTranslationResponse(
    val name: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("lang_code")
    val langCode: String,
)

@Serializable
data class RetailProductVariantResponse(
    val id: String,
    @SerialName("product_code")
    val productCode: String,
    @SerialName("type_sale")
    val typeSale: SharedProductSaleVariant,
    val price: String,
    @SerialName("price_iva")
    val priceIva: String,
    @SerialName("available_stock")
    val availableStock: Int,
    @SerialName("sale_unit_qty")
    val saleUnitQty: Int,
    val sort: Int,
    @SerialName("min_order_qty")
    val minOrderQty: Int,
)

fun RetailProductDetailResponse.toDomain(): RetailProductDetail =
    RetailProductDetail(
        id = id,
        name = name,
        title = title,
        description = description,
        iva = iva,
        productCode = productCode,
        media = productFiles
            .sortedBy { it.sort }
            .map { RetailProductDetailMedia(fileId = it.fileId, mimeType = it.mimeType, sort = it.sort) },
        categories = productCategories.map { category ->
            RetailProductDetailCategory(
                id = category.id,
                name = category.name,
                iva = category.iva,
                isPrimary = category.isPrimary,
                translations = category.translations.map { CategoryTranslation(it.langCode, it.name) },
            )
        },
        translations = productTranslations.map { translation ->
            RetailProductDetailTranslation(
                langCode = translation.langCode,
                name = translation.name,
                title = translation.title,
                description = translation.description,
            )
        },
        variants = variantProducts
            .sortedBy { it.sort }
            .map { variant ->
                RetailProductVariant(
                    id = variant.id,
                    productCode = variant.productCode,
                    typeSale = variant.typeSale,
                    price = variant.price,
                    priceIva = variant.priceIva,
                    availableStock = variant.availableStock,
                    saleUnitQty = variant.saleUnitQty,
                    sort = variant.sort,
                    minOrderQty = variant.minOrderQty,
                )
            },
    )

private fun RetailCategoryParentResponse?.pathNames(): List<String> =
    generateSequence(this) { it.parent }
        .map { it.name }
        .toList()
        .asReversed()

@Serializable
data class RetailWholesalerResponse(
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

fun RetailWholesalerResponse.toDomain(): RetailWholesaler =
    RetailWholesaler(
        id = id,
        userId = userId,
        displayName = profile?.get("displayName")?.jsonPrimitive?.content
            ?: profile?.get("display_name")?.jsonPrimitive?.content
            ?: listOfNotNull(firstName, lastName).joinToString(" ").takeIf { it.isNotBlank() }
            ?: username,
        companyName = profile?.get("companyName")?.jsonPrimitive?.content
            ?: profile?.get("company_name")?.jsonPrimitive?.content
            ?: profile?.get("businessName")?.jsonPrimitive?.content
            ?: email
            ?: userId
            ?: id,
    )
