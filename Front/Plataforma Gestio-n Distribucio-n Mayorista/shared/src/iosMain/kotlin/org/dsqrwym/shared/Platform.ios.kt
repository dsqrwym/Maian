package org.dsqrwym.shared

class IOSPlatform: Platform {
    override val type = PlatformType.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()