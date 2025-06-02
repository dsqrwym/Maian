package org.dsqrwym.shared

class WasmPlatform: Platform {
    override val type = PlatformType.Web
}

actual fun getPlatform(): Platform = WasmPlatform()