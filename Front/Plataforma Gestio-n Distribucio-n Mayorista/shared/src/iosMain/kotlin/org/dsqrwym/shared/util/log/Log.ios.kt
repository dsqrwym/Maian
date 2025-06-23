package org.dsqrwym.shared.util.log

import platform.Foundation.NSLog

private const val reset = "\u001B[0m"

private fun colorFor(level: SharedLogLevel): String = when (level) {
    SharedLogLevel.DEBUG -> "\u001B[34m"
    SharedLogLevel.INFO  -> "\u001B[92m"
    SharedLogLevel.WARN  -> "\u001B[33m"
    SharedLogLevel.ERROR -> "\u001B[31m"
}
actual object SharedLog {
    actual fun log(level: SharedLogLevel, tag: String, message: String) {
        val color = colorFor(level)
        NSLog("$color[$level][$tag]: $message$reset")
    }
}