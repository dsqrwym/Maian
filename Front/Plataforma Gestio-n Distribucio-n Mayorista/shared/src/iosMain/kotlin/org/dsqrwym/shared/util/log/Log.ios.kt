package org.dsqrwym.shared.util.log

import platform.Foundation.NSLog

actual fun sharedlog(level: LogLevel, tag: String, message: String) {
    NSLog("[$level][$tag] $message")
}