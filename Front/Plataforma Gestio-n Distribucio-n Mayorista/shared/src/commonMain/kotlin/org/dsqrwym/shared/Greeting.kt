package org.dsqrwym.shared

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.type.name}!"
    }

    fun getPlatformType(): PlatformType {
        return platform.type
    }
}