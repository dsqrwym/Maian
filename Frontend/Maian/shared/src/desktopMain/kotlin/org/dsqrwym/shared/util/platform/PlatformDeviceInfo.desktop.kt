package org.dsqrwym.shared.util.platform

import org.dsqrwym.shared.util.settings.SharedSettingsProvider
import java.net.InetAddress
import java.util.*

private object JVMDeviceInfo : PlatformDeviceInfo {
    override val deviceName: String
        get() {
            return try {
                InetAddress.getLocalHost().hostName
            } catch (_: Exception) {
                System.getProperty("user.name") ?: "Unknown-JVM-Device"
            }
        }

    override val userAgent: String
        get() {
            // 读取已有 UUID，否则生成新的
            return SharedSettingsProvider.plain.getStringOrNull(PLATFORM_DEVICE_UUID_KEY)
                ?: UUID.randomUUID().toString().also { newId ->
                    SharedSettingsProvider.plain.putString(PLATFORM_DEVICE_UUID_KEY, newId)
                }
        }
}


actual fun getPlatformDeviceInfo(): PlatformDeviceInfo {
    return JVMDeviceInfo
}