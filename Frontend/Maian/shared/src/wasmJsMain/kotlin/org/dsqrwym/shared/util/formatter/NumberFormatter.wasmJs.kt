package org.dsqrwym.shared.util.formatter

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(number, digits) => number.toFixed(digits)")
private external fun jsToFixed(number: Double, digits: Int): String

actual fun Double.toFixed(digit: Int): String {
    return jsToFixed(this, digit)
}