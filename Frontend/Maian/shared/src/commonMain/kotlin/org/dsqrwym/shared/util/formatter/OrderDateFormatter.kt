package org.dsqrwym.shared.util.formatter

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.dsqrwym.shared.localization.LanguageManager
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect fun formatIsoDateForLocale(isoDate: String, localeTag: String): String?

expect fun formatIsoDateTimeForLocale(value: String, localeTag: String): String?

fun String.toDisplayDate(): String =
    toIsoDate()?.let { formatIsoDateForLocale(it, LanguageManager.getCurrentLanguage()) ?: it } ?: this

fun String.toDisplayDateTime(): String {
    val localeTag = LanguageManager.getCurrentLanguage()
    formatIsoDateTimeForLocale(this, localeTag)?.let { return it }

    val date = toIsoDate() ?: return replace("T", " ").take(16).removeSuffix("Z")
    val time = extractTimePart()
    val localizedDate = formatIsoDateForLocale(date, localeTag) ?: date
    return if (time == null) localizedDate else "$localizedDate $time"
}

fun String.toIsoDate(): String? =
    takeIf { it.length >= 10 }?.take(10)

fun String.toUtcDateMillis(): Long? =
    runCatching { LocalDate.parse(this).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()

@OptIn(ExperimentalTime::class)
fun Long.toIsoDateFromUtcMillis(): String =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date.toString()

@OptIn(ExperimentalTime::class)
fun todayUtcMillis(): Long {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun defaultOrderHistoryDateRange(days: Int = 30): Pair<String, String> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.minus(DatePeriod(days = days)).toString() to today.toString()
}

private fun String.extractTimePart(): String? {
    val separatorIndex = indexOfFirst { it == 'T' || it == ' ' }
    if (separatorIndex < 0 || length < separatorIndex + 6) return null
    return substring(separatorIndex + 1, separatorIndex + 6)
}
