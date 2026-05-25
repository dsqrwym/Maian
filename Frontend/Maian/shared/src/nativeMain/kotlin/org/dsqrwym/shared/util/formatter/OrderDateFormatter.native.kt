package org.dsqrwym.shared.util.formatter

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale

actual fun formatIsoDateForLocale(isoDate: String, localeTag: String): String? {
    val date = parseNativeDate(isoDate, listOf("yyyy-MM-dd")) ?: return null
    return localizedNativeFormatter(localeTag, withTime = false).stringFromDate(date)
}

actual fun formatIsoDateFromDateTimeForLocale(value: String, localeTag: String): String? {
    val date = parseNativeDate(value.normalizeIsoDateTimeForParsing(), dateTimePatterns) ?: return null
    return localizedNativeFormatter(localeTag, withTime = false).stringFromDate(date)
}

actual fun formatIsoDateTimeForLocale(value: String, localeTag: String): String? {
    val normalized = value.normalizeIsoDateTimeForParsing()
    val date = parseNativeDate(normalized, dateTimePatterns)
        ?: parseNativeDate(normalized.take(10), listOf("yyyy-MM-dd"))
        ?: return null
    return localizedNativeFormatter(localeTag, withTime = normalized.length > 10).stringFromDate(date)
}

private val dateTimePatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    "yyyy-MM-dd'T'HH:mm:ssXXXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ssXX",
    "yyyy-MM-dd'T'HH:mm:ssX",
    "yyyy-MM-dd'T'HH:mmXXXXX",
    "yyyy-MM-dd'T'HH:mmXXX",
    "yyyy-MM-dd'T'HH:mmXX",
    "yyyy-MM-dd'T'HH:mmX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm"
)

private fun parseNativeDate(value: String, patterns: List<String>): NSDate? =
    patterns.firstNotNullOfOrNull { pattern ->
        NSDateFormatter().apply {
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
            lenient = false
            dateFormat = pattern
        }.dateFromString(value)
    }

private fun localizedNativeFormatter(localeTag: String, withTime: Boolean): NSDateFormatter =
    NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = localeTag)
        dateStyle = NSDateFormatterShortStyle
        timeStyle = if (withTime) NSDateFormatterShortStyle else NSDateFormatterNoStyle
    }
