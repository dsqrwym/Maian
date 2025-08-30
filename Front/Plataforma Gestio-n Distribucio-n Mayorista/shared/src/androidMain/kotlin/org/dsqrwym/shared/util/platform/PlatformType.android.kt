package org.dsqrwym.shared.util.platform

class AndroidPlatform : Platform {
    override val type = PlatformType.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()