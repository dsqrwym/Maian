package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

actual object HttpClientProvider {
    actual val client: HttpClient by lazy {
        HttpClient(OkHttp) { installCommonPlugins() }
    }
}