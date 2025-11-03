package org.dsqrwym.shared.util.formatter

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import java.util.*

actual fun Double.toFixed(digit: Int): String {
    val symbols = DecimalFormatSymbols(Locale.US) // 固定小数点为 '.'
    val pattern = buildString {
        append("#.")
        repeat(digit) { append("0") }
    }
    val df = DecimalFormat(pattern, symbols)
    return df.format(this)
}