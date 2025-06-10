package org.dsqrwym.shared.util.log


@JsFun("msg => console.log(msg)")
external fun consoleLog(msg: String)

actual fun sharedlog(level: LogLevel, tag: String, message: String) {
    consoleLog("[$level][$tag] $message")
}