package org.dsqrwym.shared.util.log

enum class SharedLogLevel { DEBUG, INFO, WARN, ERROR }

expect object SharedLog {
    fun log(message: String, level: SharedLogLevel = SharedLogLevel.INFO, tag: String = "LOG")
}