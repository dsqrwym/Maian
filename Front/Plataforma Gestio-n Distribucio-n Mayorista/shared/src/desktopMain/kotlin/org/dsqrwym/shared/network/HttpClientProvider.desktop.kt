package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*

actual object HttpClientProvider {
    actual val client: HttpClient by lazy {
        HttpClient(CIO) { installCommonPlugins() }
    }
}