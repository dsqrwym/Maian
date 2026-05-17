package org.dsqrwym.shared.util.formatter

fun String.asTaxRatePercent(): String {
    val trimmed = trim()
    if (trimmed.isEmpty() || trimmed.contains("%")) return trimmed

    val normalized = trimmed
        .trimEnd('0')
        .trimEnd('.')
        .ifBlank { "0" }

    return "$normalized%"
}
