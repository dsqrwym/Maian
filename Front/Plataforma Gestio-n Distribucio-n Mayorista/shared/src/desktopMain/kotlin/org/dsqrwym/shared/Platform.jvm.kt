package org.dsqrwym.shared

class JVMPlatform: Platform {
    override val type = PlatformType.Desktop
}

actual fun getPlatform(): Platform = JVMPlatform()