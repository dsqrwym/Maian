package org.dsqrwym.shared.network

import io.ktor.client.*

actual object HttpClientProvider {
    actual val client: HttpClient by lazy {
        HttpClient { installCommonPlugins() }
    }
}