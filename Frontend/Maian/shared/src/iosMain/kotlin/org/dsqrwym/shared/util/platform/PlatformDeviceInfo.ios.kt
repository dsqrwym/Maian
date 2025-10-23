package org.dsqrwym.shared.util.platform

import org.dsqrwym.shared.util.settings.SharedSettingsProvider
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSUUID
import platform.Foundation.currentLocale
import platform.UIKit.UIDevice

private object IOSDeviceInfo : PlatformDeviceInfo {
    override val deviceName: String
        get() = UIDevice.currentDevice.name
    override val userAgent: String
        get() = DeviceIdProvider.getDeviceId()
    override val countryCode: String
        get() = getDeviceRegion()
}

private object DeviceIdProvider {

    fun getDeviceId(): String {
        // 1. 优先用系统的 identifierForVendor
        UIDevice.currentDevice.identifierForVendor?.UUIDString?.let { identifierForVendor ->
            if (identifierForVendor.isNotBlank()) return identifierForVendor
        }

        // 2. 如果系统 ID 不可用，尝试从安全存储读取
        SharedSettingsProvider.secure.getStringOrNull(PLATFORM_DEVICE_UUID_KEY)?.let { saved ->
            if (saved.isNotBlank()) return saved
        }

        // 3. 最后生成新的 UUID 并存储
        return NSUUID().UUIDString.also { newId ->
            SharedSettingsProvider.secure.putString(PLATFORM_DEVICE_UUID_KEY, newId)
        }
    }
}

fun getDeviceRegion(): String {
    return try {
        NSLocale.currentLocale.objectForKey(NSLocaleCountryCode)?.toString() ?: "US"
    } catch (_: Exception) {
        "US"
    }
}

actual fun getPlatformDeviceInfo(): PlatformDeviceInfo {
    return IOSDeviceInfo
}