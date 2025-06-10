package org.dsqrwym.shared.util.log

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

expect fun sharedlog(level: LogLevel = LogLevel.INFO, tag: String = "LOG", message: String)