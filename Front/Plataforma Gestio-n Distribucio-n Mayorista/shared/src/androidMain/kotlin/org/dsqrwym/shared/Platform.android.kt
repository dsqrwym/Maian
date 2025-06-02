package org.dsqrwym.shared

class AndroidPlatform : Platform {
    override val type = PlatformType.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()