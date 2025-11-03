package org.dsqrwym.shared.util.formatter

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Double.toFixed(digit: Int): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        minimumFractionDigits = digit.toULong()
        maximumFractionDigits = digit.toULong()
    }
    return formatter.stringFromNumber(NSNumber(this)) ?: "0.00"
}