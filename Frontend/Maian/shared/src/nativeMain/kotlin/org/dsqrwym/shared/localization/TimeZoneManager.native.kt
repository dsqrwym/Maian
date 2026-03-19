package org.dsqrwym.shared.localization

import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone

actual fun getSystemTimeZone(): String {
    return NSTimeZone.localTimeZone.name
}