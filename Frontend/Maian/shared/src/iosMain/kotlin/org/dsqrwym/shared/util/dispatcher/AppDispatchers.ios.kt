package org.dsqrwym.shared.util.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object AppDispatchers {
    // Native (iOS) 支持 Dispatchers.IO
    actual val IO: CoroutineDispatcher = Dispatchers.IO
}
