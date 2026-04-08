package org.dsqrwym.shared.util.validation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

private val MAX_PRICE = BigDecimal.parseString("10000000.00")
private val MAX_PRICE_IVA = BigDecimal.parseString("20000000.00")
private val DEFAULT_IVA = BigDecimal.ZERO
private val HUNDRED = BigDecimal.fromInt(100)

// 除法时使用的精度和舍入模式（货币计算通常保留2位小数，但中间计算保留更高精度）
private val DIVISION_MODE = DecimalMode(20, RoundingMode.ROUND_HALF_TO_EVEN)

fun sanitizeProductPricesInput(
    price: String? = null,
    priceIva: String? = null,
    iva: String? = null
): Pair<String, String> {
    // 解析税率（无效或空白则视为0）
    val ivaValue = runCatching { BigDecimal.parseString(iva ?: "0") }
        .getOrElse { DEFAULT_IVA }
    val onePlusIva = BigDecimal.ONE + ivaValue.divide(HUNDRED, DIVISION_MODE)

    // 根据输入类型计算
    return when {
        !price.isNullOrBlank() -> {
            var priceDecimal = runCatching { BigDecimal.parseString(price) }.getOrElse { BigDecimal.ZERO }
                .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
            if (priceDecimal > MAX_PRICE) {
                priceDecimal = MAX_PRICE
            }

            val calcPriceIva = priceDecimal.multiply(onePlusIva, DIVISION_MODE)
                .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

            if (calcPriceIva > MAX_PRICE_IVA) {
                // 上限截断：重新计算不含税价 = maxPriceIva / (1+iva)
                val cappedPrice = MAX_PRICE_IVA.divide(onePlusIva, DIVISION_MODE)
                    .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                Pair(
                    cappedPrice.toPlainString(),
                    MAX_PRICE_IVA.toPlainString()
                )
            } else {
                Pair(
                    //priceDecimal.toPlainString(),
                    price,
                    calcPriceIva.toPlainString()
                )
            }
        }

        !priceIva.isNullOrBlank() -> {
            var priceIvaDecimal = runCatching { BigDecimal.parseString(priceIva) }.getOrElse { BigDecimal.ZERO }
                .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
            if (priceIvaDecimal > MAX_PRICE_IVA) {
                priceIvaDecimal = MAX_PRICE_IVA
            }

            val calcPrice = priceIvaDecimal.divide(onePlusIva, DIVISION_MODE)
                .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

            if (calcPrice > MAX_PRICE) {
                // 上限截断：重新计算含税价 = maxPrice * (1+iva)
                val cappedPriceIva = MAX_PRICE.multiply(onePlusIva, DIVISION_MODE)
                    .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                Pair(
                    MAX_PRICE.toPlainString(),
                    cappedPriceIva.toPlainString()
                )
            } else {
                Pair(
                    calcPrice.toPlainString(),
                    // priceIvaDecimal.toPlainString()
                    priceIva
                )
            }
        }

        else -> Pair("0.00", "0.00")
    }
}