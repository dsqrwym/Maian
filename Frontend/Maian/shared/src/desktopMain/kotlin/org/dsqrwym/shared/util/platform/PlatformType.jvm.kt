package org.dsqrwym.shared.util.platform

class JVMPlatform: Platform {
    override val type = PlatformType.Desktop
}

actual fun getPlatform(): Platform = JVMPlatform()