package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.cookies.*
import org.w3c.fetch.INCLUDE

actual object HttpClientProvider {
    actual val client: HttpClient by lazy {
        HttpClient(Js) {
            installCommonPlugins()
            install(HttpCookies){
                storage = AcceptAllCookiesStorage()
            }
            engine{
                this@engine.configureRequest {
                    credentials = org.w3c.fetch.RequestCredentials.INCLUDE
                }
            }
        }
    }
}