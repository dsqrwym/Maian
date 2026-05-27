package org.dsqrwym.shared.util.platform

import androidx.compose.ui.text.intl.Locale
import kotlinx.browser.window
import org.dsqrwym.shared.util.settings.SharedSettingsProvider
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val MAX_DEVICE_NAME_LENGTH = 150
private const val DEFAULT_WEB_DEVICE_NAME = "Web Browser"

private object WasmJSDeviceInfo : PlatformDeviceInfo {
    override val deviceName: String
        get() {
            // 平台 + 浏览器信息，避免过于单薄
            val platform = window.navigator.platform
            val ua = window.navigator.userAgent
            return buildWebDeviceName(platform, ua)
        }

    @OptIn(ExperimentalUuidApi::class)
    override val userAgent: String
        get() {
            // 读取已有 UUID，否则生成新的
            return SharedSettingsProvider.plain.getStringOrNull(PLATFORM_DEVICE_UUID_KEY)
                ?: Uuid.random().toString().also { newId ->
                    SharedSettingsProvider.plain.putString(PLATFORM_DEVICE_UUID_KEY, newId)
                }
        }

    override val countryCode: String
        get() = Locale.current.region
}

actual fun getPlatformDeviceInfo(): PlatformDeviceInfo {
    return WasmJSDeviceInfo
}

private fun buildWebDeviceName(platform: String, userAgent: String): String {
    val browser = detectBrowser(userAgent)
    val os = detectOperatingSystem(userAgent, platform)
    val fallbackPlatform = platform.takeIf { it.isNotBlank() }

    return listOfNotNull(browser, os, fallbackPlatform)
        .distinct()
        .joinToString(" ")
        .ifBlank { DEFAULT_WEB_DEVICE_NAME }
        .take(MAX_DEVICE_NAME_LENGTH)
}

private fun detectBrowser(userAgent: String): String? {
    return when {
        userAgent.contains("Edg/") -> "Edge"
        userAgent.contains("OPR/") || userAgent.contains("Opera") -> "Opera"
        userAgent.contains("Firefox/") -> "Firefox"
        userAgent.contains("CriOS/") || userAgent.contains("Chrome/") -> "Chrome"
        userAgent.contains("Safari/") -> "Safari"
        else -> null
    }
}

private fun detectOperatingSystem(userAgent: String, platform: String): String? {
    return when {
        userAgent.contains("Android") -> "on Android"
        userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("iPod") -> "on iOS"
        userAgent.contains("Windows") || platform.contains("Win") -> "on Windows"
        userAgent.contains("Mac OS X") || platform.contains("Mac") -> "on macOS"
        userAgent.contains("Linux") || platform.contains("Linux") -> "on Linux"
        else -> null
    }
}
