package org.dsqrwym.shared.util.secret

import io.ktor.utils.io.core.*
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import kotlin.experimental.xor

fun xorBytes(data: ByteArray): ByteArray {
    val key = getPlatformDeviceInfo().userAgent.toByteArray()
    return data.mapIndexed { index, byte ->
        byte xor key[index % key.size]
    }.toByteArray()
}