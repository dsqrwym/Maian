package org.dsqrwym.shared.localization

/**
 * 获取当前设备的 IANA 时区标识（例如：America/New_York, Asia/Shanghai）
 * Compose Multiplatform expect 声明
 */
expect fun getSystemTimeZone(): String

object TimezoneManager {
    fun getCurrentTimeZone(): String = getSystemTimeZone()
}