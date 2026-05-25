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
        let date = new Date(value);
        if (Number.isNaN(date.getTime()) && value.length >= 10) {
            date = new Date(value.substring(0, 10) + "T00:00:00");
        }
        if (Number.isNaN(date.getTime())) return null;
        const options = value.length > 10
            ? { dateStyle: "short", timeStyle: "short" }
            : { dateStyle: "short" };
        return new Intl.DateTimeFormat(localeTag, options).format(date);
    }"""
)
private external fun jsFormatIsoDateTimeForLocale(value: String, localeTag: String): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(value, localeTag) => {
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return null;
        return new Intl.DateTimeFormat(localeTag, { dateStyle: "short" }).format(date);
    }"""
)
private external fun jsFormatIsoDateFromDateTimeForLocale(value: String, localeTag: String): String?

actual fun formatIsoDateForLocale(isoDate: String, localeTag: String): String? =
    jsFormatIsoDateForLocale(isoDate, localeTag)

actual fun formatIsoDateFromDateTimeForLocale(value: String, localeTag: String): String? =
    jsFormatIsoDateFromDateTimeForLocale(value.normalizeIsoDateTimeForParsing(), localeTag)

actual fun formatIsoDateTimeForLocale(value: String, localeTag: String): String? =
    jsFormatIsoDateTimeForLocale(value.normalizeIsoDateTimeForParsing(), localeTag)
