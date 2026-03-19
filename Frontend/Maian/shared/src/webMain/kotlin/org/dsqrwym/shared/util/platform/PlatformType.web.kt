package org.dsqrwym.shared.util.platform

class WasmPlatform : Platform {
    override val type = PlatformType.Web
}

actual fun getPlatform(): Platform = WasmPlatform()