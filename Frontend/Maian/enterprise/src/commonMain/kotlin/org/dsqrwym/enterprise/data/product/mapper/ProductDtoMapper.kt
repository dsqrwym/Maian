package org.dsqrwym.enterprise.data.product.mapper

import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.domain.product.ProductImage
import org.dsqrwym.enterprise.domain.product.ProductTranslation
import org.dsqrwym.enterprise.domain.product.ProductVariant
import org.dsqrwym.shared.data.category.mapper.toDomain
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductFile
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.serialization.getOrElse
import org.dsqrwym.shared.serialization.getValOrNull

fun ProductVariantDto.toDomain(): ProductVariant = ProductVariant(
    id = id.getValOrNull(),
    typeSale = typeSale.getOrElse{SharedProductSaleVariant.BOX},
    price = price.getValOrNull(),
    priceIva = priceIva.getValOrNull(),
    productCode = productCode.getOrElse { "" },
    availableStock = availableStock.getOrElse { 100 },
    saleUnitQty = saleUnitQty.getOrElse { 1 },
    minOrderQty = minOrderQty.getOrElse { 1 },
    lowStockThreshold = lowStockThreshold.getOrElse { 0 },
    status = status.getOrElse { SharedProductStatus.ACTIVE },
    sort = sort.getOrElse { 0 },
)

fun ProductResponse.toDomain(): Product =
    Product(
        id = id,
        code = code,
        name = name,
        mainImage = mainImage.toDomain(),
        title = title,
        totalStock = totalStock,
        minOrderQty = minOrderQty,
        minPrice = minPrice,
        minPriceIva = minPriceIva,
        translations = translations.map { it.toDomain() },
        status = status,
        iva = iva,
        mainCategory = mainCategory.toDomain()
    )

private fun SharedProductFile.toDomain(): ProductImage =
    ProductImage(
        id = id,
        mimeType = mimeType
    )

private fun SharedProductTranslation.toDomain(): ProductTranslation =
    ProductTranslation(
        langCode = langCode,
        name = name,
        title = title,
        description = description
    )

