package org.dsqrwym.enterprise.domain.product.mapper

import com.mohamedrejeb.richeditor.model.RichTextState
import io.github.vinceglb.filekit.mimeType
import org.dsqrwym.business.ui.media.model.MediaSource
import org.dsqrwym.business.ui.media.model.MediaType
import org.dsqrwym.business.ui.media.model.UploadMediaItem
import org.dsqrwym.enterprise.data.product.dto.ProductVariantDto
import org.dsqrwym.enterprise.data.product.mapper.toDomain
import org.dsqrwym.enterprise.ui.viewmodels.products.BaseProductFormViewModel
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.domain.product.SharedProductDetailMedia
import org.dsqrwym.shared.domain.product.SharedProductDetailUiModel
import org.dsqrwym.shared.domain.product.SharedProductDetailVariant
import org.dsqrwym.shared.serialization.getOrElse

fun BaseProductFormViewModel.toSharedProductDetailPreview(languageCode: String): SharedProductDetailUiModel {
    val primaryTranslation = translationTabs.firstOrNull()?.first
    val localizedTranslation = translationTabs
        .firstOrNull { it.first.langCode == languageCode }
        ?: translationTabs.firstOrNull()
    val localizedText = localizedTranslation?.first
    val localizedDescription = localizedTranslation?.toHtmlOrNull()

    return SharedProductDetailUiModel(
        id = "preview",
        name = localizedText?.name?.ifBlank { null }
            ?: primaryTranslation?.name?.ifBlank { null }
            ?: productCode.ifBlank { "Preview" },
        title = localizedText?.title?.ifBlank { null } ?: primaryTranslation?.title?.ifBlank { null },
        description = localizedDescription
            ?: primaryTranslation?.description?.ifBlank { null },
        iva = productIva,
        productCode = productCode,
        categoryName = filterCategory?.localizedName(languageCode) ?: filterCategory?.name,
        media = mediaPicker.mediaItems.mapIndexedNotNull { index, item -> item.toSharedMedia(index) },
        variants = productVariants.map { it.toSharedPreviewVariant() }.sortedBy { it.sort },
    )
}

private fun ProductVariantDto.toSharedPreviewVariant(): SharedProductDetailVariant {
    val domain = toDomain()
    return SharedProductDetailVariant(
        id = domain.id ?: productCode.getOrElse { "" },
        productCode = domain.productCode,
        typeSale = domain.typeSale,
        price = domain.price ?: "0.00",
        priceIva = domain.priceIva ?: "0.00",
        availableStock = domain.availableStock,
        saleUnitQty = domain.saleUnitQty,
        sort = domain.sort,
        minOrderQty = domain.minOrderQty,
    )
}

private fun UploadMediaItem.toSharedMedia(sort: Int): SharedProductDetailMedia? {
    val model: Any?
    val mimeType: String
    when (val source = source) {
        is MediaSource.Local -> {
            model = source.file
            mimeType = source.file.mimeType()?.toString() ?: fallbackMimeType()
        }

        is MediaSource.Remote -> {
            model = source.url
            mimeType = source.mimeType ?: fallbackMimeType()
        }
    }

    return SharedProductDetailMedia(
        model = model,
        mimeType = mimeType,
        sort = sort,
    )
}

private fun UploadMediaItem.fallbackMimeType(): String =
    when (type) {
        MediaType.VIDEO -> "video/mp4"
        MediaType.IMAGE -> "image/jpeg"
        MediaType.DOCUMENT -> "application/octet-stream"
    }

private fun Pair<SharedProductTranslation, RichTextState>.toHtmlOrNull(): String? =
    second.toHtml().ifBlank { first.description?.ifBlank { null } }
