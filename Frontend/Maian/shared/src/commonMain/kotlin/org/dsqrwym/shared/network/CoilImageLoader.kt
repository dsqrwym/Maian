package org.dsqrwym.shared.network

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import io.github.vinceglb.filekit.coil.addPlatformFileSupport


@OptIn(ExperimentalCoilApi::class)
@Composable
fun InitCoil() {
    // 关键：不要在参数里访问 HttpClientProvider.client
    // 只有在工厂真正执行时（后台线程）才去获取客户端
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(HttpClientProvider.client))
                addPlatformFileSupport()
            }
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
