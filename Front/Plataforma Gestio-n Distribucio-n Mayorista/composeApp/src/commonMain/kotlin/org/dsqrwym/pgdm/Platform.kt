package org.dsqrwym.pgdm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform