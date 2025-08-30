package org.dsqrwym.shared.util.platform

interface PlatformDeviceInfo {
    val deviceName: String
    val userAgent: String
}

internal const val PLATFORM_DEVICE_UUID_KEY : String = "DeviceUUID"

expect fun getPlatformDeviceInfo(): PlatformDeviceInfo