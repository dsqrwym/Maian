package org.dsqrwym.shared.util.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object AppDispatchers {
    // Web/Wasm 不支持真正的多线程 IO 调度器，回退到 Default
    actual val IO: CoroutineDispatcher = Dispatchers.Default
}
