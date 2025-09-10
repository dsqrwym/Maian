package org.dsqrwym.shared.util.platform

import android.content.Context
import android.os.Build
import android.provider.Settings

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
        get() = getHumanDeviceName(AppContextProvider.get())

    /**
     * Gets the user agent string for the device, using the device model.
     * 获取设备的用户代理字符串，使用设备型号。
     */
    override val userAgent: String
        get() = Build.MODEL
}

/** 依次尝试：
 * 1) Settings.Global.DEVICE_NAME (API 25+)
 * 2) Settings.Secure "bluetooth_name"（不少 ROM 用它做可见名称）
 * 3) Settings.System "device_name"（部分厂商定制）
 * 4) Manufacturer + Model 兜底
 */
private fun getHumanDeviceName(context: Context): String {
    val cr = context.contentResolver

    // 1) 正式的“设备名称”
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        val name = Settings.Global.getString(cr, Settings.Global.DEVICE_NAME)
        if (!name.isNullOrBlank()) return name
    }

    // 2) 有些设备把可见名放在 bluetooth_name（只读即可，不用蓝牙权限）
    val btName = Settings.Secure.getString(cr, "bluetooth_name")
    if (!btName.isNullOrBlank()) return btName

    // 3) 某些 ROM 放在 system 表
    val sysName = Settings.System.getString(cr, "device_name")
    if (!sysName.isNullOrBlank()) return sysName

    // 4) 兜底：更友好的 “品牌 + 型号”
    val model = Build.MODEL ?: ""
    val brand = Build.MANUFACTURER ?: ""
    val pretty = if (model.startsWith(brand, ignoreCase = true)) model else "$brand $model"
    return pretty.trim().ifEmpty { "Android Device" }
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
    return AndroidDeviceInfo
}