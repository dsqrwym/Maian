package org.dsqrwym.shared.data.products

import kotlinx.serialization.Serializable

enum class SharedProductStatus {
    ACTIVE, INACTIVE
}

enum class SharedProductListSelectField {
    IVA,
    USER_ID,
    STATUS,
    CATEGORY,
}

enum class SharedProductSortField {
    NAME, TITLE, CATEGORY, PRODUCT_CODE, MIN_ORDER_QTY, AVAILABLE_STOCK, PRICE_IVA, PRICE
}

@Serializable
enum class SharedProductSaleVariant {
    UNIT,
    BOX,
    PACK
}