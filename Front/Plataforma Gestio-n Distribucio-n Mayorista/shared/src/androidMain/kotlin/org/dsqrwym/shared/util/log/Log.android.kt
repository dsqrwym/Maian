package org.dsqrwym.shared.util.log

import android.util.Log

actual object SharedLog {
    actual fun log(level: SharedLogLevel, tag: String, message: String) {
        when (level) {
            SharedLogLevel.DEBUG -> Log.d(tag, message)
            SharedLogLevel.INFO -> Log.i(tag, message)
            SharedLogLevel.WARN -> Log.w(tag, message)
            SharedLogLevel.ERROR -> Log.e(tag, message)
        }
    }
}