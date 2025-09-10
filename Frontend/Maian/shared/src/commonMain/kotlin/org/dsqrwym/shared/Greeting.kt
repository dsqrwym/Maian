package org.dsqrwym.shared

import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.type.name}!"
    }

    fun getPlatformType(): PlatformType {
        return platform.type
    }
}