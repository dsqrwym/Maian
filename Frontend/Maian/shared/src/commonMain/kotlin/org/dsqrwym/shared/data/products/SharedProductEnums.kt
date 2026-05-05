package org.dsqrwym.shared.data.products

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.product_sort_category
import maian.shared.generated.resources.product_sort_code
import maian.shared.generated.resources.product_sort_min_order_qty
import maian.shared.generated.resources.product_sort_name
import maian.shared.generated.resources.product_sort_price
import maian.shared.generated.resources.product_sort_price_without_vat
import maian.shared.generated.resources.product_sort_stock
import maian.shared.generated.resources.product_sort_title
import maian.shared.generated.resources.product_status_active
import maian.shared.generated.resources.product_status_inactive
import maian.shared.generated.resources.sale_variant_box
import maian.shared.generated.resources.sale_variant_pack
import maian.shared.generated.resources.sale_variant_unit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class SharedProductStatus {
    ACTIVE, INACTIVE
}

fun SharedProductStatus.toStringResource(): StringResource =
    when (this) {
        SharedProductStatus.ACTIVE -> SharedRes.string.product_status_active
        SharedProductStatus.INACTIVE -> SharedRes.string.product_status_inactive
    }

@Composable
fun SharedProductStatus.displayName(): String = stringResource(toStringResource())

enum class SharedProductListSelectField {
    IVA,
    USER_ID,
    STATUS,
    CATEGORY,
}

enum class SharedProductSortField {
    NAME, TITLE, CATEGORY, PRODUCT_CODE, MIN_ORDER_QTY, AVAILABLE_STOCK, PRICE_IVA, PRICE
}

val sharedEnterpriseProductSortFields = listOf(
    SharedProductSortField.NAME,
    SharedProductSortField.TITLE,
    SharedProductSortField.PRODUCT_CODE,
    SharedProductSortField.CATEGORY,
    SharedProductSortField.AVAILABLE_STOCK,
    SharedProductSortField.PRICE,
    SharedProductSortField.PRICE_IVA,
    SharedProductSortField.MIN_ORDER_QTY,
)

val sharedRetailProductSortFields = sharedEnterpriseProductSortFields
    .filterNot { it == SharedProductSortField.CATEGORY }

fun SharedProductSortField.toStringResource(): StringResource =
    when (this) {
        SharedProductSortField.NAME -> SharedRes.string.product_sort_name
        SharedProductSortField.TITLE -> SharedRes.string.product_sort_title
        SharedProductSortField.CATEGORY -> SharedRes.string.product_sort_category
        SharedProductSortField.PRODUCT_CODE -> SharedRes.string.product_sort_code
        SharedProductSortField.MIN_ORDER_QTY -> SharedRes.string.product_sort_min_order_qty
        SharedProductSortField.AVAILABLE_STOCK -> SharedRes.string.product_sort_stock
        SharedProductSortField.PRICE_IVA -> SharedRes.string.product_sort_price_without_vat
        SharedProductSortField.PRICE -> SharedRes.string.product_sort_price
    }

@Composable
fun SharedProductSortField.displayName(): String = stringResource(toStringResource())

@Serializable
enum class SharedProductSaleVariant {
    UNIT,
    BOX,
    PACK
}

fun SharedProductSaleVariant.toStringResource(): StringResource =
    when (this) {
        SharedProductSaleVariant.UNIT -> SharedRes.string.sale_variant_unit
        SharedProductSaleVariant.BOX -> SharedRes.string.sale_variant_box
        SharedProductSaleVariant.PACK -> SharedRes.string.sale_variant_pack
    }

@Composable
fun SharedProductSaleVariant.displayName(): String = stringResource(toStringResource())
