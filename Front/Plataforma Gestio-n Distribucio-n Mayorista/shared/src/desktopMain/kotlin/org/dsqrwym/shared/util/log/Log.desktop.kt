package org.dsqrwym.shared.util.log

actual fun sharedlog(level: LogLevel, tag: String, message: String) {
    println("[$level][$tag] $message")
}