package org.dsqrwym.shared.data.orders

import org.dsqrwym.shared.data.orders.dto.SharedOrderFilterMetadataDto

data class SharedOrderAmountFilterBounds(
    val minTotalPrice: Double = 0.0,
    val maxTotalPrice: Double? = null,
    val minSubtotal: Double = 0.0,
    val maxSubtotal: Double? = null,
    val minTotalIva: Double = 0.0,
    val maxTotalIva: Double? = null,
)

fun SharedOrderFilterMetadataDto?.toAmountFilterBounds(): SharedOrderAmountFilterBounds {
    if (this == null) return SharedOrderAmountFilterBounds()
    return SharedOrderAmountFilterBounds(
        minTotalPrice = minTotal.toAmountBoundOrZero(),
        maxTotalPrice = maxTotal.toAmountBoundOrNull(),
        minSubtotal = minSubtotal.toAmountBoundOrZero(),
        maxSubtotal = maxSubtotal.toAmountBoundOrNull(),
        minTotalIva = minIvaTotal.toAmountBoundOrZero(),
        maxTotalIva = maxIvaTotal.toAmountBoundOrNull(),
    )
}

private fun String?.toAmountBoundOrZero(): Double =
    toAmountBoundOrNull() ?: 0.0

private fun String?.toAmountBoundOrNull(): Double? =
    this?.toDoubleOrNull()?.takeIf { it >= 0.0 }
