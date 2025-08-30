package org.dsqrwym.shared.util.platform

import android.provider.Settings
import org.dsqrwym.shared.util.log.SharedLog

/**
 * Android implementation of [PlatformDeviceInfo] interface.
 * [PlatformDeviceInfo] 接口的 Android 实现。
 * 
 * This class provides device-specific information for Android platform.
 * 此类提供 Android 平台特定的设备信息。
 */
private object AndroidDeviceInfo : PlatformDeviceInfo {
    /**
     * Gets the name of the device as set by the user in the system settings.
     * 获取用户在系统设置中设置的设备名称。
     */
    override val deviceName: String
        get() = Settings.Secure.NAME
        
    /**
     * Gets the user agent string for the device, using the device model.
     * 获取设备的用户代理字符串，使用设备型号。
     */
    override val userAgent: String
        get() = android.os.Build.MODEL
}

/**
 * Returns the platform-specific device information implementation for Android.
 * 返回 Android 平台特定的设备信息实现。
 *
 * This function logs device information for debugging purposes and returns
 * an instance of [AndroidDeviceInfo].
 * 此函数记录设备信息用于调试，并返回 [AndroidDeviceInfo] 的实例。
 *
 * @return An instance of [PlatformDeviceInfo] for Android.
 *         返回 Android 平台的 [PlatformDeviceInfo] 实例。
 */
actual fun getPlatformDeviceInfo(): PlatformDeviceInfo {
    // Log detailed device information for debugging
    // 记录详细的设备信息用于调试
    SharedLog.log(
        tag = "PlatformDeviceInfo",
        message = """
        Android DeviceInfo
        1. Build Model -> ${android.os.Build.MODEL}
        2. Build Device -> ${android.os.Build.DEVICE}
        3. Build User -> ${android.os.Build.USER}
        4. Build Fingerprint -> ${android.os.Build.FINGERPRINT}
    """.trimIndent()
    )
    return AndroidDeviceInfo
}