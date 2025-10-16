package org.dsqrwym.shared.localization

import android.icu.util.TimeZone

actual fun getSystemTimeZone(): String {
    return TimeZone.getDefault().id
}