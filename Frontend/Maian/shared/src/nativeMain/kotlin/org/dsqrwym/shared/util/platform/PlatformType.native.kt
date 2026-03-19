package org.dsqrwym.shared.util.platform

class IOSPlatform: Platform {
    override val type = PlatformType.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()