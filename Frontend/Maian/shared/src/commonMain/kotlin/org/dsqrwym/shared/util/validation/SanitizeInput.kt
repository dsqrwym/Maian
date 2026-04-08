package org.dsqrwym.shared.util.validation

import org.dsqrwym.shared.util.formatter.toFixed

fun sanitizeIvaInput(input: String): String? {
    return sanitizeDecimalInput(
        input = input,
        allowNegative = false,
        min = 0.00,
        max = 100.00,
        maxLength = 5
    )
}

fun sanitizeProductCode(input: String): String {
    if (input.isBlank()) return ""
    return input.replace(Regex("[^A-Za-z0-9/_.-]"), "")
        .take(50)
}

fun sanitizeDecimalInput(
    input: String,
    allowNegative: Boolean = false,
    min: Double? = null,
    max: Double? = null,
    decimals: Int = 2,
    maxLength: Int? = null,
): String? {
    if (input.isBlank()) return null

    var counter = 0
    var existingDecimals = 0

    val filtered = buildString {
        input.forEachIndexed { index, c ->
            when {
                c.isDigit() -> {
                    if (counter < 1) {
                        append(c)
                    } else if (existingDecimals < decimals) {
                        append(c)
                        existingDecimals++
                    }
                }

                c == '.' -> {
                    if (counter < 1) {
                        append(c)
                        counter++
                    }
                }

                c == '-' && allowNegative && index == 0 -> append(c)
            }
        }
    }

    val value = filtered.toDoubleOrNull()?.coerceIn(min, max)?.toFixed(decimals) ?: return null

    // 长度限制（基于 toString）
    if (maxLength != null && value.length > maxLength) return null

    return value
}

fun sanitizeIntInput(
    input: String,
    allowNegative: Boolean = false,
    min: Int? = Int.MIN_VALUE,
    max: Int? = Int.MAX_VALUE
): Int? {
    val filtered = buildString {
        input.forEachIndexed { index, c ->
            when {
                c.isDigit() -> append(c)
                c == '-' && allowNegative && index == 0 -> append(c)
            }
        }
    }
    return filtered.toIntOrNull()?.coerceIn(min, max)
}