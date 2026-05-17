package org.dsqrwym.shared.util.formatter

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatIsoDateForLocale(isoDate: String, localeTag: String): String? {
    val date = parseJvmDate(isoDate, listOf("yyyy-MM-dd")) ?: return null
    return runCatching {
        DateFormat.getDateInstance(DateFormat.SHORT, localeFromTag(localeTag)).format(date)
    }.getOrNull()
}

actual fun formatIsoDateTimeForLocale(value: String, localeTag: String): String? {
    val normalized = value.replace(" ", "T")
    val date = parseJvmDate(normalized, dateTimePatterns)
        ?: parseJvmDate(normalized.take(10), listOf("yyyy-MM-dd"))
        ?: return null
    val hasTime = normalized.length > 10
    return runCatching {
        if (hasTime) {
            DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT,
                localeFromTag(localeTag)
            ).format(date)
        } else {
            DateFormat.getDateInstance(DateFormat.SHORT, localeFromTag(localeTag)).format(date)
        }
    }.getOrNull()
}

private val dateTimePatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mmXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm"
)

private fun parseJvmDate(value: String, patterns: List<String>): Date? =
    patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.ROOT).apply {
                isLenient = false
            }.parse(value)
        }.getOrNull()
    }

private fun localeFromTag(localeTag: String): Locale {
    val locale = Locale.forLanguageTag(localeTag.replace('_', '-'))
    return if (locale.language.isNullOrBlank()) Locale.getDefault() else locale
}
