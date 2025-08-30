package org.dsqrwym.shared.util.log


private const val reset = "\u001B[0m"
private fun colorFor(level: SharedLogLevel): String = when (level) {
    SharedLogLevel.DEBUG -> "\u001B[34m" // Blue
    SharedLogLevel.INFO  -> "\u001B[92m" // Green
    SharedLogLevel.WARN  -> "\u001B[33m" // Yellow
    SharedLogLevel.ERROR -> "\u001B[31m" // Red
}
actual object SharedLog {
    actual fun log(level: SharedLogLevel, tag: String, message: String) {
        val color = colorFor(level)
        println("$color[$level][$tag]: $message$reset")
    }
}