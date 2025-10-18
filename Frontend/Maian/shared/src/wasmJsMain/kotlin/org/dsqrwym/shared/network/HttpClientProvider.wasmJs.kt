package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import org.w3c.fetch.INCLUDE

actual object HttpClientProvider {
    @OptIn(ExperimentalWasmJsInterop::class)
    actual val client: HttpClient by lazy {
        HttpClient(Js) {
            installCommonPlugins()
//            install(HttpCookies){
//                storage = AcceptAllCookiesStorage()
//            }
            engine{
                this@engine.configureRequest {
                    credentials = org.w3c.fetch.RequestCredentials.INCLUDE
                }
            }
        }
    }
}