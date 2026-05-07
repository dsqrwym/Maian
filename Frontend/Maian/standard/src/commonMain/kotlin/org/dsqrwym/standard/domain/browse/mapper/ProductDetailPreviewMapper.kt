package org.dsqrwym.standard.domain.browse.mapper

import org.dsqrwym.shared.domain.product.SharedProductDetailMedia
import org.dsqrwym.shared.domain.product.SharedProductDetailUiModel
import org.dsqrwym.shared.domain.product.SharedProductDetailVariant
import org.dsqrwym.standard.domain.browse.RetailProductDetail
import org.dsqrwym.standard.domain.browse.RetailProductDetailMedia
import org.dsqrwym.standard.domain.browse.RetailProductVariant

fun RetailProductDetail.toSharedDetailUi(languageCode: String): SharedProductDetailUiModel =
    SharedProductDetailUiModel(
        id = id,
        name = localizedName(languageCode),
        title = localizedTitle(languageCode),
        description = localizedDescription(languageCode),
        iva = iva,
        productCode = productCode,
        categoryName = mainCategory?.localizedName(languageCode),
        media = media.map { it.toSharedMedia(id) },
        variants = variants.map { it.toSharedVariant() },
    )

fun RetailProductDetailMedia.toSharedMedia(productId: String): SharedProductDetailMedia =
    SharedProductDetailMedia(
        model = url(productId),
        mimeType = mimeType,
        sort = sort,
    )

fun RetailProductVariant.toSharedVariant(): SharedProductDetailVariant =
    SharedProductDetailVariant(
        id = id,
        productCode = productCode,
        typeSale = typeSale,
        price = price,
        priceIva = priceIva,
        availableStock = availableStock,
        saleUnitQty = saleUnitQty,
        sort = sort,
        minOrderQty = minOrderQty,
    )
