package org.dsqrwym.shared.util.platform

import kotlinx.browser.window
import org.dsqrwym.shared.util.settings.SharedSettingsProvider
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private object WasmJSDeviceInfo : PlatformDeviceInfo {
    override val deviceName: String
        get() {
            // 平台 + 浏览器信息，避免过于单薄
            val platform = window.navigator.platform
            val ua = window.navigator.userAgent
            return "$platform ($ua)"
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
}

actual fun getPlatformDeviceInfo(): PlatformDeviceInfo {
    return WasmJSDeviceInfo
}