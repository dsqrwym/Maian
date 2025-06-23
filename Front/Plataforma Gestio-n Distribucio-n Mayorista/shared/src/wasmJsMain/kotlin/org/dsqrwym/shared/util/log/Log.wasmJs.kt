package org.dsqrwym.shared.util.log

@JsFun("(msg, style) => console.debug(msg, style)")
external fun consoleDebug(msg: String, style: String)

@JsFun("(msg, style) => console.log(msg, style)")
external fun consoleLog(msg: String, style: String)

@JsFun("(msg, style) => console.warn(msg, style)")
external fun consoleWarn(msg: String, style: String)

@JsFun("(msg, style) => console.error(msg, style)")
external fun consoleError(msg: String, style: String)

actual object SharedLog {
    private val styleMap = mapOf(
        SharedLogLevel.DEBUG to "color: blue",
        SharedLogLevel.INFO to "color: lightgreen",
        SharedLogLevel.WARN to "color: orange",
        SharedLogLevel.ERROR to "color: red; font-weight: bold"
    )

    actual fun log(level: SharedLogLevel, tag: String, message: String) {
        val formattedMessage = "%c[$level][$tag]: $message"
        val style = styleMap[level] ?: "color: black"
        // :: 是 Kotlin 的 函数引用操作符，它用来引用函数本身
        val logFunc: (String, String) -> Unit = when (level) {
            SharedLogLevel.DEBUG -> ::consoleDebug
            SharedLogLevel.INFO -> ::consoleLog
            SharedLogLevel.WARN -> ::consoleWarn
            SharedLogLevel.ERROR -> ::consoleError
        }
        logFunc(formattedMessage, style)
    }
}