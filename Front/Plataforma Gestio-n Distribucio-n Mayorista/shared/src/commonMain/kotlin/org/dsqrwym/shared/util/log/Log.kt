package org.dsqrwym.shared.util.log

enum class SharedLogLevel { DEBUG, INFO, WARN, ERROR }

expect object SharedLog {
    fun log(level: SharedLogLevel = SharedLogLevel.INFO, tag: String = "LOG", message: String)
}