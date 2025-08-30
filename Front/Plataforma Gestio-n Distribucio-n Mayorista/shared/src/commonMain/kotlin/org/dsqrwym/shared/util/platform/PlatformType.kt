package org.dsqrwym.shared.util.platform

sealed class PlatformType(val name: String) {
    object Android : PlatformType("Android")
    object IOS : PlatformType("iOS")
    object Desktop : PlatformType("Desktop")
    object Web : PlatformType("Web")
    object Unknown : PlatformType("Unknown")
}

interface Platform {
    val type: PlatformType
}


expect fun getPlatform(): Platform