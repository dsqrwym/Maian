package org.dsqrwym.shared.util.validation

fun sanitizeIvaInput(input: String): String? {
    if (input.isBlank()) return ""

    // 仅保留数字和 .
    val filtered = input.filter { it.isDigit() || it == '.' }

    // 只允许一个小数点
    if (filtered.count { it == '.' } > 1) return null

    // 转 Double
    val value = filtered.toDoubleOrNull() ?: return null

    // 范围 0~100
    if (value !in 0.0..100.0) return null

    // 最长 5 位（比如 100.0）
    if (value.toString().length > 5) return null

    return filtered
}

fun sanitizeProductCode(input: String): String {
    if (input.isBlank()) return ""
    return input.replace(Regex("[^A-Za-z0-9/_.-]"), "")
        .take(50)
}