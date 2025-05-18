package org.dsqrwym.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform