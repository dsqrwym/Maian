package org.dsqrwym.shared.localization

import java.util.*

actual fun getSystemTimeZone(): String {
    return TimeZone.getDefault().id
}