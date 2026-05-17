package org.dsqrwym.shared.util.formatter

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(isoDate, localeTag) => {
        const date = new Date(isoDate + "T00:00:00");
        if (Number.isNaN(date.getTime())) return null;
        return new Intl.DateTimeFormat(localeTag, { dateStyle: "short" }).format(date);
    }"""
)
private external fun jsFormatIsoDateForLocale(isoDate: String, localeTag: String): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(value, localeTag) => {
        const normalized = value.replace(" ", "T");
        let date = new Date(normalized);
        if (Number.isNaN(date.getTime()) && normalized.length >= 10) {
            date = new Date(normalized.substring(0, 10) + "T00:00:00");
        }
        if (Number.isNaN(date.getTime())) return null;
        const options = normalized.length > 10
            ? { dateStyle: "short", timeStyle: "short" }
            : { dateStyle: "short" };
        return new Intl.DateTimeFormat(localeTag, options).format(date);
    }"""
)
private external fun jsFormatIsoDateTimeForLocale(value: String, localeTag: String): String?

actual fun formatIsoDateForLocale(isoDate: String, localeTag: String): String? =
    jsFormatIsoDateForLocale(isoDate, localeTag)

actual fun formatIsoDateTimeForLocale(value: String, localeTag: String): String? =
    jsFormatIsoDateTimeForLocale(value, localeTag)
