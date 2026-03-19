package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.OMIT

actual object HttpClientProvider {
    @OptIn(ExperimentalWasmJsInterop::class)
    actual val client: HttpClient by lazy {
        HttpClient(Js) {
            installCommonPlugins()
            engine {
                this@engine.configureRequest {
                    credentials = org.w3c.fetch.RequestCredentials.INCLUDE
                }
            }
        }
    }
    @OptIn(ExperimentalWasmJsInterop::class)
    actual val publicClient: HttpClient?
        get() = HttpClient(Js) {
            installCommonPlugins()
            engine {
                this@engine.configureRequest {
                    credentials = org.w3c.fetch.RequestCredentials.OMIT
                }
            }
        }
}