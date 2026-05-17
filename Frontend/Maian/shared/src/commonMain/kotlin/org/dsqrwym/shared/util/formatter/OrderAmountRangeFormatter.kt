package org.dsqrwym.shared.util.formatter

import kotlin.math.roundToInt

const val DefaultOrderTotalRangeMax = 50_000f

fun orderTotalSliderValue(
    value: Double?,
    defaultValue: Float,
    maxValue: Float = DefaultOrderTotalRangeMax,
    minValue: Float = 0f,
): Float =
    value?.toFloat()?.coerceIn(minValue, maxValue) ?: defaultValue

fun roundOrderAmount(value: Float): Double =
    value.roundToInt().toDouble()

fun orderTotalFilterMin(value: Float): Double? =
    roundOrderAmount(value).takeIf { it > 0.0 }

fun orderTotalFilterMax(value: Float, maxValue: Float = DefaultOrderTotalRangeMax): Double? =
    roundOrderAmount(value).takeIf { it < maxValue }
